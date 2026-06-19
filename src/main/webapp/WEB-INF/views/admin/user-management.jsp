<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | User Management</title>
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
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a>
            <a href="${pageContext.request.contextPath}/patient-manager" class="nav-link">Patients</a>
            <a href="${pageContext.request.contextPath}/admin/users" class="nav-link active">Users</a>
            <a href="#" class="nav-link">Reports</a>
        </div>
    </div>
    <div class="d-flex align-items-center gap-3 text-muted">
        <i class="far fa-bell"></i>
        <i class="fas fa-cog"></i>
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.loginUser ? sessionScope.loginUser.hoTen : 'Admin'}&background=0D8ABC&color=fff" alt="User" class="rounded-circle" width="28" height="28">
    </div>
</nav>

<div class="app-container">

    <jsp:include page="/WEB-INF/views/admin/sidebar.jsp">
        <jsp:param name="activeMenu" value="users" />
    </jsp:include>

    <main class="main-content d-flex flex-column">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">User Management</h2>
                <p class="text-muted small mb-0">Manage system access, roles, and staff accounts.</p>
            </div>
            <div class="d-flex gap-2">
                <button class="btn btn-primary btn-sm fw-medium px-3 rounded-pill" style="background-color: var(--primary-blue); border:none;" data-bs-toggle="modal" data-bs-target="#addUserModal">
                    <i class="fas fa-plus me-1"></i> Add New User
                </button>
            </div>
        </div>

        <div class="custom-card flex-grow-1">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h5 class="h6 mb-0 fw-bold text-dark">System Users</h5>

                <div class="d-flex gap-2">
                    <select class="form-select form-select-sm shadow-none border-light-subtle text-muted" style="width: 150px;">
                        <option value="all">All Roles</option>
                        <option value="bac_si">Doctor</option>
                        <option value="y_ta">Nurse</option>
                        <option value="quan_tri_vien">Admin</option>
                    </select>
                    <div class="input-group input-group-sm" style="width: 250px;">
                        <span class="input-group-text bg-white border-end-0 text-muted"><i class="fas fa-search"></i></span>
                        <input type="text" class="form-control border-start-0 ps-0 shadow-none" placeholder="Search users...">
                    </div>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table custom-table mb-0">
                    <thead>
                    <tr>
                        <th>User Info</th>
                        <th>Contact</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th class="text-end" style="min-width: 100px;">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${userList}">
                        <tr>
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <img src="https://ui-avatars.com/api/?name=${u.hoTen}&background=random&color=fff" class="rounded-circle" width="32" height="32" alt="${u.hoTen}">
                                    <div>
                                        <div class="fw-bold text-dark" style="font-size:0.85rem;"><c:out value="${u.hoTen}"/></div>
                                        <div class="text-muted" style="font-size:0.7rem;">ID: <c:out value="${u.id}"/></div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-muted small">
                                <div style="font-size: 0.75rem;"><i class="fas fa-envelope me-1"></i> <c:out value="${u.email}"/></div>
                                <c:if test="${not empty u.soDienThoai}">
                                    <div style="font-size: 0.75rem;"><i class="fas fa-phone-alt me-1"></i> <c:out value="${u.soDienThoai}"/></div>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.vaiTro == 'quan_tri_vien'}">
                                        <span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle rounded-pill px-2" style="font-size: 0.7rem;">Admin</span>
                                    </c:when>
                                    <c:when test="${u.vaiTro == 'bac_si'}">
                                        <span class="badge bg-primary bg-opacity-10 text-primary border border-primary-subtle rounded-pill px-2" style="font-size: 0.7rem;">Doctor</span>
                                    </c:when>
                                    <c:when test="${u.vaiTro == 'y_ta'}">
                                        <span class="badge bg-info bg-opacity-10 text-info border border-info-subtle rounded-pill px-2" style="font-size: 0.7rem;">Nurse</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary bg-opacity-10 text-secondary border border-secondary-subtle rounded-pill px-2" style="font-size: 0.7rem;">Patient</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.kichHoat == 1}">
                                        <span class="text-success small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-danger small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Locked</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <a href="${pageContext.request.contextPath}/admin/users?action=edit&id=${u.id}" class="btn btn-sm btn-light text-primary me-1" title="Edit User">
                                    <i class="fas fa-edit"></i>
                                </a>
                                <c:choose>
                                    <c:when test="${u.kichHoat == 1}">
                                        <a href="${pageContext.request.contextPath}/admin/users?action=toggleStatus&id=${u.id}&status=0" class="btn btn-sm btn-light text-danger" title="Lock User" onclick="return confirm('Are you sure you want to lock this user?');">
                                            <i class="fas fa-lock"></i>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/admin/users?action=toggleStatus&id=${u.id}&status=1" class="btn btn-sm btn-light text-success" title="Unlock User">
                                            <i class="fas fa-unlock"></i>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty userList}">
                        <tr>
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <div class="avatar-sm" style="background:#e0f2fe; color:#0284c7;">AD</div>
                                    <div>
                                        <div class="fw-bold text-dark" style="font-size:0.85rem;">System Admin (Demo)</div>
                                        <div class="text-muted" style="font-size:0.7rem;">ID: USR-0001</div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-muted small">
                                <div style="font-size: 0.75rem;"><i class="fas fa-envelope me-1"></i> admin@diabcare.vn</div>
                                <div style="font-size: 0.75rem;"><i class="fas fa-phone-alt me-1"></i> 0934-567-890</div>
                            </td>
                            <td><span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle rounded-pill px-2" style="font-size: 0.7rem;">Admin</span></td>
                            <td><span class="text-success small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Active</span></td>
                            <td class="text-end">
                                <button class="btn btn-sm btn-light text-primary me-1"><i class="fas fa-edit"></i></button>
                                <button class="btn btn-sm btn-light text-danger"><i class="fas fa-lock"></i></button>
                            </td>
                        </tr>
                    </c:if>

                    </tbody>
                </table>
            </div>

            <div class="d-flex justify-content-between align-items-center mt-4">
                <span class="text-muted small">Showing 1 to 10 of 45 entries</span>
                <nav>
                    <ul class="pagination pagination-sm mb-0">
                        <li class="page-item disabled"><a class="page-link" href="#">Previous</a></li>
                        <li class="page-item active"><a class="page-link" href="#">1</a></li>
                        <li class="page-item"><a class="page-link" href="#">2</a></li>
                        <li class="page-item"><a class="page-link" href="#">3</a></li>
                        <li class="page-item"><a class="page-link" href="#">Next</a></li>
                    </ul>
                </nav>
            </div>
        </div>

    </main>
</div>

<div class="footer-bar mt-auto">
    <div>
        <span class="fw-bold" style="color: var(--primary-blue);">HealthAlert</span>
        <span class="ms-2">© 2026 HealthAlert Systems. All rights reserved.</span>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>
</body>
</html>