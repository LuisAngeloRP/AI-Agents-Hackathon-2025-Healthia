package com.healthia.functions.entities;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "user_health_metrics")
public class UserHealthMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @Column(name = "user_id", nullable = false)
    private String userId; // Foreign key to your UserDataEntity or just the user identifier

    @Column(name = "metric_type", nullable = false)
    private String metricType; // e.g., "HEART_RATE", "STEPS", "SLEEP_DURATION", "CALORIES_BURNED"

    @Column(name = "data_source", nullable = false)
    private String dataSource; // e.g., "GoogleFit", "AppleHealthKit", "FitbitAPI"
    
    // This field can store a summary or the latest value if the metric is a single point in time
    // For time series data, UserTimeSeriesDataPointEntity will be used.
    @Column(name = "latest_value_numeric")
    private Double latestValueNumeric;

    @Column(name = "latest_value_text")
    private String latestValueText; // For metrics that are not purely numeric

    @Column(name = "unit")
    private String unit; // e.g., "bpm", "count", "minutes", "kcal"

    @Column(name = "last_updated_from_source", nullable = false)
    private OffsetDateTime lastUpdatedFromSource; // Timestamp of when the data was fetched or reported by the source

    @Column(name = "recorded_in_db_at", nullable = false)
    private OffsetDateTime recordedInDbAt;

    // Relationship to time series data points for this metric
    @OneToMany(mappedBy = "healthMetric", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserTimeSeriesDataPointEntity> timeSeriesDataPoints = new ArrayList<>();

    public UserHealthMetricEntity() {
    }

    // Getters and Setters
    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Double getLatestValueNumeric() {
        return latestValueNumeric;
    }

    public void setLatestValueNumeric(Double latestValueNumeric) {
        this.latestValueNumeric = latestValueNumeric;
    }

    public String getLatestValueText() {
        return latestValueText;
    }

    public void setLatestValueText(String latestValueText) {
        this.latestValueText = latestValueText;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public OffsetDateTime getLastUpdatedFromSource() {
        return lastUpdatedFromSource;
    }

    public void setLastUpdatedFromSource(OffsetDateTime lastUpdatedFromSource) {
        this.lastUpdatedFromSource = lastUpdatedFromSource;
    }

    public OffsetDateTime getRecordedInDbAt() {
        return recordedInDbAt;
    }

    public void setRecordedInDbAt(OffsetDateTime recordedInDbAt) {
        this.recordedInDbAt = recordedInDbAt;
    }

    public List<UserTimeSeriesDataPointEntity> getTimeSeriesDataPoints() {
        return timeSeriesDataPoints;
    }

    public void setTimeSeriesDataPoints(List<UserTimeSeriesDataPointEntity> timeSeriesDataPoints) {
        this.timeSeriesDataPoints = timeSeriesDataPoints;
    }
    
    public void addDataPoint(UserTimeSeriesDataPointEntity dataPoint) {
        timeSeriesDataPoints.add(dataPoint);
        dataPoint.setHealthMetric(this);
    }

    @PrePersist
    protected void onCreate() {
        recordedInDbAt = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserHealthMetricEntity that = (UserHealthMetricEntity) o;
        return Objects.equals(metricId, that.metricId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricId);
    }
} 