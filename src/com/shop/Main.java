package com.shop;

import com.shop.exception.DataFormatException;
import com.shop.model.Order;
import com.shop.model.Product;
import com.shop.repository.Repository;
import com.shop.service.ShopService;
import com.shop.storage.DataStorage;
import com.shop.storage.FileDataStorage;
import com.shop.ui.ConsoleMenu;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Repository<Product> products = new Repository<>();
        Repository<Order> orders = new Repository<>();
        DataStorage storage = new FileDataStorage("data");

        try {
            storage.load(products, orders);
            System.out.println("Данные загружены. Товаров: " + products.size() + ", заказов: " + orders.size());
        } catch (IOException | DataFormatException e) {
            System.out.println("Не удалось загрузить данные: " + e.getMessage());
            System.out.println("Работа начнётся с пустыми данными");
            products.clear();
            orders.clear();
        }

        ShopService service = new ShopService(products, orders);
        Scanner scanner = new Scanner(System.in);
        ConsoleMenu menu = new ConsoleMenu(service, storage, scanner);
        menu.run();
    }
}
