package com.shop.model;

import com.shop.exception.OrderException;
import com.shop.util.MoneyFormat;
import com.shop.util.TextCheck;

import java.util.ArrayList;
import java.util.List;

public class Order implements Identifiable, Savable {

    private final int id;
    private final String customerName;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(int id, String customerName) {
        this(id, customerName, OrderStatus.NEW, new ArrayList<>());
    }

    public Order(int id, String customerName, OrderStatus status, List<OrderItem> items) {
        this.id = id;
        this.customerName = TextCheck.requireText(customerName, "Имя клиента");
        this.status = status;
        this.items = new ArrayList<>(items);
    }

    @Override
    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(Product product, int quantity) throws OrderException {
        if (status != OrderStatus.NEW) {
            throw new OrderException("Менять состав можно только у нового заказа");
        }
        if (quantity <= 0) {
            throw new OrderException("Количество должно быть больше нуля");
        }
        for (OrderItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.addQuantity(quantity);
                return;
            }
        }
        items.add(new OrderItem(product, quantity));
    }

    public boolean containsProduct(int productId) {
        for (OrderItem item : items) {
            if (item.getProduct().getId() == productId) {
                return true;
            }
        }
        return false;
    }

    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getTotal();
        }
        return total;
    }

    public void changeStatus(OrderStatus newStatus) throws OrderException {
        if (!status.canMoveTo(newStatus)) {
            throw new OrderException("Переход из статуса \"" + status.getTitle()
                    + "\" в статус \"" + newStatus.getTitle() + "\" невозможен");
        }
        if (newStatus == OrderStatus.PAID && items.isEmpty()) {
            throw new OrderException("Нельзя оплатить пустой заказ");
        }
        status = newStatus;
    }

    public void cancel() throws OrderException {
        if (status == OrderStatus.COMPLETED) {
            throw new OrderException("Выполненный заказ отменить нельзя");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new OrderException("Заказ уже отменён");
        }
        status = OrderStatus.CANCELLED;
    }

    public String describe() {
        return "Заказ №" + id + ", клиент " + customerName
                + ", статус " + status.getTitle()
                + ", позиций " + items.size()
                + ", итого " + MoneyFormat.format(calculateTotal());
    }

    public String getDetails() {
        StringBuilder builder = new StringBuilder(describe());
        for (OrderItem item : items) {
            builder.append("\n   ")
                    .append(item.getProduct().getName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append(" = ")
                    .append(MoneyFormat.format(item.getTotal()));
        }
        return builder.toString();
    }

    @Override
    public String toFileLine() {
        StringBuilder itemsText = new StringBuilder();
        for (OrderItem item : items) {
            if (itemsText.length() > 0) {
                itemsText.append(",");
            }
            itemsText.append(item.getProduct().getId()).append(":").append(item.getQuantity());
        }
        return id + ";" + customerName + ";" + status.name() + ";" + itemsText;
    }
}
