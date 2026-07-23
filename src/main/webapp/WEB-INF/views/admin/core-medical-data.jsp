<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Dữ liệu y khoa lõi</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
    <style>
        .data-action {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 1rem;
            padding: 1rem 0;
            border-bottom: 1px solid #eef2f7;
        }
        .data-action:last-child {
            border-bottom: 0;
        }
        .data-icon {
            width: 42px;
            height: 42px;
            border-radius: 10px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: #eef6ff;
            color: var(--primary-blue, #0d6efd);
            flex-shrink: 0;
        }
    </style>
</head>
<body>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <a href="${pageContext.request.contextPath}/" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Bảng điều khiển</a>
            <a href="${pageContext.request.contextPath}/admin/core-medical-data" class="nav-link active">Dữ liệu y khoa</a>
            <a href="${pageContext.request.contextPath}/admin/reports" class="nav-link">Báo cáo</a>
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
        <jsp:param name="activeMenu" value="core-medical-data" />
    </jsp:include>

    <main class="main-content d-flex flex-column">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">Quản lý dữ liệu y khoa lõi</h2>
                <p class="text-muted small mb-0">Trung tâm quản trị dữ liệu dùng chung cho kê đơn, thực đơn, giáo dục và theo dõi bệnh tiểu đường.</p>
            </div>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Thuốc</div>
                    <h3 class="stat-card-value mb-0">${totalMedications}</h3>
                    <div class="text-muted small">${activeMedications} đang dùng</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Insulin</div>
                    <h3 class="stat-card-value text-primary mb-0">${insulinCount}</h3>
                    <div class="text-muted small">Trong danh mục thuốc</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Thực phẩm</div>
                    <h3 class="stat-card-value text-success mb-0">${totalFoods}</h3>
                    <div class="text-muted small">${highGiFoods} thực phẩm GI cao</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="custom-card">
                    <div class="stat-card-title">Nội dung giáo dục</div>
                    <h3 class="stat-card-value text-warning mb-0">${educationContents}</h3>
                    <div class="text-muted small">${activeEducationContents} đang hiển thị</div>
                </div>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-7">
                <div class="custom-card">
                    <h5 class="h6 fw-bold mb-3">Khu vực quản trị dữ liệu</h5>
                    <div class="data-action">
                        <div class="d-flex align-items-center gap-3">
                            <div class="data-icon"><i class="fas fa-pills"></i></div>
                            <div>
                                <div class="fw-bold">Manage Medication & Insulin</div>
                                <div class="text-muted small">Quản lý thuốc uống, thuốc tiêm, insulin, hoạt chất và hướng dẫn dùng.</div>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/master-medications" class="btn btn-sm btn-primary px-3">Mở</a>
                    </div>
                    <div class="data-action">
                        <div class="d-flex align-items-center gap-3">
                            <div class="data-icon"><i class="fas fa-apple-alt"></i></div>
                            <div>
                                <div class="fw-bold">Manage Food Database</div>
                                <div class="text-muted small">Quản lý khẩu phần, carbohydrate, calo và chỉ số đường huyết GI.</div>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/master-foods" class="btn btn-sm btn-primary px-3">Mở</a>
                    </div>
                    <div class="data-action">
                        <div class="d-flex align-items-center gap-3">
                            <div class="data-icon"><i class="fas fa-book-medical"></i></div>
                            <div>
                                <div class="fw-bold">Manage Educational Content</div>
                                <div class="text-muted small">Quản lý kiến thức dinh dưỡng, vận động, thuốc và tự chăm sóc.</div>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin/educational-content" class="btn btn-sm btn-primary px-3">Mở</a>
                    </div>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="custom-card">
                    <h5 class="h6 fw-bold mb-3">Trạng thái dữ liệu</h5>
                    <div class="mb-3">
                        <div class="d-flex justify-content-between small text-muted mb-1">
                            <span>Thuốc đang hoạt động</span>
                            <span>${activeMedications}/${totalMedications}</span>
                        </div>
                        <div class="progress progress-thin">
                            <div class="progress-bar bg-primary" style="width: ${totalMedications == 0 ? 0 : (activeMedications * 100 / totalMedications)}%;"></div>
                        </div>
                    </div>
                    <div class="mb-3">
                        <div class="d-flex justify-content-between small text-muted mb-1">
                            <span>Thực phẩm đang hoạt động</span>
                            <span>${activeFoods}/${totalFoods}</span>
                        </div>
                        <div class="progress progress-thin">
                            <div class="progress-bar bg-success" style="width: ${totalFoods == 0 ? 0 : (activeFoods * 100 / totalFoods)}%;"></div>
                        </div>
                    </div>
                    <div>
                        <div class="d-flex justify-content-between small text-muted mb-1">
                            <span>Nội dung đang hiển thị</span>
                            <span>${activeEducationContents}/${educationContents}</span>
                        </div>
                        <div class="progress progress-thin">
                            <div class="progress-bar bg-warning" style="width: ${educationContents == 0 ? 0 : (activeEducationContents * 100 / educationContents)}%;"></div>
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
        <span class="ms-2">© 2026 Hệ thống HealthAlert. Dữ liệu y khoa lõi phục vụ toàn bộ luồng admin.</span>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>
</body>
</html>
