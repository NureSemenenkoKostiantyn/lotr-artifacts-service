package com.profitsoft.lotrstat.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class StatItem{

    @JacksonXmlProperty(localName = "value")
    private String value;

    @JacksonXmlProperty(localName = "count")
    private long count;

    public StatItem() {
    }

    public StatItem(String value, long count) {
        this.value = value;
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public long getCount() {
        return count;
    }
}
