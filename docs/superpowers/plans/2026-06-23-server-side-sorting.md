# Server-Side Sorting (Multiplayer Survival Support) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable ShulkerSorter to work on dedicated multiplayer survival servers by adding custom networking packets so the client sends a sort/undo request and the server executes it.

**Architecture:** The client sends a lightweight `SortRequestPayload` (containing action type: SORT or UNDO) to the server via Fabric Networking API. The server receives it, runs `ShulkerSorterEngine.sort()` on the server-side player inventory (or restores the undo snapshot), then sends a `SortResultPayload` back to the client with the outcome (success/error, counts). The client uses this response to show HUD, play sounds, and display chat messages. The server holds the undo snapshot per-player. Config is loaded on both sides independently.

**Tech Stack:** Fabric Networking API v1 (`CustomPacketPayload`, `PayloadTypeRegistry`, `ServerPlayNetworking`, `ClientPlayNetworking`), `StreamCodec` for serialization.

## Global Constraints

- MC 26.2, Fabric Loader >= 0.19.2, Java 25+
- `loom.splitEnvironmentSourceSets()` — `src/main/` is server-safe, `src/client/` is client-only
- Networking payload classes go in `src/main/` (both sides need them)
- Server-side handler goes in `src/main/` (runs on dedicated server)
- Client-side handler goes in `src/client/` (renders feedback)
- `environment` in `fabric.mod.json` changes from `"client"` to `"*"`
- Mod must still work without server-side installation (graceful fallback: singleplayer + creative still work, survival shows "install on server" message)
- No test suite exists; verification is done by running the client

---

### Task 1: Define Networking Payloads

**Files:**
- Create: `src/main/java/de/dennisthegamer/shulkersorter/network/SortRequestPayload.java`
- Create: `src/main/java/de/dennisthegamer/shulkersorter/network/SortResultPayload.java`

**Interfaces:**
- Consumes: Nothing (foundational)
- Produces:
  - `SortRequestPayload` — implements `CustomPacketPayload`, has `Action action()` enum (SORT, UNDO), `TYPE` constant, `CODEC` static field
  - `SortResultPayload` — implements `CustomPacketPayload`, has `boolean success()`, `int boxesSorted()`, `int itemsMoved()`, `String errorKey()`, `TYPE` constant, `CODEC` static field

- [ ] **Step 1: Create SortRequestPayload**

```java
// src/main/java/de/dennisthegamer/shulkersorter/network/SortRequestPayload.java
package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.ShulkerSorter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SortRequestPayload(Action action) implements CustomPacketPayload {

    public enum Action { SORT, UNDO }

    public static final Type<SortRequestPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ShulkerSorter.MOD_ID, "sort_request"));

    public static final StreamCodec<FriendlyByteBuf, SortRequestPayload> CODEC =
            CustomPacketPayload.codec(SortRequestPayload::write, SortRequestPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeByte(action.ordinal());
    }

    private static SortRequestPayload read(FriendlyByteBuf buf) {
        return new SortRequestPayload(Action.values()[buf.readByte()]);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

- [ ] **Step 2: Create SortResultPayload**

```java
// src/main/java/de/dennisthegamer/shulkersorter/network/SortResultPayload.java
package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.ShulkerSorter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SortResultPayload(boolean success, int boxesSorted, int itemsMoved, String errorKey) implements CustomPacketPayload {

    public static final Type<SortResultPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ShulkerSorter.MOD_ID, "sort_result"));

    public static final StreamCodec<FriendlyByteBuf, SortResultPayload> CODEC =
            CustomPacketPayload.codec(SortResultPayload::write, SortResultPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(success);
        buf.writeVarInt(boxesSorted);
        buf.writeVarInt(itemsMoved);
        buf.writeUtf(errorKey != null ? errorKey : "");
    }

    private static SortResultPayload read(FriendlyByteBuf buf) {
        boolean success = buf.readBoolean();
        int boxesSorted = buf.readVarInt();
        int itemsMoved = buf.readVarInt();
        String errorKey = buf.readUtf();
        return new SortResultPayload(success, boxesSorted, itemsMoved, errorKey.isEmpty() ? null : errorKey);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/de/dennisthegamer/shulkersorter/network/
git commit -m "feat: add networking payload records for sort request/result"
```

---

### Task 2: Server-Side Undo Manager and Packet Handler

**Files:**
- Create: `src/main/java/de/dennisthegamer/shulkersorter/network/ServerSortHandler.java`
- Create: `src/main/java/de/dennisthegamer/shulkersorter/network/ServerUndoManager.java`

**Interfaces:**
- Consumes: `SortRequestPayload`, `SortResultPayload`, `ShulkerSorterEngine.sort()`, `ShulkerSorterConfig`
- Produces:
  - `ServerSortHandler.register()` — called from `ShulkerSorter.onInitialize()`, registers `PayloadTypeRegistry` entries and `ServerPlayNetworking.registerGlobalReceiver`
  - `ServerUndoManager` — per-player snapshot storage on the server, `saveSnapshot(ServerPlayer)`, `restore(ServerPlayer)`, `clear(UUID)`, `hasSnapshot(UUID)`

- [ ] **Step 1: Create ServerUndoManager**

```java
// src/main/java/de/dennisthegamer/shulkersorter/network/ServerUndoManager.java
package de.dennisthegamer.shulkersorter.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ServerUndoManager {
    private static final ServerUndoManager INSTANCE = new ServerUndoManager();
    private final Map<UUID, List<ItemStack>> snapshots = new HashMap<>();

    private ServerUndoManager() {}

    public static ServerUndoManager get() { return INSTANCE; }

    public void saveSnapshot(ServerPlayer player) {
        List<ItemStack> snapshot = new ArrayList<>(36);
        for (int i = 0; i < 36; i++) {
            snapshot.add(player.getInventory().getItem(i).copy());
        }
        snapshots.put(player.getUUID(), snapshot);
    }

    public boolean restore(ServerPlayer player) {
        List<ItemStack> snapshot = snapshots.remove(player.getUUID());
        if (snapshot == null) return false;
        for (int i = 0; i < 36; i++) {
            player.getInventory().setItem(i, snapshot.get(i).copy());
        }
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    public boolean hasSnapshot(UUID uuid) {
        return snapshots.containsKey(uuid);
    }

    public void clear(UUID uuid) {
        snapshots.remove(uuid);
    }
}
```

- [ ] **Step 2: Create ServerSortHandler**

```java
// src/main/java/de/dennisthegamer/shulkersorter/network/ServerSortHandler.java
package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.sort.ShulkerSorterEngine;
import de.dennisthegamer.shulkersorter.sort.SortResult;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ServerSortHandler {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(SortRequestPayload.TYPE, SortRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SortResultPayload.TYPE, SortResultPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SortRequestPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleRequest(player, payload));
        });
    }

    private static void handleRequest(ServerPlayer player, SortRequestPayload payload) {
        switch (payload.action()) {
            case SORT -> handleSort(player);
            case UNDO -> handleUndo(player);
        }
    }

    private static void handleSort(ServerPlayer player) {
        ServerUndoManager.get().saveSnapshot(player);

        SortResult result = ShulkerSorterEngine.sort(player.getInventory());
        player.inventoryMenu.broadcastChanges();

        if (!result.success()) {
            ServerUndoManager.get().clear(player.getUUID());
        }

        ServerPlayNetworking.send(player, new SortResultPayload(
                result.success(), result.boxesSorted(), result.itemsMoved(), result.errorMessage()));
    }

    private static void handleUndo(ServerPlayer player) {
        boolean restored = ServerUndoManager.get().restore(player);
        if (restored) {
            ServerPlayNetworking.send(player, new SortResultPayload(true, 0, 0, "shulkersorter.message.undo_success"));
        } else {
            ServerPlayNetworking.send(player, new SortResultPayload(false, 0, 0, "shulkersorter.message.undo_nothing"));
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/de/dennisthegamer/shulkersorter/network/
git commit -m "feat: add server-side sort handler and undo manager"
```

---

### Task 3: Client-Side Response Handler

**Files:**
- Create: `src/client/java/de/dennisthegamer/shulkersorter/network/ClientSortHandler.java`

**Interfaces:**
- Consumes: `SortResultPayload`, `SortingHudOverlay`, `NotificationHelper`
- Produces: `ClientSortHandler.register()` — called from `ShulkerSorterClient.onInitializeClient()`, registers `ClientPlayNetworking.registerGlobalReceiver` for `SortResultPayload`

- [ ] **Step 1: Create ClientSortHandler**

```java
// src/client/java/de/dennisthegamer/shulkersorter/network/ClientSortHandler.java
package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.util.NotificationHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class ClientSortHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SortResultPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> handleResult(payload));
        });
    }

    private static void handleResult(SortResultPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (payload.errorKey() != null && payload.errorKey().startsWith("shulkersorter.message.undo")) {
            // Undo response
            if (payload.success()) {
                NotificationHelper.sendInfo(client.player, payload.errorKey());
            } else {
                NotificationHelper.sendInfo(client.player, payload.errorKey());
            }
        } else if (payload.success()) {
            NotificationHelper.sendSuccess(client.player, payload.boxesSorted(), payload.itemsMoved());
        } else {
            NotificationHelper.sendError(client.player, payload.errorKey());
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/client/java/de/dennisthegamer/shulkersorter/network/
git commit -m "feat: add client-side sort result handler"
```

---

### Task 4: Wire Up Entry Points and Update Keybind Handler

**Files:**
- Modify: `src/main/java/de/dennisthegamer/shulkersorter/ShulkerSorter.java`
- Modify: `src/client/java/de/dennisthegamer/shulkersorter/ShulkerSorterClient.java`
- Modify: `src/client/java/de/dennisthegamer/shulkersorter/keybind/SortKeybindHandler.java`
- Modify: `src/main/resources/fabric.mod.json`

**Interfaces:**
- Consumes: `ServerSortHandler.register()`, `ClientSortHandler.register()`, `SortRequestPayload`, `ClientPlayNetworking.send()`
- Produces: Working multiplayer survival sorting via networking

- [ ] **Step 1: Update ShulkerSorter main entry point to register server handler**

In `ShulkerSorter.java`, add the server handler registration:

```java
package de.dennisthegamer.shulkersorter;

import de.dennisthegamer.shulkersorter.network.ServerSortHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerSorter implements ModInitializer {
    public static final String MOD_ID = "shulkersorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerSortHandler.register();
        LOGGER.info("ShulkerSorter initialized!");
    }
}
```

- [ ] **Step 2: Update ShulkerSorterClient to register client handler**

In `ShulkerSorterClient.java`, add `ClientSortHandler.register()`:

```java
package de.dennisthegamer.shulkersorter;

import de.dennisthegamer.shulkersorter.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersorter.keybind.SortKeybindHandler;
import de.dennisthegamer.shulkersorter.tooltip.ShulkerTooltipRenderer;
import de.dennisthegamer.shulkersorter.config.ShulkerSorterConfig;
import de.dennisthegamer.shulkersorter.network.ClientSortHandler;
import de.dennisthegamer.shulkersorter.undo.SortUndoManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ShulkerSorterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ShulkerSorterConfig.getInstance();
        SortKeybindHandler.register();
        ShulkerTooltipRenderer.register();
        SortingHudOverlay.register();
        ClientSortHandler.register();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SortUndoManager.get().clear());

        ShulkerSorter.LOGGER.info("ShulkerSorter client initialized!");
    }
}
```

- [ ] **Step 3: Update SortKeybindHandler to use networking for multiplayer survival**

Replace the multiplayer survival error block with a networking send. The key change is in `onClientTick`: when on a dedicated server in survival, send a `SortRequestPayload` instead of showing an error. Similarly for undo.

The logic becomes:
1. **Singleplayer** (`getSingleplayerServer() != null`): Direct server access (existing code, unchanged)
2. **Multiplayer with mod on server** (`ClientPlayNetworking.canSend(SortRequestPayload.TYPE)`): Send packet
3. **Multiplayer without mod on server**: Show error message

```java
package de.dennisthegamer.shulkersorter.keybind;

import de.dennisthegamer.shulkersorter.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersorter.sort.ShulkerSorterEngine;
import de.dennisthegamer.shulkersorter.sort.SortResult;
import de.dennisthegamer.shulkersorter.network.SortRequestPayload;
import de.dennisthegamer.shulkersorter.undo.SortUndoManager;
import de.dennisthegamer.shulkersorter.util.NotificationHelper;
import de.dennisthegamer.shulkersorter.util.ShulkerBoxHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class SortKeybindHandler {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("shulkersorter", "shulkersorter"));

    private static KeyMapping sortKeybind;

    public static void register() {
        sortKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.shulkersorter.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SortKeybindHandler::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        while (sortKeybind.consumeClick()) {
            if (client.player == null) continue;

            boolean shiftHeld = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                    || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (shiftHeld) {
                handleUndo(client);
                continue;
            }

            MinecraftServer integratedServer = client.getSingleplayerServer();
            if (integratedServer != null) {
                // Singleplayer: direct server access
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortOnServer(client, integratedServer);
            } else if (ClientPlayNetworking.canSend(SortRequestPayload.TYPE)) {
                // Multiplayer with mod on server: send packet
                SortingHudOverlay.show();
                ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.SORT));
            } else if (client.gameMode != null && client.gameMode.getPlayerMode() == GameType.CREATIVE) {
                // Multiplayer creative without server mod: client-side sort
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortClientAndSyncCreative(client);
            } else {
                NotificationHelper.sendError(client.player, "shulkersorter.message.error.multiplayer_no_mod");
            }
        }
    }

    private static void handleUndo(Minecraft client) {
        MinecraftServer integratedServer = client.getSingleplayerServer();
        if (integratedServer != null) {
            undoOnServer(client, integratedServer);
        } else if (ClientPlayNetworking.canSend(SortRequestPayload.TYPE)) {
            ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.UNDO));
        } else if (client.gameMode != null && client.gameMode.getPlayerMode() == GameType.CREATIVE) {
            undoClientAndSyncCreative(client);
        } else {
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
        }
    }

    // --- Singleplayer methods (unchanged) ---

    private static void sortOnServer(Minecraft client, MinecraftServer server) {
        SortingHudOverlay.show();
        UUID playerUUID = client.player.getUUID();

        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            SortResult result = ShulkerSorterEngine.sort(serverPlayer.getInventory());
            serverPlayer.inventoryMenu.broadcastChanges();

            if (result.success()) {
                NotificationHelper.sendSuccess(serverPlayer, result.boxesSorted(), result.itemsMoved());
            } else {
                Minecraft.getInstance().execute(() -> SortUndoManager.get().clear());
                NotificationHelper.sendError(serverPlayer, result.errorMessage());
            }
        });
    }

    private static void sortClientAndSyncCreative(Minecraft client) {
        SortingHudOverlay.show();

        SortResult result = ShulkerSorterEngine.sort(client.player.getInventory());

        if (result.success()) {
            MultiPlayerGameMode gameMode = client.gameMode;
            for (int i = 0; i < 36; i++) {
                ItemStack stack = client.player.getInventory().getItem(i);
                if (ShulkerBoxHelper.isShulkerBox(stack)) {
                    int containerSlot = i < 9 ? i + 36 : i;
                    gameMode.handleCreativeModeItemAdd(stack.copy(), containerSlot);
                }
            }
            NotificationHelper.sendSuccess(client.player, result.boxesSorted(), result.itemsMoved());
        } else {
            SortUndoManager.get().clear();
            NotificationHelper.sendError(client.player, result.errorMessage());
        }
    }

    private static void undoOnServer(Minecraft client, MinecraftServer server) {
        UUID playerUUID = client.player.getUUID();
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
            return;
        }

        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            for (int i = 0; i < 36; i++) {
                serverPlayer.getInventory().setItem(i, snapshot.get(i).copy());
            }
            serverPlayer.inventoryMenu.broadcastChanges();
            NotificationHelper.sendInfo(serverPlayer, "shulkersorter.message.undo_success");
        });
    }

    private static void undoClientAndSyncCreative(Minecraft client) {
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
            return;
        }

        for (int i = 0; i < 36; i++) {
            client.player.getInventory().setItem(i, snapshot.get(i).copy());
        }
        MultiPlayerGameMode gameMode = client.gameMode;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = snapshot.get(i);
            if (!stack.isEmpty()) {
                int containerSlot = i < 9 ? i + 36 : i;
                gameMode.handleCreativeModeItemAdd(stack.copy(), containerSlot);
            }
        }
        NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_success");
    }
}
```

- [ ] **Step 4: Update fabric.mod.json — change environment to `"*"`**

Change `"environment": "client"` to `"environment": "*"` so the mod loads on both client and dedicated server.

- [ ] **Step 5: Add new translation key for "no mod on server" error**

In both `en_us.json` and `de_de.json`, add:

EN: `"shulkersorter.message.error.multiplayer_no_mod": "ShulkerSorter is not installed on this server."`
DE: `"shulkersorter.message.error.multiplayer_no_mod": "ShulkerSorter ist auf diesem Server nicht installiert."`

The old `multiplayer_survival` key can be kept for backwards compat or removed.

- [ ] **Step 6: Build and verify**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add multiplayer survival support via server-side networking"
```

---

### Task 5: Clean Up Server-Side Undo on Disconnect

**Files:**
- Modify: `src/main/java/de/dennisthegamer/shulkersorter/ShulkerSorter.java`

**Interfaces:**
- Consumes: `ServerUndoManager`, `ServerPlayConnectionEvents`
- Produces: Automatic cleanup of server-side undo snapshots when players disconnect

- [ ] **Step 1: Register disconnect listener in ShulkerSorter.onInitialize()**

```java
package de.dennisthegamer.shulkersorter;

import de.dennisthegamer.shulkersorter.network.ServerSortHandler;
import de.dennisthegamer.shulkersorter.network.ServerUndoManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerSorter implements ModInitializer {
    public static final String MOD_ID = "shulkersorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerSortHandler.register();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ServerUndoManager.get().clear(handler.getPlayer().getUUID()));
        LOGGER.info("ShulkerSorter initialized!");
    }
}
```

- [ ] **Step 2: Build and verify**

```bash
./gradlew build
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/de/dennisthegamer/shulkersorter/ShulkerSorter.java
git commit -m "feat: clean up server undo snapshots on player disconnect"
```
