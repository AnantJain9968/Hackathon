package com.st.trex.dto;

import java.util.List;

public class DashboardData {
    private String name;
    private List<SeriesData> series;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeriesData> getSeries() {
        return series;
    }

    public void setSeries(List<SeriesData> series) {
        this.series = series;
    }
}
