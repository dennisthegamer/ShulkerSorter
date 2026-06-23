package de.dennisthegamer.shulkersort.config;

import java.util.List;

public class CategoryDefinition {
    private final String key;
    private final String labelPrefix;
    private final List<String> patterns;

    public CategoryDefinition(String key, String labelPrefix, List<String> patterns) {
        this.key = key;
        this.labelPrefix = labelPrefix;
        this.patterns = patterns;
    }

    public String getKey() {
        return key;
    }

    public String getLabelPrefix() {
        return labelPrefix;
    }

    public List<String> getPatterns() {
        return patterns;
    }
}
