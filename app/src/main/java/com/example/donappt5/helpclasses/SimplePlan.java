package com.example.donappt5.helpclasses;

public class SimplePlan{
    String  name, description;
    Integer amount;

    public SimplePlan(Integer amount, String name, String description) {
        this.amount = amount;
        this.name = name;
        this.description = description;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}