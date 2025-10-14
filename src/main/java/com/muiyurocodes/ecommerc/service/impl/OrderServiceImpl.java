package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.*;
import com.muiyurocodes.ecommerc.exception.InsufficientStockException;
import com.muiyurocodes.ecommerc.exception.UserNotFoundException;
import com.muiyurocodes.ecommerc.model.*;
import com.muiyurocodes.ecommerc.repository.OrderRepository;
import com.muiyurocodes.ecommerc.repository.ProductRepository;
import com.muiyurocodes.ecommerc.repository.UserRepository;
import com.muiyurocodes.ecommerc.service.CartService;
import com.muiyurocodes.ecommerc.service.OrderService;
import com.muiyurocodes.ecommerc.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final ModelMapper modelMapper;

    @Override
    public OrderDTO placeOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        com.muiyurocodes.ecommerc.dto.CartDTO cartDTO = cartService.getCartForUser(userId);
        if (cartDTO.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        // 1. Validate stock for all items
        for (com.muiyurocodes.ecommerc.dto.CartItemDTO item : cartDTO.getItems()) {
            productRepository.findByIdAndStockQuantityGreaterThan(item.getProductId(), item.getQuantity() - 1)
                    .orElseThrow(() -> new InsufficientStockException(
                            "Not enough stock for product: " + item.getProductName()));
        }

        // 2. Create and save the order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setShippingAddress("Default Shipping Address"); // Placeholder

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 3. Create OrderItems and decrement stock
        for (com.muiyurocodes.ecommerc.dto.CartItemDTO cartItemDTO : cartDTO.getItems()) {
            Product product = productRepository.findById(cartItemDTO.getProductId()).get(); // Already validated stock
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);

            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(cartItemDTO.getQuantity())));

            // Decrement stock
            product.setStockQuantity(product.getStockQuantity() - cartItemDTO.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        // 4. Clear the cart
        cartService.clearCart(userId);

        return convertToOrderDTO(savedOrder);
    }

    @Override
    public Page<OrderDTO> getOrderHistoryForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Page<Order> orderPage = orderRepository.findByUser(user, pageable);
        return orderPage.map(this::convertToOrderDTO);
    }

    @Override
    public Optional<OrderDTO> getOrderDetails(Long userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return orderRepository.findByIdAndUser(orderId, user)
                .map(this::convertToOrderDTO);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderDTO.setUserId(order.getUser().getId());
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList());
        orderDTO.setOrderItems(itemDTOs);
        return orderDTO;
    }

    // Admin dashboard and reporting methods

    @Override
    public DashboardDTO getDashboardStats() {
        // Get total sales
        BigDecimal totalSales = orderRepository.findAll().stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get order count
        long orderCount = orderRepository.count();

        // Calculate average order value
        BigDecimal averageOrderValue = orderCount > 0
                ? totalSales.divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        // Get user count
        long userCount = userRepository.count();

        // Get new users today
        LocalDate today = LocalDate.now();
        // This is a placeholder - in a real implementation, you would query users
        // created today
        long newUsersToday = 5; // Placeholder value

        // Get product count
        long productCount = productRepository.count();

        // Get low stock products
        List<ProductResponseDTO> lowStockProducts = productService.getLowStockProducts(5);

        // Get recent orders (last 5)
        List<Order> recentOrders = orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<OrderDTO> recentOrderDTOs = recentOrders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        // Build and return the dashboard DTO
        return DashboardDTO.builder()
                .totalSales(totalSales)
                .orderCount(orderCount)
                .averageOrderValue(averageOrderValue)
                .userCount(userCount)
                .newUsersToday(newUsersToday)
                .productCount(productCount)
                .lowStockProducts(lowStockProducts)
                .recentOrders(recentOrderDTOs)
                .build();
    }

    @Override
    public SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        // Validate input
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Get all orders in the date range
        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> {
                    LocalDate orderDate = order.getOrderDate().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // Calculate total sales and orders
        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();

        // Calculate average order value
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        // Group sales by period (day, week, or month)
        Map<String, BigDecimal> salesByPeriod = new LinkedHashMap<>();
        Map<String, Long> ordersByPeriod = new LinkedHashMap<>();

        DateTimeFormatter formatter;
        switch (groupBy.toLowerCase()) {
            case "week":
                formatter = DateTimeFormatter.ofPattern("yyyy-'W'w");
                break;
            case "month":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case "day":
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                groupBy = "day"; // Default to day if invalid
        }

        // Initialize periods
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String periodKey = current.format(formatter);
            salesByPeriod.putIfAbsent(periodKey, BigDecimal.ZERO);
            ordersByPeriod.putIfAbsent(periodKey, 0L);

            // Increment based on groupBy
            switch (groupBy) {
                case "week":
                    current = current.plusWeeks(1);
                    break;
                case "month":
                    current = current.plusMonths(1);
                    break;
                default:
                    current = current.plusDays(1);
            }
        }

        // Populate data
        for (Order order : orders) {
            String periodKey = order.getOrderDate().toLocalDate().format(formatter);

            // Update sales for period
            BigDecimal currentSales = salesByPeriod.get(periodKey);
            salesByPeriod.put(periodKey, currentSales.add(order.getTotalPrice()));

            // Update order count for period
            Long currentOrders = ordersByPeriod.get(periodKey);
            ordersByPeriod.put(periodKey, currentOrders + 1);
        }

        // Calculate top selling products
        Map<Long, BigDecimal> productSales = new HashMap<>();
        Map<Long, String> productNames = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                Long productId = item.getProduct().getId();
                String productName = item.getProduct().getName();
                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

                productNames.put(productId, productName);
                productSales.merge(productId, itemTotal, BigDecimal::add);
            }
        }

        List<SalesReportDTO.ProductSalesDTO> topSellingProducts = productSales.entrySet().stream()
                .map(entry -> new SalesReportDTO.ProductSalesDTO(
                        entry.getKey(),
                        productNames.get(entry.getKey()),
                        1L, // Placeholder for quantity sold
                        entry.getValue()))
                .sorted(Comparator.comparing(SalesReportDTO.ProductSalesDTO::getTotalRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Calculate top spending customers
        Map<Long, BigDecimal> customerSales = new HashMap<>();
        Map<Long, String> customerNames = new HashMap<>();

        for (Order order : orders) {
            Long userId = order.getUser().getId();
            String userName = order.getUser().getFirstName() + " " + order.getUser().getLastName();

            customerNames.put(userId, userName);
            customerSales.merge(userId, order.getTotalPrice(), BigDecimal::add);
        }

        List<SalesReportDTO.CustomerSalesDTO> topSpendingCustomers = customerSales.entrySet().stream()
                .map(entry -> new SalesReportDTO.CustomerSalesDTO(
                        entry.getKey(),
                        "", // Placeholder for user email
                        customerNames.get(entry.getKey()),
                        1L, // Placeholder for order count
                        entry.getValue()))
                .sorted(Comparator.comparing(SalesReportDTO.CustomerSalesDTO::getTotalSpent).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Build and return the sales report DTO
        return SalesReportDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .groupBy(groupBy)
                .totalSales(totalSales)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .salesByPeriod(salesByPeriod)
                .ordersByPeriod(ordersByPeriod)
                .topSellingProducts(topSellingProducts)
                .topSpendingCustomers(topSpendingCustomers)
                .build();
    }

    @Override
    public Page<OrderDTO> getAllOrders(String status, Pageable pageable) {
        Page<Order> orders;

        if (status != null && !status.isEmpty()) {
            // If status is provided, filter by status
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            // Otherwise, get all orders
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::convertToOrderDTO);
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return convertToOrderDTO(updatedOrder);
    }

    @Override
    public void updateOrderStatusFromWebhook(Long orderId, String status) {

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        } else {
            // better logging in order to track issues and prevent silent failures
            log.error("Could not find order with ID: {}", orderId);
        }

    }
}
