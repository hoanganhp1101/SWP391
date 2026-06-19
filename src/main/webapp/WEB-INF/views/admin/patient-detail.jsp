<%--
  Created by IntelliJ IDEA.
  User: nguye
  Date: 09/06/2026
  Time: 10:53 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết Hồ sơ Bệnh nhân | Diabetes Manage</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        .profile-header {
            background: linear-gradient(135deg, #0d6efd, #0dcaf0);
            color: white;
            padding: 2.5rem 0;
            border-radius: 10px 10px 0 0;
        }
        .card-detail {
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.05);
            border: none;
        }
        .info-label {
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-weight: 600;
            color: #6c757d;
        }
    </style>
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="mb-4">
        <a href="${pageContext.request.contextPath}/patient-manager" class="btn btn-outline-secondary shadow-sm rounded-pill px-4">
            <i class="fas fa-arrow-left me-2"></i>Quay lại danh sách
        </a>
    </div>

    <div class="card card-detail mx-auto" style="max-width: 800px;">
        <div class="profile-header text-center position-relative">
            <img src="https://ui-avatars.com/api/?name=${patient.tenBenhNhan}&background=ffffff&color=0d6efd&size=120"
                 class="rounded-circle border border-4 border-white shadow-sm mb-3" alt="Avatar">
            <h2 class="fw-bold mb-1"><c:out value="${patient.tenBenhNhan}" default="Chưa cập nhật tên" /></h2>
            <p class="mb-0 opacity-75"><i class="fas fa-fingerprint me-1"></i> ID: ${patient.id}</p>
        </div>

        <div class="card-body p-5">
            <h5 class="border-bottom pb-3 mb-4 text-primary fw-bold">
                <i class="fas fa-id-card-alt me-2"></i>Thông tin liên hệ & Bệnh lý
            </h5>

            <div class="row g-4 mb-4">
                <div class="col-md-6">
                    <span class="info-label d-block mb-1">Số điện thoại</span>
                    <div class="fs-5">
                        <i class="fas fa-phone-alt me-2 text-secondary"></i>
                        <c:out value="${patient.soDienThoai}" default="Chưa cập nhật" />
                    </div>
                </div>

                <div class="col-md-6">
                    <span class="info-label d-block mb-1">Email</span>
                    <div class="fs-5">
                        <i class="fas fa-envelope me-2 text-secondary"></i>
                        <c:out value="${patient.email}" default="Chưa cập nhật" />
                    </div>
                </div>

                <div class="col-md-6">
                    <span class="info-label d-block mb-1">Loại tiểu đường</span>
                    <div>
                        <c:choose>
                            <c:when test="${not empty patient.loaiTieuDuong}">
                                <span class="badge bg-danger fs-6 px-3 py-2 rounded-pill shadow-sm">
                                    <i class="fas fa-tint me-1"></i> ${patient.loaiTieuDuong}
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary fs-6 px-3 py-2 rounded-pill">Chưa phân loại</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="col-md-6">
                    <span class="info-label d-block mb-1">Trạng thái hồ sơ</span>
                    <div class="fs-5 text-success fw-bold">
                        <i class="fas fa-check-circle me-1"></i> Đang theo dõi
                    </div>
                </div>
            </div>

            <h5 class="border-bottom pb-3 mt-5 mb-4 text-success fw-bold">
                <i class="fas fa-file-medical-alt me-2"></i>Ghi chú lâm sàng
            </h5>
            <div class="alert alert-light border text-muted fst-italic p-4" role="alert">
                Hồ sơ bệnh nhân đã được liên kết với cơ sở dữ liệu (UUID: ${patient.id}). Các chỉ số đo đường huyết và thông báo AI sẽ được tích hợp vào khu vực này trong các bản cập nhật module tiếp theo.
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
