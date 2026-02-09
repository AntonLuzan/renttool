package com.example.renttool.model;

import java.math.BigDecimal;

public class SessionCartItem {

    private Tool tool;
    private int quantity;

    public SessionCartItem(Tool tool, int quantity) {
        this.tool = tool;
        this.quantity = quantity;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPriceDecimal() {
        if (tool == null || tool.getPricePerDay() == null) return BigDecimal.ZERO;
        return tool.getPricePerDay().multiply(BigDecimal.valueOf(quantity));
    }

    public double getTotalPrice() {
        return getTotalPriceDecimal().doubleValue();
    }
}
