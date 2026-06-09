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
        <i class="fas fa-cog"></i>
        <img src="https://ui-avatars.com/api/?name=Admin&background=0D8ABC&color=fff" alt="User" class="rounded-circle" width="28" height="28">
    </div>
</nav>

<div class="app-container">

    <aside class="sidebar">
        <div class="user-profile-sm">
            <img src="https://ui-avatars.com/api/?name=Dr+Smith&background=1e293b&color=fff" alt="Dr. Smith">
            <div>
                <div class="name">Dr. Smith</div>
                <div class="role">Chief Surgeon</div>
            </div>
        </div>

        <div class="px-3 pb-3">
            <button class="btn btn-primary w-100 rounded-1" style="background-color: var(--primary-blue); border:none;">
                <i class="fas fa-plus me-1"></i> New Patient
            </button>
        </div>

        <ul class="sidebar-menu">
            <li><a href="${pageContext.request.contextPath}/dashboard" class="active"><i class="fas fa-th-large"></i> Overview</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/users"><i class="fas fa-users-cog"></i> User Management</a></li>
            <li><a href="${pageContext.request.contextPath}/patient-manager"><i class="fas fa-user-injured"></i> Patient List</a></li>
        </ul>

        <div class="sidebar-footer">
            <ul class="sidebar-menu p-0 m-0">
                <li><a href="#"><i class="far fa-question-circle"></i> Support</a></li>
                <li><a href="#"><i class="fas fa-sign-out-alt"></i> Sign Out</a></li>
            </ul>
        </div>
    </aside>

    <main class="main-content d-flex flex-column">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">Patient Management</h2>
                <p class="text-muted small mb-0">Manage and monitor patient medical records.</p>
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
                    <h3 class="stat-card-value"><c:out value="${totalUsers != null ? totalUsers : '1,284'}"/></h3>
                </div>
            </div>
            <div class="col-md-4">
                <div class="custom-card position-relative">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div class="icon-box text-info" style="background: #e0f2fe;"><i class="fas fa-user-md"></i></div>
                        <span class="text-info small fw-bold">+4 <i class="fas fa-arrow-up"></i></span>
                    </div>
                    <div class="stat-card-title">Active Staff</div>
                    <h3 class="stat-card-value"><c:out value="${activeStaff != null ? activeStaff : '156'}"/></h3>
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
                    <h3 class="stat-card-value position-relative z-1"><c:out value="${systemErrors != null ? systemErrors : '24'}"/></h3>
                </div>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <div class="col-md-8">
                <div class="custom-card">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h5 class="h6 mb-0 fw-bold text-dark">Patient Records</h5>
                        <div class="input-group input-group-sm" style="width: 250px;">
                            <span class="input-group-text bg-white border-end-0 text-muted"><i class="fas fa-search"></i></span>
                            <input type="text" class="form-control border-start-0 ps-0 shadow-none" placeholder="Search by name, ID or doctor">
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table custom-table mb-0">
                            <thead>
                            <tr>
                                <th>Patient Info</th>
                                <th>Contact Details</th>
                                <th>Diabetes Type</th>
                                <th>Assigned Doctor</th>
                                <th class="text-end" style="min-width: 120px;">Actions</th>
                            </tr>
                            </thead>
                            <tbody>

                            <c:forEach var="p" items="${patientList}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <img src="https://ui-avatars.com/api/?name=${p.tenBenhNhan}&background=random&color=fff" class="rounded-circle" width="32" height="32" alt="${p.tenBenhNhan}">
                                            <div>
                                                <div class="fw-bold" style="font-size:0.85rem;"><c:out value="${p.tenBenhNhan}"/></div>
                                                <div class="text-muted" style="font-size:0.7rem;">ID: <c:out value="${p.id}"/></div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-muted small">
                                        <div style="font-size: 0.75rem;"><i class="fas fa-phone-alt me-1"></i> <c:out value="${p.soDienThoai}"/></div>
                                        <div style="font-size: 0.75rem;"><i class="fas fa-envelope me-1"></i> <c:out value="${p.email}"/></div>
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
                                    <td class="text-muted small">
                                        <c:choose>
                                            <c:when test="${not empty p.tenBacSi}">
                                                <div class="fw-medium text-dark" style="font-size:0.8rem;">Dr. <c:out value="${p.tenBacSi}"/></div>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted fst-italic">Unassigned</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/RecordController?action=viewByPatient&patientId=${p.id}" class="btn btn-sm btn-light text-success me-1" title="View Medical Records">
                                            <i class="fas fa-clipboard-list"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/patient-manager?action=view&id=${p.id}" class="btn btn-sm btn-light text-primary me-1" title="View Profile">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/patient-manager?action=edit&id=${p.id}" class="btn btn-sm btn-light text-secondary" title="Edit Patient Info">
                                            <i class="fas fa-edit"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty patientList}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="avatar-sm" style="background:#fce7f3; color:#be185d;">AW</div>
                                            <div>
                                                <div class="fw-bold" style="font-size:0.85rem;">Alice Williams (Demo)</div>
                                                <div class="text-muted" style="font-size:0.7rem;">ID: PT-10294</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-muted small">
                                        <div style="font-size: 0.75rem;"><i class="fas fa-phone-alt me-1"></i> 0987-654-321</div>
                                        <div style="font-size: 0.75rem;"><i class="fas fa-envelope me-1"></i> a.williams@email.com</div>
                                    </td>
                                    <td><span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle rounded-pill" style="font-size: 0.7rem;">Type 1</span></td>
                                    <td class="text-muted small">
                                        <div class="fw-medium text-dark" style="font-size:0.8rem;">Dr. Smith</div>
                                    </td>
                                    <td class="text-end">
                                        <a href="#" class="btn btn-sm btn-light text-success me-1" title="Medical Records"><i class="fas fa-clipboard-list"></i></a>
                                        <button class="btn btn-sm btn-light text-primary me-1"><i class="fas fa-eye"></i></button>
                                        <button class="btn btn-sm btn-light text-secondary"><i class="fas fa-edit"></i></button>
                                    </td>
                                </tr>
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
                            <span>CPU Usage</span>
                            <span>42%</span>
                        </div>
                        <div class="progress progress-thin">
                            <div class="progress-bar" style="width: 42%; background-color: var(--primary-blue);"></div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <div class="d-flex justify-content-between small text-muted mb-1">
                            <span>Memory Usage</span>
                            <span>68%</span>
                        </div>
                        <div class="progress progress-thin">
                            <div class="progress-bar" style="width: 68%; background-color: var(--primary-blue);"></div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <div class="d-flex justify-content-between small text-muted mb-1">
                            <span>Processing Queue</span>
                            <span class="text-success">Low</span>
                        </div>
                        <div class="progress progress-thin">
                            <div class="progress-bar bg-success" style="width: 15%;"></div>
                        </div>
                    </div>

                    <h6 class="small fw-bold text-muted text-uppercase mb-3" style="font-size:0.7rem;">Recent Activity</h6>
                    <ul class="list-unstyled mb-0 small">
                        <li class="mb-2">
                            <div class="fw-medium text-dark" style="font-size: 0.8rem;">Record Indexing Completed</div>
                            <div class="text-muted" style="font-size: 0.75rem;">4,203 files processed</div>
                        </li>
                        <li>
                            <div class="fw-medium text-dark" style="font-size: 0.8rem;">API Update Deployed</div>
                            <div class="text-muted" style="font-size: 0.75rem;">v2.4.1 successful</div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <div class="row g-4 flex-grow-1">
            <div class="col-md-8">
                <div class="custom-card">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h5 class="h6 mb-0 fw-bold text-dark">Alert Accuracy Trends</h5>
                            <p class="text-muted small mb-0" style="font-size: 0.75rem;">Precision vs Recall performance over 30 days</p>
                        </div>
                        <button class="btn btn-sm btn-outline-secondary bg-white text-muted" style="font-size: 0.75rem;">Last 30 Days <i class="fas fa-chevron-down ms-1"></i></button>
                    </div>
                    <div class="bar-chart-mockup mt-4">
                        <div class="bar" style="height: 40%;"></div>
                        <div class="bar" style="height: 70%;"></div>
                        <div class="bar" style="height: 30%;"></div>
                        <div class="bar" style="height: 85%;"></div>
                        <div class="bar" style="height: 45%;"></div>
                        <div class="bar" style="height: 60%;"></div>
                        <div class="bar" style="height: 90%;"></div>
                        <div class="bar" style="height: 65%;"></div>
                        <div class="bar" style="height: 50%;"></div>
                    </div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="custom-card" style="background-color: #f1f5f9; border:none;">
                    <h5 class="h6 mb-4 fw-bold text-dark">Alert Thresholds</h5>

                    <div class="mb-4">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="fw-bold text-dark" style="font-size: 0.85rem;">Vital Anomaly Sensitivity</span>
                            <span class="badge bg-primary rounded-1">High</span>
                        </div>
                        <input type="range" class="form-range" min="0" max="100" value="80" id="sensitivityRange">
                        <div class="text-muted mt-1" style="font-size: 0.7rem;">Lowering this reduces noise but might miss critical shifts.</div>
                    </div>

                    <div class="mb-4">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="fw-bold text-dark" style="font-size: 0.85rem;">Latency Timeout (ms)</span>
                            <span class="badge bg-secondary rounded-1" id="latencyDisplay">250ms</span>
                        </div>
                        <input type="range" class="form-range" min="0" max="500" value="250" id="latencyRange">
                    </div>

                    <div class="bg-white p-3 rounded d-flex justify-content-between align-items-center shadow-sm">
                        <div>
                            <div class="fw-bold text-dark" style="font-size: 0.85rem;">Auto-Escalation</div>
                            <div class="text-muted" style="font-size: 0.7rem;">Notify Chief if unacknowledged</div>
                        </div>
                        <div class="form-check form-switch m-0 p-0">
                            <input class="form-check-input ms-0" type="checkbox" role="switch" checked style="width: 36px; height: 18px; margin-top:0;">
                        </div>
                    </div>
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
    <div class="d-flex gap-3">
        <a href="#" class="text-muted text-decoration-none">Privacy Policy</a>
        <a href="#" class="text-muted text-decoration-none">Terms of Service</a>
        <a href="#" class="text-muted text-decoration-none">HIPAA Compliance</a>
        <a href="#" class="text-muted text-decoration-none">Contact Support</a>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>
</body>
</html>