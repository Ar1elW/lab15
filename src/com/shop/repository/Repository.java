package com.shop.repository;

import com.shop.model.Identifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Repository<T extends Identifiable> {

    private final Map<Integer, T> items = new LinkedHashMap<>();

    public void add(T item) {
        items.put(item.getId(), item);
    }

    public boolean remove(int id) {
        return items.remove(id) != null;
    }

    public T findById(int id) {
        return items.get(id);
    }

    public List<T> findAll() {
        return new ArrayList<>(items.values());
    }

    public List<T> findBy(Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        for (T item : items.values()) {
            if (condition.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public int nextId() {
        int maxId = 0;
        for (int id : items.keySet()) {
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }

    public int size() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }
}
