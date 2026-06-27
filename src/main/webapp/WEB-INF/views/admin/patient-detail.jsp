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
        /* Tối ưu CSS giao diện đơn thuốc */
        .prescription-header {
            background-color: #f8f9fa;
            font-weight: 500;
            transition: background-color 0.2s ease;
        }
        .prescription-header:not(.collapsed) {
            background-color: #e7f1ff;
            color: #0c63e4;
        }
        .med-table th {
            font-size: 0.85rem;
            color: #495057;
            background-color: #f1f3f5;
            font-weight: 600;
        }
        .border-dashed {
            border-style: dashed !important;
        }
    </style>
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="mb-4 d-flex justify-content-between align-items-center" style="max-width: 800px; margin: 0 auto;">
        <a href="${pageContext.request.contextPath}/patient-manager" class="btn btn-outline-secondary shadow-sm rounded-pill px-4">
            <i class="fas fa-arrow-left me-2"></i>Quay lại danh sách
        </a>
        <a href="${pageContext.request.contextPath}/admin/prescribe?patientId=${patient.id}" class="btn btn-success shadow-sm rounded-pill px-4">
            <i class="fas fa-plus me-2"></i>Kê đơn mới
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

            <h5 class="border-bottom pb-3 mt-5 mb-4 text-danger fw-bold">
                <i class="fas fa-prescription me-2"></i>Lịch sử đơn thuốc gần đây
            </h5>

            <c:choose>
                <c:when test="${not empty prescriptionList}">
                    <div class="accordion" id="prescriptionAccordion">
                        <c:forEach var="pres" items="${prescriptionList}" varStatus="status">
                            <div class="accordion-item border mb-2 shadow-sm rounded-2 overflow-hidden">
                                <h2 class="accordion-header">
                                    <button class="accordion-button prescription-header ${status.first ? '' : 'collapsed'}"
                                            type="button" data-bs-toggle="collapse" data-bs-target="#collapse${pres.id}">
                                        <div class="d-flex justify-content-between w-100 align-items-center me-3">
                                            <div>
                                                <i class="fas fa-file-invoice text-primary me-2"></i>
                                                <strong>Ngày kê đơn:</strong> ${pres.ngayKeDon}
                                            </div>
                                            <span class="badge bg-secondary bg-opacity-10 text-secondary small">
                                                Bác sĩ: <c:out value="${pres.bacSiName}" default="Hệ thống" />
                                            </span>
                                        </div>
                                    </button>
                                </h2>
                                <div id="collapse${pres.id}" class="accordion-collapse collapse ${status.first ? 'show' : ''}"
                                     data-bs-parent="#prescriptionAccordion">
                                    <div class="accordion-body bg-white">
                                        <div class="row mb-3 g-2 small text-muted bg-light p-2 rounded mx-0">
                                            <div class="col-md-6">
                                                <strong>Chẩn đoán:</strong>
                                                <c:out value="${pres.chanDoan}" default="Theo dõi đái tháo đường" />
                                            </div>
                                            <div class="col-md-6">
                                                <strong>Hẹn tái khám:</strong>
                                                <c:out value="${not empty pres.ngayTaiKham ? pres.ngayTaiKham : 'Chưa có lịch hẹn'}" />
                                            </div>
                                        </div>

                                        <c:if test="${not empty pres.huongDieuTri}">
                                            <p class="text-muted small mb-2">
                                                <i class="fas fa-notes-medical me-1 text-info"></i>
                                                <strong>Hướng điều trị:</strong> ${pres.huongDieuTri}
                                            </p>
                                        </c:if>

                                        <div class="table-responsive mt-2">
                                            <table class="table table-sm table-bordered align-middle med-table mb-0">
                                                <thead>
                                                <tr>
                                                    <th>Tên thuốc</th>
                                                    <th>Liều lượng</th>
                                                    <th>Đơn vị</th>
                                                    <th>Tần suất sử dụng</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <c:forEach var="med" items="${pres.medications}">
                                                    <tr>
                                                        <td class="fw-bold text-dark">${med.tenThuoc}</td>
                                                        <td>
                                                            <span class="badge bg-info bg-opacity-10 text-info fw-semibold px-2 py-1">
                                                                    ${med.lieuLuong}
                                                            </span>
                                                        </td>
                                                        <td class="text-muted small">${med.donVi}</td>
                                                        <td>${med.tanSuat}</td>
                                                    </tr>
                                                </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="text-center text-muted py-5 border rounded bg-white border-dashed">
                        <i class="fas fa-pills fa-2x mb-2 text-black-50"></i>
                        <p class="mb-0 small fst-italic">Bệnh nhân này chưa có lịch sử kê đơn thuốc nào.</p>
                    </div>
                </c:otherwise>
            </c:choose>

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