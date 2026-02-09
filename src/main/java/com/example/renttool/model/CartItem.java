package com.example.renttool.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Tool tool;

    private LocalDate startDate;
    private LocalDate endDate;

    public CartItem() {}

    public CartItem(User user, Tool tool, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.tool = tool;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
