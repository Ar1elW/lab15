package com.shop.service;

import com.shop.exception.OrderException;
import com.shop.model.DigitalProduct;
import com.shop.model.Order;
import com.shop.model.OrderStatus;
import com.shop.model.PhysicalProduct;
import com.shop.model.Product;
import com.shop.repository.Repository;
import com.shop.util.MoneyFormat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ShopService {

    private final Repository<Product> products;
    private final Repository<Order> orders;

    public ShopService(Repository<Product> products, Repository<Order> orders) {
        this.products = products;
        this.orders = orders;
    }

    public Repository<Product> getProductRepository() {
        return products;
    }

    public Repository<Order> getOrderRepository() {
        return orders;
    }

    public Product addPhysicalProduct(String name, double price, double weightKg) {
        Product product = new PhysicalProduct(products.nextId(), name, price, weightKg);
        products.add(product);
        return product;
    }

    public Product addDigitalProduct(String name, double price, double fileSizeMb) {
        Product product = new DigitalProduct(products.nextId(), name, price, fileSizeMb);
        products.add(product);
        return product;
    }

    public List<Product> getAllProducts() {
        return products.findAll();
    }

    public List<Product> findProductsByName(String text) {
        String lowerText = text.trim().toLowerCase();
        return products.findBy(product -> product.getName().toLowerCase().contains(lowerText));
    }

    public void removeProduct(int productId) throws OrderException {
        if (products.findById(productId) == null) {
            throw new OrderException("Товар с номером " + productId + " не найден");
        }
        for (Order order : orders.findAll()) {
            if (order.containsProduct(productId)) {
                throw new OrderException("Товар нельзя удалить, он есть в заказе №" + order.getId());
            }
        }
        products.remove(productId);
    }

    public Order createOrder(String customerName) {
        Order order = new Order(orders.nextId(), customerName);
        orders.add(order);
        return order;
    }

    public Order getOrder(int orderId) throws OrderException {
        Order order = orders.findById(orderId);
        if (order == null) {
            throw new OrderException("Заказ с номером " + orderId + " не найден");
        }
        return order;
    }

    public List<Order> getAllOrders() {
        return orders.findAll();
    }

    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orders.findBy(order -> order.getStatus() == status);
    }

    public void addItemToOrder(int orderId, int productId, int quantity) throws OrderException {
        Order order = getOrder(orderId);
        Product product = products.findById(productId);
        if (product == null) {
            throw new OrderException("Товар с номером " + productId + " не найден");
        }
        order.addItem(product, quantity);
    }

    public void changeOrderStatus(int orderId, OrderStatus newStatus) throws OrderException {
        getOrder(orderId).changeStatus(newStatus);
    }

    public void cancelOrder(int orderId) throws OrderException {
        getOrder(orderId).cancel();
    }

    public double calculateRevenue() {
        double revenue = 0;
        for (Order order : findOrdersByStatus(OrderStatus.COMPLETED)) {
            revenue += order.calculateTotal();
        }
        return revenue;
    }

    public String buildReport() {
        Map<OrderStatus, Integer> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, 0);
        }
        for (Order order : orders.findAll()) {
            counts.put(order.getStatus(), counts.get(order.getStatus()) + 1);
        }
        StringBuilder report = new StringBuilder();
        report.append("Товаров в каталоге: ").append(products.size()).append("\n");
        report.append("Заказов всего: ").append(orders.size()).append("\n");
        for (OrderStatus status : OrderStatus.values()) {
            report.append("   ").append(status.getTitle()).append(": ").append(counts.get(status)).append("\n");
        }
        report.append("Выручка по выполненным заказам: ").append(MoneyFormat.format(calculateRevenue()));
        return report.toString();
    }
}
