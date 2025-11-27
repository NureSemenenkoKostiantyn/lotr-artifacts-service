package com.profitsoft.lotrstat.model;

public enum ArtifactAttribute {
    NAME("name", false),
    CREATOR("creator", false),
    ORIGIN("origin", false),
    TAGS("tags", true),            // ← multi-value
    YEAR_CREATED("year_created", false),
    POWER_LEVEL("power_level", false);

    private final String jsonFieldName;
    private final boolean multiValued;

    ArtifactAttribute(String jsonFieldName, boolean multiValued) {
        this.jsonFieldName = jsonFieldName;
        this.multiValued = multiValued;
    }

    public String getJsonFieldName() {
        return jsonFieldName;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public static ArtifactAttribute fromString(String name) {
        for (ArtifactAttribute a : values()) {
            if (a.jsonFieldName.equalsIgnoreCase(name)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unsupported attribute: " + name);
    }
}