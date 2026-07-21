<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Báo cáo hệ thống</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
    <style>
        .report-section-title {
            font-size: 0.78rem;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            color: #64748b;
            font-weight: 700;
        }
        .kpi-subtitle {
            color: #64748b;
            font-size: 0.78rem;
        }
        .metric-row {
            display: flex;
            justify-content: space-between;
            gap: 1rem;
            padding: 0.85rem 0;
            border-bottom: 1px solid #eef2f7;
        }
        .metric-row:last-child {
            border-bottom: 0;
        }
        .bucket-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.55rem 0;
            border-bottom: 1px solid #eef2f7;
        }
        .bucket-item:last-child {
            border-bottom: 0;
        }
        .priority-score {
            min-width: 42px;
            height: 42px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border: 1px solid #e2e8f0;
            font-weight: 700;
            background: #f8fafc;
        }
    </style>
</head>
<body>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <a href="${pageContext.request.contextPath}/" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Bảng điều khiển</a>
            <a href="${pageContext.request.contextPath}/patient-manager" class="nav-link">Bệnh nhân</a>
            <a href="${pageContext.request.contextPath}/admin/reports" class="nav-link active">Báo cáo</a>
        </div>
    </div>
    <div class="d-flex align-items-center gap-3 text-muted">
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.adminUser ? sessionScope.adminUser.hoTen : 'Admin'}&background=0D8ABC&color=fff"
             alt="Admin" class="rounded-circle" width="28" height="28">
        <a href="${pageContext.request.contextPath}/admin/logout" class="text-danger ms-2" title="Đăng xuất" style="text-decoration: none;">
            <i class="fas fa-sign-out-alt fs-5"></i>
        </a>
    </div>
</nav>

<div class="app-container">
    <jsp:include page="/WEB-INF/views/admin/sidebar.jsp">
        <jsp:param name="activeMenu" value="reports" />
    </jsp:include>

    <main class="main-content d-flex flex-column">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">Báo cáo hệ thống</h2>
                <p class="text-muted small mb-0">Theo dõi hiệu quả kiểm soát bệnh và hoạt động lịch hẹn trong hệ thống.</p>
            </div>
            <form action="${pageContext.request.contextPath}/admin/reports" method="get" class="d-flex gap-2">
                <select name="periodDays" class="form-select form-select-sm shadow-none" style="width: 150px;">
                    <option value="7" ${selectedPeriodDays == 7 ? 'selected' : ''}>7 ngày gần nhất</option>
                    <option value="30" ${selectedPeriodDays == 30 ? 'selected' : ''}>30 ngày gần nhất</option>
                    <option value="90" ${selectedPeriodDays == 90 ? 'selected' : ''}>90 ngày gần nhất</option>
                </select>
                <button type="submit" class="btn btn-sm btn-primary px-3">Cập nhật</button>
            </form>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Tổng bệnh nhân</div>
                    <h3 class="stat-card-value mb-0">${reportStats.totalPatients}</h3>
                    <div class="kpi-subtitle">${reportStats.patientsWithRecentMeasurements} có đo đường huyết gần đây</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Tỷ lệ kiểm soát đường huyết</div>
                    <h3 class="stat-card-value text-success mb-0">${reportStats.glucoseControlRate}%</h3>
                    <div class="kpi-subtitle">Mức 70-180 mg/dL</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Cảnh báo cao/nguy hiểm</div>
                    <h3 class="stat-card-value text-danger mb-0">${reportStats.highAlerts}</h3>
                    <div class="kpi-subtitle">${reportStats.unreadDoctorAlerts} cảnh báo bác sĩ chưa đọc</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Tỷ lệ hoàn tất lịch hẹn</div>
                    <h3 class="stat-card-value text-primary mb-0">${reportStats.appointmentCompletionRate}%</h3>
                    <div class="kpi-subtitle">${reportStats.upcomingAppointments} lịch hẹn trong 7 ngày tới</div>
                </div>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="custom-card mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <div class="report-section-title">Disease Control Statistics</div>
                            <h5 class="h6 fw-bold mb-0">Kiểm soát bệnh tiểu đường</h5>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/high-risk-patients" class="btn btn-sm btn-outline-danger rounded-pill px-3">
                            <i class="fas fa-triangle-exclamation me-1"></i> Bệnh nhân nguy cơ cao
                        </a>
                    </div>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="metric-row">
                                <span class="text-muted">Đường huyết trung bình</span>
                                <strong><fmt:formatNumber value="${reportStats.averageGlucose}" maxFractionDigits="1"/> mg/dL</strong>
                            </div>
                            <div class="metric-row">
                                <span class="text-muted">Kiểm soát tốt</span>
                                <strong class="text-success">${reportStats.controlledGlucoseCount}</strong>
                            </div>
                            <div class="metric-row">
                                <span class="text-muted">Đường huyết cao</span>
                                <strong class="text-warning">${reportStats.highGlucoseCount}</strong>
                            </div>
                            <div class="metric-row">
                                <span class="text-muted">Ngưỡng nguy hiểm</span>
                                <strong class="text-danger">${reportStats.criticalGlucoseCount}</strong>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="metric-row">
                                <span class="text-muted">HbA1c trung bình</span>
                                <strong><fmt:formatNumber value="${reportStats.averageHba1c}" maxFractionDigits="1"/>%</strong>
                            </div>
                            <div class="metric-row">
                                <span class="text-muted">HbA1c dưới 7%</span>
                                <strong class="text-success">${reportStats.controlledHba1cCount}</strong>
                            </div>
                            <div class="metric-row">
                                <span class="text-muted">HbA1c từ 8% trở lên</span>
                                <strong class="text-danger">${reportStats.highHba1cCount}</strong>
                            </div>
                            <div class="metric-row">
                                <span class="text-muted">Huyết áp cao</span>
                                <strong class="text-warning">${reportStats.highBloodPressureCount}</strong>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="custom-card">
                    <div class="report-section-title mb-1">Distribution</div>
                    <h5 class="h6 fw-bold mb-3">Phân bổ chỉ số đường huyết</h5>
                    <c:forEach var="bucket" items="${reportStats.glucoseBuckets}">
                        <div class="bucket-item">
                            <span class="text-muted"><c:out value="${bucket.label}"/></span>
                            <strong>${bucket.value}</strong>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="custom-card mb-4">
                    <div class="report-section-title mb-1">Appointment & Telehealth Statistics</div>
                    <h5 class="h6 fw-bold mb-3">Lịch hẹn và khám từ xa</h5>
                    <div class="metric-row">
                        <span class="text-muted">Tổng lịch hẹn</span>
                        <strong>${reportStats.totalAppointments}</strong>
                    </div>
                    <div class="metric-row">
                        <span class="text-muted">Chờ khám</span>
                        <strong class="text-warning">${reportStats.pendingAppointments}</strong>
                    </div>
                    <div class="metric-row">
                        <span class="text-muted">Đã khám</span>
                        <strong class="text-success">${reportStats.completedAppointments}</strong>
                    </div>
                    <div class="metric-row">
                        <span class="text-muted">Đã hủy</span>
                        <strong class="text-danger">${reportStats.cancelledAppointments}</strong>
                    </div>
                    <div class="metric-row">
                        <span class="text-muted">Telehealth</span>
                        <strong class="text-primary">${reportStats.telehealthAppointments}</strong>
                    </div>
                </div>

                <div class="custom-card">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <div class="report-section-title">Priority</div>
                            <h5 class="h6 fw-bold mb-0">Bệnh nhân cần ưu tiên</h5>
                        </div>
                    </div>
                    <c:forEach var="p" items="${priorityPatients}">
                        <div class="d-flex align-items-center justify-content-between py-2 border-bottom">
                            <div class="pe-3">
                                <div class="fw-bold small"><c:out value="${p.patientName}"/></div>
                                <div class="text-muted" style="font-size:0.75rem;"><c:out value="${p.riskLabel}"/></div>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <span class="priority-score">${p.riskScore}</span>
                                <a href="${pageContext.request.contextPath}/patient-manager?action=view&id=${p.patientId}" class="btn btn-sm btn-light text-primary" title="Xem hồ sơ">
                                    <i class="fas fa-eye"></i>
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty priorityPatients}">
                        <div class="text-muted small py-3">Chưa có dữ liệu bệnh nhân ưu tiên.</div>
                    </c:if>
                </div>
            </div>
        </div>
    </main>
</div>

<div class="footer-bar">
    <div>
        <span class="fw-bold" style="color: var(--primary-blue);">HealthAlert</span>
        <span class="ms-2">© 2026 Hệ thống HealthAlert. Báo cáo dựa trên dữ liệu trong ${selectedPeriodDays} ngày gần nhất.</span>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>
</body>
</html>
