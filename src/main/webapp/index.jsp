<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>HealthAlert - Cổng Điều Hướng Hệ Thống</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex flex-column align-items-center justify-content-center" style="height: 100vh;">

<div class="card p-5 shadow-sm text-center" style="max-width: 500px; width: 100%;">
    <h1 class="mb-4 text-primary fw-bold">HealthAlert Portal</h1>
    <p class="text-muted mb-5">Vui lòng chọn cổng truy cập phù hợp với vai trò của bạn</p>

    <div class="mb-3">
        <a href="${pageContext.request.contextPath}/admin/login" class="btn btn-danger btn-lg w-100 py-3 fw-bold shadow-sm">
            <i class="fas fa-user-shield me-2"></i> Cổng Quản Trị (Admin Portal)
        </a>
    </div>

    <div class="mb-3">
        <a href="${pageContext.request.contextPath}/patient-dashboard" class="btn btn-primary btn-lg w-100 py-3 fw-bold shadow-sm">
            <i class="fas fa-user-injured me-2"></i> Cổng Bệnh Nhân (Patient Portal)
        </a>
    </div>

    <div>
        <a href="${pageContext.request.contextPath}/Logincontroller" class="btn btn-success btn-lg w-100 py-3 fw-bold shadow-sm">
            <i class="fas fa-user-md me-2"></i> Cổng Bác Sĩ (Doctor Portal)
        </a>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/js/all.min.js"></script>
</body>
</html>
