package com.shop.test;

import com.shop.exception.DataFormatException;
import com.shop.exception.OrderException;
import com.shop.model.DigitalProduct;
import com.shop.model.Order;
import com.shop.model.OrderStatus;
import com.shop.model.PhysicalProduct;
import com.shop.model.Product;
import com.shop.repository.Repository;
import com.shop.service.ShopService;
import com.shop.storage.FileDataStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class SelfTest {

    private interface Action {
        void run() throws Exception;
    }

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testProductCost();
        testProductValidation();
        testOrderTotal();
        testStatusTransitions();
        testCancel();
        testOrderEditing();
        testService();
        testFileStorage();
        testBrokenFile();
        System.out.println();
        System.out.println("Пройдено проверок: " + passed + ", провалено: " + failed);
    }

    private static void testProductCost() {
        System.out.println("--- Стоимость товаров ---");
        Product book = new PhysicalProduct(1, "Книга", 100, 2);
        Product ebook = new DigitalProduct(2, "Электронная книга", 50, 3);
        check("Доставка физического товара: 2 + 2 * 1.5 = 5", same(book.getDeliveryCost(), 5));
        check("Стоимость двух книг: 200 + 2 * 5 = 210", same(book.calculateCost(2), 210));
        check("Доставка цифрового товара равна нулю", same(ebook.getDeliveryCost(), 0));
        check("Стоимость трёх электронных книг: 150", same(ebook.calculateCost(3), 150));
        check("Скидка 10 процентов на 100: 90", same(ebook.calculateCost(2, 10), 90));
    }

    private static void testProductValidation() {
        System.out.println("--- Проверка некорректных данных ---");
        expectError("Отрицательная цена", () -> new DigitalProduct(1, "Игра", -5, 10), IllegalArgumentException.class);
        expectError("Пустое название", () -> new DigitalProduct(1, "   ", 5, 10), IllegalArgumentException.class);
        expectError("Нулевой вес", () -> new PhysicalProduct(1, "Стол", 100, 0), IllegalArgumentException.class);
        expectError("Название с символом точки с запятой", () -> new DigitalProduct(1, "Игра;2", 5, 10), IllegalArgumentException.class);
        expectError("Нулевое количество", () -> new DigitalProduct(1, "Игра", 5, 10).calculateCost(0), IllegalArgumentException.class);
        expectError("Скидка больше ста процентов", () -> new DigitalProduct(1, "Игра", 5, 10).calculateCost(1, 150), IllegalArgumentException.class);
    }

    private static void testOrderTotal() {
        System.out.println("--- Итоговая стоимость заказа ---");
        try {
            Order order = new Order(1, "Иван");
            Product book = new PhysicalProduct(1, "Книга", 100, 2);
            Product game = new DigitalProduct(2, "Игра", 10, 500);
            order.addItem(book, 1);
            order.addItem(game, 10);
            check("Книга 105 плюс игра с оптовой скидкой 95 равно 200", same(order.calculateTotal(), 200));
            order.addItem(book, 1);
            check("Повторное добавление увеличивает количество, позиций по-прежнему две", order.getItems().size() == 2);
        } catch (OrderException e) {
            check("Расчёт заказа без исключений", false);
        }
    }

    private static void testStatusTransitions() {
        System.out.println("--- Переходы между статусами ---");
        Product game = new DigitalProduct(1, "Игра", 10, 500);
        Order order = new Order(1, "Иван");
        try {
            order.addItem(game, 1);
            order.changeStatus(OrderStatus.PAID);
            order.changeStatus(OrderStatus.SHIPPED);
            order.changeStatus(OrderStatus.COMPLETED);
            check("Заказ прошёл путь до статуса Выполнен", order.getStatus() == OrderStatus.COMPLETED);
        } catch (OrderException e) {
            check("Корректная цепочка статусов", false);
        }
        Order second = new Order(2, "Пётр");
        expectError("Пропуск статуса: Новый сразу в Выполнен", () -> second.changeStatus(OrderStatus.COMPLETED), OrderException.class);
        expectError("Оплата пустого заказа", () -> second.changeStatus(OrderStatus.PAID), OrderException.class);
    }

    private static void testCancel() {
        System.out.println("--- Отмена заказа ---");
        Product game = new DigitalProduct(1, "Игра", 10, 500);
        Order completed = new Order(1, "Иван", OrderStatus.COMPLETED, Collections.emptyList());
        expectError("Отмена выполненного заказа запрещена", completed::cancel, OrderException.class);
        Order fresh = new Order(2, "Пётр");
        try {
            fresh.addItem(game, 1);
            fresh.cancel();
            check("Новый заказ успешно отменён", fresh.getStatus() == OrderStatus.CANCELLED);
        } catch (OrderException e) {
            check("Отмена нового заказа", false);
        }
        expectError("Повторная отмена заказа", fresh::cancel, OrderException.class);
    }

    private static void testOrderEditing() {
        System.out.println("--- Изменение состава заказа ---");
        Product game = new DigitalProduct(1, "Игра", 10, 500);
        Order paid = new Order(1, "Иван", OrderStatus.PAID, Collections.emptyList());
        expectError("Добавление товара в оплаченный заказ", () -> paid.addItem(game, 1), OrderException.class);
        Order fresh = new Order(2, "Пётр");
        expectError("Добавление нулевого количества", () -> fresh.addItem(game, 0), OrderException.class);
    }

    private static void testService() {
        System.out.println("--- Сервис магазина ---");
        ShopService service = new ShopService(new Repository<>(), new Repository<>());
        Product book = service.addPhysicalProduct("Книга", 100, 2);
        service.addDigitalProduct("Игра", 10, 500);
        check("Поиск по части названия находит товар", service.findProductsByName("кни").size() == 1);
        check("Поиск без совпадений возвращает пустой список", service.findProductsByName("xyz").isEmpty());
        try {
            Order order = service.createOrder("Иван");
            service.addItemToOrder(order.getId(), book.getId(), 1);
            expectError("Удаление товара из заказа", () -> service.removeProduct(book.getId()), OrderException.class);
            expectError("Несуществующий заказ", () -> service.getOrder(99), OrderException.class);
            expectError("Несуществующий товар в заказе", () -> service.addItemToOrder(order.getId(), 99, 1), OrderException.class);
            service.changeOrderStatus(order.getId(), OrderStatus.PAID);
            service.changeOrderStatus(order.getId(), OrderStatus.SHIPPED);
            service.changeOrderStatus(order.getId(), OrderStatus.COMPLETED);
            check("Выручка равна стоимости выполненного заказа 105", same(service.calculateRevenue(), 105));
        } catch (OrderException e) {
            check("Сценарий сервиса без исключений: " + e.getMessage(), false);
        }
    }

    private static void testFileStorage() {
        System.out.println("--- Сохранение и загрузка ---");
        Path directory = null;
        try {
            directory = Files.createTempDirectory("shop-test");
            FileDataStorage storage = new FileDataStorage(directory.toString());
            ShopService service = new ShopService(new Repository<>(), new Repository<>());
            Product book = service.addPhysicalProduct("Книга", 100.5, 2);
            Product game = service.addDigitalProduct("Игра", 10, 500);
            Order order = service.createOrder("Иван");
            service.addItemToOrder(order.getId(), book.getId(), 2);
            service.addItemToOrder(order.getId(), game.getId(), 1);
            service.changeOrderStatus(order.getId(), OrderStatus.PAID);
            storage.save(service.getProductRepository(), service.getOrderRepository());

            Repository<Product> loadedProducts = new Repository<>();
            Repository<Order> loadedOrders = new Repository<>();
            storage.load(loadedProducts, loadedOrders);
            check("Загружено два товара", loadedProducts.size() == 2);
            check("Загружен один заказ", loadedOrders.size() == 1);
            Order loaded = loadedOrders.findById(order.getId());
            check("Статус заказа сохранился", loaded.getStatus() == OrderStatus.PAID);
            check("Стоимость заказа сохранилась", same(loaded.calculateTotal(), order.calculateTotal()));
        } catch (IOException | DataFormatException | OrderException e) {
            check("Сохранение и загрузка без ошибок: " + e.getMessage(), false);
        } finally {
            deleteDirectory(directory);
        }
    }

    private static void testBrokenFile() {
        System.out.println("--- Повреждённый файл ---");
        Path directory = null;
        try {
            directory = Files.createTempDirectory("shop-test");
            Files.write(directory.resolve("products.txt"),
                    Collections.singletonList("PHYSICAL;1;Книга;abc;2"), StandardCharsets.UTF_8);
            FileDataStorage storage = new FileDataStorage(directory.toString());
            expectError("Нечисловая цена в файле", () -> storage.load(new Repository<>(), new Repository<>()), DataFormatException.class);
            Files.write(directory.resolve("products.txt"),
                    Collections.singletonList("DIGITAL;1;Игра;10;500"), StandardCharsets.UTF_8);
            Files.write(directory.resolve("orders.txt"),
                    Collections.singletonList("1;Иван;NEW;7:1"), StandardCharsets.UTF_8);
            expectError("Заказ ссылается на несуществующий товар", () -> storage.load(new Repository<>(), new Repository<>()), DataFormatException.class);
        } catch (IOException e) {
            check("Подготовка тестовых файлов: " + e.getMessage(), false);
        } finally {
            deleteDirectory(directory);
        }
    }

    private static void deleteDirectory(Path directory) {
        if (directory == null) {
            return;
        }
        try {
            Files.deleteIfExists(directory.resolve("products.txt"));
            Files.deleteIfExists(directory.resolve("orders.txt"));
            Files.deleteIfExists(directory);
        } catch (IOException e) {
            System.out.println("Не удалось удалить временную папку: " + directory);
        }
    }

    private static boolean same(double first, double second) {
        return Math.abs(first - second) < 0.001;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[OK]   " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static void expectError(String name, Action action, Class<? extends Exception> expected) {
        try {
            action.run();
            check(name + " (ожидалась ошибка)", false);
        } catch (Exception e) {
            check(name + " (ошибка: " + e.getMessage() + ")", expected.isInstance(e));
        }
    }
}
