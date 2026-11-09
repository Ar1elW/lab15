package com.shop.model;

public enum OrderStatus {
    NEW("Новый"),
    PAID("Оплачен"),
    SHIPPED("Отправлен"),
    COMPLETED("Выполнен"),
    CANCELLED("Отменён");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean canMoveTo(OrderStatus next) {
        switch (this) {
            case NEW:
                return next == PAID;
            case PAID:
                return next == SHIPPED;
            case SHIPPED:
                return next == COMPLETED;
            default:
                return false;
        }
    }
}
