package com.shop.storage;

import com.shop.exception.DataFormatException;
import com.shop.model.Order;
import com.shop.model.Product;
import com.shop.repository.Repository;

import java.io.IOException;

public interface DataStorage {

    void save(Repository<Product> products, Repository<Order> orders) throws IOException;

    void load(Repository<Product> products, Repository<Order> orders) throws IOException, DataFormatException;
}
