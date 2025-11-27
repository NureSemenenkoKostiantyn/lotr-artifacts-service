package com.profitsoft.lotrstat.model;

public class Artifact {
    private String name;
    private String creator;
    private String origin;
    private String tags;
    private Integer year_created;
    private Integer power_level;

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTags() {
        return tags;
    }

    public Integer getYear_created() {
        return year_created;
    }

    public Integer getPower_level() {
        return power_level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setYear_created(int year_created) {
        this.year_created = year_created;
    }

    public void setPower_level(int power_level) {
        this.power_level = power_level;
    }

    public Artifact(String name, String creator, String origin, String tags, Integer year_created, Integer power_level) {
        this.name = name;
        this.creator = creator;
        this.origin = origin;
        this.tags = tags;
        this.year_created = year_created;
        this.power_level = power_level;
    }
}
