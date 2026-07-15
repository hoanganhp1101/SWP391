<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ bệnh nhân</title>
    <style>
        body { font-family: "Segoe UI", Arial, sans-serif; background: #f5f7fb; margin: 0; padding: 28px; color: #1f2937; }
        .card { max-width: 720px; margin: 0 auto; background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 24px; }
        .btn { display: inline-flex; align-items: center; height: 40px; padding: 0 16px; border-radius: 6px; text-decoration: none; font-weight: 700; background: #1557d5; color: #fff; }
        .muted { color: #6b7280; margin-top: 8px; }
    </style>
</head>
<body>
<div class="card">
    <h1>Hồ sơ bệnh nhân</h1>
    <c:choose>
        <c:when test="${not empty patientId}">
            <p>Mã bệnh nhân: <strong><c:out value="${patientId}" /></strong></p>
            <p class="muted">Trang chi tiết bệnh án đang được phát triển. Bạn có thể quay lại danh sách cảnh báo để tiếp tục xử lý.</p>
        </c:when>
        <c:otherwise>
            <p class="muted">Không tìm thấy mã bệnh nhân.</p>
        </c:otherwise>
    </c:choose>
    <a class="btn" href="${pageContext.request.contextPath}/doctor/alerts">Quay lại cảnh báo</a>
</div>
</body>
</html>
