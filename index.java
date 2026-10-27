package com.architecture.engine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Domain primitive representing a single order item.
 * Uses Java Records for built-in immutability, thread-safety, and data integrity.
 */
record OrderItem(String productId, int quantity, BigDecimal pricePerUnit) {
    public OrderItem {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        if (quantity  items, Instant createdAt) {
    public Order {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(items, "Items list cannot be null");
        if (items.isEmpty()) throw new IllegalArgumentException("Order must contain at least one item");
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

/**
 * Strongly-typed domain-specific exception handling.
 */
class OrderProcessingException extends RuntimeException {
    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Functional interface simulating an external enterprise payment gateway service.
 */
@FunctionalInterface
interface PaymentGateway {
    boolean process(UUID orderId, BigDecimal amount);
}

/**
 * Core Orchestrator Engine handling high-throughput asynchronous operations.
 */
public final class OrderProcessorEngine implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(OrderProcessorEngineEngine.class.getName());
    private final ExecutorService virtualThreadExecutor;
    private final PaymentGateway paymentGateway;

    public OrderProcessorEngine(PaymentGateway paymentGateway) {
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
        // Utilizing modern Java Virtual Threads for scalable, lightweight concurrency
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Executes asynchronous processing pipeline using functional programming paradigms.
     * Separates thread allocation from business logic via CompletableFuture.
     */
    public CompletableFuture<Boolean> processOrderAsync(final Order order) {
        LOGGER.info(() -> "Initiating async processing for Order ID: " + order.orderId());

        return CompletableFuture.supplyAsync(() -> validateInventory(order), virtualThreadExecutor)
                .thenApplyAsync(isValid -> {
                    if (!isValid) throw new IllegalStateException("Inventory validation failed.");
                    return order.calculateTotal();
                }, virtualThreadExecutor)
                .thenApplyAsync(totalAmount -> paymentGateway.process(order.orderId(), totalAmount), virtualThreadExecutor)
                .exceptionally(ex -> {
                    LOGGER.severe(() -> "Pipeline failed for Order: " + order.orderId() + ". Reason: " + ex.getMessage());
                    throw new OrderProcessingException("Critical processing failure occurred", ex);
                });
    }

    private boolean validateInventory(Order order) {
        // Business logic simulation: Verifying items via functional streams
        return order.items().stream()
                .allMatch(item -> item.quantity() < 100); // Mock constraint
    }

    @Override
    public void close() {
        // Clean resource teardown preventing memory leaks
        this.virtualThreadExecutor.shutdown();
    }

    /**
     * Main runtime entrypoint showing production execution flow.
     */
    public static void main(String[] args) {
        // Mock external Dependency Injection
        PaymentGateway mockGateway = (id, amount) -> {
            LOGGER.info(() -> "Charging card for order " + id + " in amount of $" + amount);
            return true; 
        };

        try (OrderProcessorEngine engine = new OrderProcessorEngine(mockGateway)) {
            // Constructing clean, immutable domain models
            List<OrderItem> items = List.of(
                new OrderItem("PROD-001", 2, new BigDecimal("49.99")),
                new OrderItem("PROD-002", 1, new BigDecimal("12.50"))
            );
            
            Order newOrder = new Order(UUID.randomUUID(), "CUST-9921", items, Instant.now());

            // Non-blocking trigger and callback registration
            engine.processOrderAsync(newOrder)
                .thenAccept(success -> LOGGER.info("Order Pipeline Finished Successfully: " + success))
                .join(); // Blocks main thread just for demonstration verification

        } catch (Exception e) {
            LOGGER.severe("Application context failed to execute safely: " + e.getMessage());
        }
    }
}
package com.example.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain model representing an order item.
 * Uses a Java Record for immutability and concise boilerplate.
 */
record OrderItem(String sku, String productName, int quantity, BigDecimal unitPrice) {
    public OrderItem {
        Objects.requireNonNull(sku, "SKU must not be null");
        Objects.requireNonNull(unitPrice, "Unit price must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/**
 * Service class handling order calculation logic.
 */
public class OrderService {

    /**
     * Calculates the total price for a collection of order items.
     *
     * @.param items the list of order items
     * @return the total aggregate price
     */
    public BigDecimal calculateOrderTotal(List<OrderItem> items) {
        return Optional.ofNullable(items)
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static void main(String[] args) {
        OrderService orderService = new OrderService();

        List<OrderItem> sampleOrder = List.of(
                new OrderItem("SKU-1001", "Developer Laptop", 1, new BigDecimal("1299.99")),
                new OrderItem("SKU-1002", "Wireless Mouse", 2, new BigDecimal("49.50"))
        );

        BigDecimal total = orderService.calculateOrderTotal(sampleOrder);
        System.out.printf("Total Order Amount: $%s%n", total);
    }
}