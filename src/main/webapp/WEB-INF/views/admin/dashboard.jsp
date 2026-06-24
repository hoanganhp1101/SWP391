<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | System Overview</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <a href="${pageContext.request.contextPath}/" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link active">Dashboard</a>
            <a href="${pageContext.request.contextPath}/patient-manager" class="nav-link">Patients</a>
            <a href="${pageContext.request.contextPath}/RecordController" class="nav-link">Records</a>
            <a href="#" class="nav-link">Reports</a>
        </div>
    </div>
    <div class="d-flex align-items-center gap-3 text-muted">
        <i class="far fa-bell"></i>
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.loginUser ? sessionScope.loginUser.hoTen : 'Admin'}&background=0D8ABC&color=fff" alt="User" class="rounded-circle" width="28" height="28">
    </div>
</nav>

<div class="app-container">

    <jsp:include page="/WEB-INF/views/admin/sidebar.jsp">
        <jsp:param name="activeMenu" value="dashboard" />
    </jsp:include>

    <main class="main-content d-flex flex-column">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">System Overview</h2>
                <p class="text-muted small mb-0">Monitor platform health and critical patient alerts.</p>
            </div>
            <div class="d-flex gap-2">
                <button class="btn btn-outline-secondary btn-sm bg-white fw-medium px-3 rounded-pill">Export Data</button>
                <button class="btn btn-primary btn-sm fw-medium px-3 rounded-pill" style="background-color: var(--primary-blue); border:none;">Refresh</button>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <div class="col-md-4">
                <div class="custom-card position-relative">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div class="icon-box"><i class="fas fa-user-injured"></i></div>
                        <span class="text-success small fw-bold">+12% <i class="fas fa-arrow-up"></i></span>
                    </div>
                    <div class="stat-card-title">Total Patients</div>
                    <h3 class="stat-card-value"><c:out value="${totalPatients != null ? totalPatients : '0'}"/></h3>
                </div>
            </div>
            <div class="col-md-4">
                <div class="custom-card position-relative">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div class="icon-box text-info" style="background: #e0f2fe;"><i class="fas fa-user-md"></i></div>
                    </div>
                    <div class="stat-card-title">Active Staff</div>
                    <h3 class="stat-card-value"><c:out value="${activeStaff != null ? activeStaff : '0'}"/></h3>
                </div>
            </div>
            <div class="col-md-4">
                <div class="custom-card alert-card">
                    <i class="fas fa-exclamation-triangle alert-card-bg-icon"></i>
                    <div class="d-flex justify-content-between align-items-start mb-3 position-relative z-1">
                        <div class="icon-box" style="background: rgba(255,255,255,0.2); color:white;"><i class="fas fa-exclamation"></i></div>
                        <span class="badge rounded-pill" style="background: rgba(0,0,0,0.3); font-size:0.7rem; font-weight:500;">Urgent Attention</span>
                    </div>
                    <div class="stat-card-title">Critical Patients</div>
                    <h3 class="stat-card-value position-relative z-1"><c:out value="${criticalAlerts != null ? criticalAlerts : '0'}"/></h3>
                </div>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <div class="col-md-8">
                <div class="custom-card">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h5 class="h6 mb-0 fw-bold text-dark">Recent Patient Records</h5>
                        <a href="${pageContext.request.contextPath}/patient-manager" class="btn btn-sm btn-link text-decoration-none">View All</a>
                    </div>

                    <div class="table-responsive">
                        <table class="table custom-table mb-0">
                            <thead>
                            <tr>
                                <th>Patient Info</th>
                                <th>Contact Details</th>
                                <th>Diabetes Type</th>
                                <th class="text-end" style="min-width: 120px;">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="p" items="${patientList}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <img src="https://ui-avatars.com/api/?name=${p.tenBenhNhan}&background=random&color=fff" class="rounded-circle" width="32" height="32">
                                            <div>
                                                <div class="fw-bold" style="font-size:0.85rem;"><c:out value="${p.tenBenhNhan}"/></div>
                                                <div class="text-muted" style="font-size:0.7rem;">ID: <c:out value="${p.id}"/></div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-muted small">
                                        <div style="font-size: 0.75rem;"><i class="fas fa-phone-alt me-1"></i> <c:out value="${p.soDienThoai}"/></div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.loaiTieuDuong == 'Type 1'}">
                                                <span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle rounded-pill" style="font-size: 0.7rem;">Type 1</span>
                                            </c:when>
                                            <c:when test="${p.loaiTieuDuong == 'Type 2'}">
                                                <span class="badge bg-warning bg-opacity-10 text-warning-emphasis border border-warning-subtle rounded-pill" style="font-size: 0.7rem;">Type 2</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-info bg-opacity-10 text-info-emphasis border border-info-subtle rounded-pill" style="font-size: 0.7rem;"><c:out value="${p.loaiTieuDuong}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/patient-manager?action=view&id=${p.id}" class="btn btn-sm btn-light text-primary" title="View Profile">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty patientList}">
                                <tr><td colspan="4" class="text-center text-muted">No recent records found.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="custom-card">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h5 class="h6 mb-0 fw-bold text-dark">System Health</h5>
                        <span class="text-success small fw-medium"><i class="fas fa-circle" style="font-size:8px;"></i> Stable</span>
                    </div>

                    <div class="mb-3">
                        <div class="d-flex justify-content-between small text-muted mb-1">
                            <span>Database Connection</span>
                            <span class="text-success">Good</span>
                        </div>
                        <div class="progress progress-thin">
                            <div class="progress-bar bg-success" style="width: 100%;"></div>
                        </div>
                    </div>

                    <h6 class="small fw-bold text-muted text-uppercase mt-4 mb-3" style="font-size:0.7rem;">Recent Activity</h6>
                    <ul class="list-unstyled mb-0 small">
                        <li class="mb-2">
                            <div class="fw-medium text-dark" style="font-size: 0.8rem;">New Patient Registered</div>
                            <div class="text-muted" style="font-size: 0.75rem;">Just now</div>
                        </li>
                        <li>
                            <div class="fw-medium text-dark" style="font-size: 0.8rem;">Daily Backup Completed</div>
                            <div class="text-muted" style="font-size: 0.75rem;">2 hours ago</div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

    </main>
</div>

<div class="footer-bar">
    <div>
        <span class="fw-bold" style="color: var(--primary-blue);">HealthAlert</span>
        <span class="ms-2">© 2026 HealthAlert Systems. All rights reserved. Confidential Medical Data.</span>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>
</body>
</html>