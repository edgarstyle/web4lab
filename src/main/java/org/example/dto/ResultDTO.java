package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResultDTO {
    private Long id;
    private String x;  // Храним как строку для точной сериализации в JSON
    private String y;  // Храним как строку для точной сериализации в JSON
    private String r;  // Храним как строку для точной сериализации в JSON
    private Boolean hit;
    private LocalDateTime timestamp;
    private Long executionTime;

    public ResultDTO() {
    }

    public ResultDTO(Long id, BigDecimal x, BigDecimal y, BigDecimal r, Boolean hit, LocalDateTime timestamp, Long executionTime) {
        this.id = id;
        this.x = x != null ? x.toPlainString() : null;
        this.y = y != null ? y.toPlainString() : null;
        this.r = r != null ? r.toPlainString() : null;
        this.hit = hit;
        this.timestamp = timestamp;
        this.executionTime = executionTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Возвращаем строку для точной сериализации в JSON
    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    // Возвращаем строку для точной сериализации в JSON
    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    // Возвращаем строку для точной сериализации в JSON
    public String getR() {
        return r;
    }

    public void setR(String r) {
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


