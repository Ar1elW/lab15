package com.shop.storage;

import com.shop.exception.DataFormatException;
import com.shop.model.Order;
import com.shop.model.Product;
import com.shop.model.Savable;
import com.shop.repository.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileDataStorage implements DataStorage {

    private final Path directory;
    private final Path productsFile;
    private final Path ordersFile;

    public FileDataStorage(String directoryName) {
        this.directory = Paths.get(directoryName);
        this.productsFile = directory.resolve("products.txt");
        this.ordersFile = directory.resolve("orders.txt");
    }

    @Override
    public void save(Repository<Product> products, Repository<Order> orders) throws IOException {
        Files.createDirectories(directory);
        writeLines(productsFile, products.findAll());
        writeLines(ordersFile, orders.findAll());
    }

    @Override
    public void load(Repository<Product> products, Repository<Order> orders) throws IOException, DataFormatException {
        products.clear();
        orders.clear();
        if (Files.exists(productsFile)) {
            for (String line : Files.readAllLines(productsFile, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) {
                    products.add(DataParser.parseProduct(line));
                }
            }
        }
        if (Files.exists(ordersFile)) {
            for (String line : Files.readAllLines(ordersFile, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) {
                    orders.add(DataParser.parseOrder(line, products));
                }
            }
        }
    }

    private <T extends Savable> void writeLines(Path file, List<T> objects) throws IOException {
        List<String> lines = new ArrayList<>();
        for (T object : objects) {
            lines.add(object.toFileLine());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
