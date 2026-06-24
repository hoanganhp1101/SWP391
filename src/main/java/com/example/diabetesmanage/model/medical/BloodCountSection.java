package com.example.diabetesmanage.model.medical;

import java.util.ArrayList;
import java.util.List;

public class BloodCountSection {

    private List<MedicalFieldItem> items = new ArrayList<>();

    public List<MedicalFieldItem> getItems() {
        return items;
    }

    public void setItems(List<MedicalFieldItem> items) {
        this.items = items;
    }

    public boolean hasData() {
        return items.stream().anyMatch(item ->
                item.getValue() != null && !item.getValue().isBlank() && !"—".equals(item.getValue()));
    }
}
