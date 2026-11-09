package com.shop.model;

public class PhysicalProduct extends Product {

    private static final double BASE_DELIVERY = 2.0;
    private static final double DELIVERY_PER_KG = 1.5;

    private double weightKg;

    public PhysicalProduct(int id, String name, double price, double weightKg) {
        super(id, name, price);
        setWeightKg(weightKg);
    }

    public double getWeightKg() {
        return weightKg;
    }

    public final void setWeightKg(double weightKg) {
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Вес должен быть больше нуля");
        }
        this.weightKg = weightKg;
    }

    @Override
    public double getDeliveryCost() {
        return BASE_DELIVERY + weightKg * DELIVERY_PER_KG;
    }

    @Override
    public String getTypeName() {
        return "Физический товар";
    }

    @Override
    public String describe() {
        return super.describe() + ", вес " + weightKg + " кг";
    }

    @Override
    public String toFileLine() {
        return "PHYSICAL;" + getId() + ";" + getName() + ";" + getPrice() + ";" + weightKg;
    }
}
