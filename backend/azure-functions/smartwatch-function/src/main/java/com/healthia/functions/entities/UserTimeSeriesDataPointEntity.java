package com.healthia.functions.entities;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_time_series_data_points")
public class UserTimeSeriesDataPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id", nullable = false)
    private UserHealthMetricEntity healthMetric;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp; // The exact time of the data point

    @Column(name = "value_numeric")
    private Double valueNumeric;

    @Column(name = "value_text")
    private String valueText; // For non-numeric time series data, if any

    // Potentially add start_time and end_time if the data point represents a duration/interval
    // @Column(name = "start_time")
    // private OffsetDateTime startTime;
    // @Column(name = "end_time")
    // private OffsetDateTime endTime;

    public UserTimeSeriesDataPointEntity() {
    }

    // Getters and Setters
    public Long getPointId() {
        return pointId;
    }

    public void setPointId(Long pointId) {
        this.pointId = pointId;
    }

    public UserHealthMetricEntity getHealthMetric() {
        return healthMetric;
    }

    public void setHealthMetric(UserHealthMetricEntity healthMetric) {
        this.healthMetric = healthMetric;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(Double valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTimeSeriesDataPointEntity that = (UserTimeSeriesDataPointEntity) o;
        return Objects.equals(pointId, that.pointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointId);
    }
} 