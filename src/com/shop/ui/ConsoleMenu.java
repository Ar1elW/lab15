package com.shop.ui;

import com.shop.exception.OrderException;
import com.shop.model.Order;
import com.shop.model.OrderStatus;
import com.shop.model.Product;
import com.shop.service.ShopService;
import com.shop.storage.DataStorage;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final ShopService service;
    private final DataStorage storage;
    private final Scanner scanner;

    public ConsoleMenu(ShopService service, DataStorage storage, Scanner scanner) {
        this.service = service;
        this.storage = storage;
        this.scanner = scanner;
    }

    public void run() {
        boolean working = true;
        try {
            while (working) {
                printMenu();
                int choice = readInt("Выберите пункт: ");
                working = handle(choice);
            }
        } finally {
            saveData();
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Система управления заказами");
        System.out.println("1. Показать все товары");
        System.out.println("2. Добавить физический товар");
        System.out.println("3. Добавить цифровой товар");
        System.out.println("4. Найти товар по названию");
        System.out.println("5. Удалить товар");
        System.out.println("6. Создать заказ");
        System.out.println("7. Добавить товар в заказ");
        System.out.println("8. Показать все заказы");
        System.out.println("9. Показать заказ подробно");
        System.out.println("10. Изменить статус заказа");
        System.out.println("11. Отменить заказ");
        System.out.println("12. Найти заказы по статусу");
        System.out.println("13. Показать отчёт");
        System.out.println("0. Сохранить и выйти");
    }

    private boolean handle(int choice) {
        try {
            switch (choice) {
                case 1:
                    showProducts(service.getAllProducts());
                    break;
                case 2:
                    addPhysicalProduct();
                    break;
                case 3:
                    addDigitalProduct();
                    break;
                case 4:
                    findProducts();
                    break;
                case 5:
                    removeProduct();
                    break;
                case 6:
                    createOrder();
                    break;
                case 7:
                    addItemToOrder();
                    break;
                case 8:
                    showOrders(service.getAllOrders());
                    break;
                case 9:
                    showOrderDetails();
                    break;
                case 10:
                    changeOrderStatus();
                    break;
                case 11:
                    cancelOrder();
                    break;
                case 12:
                    findOrdersByStatus();
                    break;
                case 13:
                    System.out.println(service.buildReport());
                    break;
                case 0:
                    return false;
                default:
                    System.out.println("Такого пункта нет в меню");
            }
        } catch (OrderException | IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        return true;
    }

    private void showProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("Товары не найдены");
            return;
        }
        for (Product product : products) {
            System.out.println(product.describe());
        }
    }

    private void showOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            System.out.println("Заказы не найдены");
            return;
        }
        for (Order order : orders) {
            System.out.println(order.describe());
        }
    }

    private void addPhysicalProduct() {
        String name = readText("Название: ");
        double price = readDouble("Цена: ");
        double weight = readDouble("Вес в килограммах: ");
        Product product = service.addPhysicalProduct(name, price, weight);
        System.out.println("Добавлено: " + product.describe());
    }

    private void addDigitalProduct() {
        String name = readText("Название: ");
        double price = readDouble("Цена: ");
        double size = readDouble("Размер файла в мегабайтах: ");
        Product product = service.addDigitalProduct(name, price, size);
        System.out.println("Добавлено: " + product.describe());
    }

    private void findProducts() {
        String text = readText("Часть названия: ");
        showProducts(service.findProductsByName(text));
    }

    private void removeProduct() throws OrderException {
        int productId = readInt("Номер товара: ");
        service.removeProduct(productId);
        System.out.println("Товар удалён");
    }

    private void createOrder() {
        String customerName = readText("Имя клиента: ");
        Order order = service.createOrder(customerName);
        System.out.println("Создан " + order.describe());
    }

    private void addItemToOrder() throws OrderException {
        int orderId = readInt("Номер заказа: ");
        int productId = readInt("Номер товара: ");
        int quantity = readInt("Количество: ");
        service.addItemToOrder(orderId, productId, quantity);
        System.out.println("Товар добавлен в заказ");
    }

    private void showOrderDetails() throws OrderException {
        int orderId = readInt("Номер заказа: ");
        System.out.println(service.getOrder(orderId).getDetails());
    }

    private void changeOrderStatus() throws OrderException {
        int orderId = readInt("Номер заказа: ");
        OrderStatus[] statuses = OrderStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i].getTitle());
        }
        int number = readInt("Номер нового статуса: ");
        if (number < 1 || number > statuses.length) {
            System.out.println("Такого статуса нет");
            return;
        }
        service.changeOrderStatus(orderId, statuses[number - 1]);
        System.out.println("Статус изменён");
    }

    private void cancelOrder() throws OrderException {
        int orderId = readInt("Номер заказа: ");
        service.cancelOrder(orderId);
        System.out.println("Заказ отменён");
    }

    private void findOrdersByStatus() {
        OrderStatus[] statuses = OrderStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i].getTitle());
        }
        int number = readInt("Номер статуса: ");
        if (number < 1 || number > statuses.length) {
            System.out.println("Такого статуса нет");
            return;
        }
        showOrders(service.findOrdersByStatus(statuses[number - 1]));
    }

    private void saveData() {
        try {
            storage.save(service.getProductRepository(), service.getOrderRepository());
            System.out.println("Данные сохранены");
        } catch (IOException e) {
            System.out.println("Не удалось сохранить данные: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести целое число");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести число");
            }
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
