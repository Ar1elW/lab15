package com.shop.model;

public class OrderItem {

    private static final int BULK_QUANTITY = 10;
    private static final double BULK_DISCOUNT_PERCENT = 5;

    private final Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Товар не указан");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        quantity += amount;
    }

    public double getTotal() {
        if (quantity >= BULK_QUANTITY) {
            return product.calculateCost(quantity, BULK_DISCOUNT_PERCENT);
        }
        return product.calculateCost(quantity);
    }
}
