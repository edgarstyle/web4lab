package org.example.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "results")
@NamedQueries({
    @NamedQuery(name = "Result.findAll", query = "SELECT r FROM Result r ORDER BY r.timestamp DESC")
})
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, precision = 50, scale = 30)
    private BigDecimal x;

    @Column(nullable = false, precision = 50, scale = 30)
    private BigDecimal y;

    @Column(nullable = false, precision = 50, scale = 30)
    private BigDecimal r;

    @Column(nullable = false)
    private Boolean hit;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private Long executionTime;

    public Result() {
    }

    public Result(BigDecimal x, BigDecimal y, BigDecimal r, Boolean hit) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getX() {
        return x;
    }

    public void setX(BigDecimal x) {
        this.x = x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }

    public BigDecimal getR() {
        return r;
    }

    public void setR(BigDecimal r) {
        this.r = r;
    }

    public Boolean getHit() {
        return hit;
    }

    public void setHit(Boolean hit) {
        this.hit = hit;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }
}

