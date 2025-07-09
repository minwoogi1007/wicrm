package com.wio.repairsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "RELEASE_ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "release_order_seq")
    @SequenceGenerator(name = "release_order_seq", sequenceName = "RELEASE_ORDER_SEQ", allocationSize = 1)
    private Long releaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private MainRequest mainRequest;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private String partCode;

    private int quantity;
    private String status;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }

}
