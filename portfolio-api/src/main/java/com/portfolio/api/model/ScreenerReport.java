package com.portfolio.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "screener_reports")
public class ScreenerReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType;

    @Column(nullable = false, length = 100)
    private String target;

    @Column(name = "report_data", nullable = false, columnDefinition = "TEXT")
    private String reportData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getReportData() { return reportData; }
    public void setReportData(String reportData) { this.reportData = reportData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
