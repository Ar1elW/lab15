package com.shop.storage;

import com.shop.exception.DataFormatException;
import com.shop.model.DigitalProduct;
import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.OrderStatus;
import com.shop.model.PhysicalProduct;
import com.shop.model.Product;
import com.shop.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class DataParser {

    private DataParser() {
    }

    public static Product parseProduct(String line) throws DataFormatException {
        String[] parts = line.split(";", -1);
        if (parts.length != 5) {
            throw new DataFormatException("Неверное число полей в строке товара: " + line);
        }
        try {
            String type = parts[0];
            int id = Integer.parseInt(parts[1]);
            String name = parts[2];
            double price = Double.parseDouble(parts[3]);
            double extra = Double.parseDouble(parts[4]);
            if (type.equals("PHYSICAL")) {
                return new PhysicalProduct(id, name, price, extra);
            }
            if (type.equals("DIGITAL")) {
                return new DigitalProduct(id, name, price, extra);
            }
        } catch (IllegalArgumentException e) {
            throw new DataFormatException("Некорректные данные в строке товара: " + line);
        }
        throw new DataFormatException("Неизвестный тип товара в строке: " + line);
    }

    public static Order parseOrder(String line, Repository<Product> products) throws DataFormatException {
        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            throw new DataFormatException("Неверное число полей в строке заказа: " + line);
        }
        try {
            int id = Integer.parseInt(parts[0]);
            String customerName = parts[1];
            OrderStatus status = OrderStatus.valueOf(parts[2]);
            List<OrderItem> items = parseItems(parts[3], products);
            return new Order(id, customerName, status, items);
        } catch (IllegalArgumentException e) {
            throw new DataFormatException("Некорректные данные в строке заказа: " + line);
        }
    }

    private static List<OrderItem> parseItems(String text, Repository<Product> products) throws DataFormatException {
        List<OrderItem> items = new ArrayList<>();
        if (text.isEmpty()) {
            return items;
        }
        for (String itemText : text.split(",")) {
            String[] pair = itemText.split(":");
            if (pair.length != 2) {
                throw new DataFormatException("Неверная позиция заказа: " + itemText);
            }
            int productId = Integer.parseInt(pair[0]);
            int quantity = Integer.parseInt(pair[1]);
            Product product = products.findById(productId);
            if (product == null) {
                throw new DataFormatException("В заказе указан несуществующий товар: " + productId);
            }
            items.add(new OrderItem(product, quantity));
        }
        return items;
    }
}
