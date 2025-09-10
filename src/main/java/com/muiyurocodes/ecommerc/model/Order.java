package com.muiyurocodes.ecommerc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Double totalAmount;

    private String status; // e.g., PENDING, COMPLETED, SHIPPED

    private LocalDateTime orderDate;

    @Column
    private String shippingAddress;

    @Column
    private String paymentId;

    private void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}
