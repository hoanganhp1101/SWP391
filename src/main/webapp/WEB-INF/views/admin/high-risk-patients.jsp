<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Bệnh nhân nguy cơ cao</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
    <style>
        .risk-score {
            width: 54px;
            height: 54px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            font-weight: 700;
            background: #f8fafc;
            border: 1px solid #e2e8f0;
        }
        .reason-list {
            margin: 0;
            padding-left: 1rem;
        }
        .reason-list li {
            margin-bottom: 0.2rem;
        }
        .metric-pill {
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
            padding: 0.25rem 0.5rem;
            border-radius: 999px;
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            font-size: 0.75rem;
            color: #475569;
            white-space: nowrap;
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
            <a href="${pageContext.request.contextPath}/admin/high-risk-patients" class="nav-link active">Nguy cơ cao</a>
            <a href="${pageContext.request.contextPath}/admin/users" class="nav-link">Người dùng</a>
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
        <jsp:param name="activeMenu" value="high-risk-patients" />
    </jsp:include>

    <main class="main-content d-flex flex-column">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">Giám sát bệnh nhân nguy cơ cao</h2>
                <p class="text-muted small mb-0">Ưu tiên theo dõi bệnh nhân có chỉ số đường huyết, HbA1c, huyết áp hoặc cảnh báo bất thường.</p>
            </div>
            <a href="${pageContext.request.contextPath}/patient-manager" class="btn btn-outline-primary btn-sm rounded-pill px-3">
                <i class="fas fa-user-injured me-1"></i> Danh sách bệnh nhân
            </a>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Đang theo dõi</div>
                    <h3 class="stat-card-value mb-0">${totalMonitored}</h3>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Nguy kịch</div>
                    <h3 class="stat-card-value text-danger mb-0">${criticalCount}</h3>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Nguy cơ cao</div>
                    <h3 class="stat-card-value text-warning mb-0">${highCount}</h3>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Cần theo dõi</div>
                    <h3 class="stat-card-value text-info mb-0">${mediumCount}</h3>
                </div>
            </div>
        </div>

        <div class="custom-card flex-grow-1">
            <div class="d-flex justify-content-between align-items-center mb-4 gap-3">
                <h5 class="h6 mb-0 fw-bold text-dark">Danh sách ưu tiên</h5>
                <form action="${pageContext.request.contextPath}/admin/high-risk-patients" method="get" class="d-flex gap-2">
                    <select name="riskLevel" class="form-select form-select-sm shadow-none" style="width: 165px;">
                        <option value="">Tất cả mức nguy cơ</option>
                        <option value="critical" ${selectedRiskLevel == 'critical' ? 'selected' : ''}>Nguy kịch</option>
                        <option value="high" ${selectedRiskLevel == 'high' ? 'selected' : ''}>Nguy cơ cao</option>
                        <option value="medium" ${selectedRiskLevel == 'medium' ? 'selected' : ''}>Cần theo dõi</option>
                        <option value="low" ${selectedRiskLevel == 'low' ? 'selected' : ''}>Ổn định</option>
                    </select>
                    <div class="input-group input-group-sm" style="width: 260px;">
                        <span class="input-group-text bg-white border-end-0 text-muted"><i class="fas fa-search"></i></span>
                        <input type="text" name="keyword" value="${fn:escapeXml(searchKeyword)}" class="form-control border-start-0 ps-0 shadow-none" placeholder="Tên, email, số điện thoại...">
                    </div>
                    <button type="submit" class="btn btn-sm btn-primary px-3">Lọc</button>
                    <a href="${pageContext.request.contextPath}/admin/high-risk-patients" class="btn btn-sm btn-light border px-3">Reset</a>
                </form>
            </div>

            <div class="table-responsive">
                <table class="table custom-table align-middle mb-0">
                    <thead>
                    <tr>
                        <th>Bệnh nhân</th>
                        <th>Chỉ số mới nhất</th>
                        <th>Lý do cảnh báo</th>
                        <th class="text-center">Điểm</th>
                        <th class="text-end">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${patientList}">
                        <tr>
                            <td style="min-width: 230px;">
                                <div class="d-flex align-items-center gap-3">
                                    <img src="https://ui-avatars.com/api/?name=${p.patientName}&background=random&color=fff" class="rounded-circle" width="36" height="36" alt="${p.patientName}">
                                    <div>
                                        <div class="fw-bold text-dark" style="font-size:0.9rem;"><c:out value="${p.patientName}"/></div>
                                        <div class="text-muted" style="font-size:0.75rem;">Mã BN: <c:out value="${p.maBenhNhanDisplay}"/></div>
                                        <div class="text-muted" style="font-size:0.75rem;"><c:out value="${p.diabetesType}" default="Chưa rõ tuýp"/></div>
                                        <div class="text-muted" style="font-size:0.75rem;">
                                            <i class="fas fa-user-md me-1"></i><c:out value="${p.doctorName}" default="Chưa phân công"/>
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td style="min-width: 300px;">
                                <div class="d-flex flex-wrap gap-1">
                                    <span class="metric-pill"><i class="fas fa-tint text-danger"></i>
                                        <c:choose>
                                            <c:when test="${p.latestGlucose != null}"><fmt:formatNumber value="${p.latestGlucose}" maxFractionDigits="0"/> mg/dL</c:when>
                                            <c:otherwise>Chưa có đường huyết</c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span class="metric-pill"><i class="fas fa-vial text-primary"></i>
                                        <c:choose>
                                            <c:when test="${p.latestHba1c != null}">HbA1c <fmt:formatNumber value="${p.latestHba1c}" maxFractionDigits="1"/>%</c:when>
                                            <c:otherwise>Chưa có HbA1c</c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span class="metric-pill"><i class="fas fa-heartbeat text-danger"></i>
                                        <c:choose>
                                            <c:when test="${p.systolicBloodPressure != null && p.diastolicBloodPressure != null}">${p.systolicBloodPressure}/${p.diastolicBloodPressure}</c:when>
                                            <c:otherwise>Chưa có HA</c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span class="metric-pill"><i class="fas fa-weight text-secondary"></i>
                                        <c:choose>
                                            <c:when test="${p.bmi != null}">BMI <fmt:formatNumber value="${p.bmi}" maxFractionDigits="1"/></c:when>
                                            <c:otherwise>Chưa có BMI</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="text-muted mt-2" style="font-size:0.75rem;">
                                    <i class="far fa-clock me-1"></i>
                                    <c:choose>
                                        <c:when test="${p.lastMeasurementTime != null}">
                                            Cập nhật: <fmt:formatDate value="${p.lastMeasurementTime}" pattern="dd/MM/yyyy HH:mm"/>
                                        </c:when>
                                        <c:otherwise>Chưa có lần đo nào</c:otherwise>
                                    </c:choose>
                                </div>
                            </td>
                            <td style="min-width: 310px;">
                                <span class="badge ${p.riskBadgeClass} mb-2">${p.riskLabel}</span>
                                <ul class="reason-list small text-muted">
                                    <c:forEach var="reason" items="${p.riskReasons}">
                                        <li><c:out value="${reason}"/></li>
                                    </c:forEach>
                                </ul>
                            </td>
                            <td class="text-center">
                                <div class="risk-score">${p.riskScore}</div>
                            </td>
                            <td class="text-end" style="min-width: 130px;">
                                <a href="${pageContext.request.contextPath}/patient-manager?action=view&id=${p.patientId}" class="btn btn-sm btn-light text-primary" title="Xem hồ sơ">
                                    <i class="fas fa-eye"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/ai-report?patientId=${p.patientId}" class="btn btn-sm btn-light text-success" title="Tạo báo cáo AI">
                                    <i class="fas fa-file-medical-alt"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty patientList}">
                        <tr>
                            <td colspan="5" class="text-center text-muted py-5">
                                <i class="fas fa-shield-heart d-block mb-2 fs-4"></i>
                                Không có bệnh nhân phù hợp với bộ lọc hiện tại.
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<div class="footer-bar">
    <div>
        <span class="fw-bold" style="color: var(--primary-blue);">HealthAlert</span>
        <span class="ms-2">© 2026 Hệ thống HealthAlert. Dữ liệu nguy cơ được tính từ chỉ số bệnh nhân mới nhất.</span>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>
</body>
</html>
