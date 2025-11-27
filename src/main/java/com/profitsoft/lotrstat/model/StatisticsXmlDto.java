package com.profitsoft.lotrstat.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "statistics")
public class StatisticsXmlDto {

    @JacksonXmlProperty(isAttribute = true, localName = "attribute")
    private String attribute;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private List<StatItem> items;

    public StatisticsXmlDto() {
    }

    public StatisticsXmlDto(String attribute, List<StatItem> items) {
        this.attribute = attribute;
        this.items = items;
    }

    public String getAttribute() {
        return attribute;
    }

    public List<StatItem> getItems() {
        return items;
    }
}
