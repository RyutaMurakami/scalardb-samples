package sample;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;
import com.scalar.db.service.TransactionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Sample implements AutoCloseable {

    private final DistributedTransactionManager manager;

    public Sample() throws IOException {
        // Create a transaction manager object
        TransactionFactory factory = TransactionFactory.create("database.properties");
        manager = factory.getTransactionManager();
    }

    /// 村上 修正してます
    public void loadInitialData() throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            transaction = manager.start();
            loadCustomerIfNotExists(transaction, 1, "Naoi Tomoki", 1122, 123);
            loadCustomerIfNotExists(transaction, 2, "Murakami Ryuta", 3344, 456);
            loadCustomerIfNotExists(transaction, 3, "Murata Kijun", 5566, 789);
            loadStockIfNotExists(transaction, 1, "Mac", 1000, 1, 1);
            loadStockIfNotExists(transaction, 2, "WinPC", 2000, 2, 2);
            loadStockIfNotExists(transaction, 3, "WinPC", 2500, 1, 3);
            loadStockIfNotExists(transaction, 4, "Mouse", 5000, 2, 2);
            loadStockIfNotExists(transaction, 5, "Keyboard", 3000, 1, 1);
            loadShopIfNotExists(transaction, 1, "kojima", 135);
            loadShopIfNotExists(transaction, 2, "bigcamera", 246);
            transaction.commit();
        } catch (TransactionException e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    /// 村上 修正してます
    private void loadCustomerIfNotExists(
            DistributedTransaction transaction,
            int customerId,
            String name,
            int creditNumber,
            int customerAddress)
            throws TransactionException {
        Optional<Result> customer = transaction.get(
                Get.newBuilder()
                        .namespace("customer")
                        .table("customers")
                        .partitionKey(Key.ofInt("customer_id", customerId))
                        .build());
        if (!customer.isPresent()) {
            transaction.put(
                    Put.newBuilder()
                            .namespace("customer")
                            .table("customers")
                            .partitionKey(Key.ofInt("customer_id", customerId))
                            .textValue("name", name)
                            .intValue("credit_number", creditNumber)
                            .intValue("customer_address", customerAddress)
                            .build());
        }
    }

    /// 村上 修正してます
    private void loadStockIfNotExists(
            DistributedTransaction transaction, int itemId, String name, int price, int shopId, int stock)
            throws TransactionException {
        Optional<Result> item = transaction.get(
                Get.newBuilder()
                        .namespace("item")
                        .table("stocks")
                        .partitionKey(Key.ofInt("item_id", itemId))
                        .build());
        if (!item.isPresent()) {
            transaction.put(
                    Put.newBuilder()
                            .namespace("item")
                            .table("stocks")
                            .partitionKey(Key.ofInt("item_id", itemId))
                            .textValue("name", name)
                            .intValue("price", price)
                            .intValue("shop_id", shopId)
                            .intValue("stock", stock)
                            .build());
        }
    }

    private void loadShopIfNotExists(
            DistributedTransaction transaction, int shopId, String shopName, int shopAddress)
            throws TransactionException {
        // Check if shop exists
        Optional<Result> shop = transaction.get(
                Get.newBuilder()
                        .namespace("item")
                        .table("shops")
                        .partitionKey(Key.ofInt("shop_id", shopId))
                        .build());
        // If not, add new shop
        if (!shop.isPresent()) {
            transaction.put(
                    Put.newBuilder()
                            .namespace("item")
                            .table("shops")
                            .partitionKey(Key.ofInt("shop_id", shopId))
                            .textValue("shop_name", shopName)
                            .intValue("shop_address", shopAddress)
                            .build());
        }
    }

    /// 村上 修正してます
    public String getCustomerInfo(int customerId) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Retrieve the customer info for the specified customer ID from the customers
            // table
            Optional<Result> customer = transaction.get(
                    Get.newBuilder()
                            .namespace("customer")
                            .table("customers")
                            .partitionKey(Key.ofInt("customer_id", customerId))
                            .build());

            if (!customer.isPresent()) {
                // If the customer info the specified customer ID doesn't exist, throw an
                // exception
                throw new RuntimeException("Customer not found");
            }

            // Commit the transaction (even when the transaction is read-only, we need to
            // commit)
            transaction.commit();

            // Return the customer info as a JSON format
            return String.format(
                    "{\"id\": %d, \"name\": \"%s\", \"credit_number\": %d, \"customer_address\": %d}",
                    customerId,
                    customer.get().getText("name"),
                    customer.get().getInt("credit_number"),
                    customer.get().getInt("customer_address"));
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    // 村田追加
    public void addNewItem(int itemId, String itemName, int price, int shopId, int stock) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Check if the item already exists
            Get get = Get.newBuilder()
                    .namespace("item")
                    .table("stocks")
                    .partitionKey(Key.ofInt("item_id", itemId))
                    .build();
            Optional<Result> result = transaction.get(get);

            if (result.isPresent()) {
                System.out.println("The item already exists.");
                return;
            }

            // Load the item stock if it doesn't exist
            loadStockIfNotExists(transaction, itemId, itemName, price, shopId, stock);

            // Commit the transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    // 村田追加
    public List<String> getItemInfo(String itemName) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Assume that the item IDs are consecutive integers starting from 1
            int itemId = 1;
            List<String> itemResults = new ArrayList<>();
            while (true) {
                Optional<Result> item = transaction.get(
                        Get.newBuilder()
                                .namespace("item")
                                .table("stocks")
                                .partitionKey(Key.ofInt("item_id", itemId))
                                .build());
                if (!item.isPresent()) {
                    break;
                }
                if (item.get().getText("name").equals(itemName)) {
                    itemResults.add(
                            String.format(
                                    "{\"name\": \"%s\", \"price\": %d, \"shop_id\": %d, \"stock\": %d}",
                                    itemName,
                                    item.get().getInt("price"),
                                    item.get().getInt("shop_id"),
                                    item.get().getInt("stock")));
                }
                itemId++;
            }

            if (itemResults.isEmpty()) {
                // If the item info for the specified item name doesn't exist, throw an
                // exception
                throw new RuntimeException("Item not found");
            }

            // Commit the transaction (even when the transaction is read-only, we need to
            // commit)
            transaction.commit();

            // Return the item info as a JSON format
            return itemResults;
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    // 村田追加
    public List<String> getItemsByShopId(int shopId) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Retrieve the item info for the shop ID from the stocks table
            List<Result> items = transaction.scan(
                    Scan.newBuilder()
                            .namespace("item")
                            .table("stocks")
                            .indexKey(Key.ofInt("shop_id", shopId))
                            .build());

            // Make item JSONs for the items of the shop
            List<String> itemJsons = new ArrayList<>();
            for (Result item : items) {
                itemJsons.add(
                        String.format(
                                "{\"item_id\": %d, \"name\": \"%s\", \"price\": %d, \"stock\": %d}",
                                item.getInt("item_id"),
                                item.getText("name"),
                                item.getInt("price"),
                                item.getInt("stock")));
            }

            // Commit the transaction (even when the transaction is read-only, we need to
            // commit)
            transaction.commit();

            // Return the item info as a JSON format
            return itemJsons;
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    // 村田変更
    public String placeOrder(int customerId, int[] itemIds, int[] itemCounts)
            throws TransactionException {
        assert itemIds.length == itemCounts.length;

        DistributedTransaction transaction = null;
        try {
            String orderId = UUID.randomUUID().toString();

            // Start a transaction
            transaction = manager.start();

            // Put the order info into the orders table
            transaction.put(
                    Put.newBuilder()
                            .namespace("item")// .namespace("order")
                            .table("orders")
                            .partitionKey(Key.ofInt("customer_id", customerId))
                            .clusteringKey(Key.ofBigInt("timestamp", System.currentTimeMillis()))
                            .textValue("order_id", orderId)
                            .build());

            for (int i = 0; i < itemIds.length; i++) {
                int itemId = itemIds[i];
                int count = itemCounts[i];

                // Put the order statement into the statements table
                transaction.put(
                        Put.newBuilder()
                                .namespace("item")
                                .table("statements")
                                .partitionKey(Key.ofText("order_id", orderId))
                                .clusteringKey(Key.ofInt("item_id", itemId))
                                .intValue("count", count)
                                .build());

                // Retrieve the item info from the items table
                Optional<Result> item = transaction.get(
                        Get.newBuilder()
                                .namespace("item")
                                .table("stocks")
                                .partitionKey(Key.ofInt("item_id", itemId))
                                .build());

                if (!item.isPresent()) {
                    throw new RuntimeException("Item not found");
                }

                // Decrease the stock of the item
                int updatedStock = item.get().getInt("stock") - count;
                if (updatedStock < 0) {
                    throw new RuntimeException("Insufficient stock");
                }

                // Update the stock of the item
                transaction.put(
                        Put.newBuilder()
                                .namespace("item")
                                .table("stocks")
                                .partitionKey(Key.ofInt("item_id", itemId))
                                .intValue("stock", updatedStock)
                                .build());
            }

            // Commit the transaction
            transaction.commit();

            // Return the order id
            return String.format("{\"order_id\": \"%s\"}", orderId);
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    // 村田追加
    public void increaseItemStock(int itemId, int amount) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Retrieve the item info for the item ID from the stocks table
            Get get = Get.newBuilder()
                    .namespace("item")
                    .table("stocks")
                    .partitionKey(Key.ofInt("item_id", itemId))
                    .build();
            Optional<Result> result = transaction.get(get);

            if (result.isPresent()) {
                // Increase the stock of the item
                int currentStock = result.get().getInt("stock");
                int newStock = currentStock + amount;

                // Create a new Put operation
                Put put = Put.newBuilder()
                        .namespace("item")
                        .table("stocks")
                        .partitionKey(Key.ofInt("item_id", itemId))
                        .intValue("stock", newStock) // Update the stock column value
                        .build();
                transaction.put(put);

                // Commit the transaction
                transaction.commit();
            } else {
                throw new RuntimeException("The specified item does not exist.");
            }
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    // 村田変更
    private String getOrderJson(DistributedTransaction transaction, String orderId)
            throws TransactionException {
        // Retrieve the order info for the order ID from the orders table
        Optional<Result> order = transaction.get(
                Get.newBuilder()
                        .namespace("item")
                        .table("orders")
                        .indexKey(Key.ofText("order_id", orderId))
                        .build());

        if (!order.isPresent()) {
            throw new RuntimeException("Order not found");
        }

        int customerId = order.get().getInt("customer_id");

        // Retrieve the customer info for the specified customer ID from the customers
        // table
        Optional<Result> customer = transaction.get(
                Get.newBuilder()
                        .namespace("customer")
                        .table("customers")
                        .partitionKey(Key.ofInt("customer_id", customerId))
                        .build());
        assert customer.isPresent();

        // Retrieve the order statements for the order ID from the statements table
        List<Result> statements = transaction.scan(
                Scan.newBuilder()
                        .namespace("item")
                        .table("statements")
                        .partitionKey(Key.ofText("order_id", orderId))
                        .build());

        // Make the statements JSONs
        List<String> statementJsons = new ArrayList<>();
        int total = 0;
        for (Result statement : statements) {
            int itemId = statement.getInt("item_id");

            // Retrieve the item data from the items table
            Optional<Result> item = transaction.get(
                    Get.newBuilder()
                            .namespace("item")
                            .table("stocks")
                            .partitionKey(Key.ofInt("item_id", itemId))
                            .build());

            if (!item.isPresent()) {
                throw new RuntimeException("Item not found");
            }

            int price = item.get().getInt("price");
            int count = statement.getInt("count");

            statementJsons.add(
                    String.format(
                            "{\"item_id\": %d,\"item_name\": \"%s\",\"price\": %d,\"count\": %d,\"total\": %d}",
                            itemId, item.get().getText("name"), price, count, price * count));

            total += price * count;
        }

        // Return the order info as a JSON format
        return String.format(
                "{\"order_id\": \"%s\",\"timestamp\": %d,\"customer_id\": %d,\"customer_name\": \"%s\",\"statement\": [%s],\"total\": %d}",
                orderId,
                order.get().getBigInt("timestamp"),
                customerId,
                customer.get().getText("name"),
                String.join(",", statementJsons),
                total);
    }

    public String getOrderByOrderId(String orderId) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Get an order JSON for the specified order ID
            String orderJson = getOrderJson(transaction, orderId);

            // Commit the transaction (even when the transaction is read-only, we need to
            // commit)
            transaction.commit();

            // Return the order info as a JSON format
            return String.format("{\"order\": %s}", orderJson);
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    public String getOrdersByCustomerId(int customerId) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Retrieve the order info for the customer ID from the orders table
            List<Result> orders = transaction.scan(
                    Scan.newBuilder()
                            .namespace("order")
                            .table("orders")
                            .partitionKey(Key.ofInt("customer_id", customerId))
                            .build());

            // Make order JSONs for the orders of the customer
            List<String> orderJsons = new ArrayList<>();
            for (Result order : orders) {
                orderJsons.add(getOrderJson(transaction, order.getText("order_id")));
            }

            // Commit the transaction (even when the transaction is read-only, we need to
            // commit)
            transaction.commit();

            // Return the order info as a JSON format
            return String.format("{\"order\": [%s]}", String.join(",", orderJsons));
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    public void repayment(int customerId, int amount) throws TransactionException {
        DistributedTransaction transaction = null;
        try {
            // Start a transaction
            transaction = manager.start();

            // Retrieve the customer info for the specified customer ID from the customers
            // table
            Optional<Result> customer = transaction.get(
                    Get.newBuilder()
                            .namespace("customer")
                            .table("customers")
                            .partitionKey(Key.ofInt("customer_id", customerId))
                            .build());
            if (!customer.isPresent()) {
                throw new RuntimeException("Customer not found");
            }

            int updatedCreditTotal = customer.get().getInt("credit_total") - amount;

            // Check if over repayment or not
            if (updatedCreditTotal < 0) {
                throw new RuntimeException("Over repayment");
            }

            // Reduce credit_total for the customer
            transaction.put(
                    Put.newBuilder()
                            .namespace("customer")
                            .table("customers")
                            .partitionKey(Key.ofInt("customer_id", customerId))
                            .intValue("credit_total", updatedCreditTotal)
                            .build());

            // Commit the transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                // If an error occurs, abort the transaction
                transaction.abort();
            }
            throw e;
        }
    }

    @Override
    public void close() {
        manager.close();
    }
}
