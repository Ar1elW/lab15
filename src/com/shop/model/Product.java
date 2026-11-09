package com.shop.model;

import com.shop.util.MoneyFormat;
import com.shop.util.TextCheck;

public abstract class Product implements Identifiable, Savable {

    private final int id;
    private String name;
    private double price;

    protected Product(int id, String name, double price) {
        this.id = id;
        setName(name);
        setPrice(price);
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = TextCheck.requireText(name, "Название товара");
    }

    public double getPrice() {
        return price;
    }

    public final void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Цена должна быть больше нуля");
        }
        this.price = price;
    }

    public abstract double getDeliveryCost();

    public abstract String getTypeName();

    public double calculateCost(int quantity) {
        return calculateCost(quantity, 0);
    }

    public double calculateCost(int quantity, double discountPercent) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Скидка должна быть от 0 до 100 процентов");
        }
        double goodsCost = price * quantity;
        double discount = goodsCost * discountPercent / 100;
        double deliveryCost = getDeliveryCost() * quantity;
        return goodsCost - discount + deliveryCost;
    }

    public String describe() {
        return getTypeName() + " №" + id + " " + name
                + ", цена " + MoneyFormat.format(price)
                + ", доставка за единицу " + MoneyFormat.format(getDeliveryCost());
    }
}
