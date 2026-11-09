package com.shop.model;

public class DigitalProduct extends Product {

    private double fileSizeMb;

    public DigitalProduct(int id, String name, double price, double fileSizeMb) {
        super(id, name, price);
        setFileSizeMb(fileSizeMb);
    }

    public double getFileSizeMb() {
        return fileSizeMb;
    }

    public final void setFileSizeMb(double fileSizeMb) {
        if (fileSizeMb <= 0) {
            throw new IllegalArgumentException("Размер файла должен быть больше нуля");
        }
        this.fileSizeMb = fileSizeMb;
    }

    @Override
    public double getDeliveryCost() {
        return 0;
    }

    @Override
    public String getTypeName() {
        return "Цифровой товар";
    }

    @Override
    public String describe() {
        return super.describe() + ", размер файла " + fileSizeMb + " МБ";
    }

    @Override
    public String toFileLine() {
        return "DIGITAL;" + getId() + ";" + getName() + ";" + getPrice() + ";" + fileSizeMb;
    }
}
