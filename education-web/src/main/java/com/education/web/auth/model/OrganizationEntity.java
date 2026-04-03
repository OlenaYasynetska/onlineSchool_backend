package com.education.web.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "admin_user_id", nullable = false, length = 36)
    private String adminUserId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlanEntity plan;

    @Column(name = "payment_period", nullable = false, length = 16)
    private String paymentPeriod;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "next_billing_at")
    private LocalDateTime nextBillingAt;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "total_received", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalReceived;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "country", length = 64)
    private String country;

    @PrePersist
    void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.registeredAt == null) {
            this.registeredAt = Instant.now();
        }
        if (this.totalReceived == null) {
            this.totalReceived = BigDecimal.ZERO;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        this.adminUserId = adminUserId;
    }

    public SubscriptionPlanEntity getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlanEntity plan) {
        this.plan = plan;
    }

    public String getPaymentPeriod() {
        return paymentPeriod;
    }

    public void setPaymentPeriod(String paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getNextBillingAt() {
        return nextBillingAt;
    }

    public void setNextBillingAt(LocalDateTime nextBillingAt) {
        this.nextBillingAt = nextBillingAt;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public BigDecimal getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(BigDecimal totalReceived) {
        this.totalReceived = totalReceived;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

