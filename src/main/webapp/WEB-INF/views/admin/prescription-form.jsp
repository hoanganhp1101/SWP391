<%--
  Created by IntelliJ IDEA.
  User: nguye
  Date: 27/06/2026
  Time: 5:16 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>HealthAlert | Kê Đơn Thuốc</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-light">

<div class="container mt-5">
    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
            <h4 class="mb-0"><i class="fas fa-file-medical me-2"></i>Kê Đơn Thuốc Mới</h4>
            <a href="${pageContext.request.contextPath}/patient-manager" class="btn btn-sm btn-light">Quay lại</a>
        </div>
        <div class="card-body p-4">
            <form action="${pageContext.request.contextPath}/admin/prescribe" method="post">
                <input type="hidden" name="patientId" value="${patientId}">

                <div class="mb-3">
                    <label class="form-label fw-bold">Lời dặn dò / Ghi chú chung</label>
                    <textarea class="form-control" name="ghiChu" rows="2" placeholder="Ví dụ: Hạn chế ăn mặn, tập thể dục nhẹ nhàng..."></textarea>
                </div>

                <h5 class="mt-4 mb-3 border-bottom pb-2">Danh sách thuốc chỉ định</h5>

                <div id="medication-container">
                    <div class="row mb-3 medication-row align-items-end">
                        <div class="col-md-4">
                            <label class="form-label text-muted small">Tên thuốc</label>
                            <select class="form-select" name="medicationId[]" required>
                                <option value="" selected disabled>-- Chọn thuốc --</option>
                                <c:forEach var="med" items="${medList}">
                                    <option value="${med.id}">${med.tenThuoc} (${med.hoatChat})</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label text-muted small">Liều lượng</label>
                            <input type="text" class="form-control" name="lieuLuong[]" placeholder="VD: 1 viên, 5ml..." required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label text-muted small">Tần suất / Cách dùng</label>
                            <input type="text" class="form-control" name="tanSuat[]" placeholder="VD: Sáng 1, Tối 1 sau ăn" required>
                        </div>
                        <div class="col-md-1">
                            <button type="button" class="btn btn-danger btn-remove-row" disabled><i class="fas fa-trash"></i></button>
                        </div>
                    </div>
                </div>

                <div class="mt-3">
                    <button type="button" class="btn btn-outline-success btn-sm" id="btn-add-row">
                        <i class="fas fa-plus me-1"></i> Thêm loại thuốc khác
                    </button>
                </div>

                <hr class="my-4">
                <div class="text-end">
                    <button type="submit" class="btn btn-primary px-4 py-2 fw-bold">Lưu Đơn Thuốc</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // Xử lý Javascript để thêm/xóa dòng thuốc động
    document.getElementById('btn-add-row').addEventListener('click', function() {
        const container = document.getElementById('medication-container');
        const rows = container.getElementsByClassName('medication-row');
        const newRow = rows[0].cloneNode(true); // Copy dòng đầu tiên

        // Xóa sạch giá trị đã nhập ở dòng copy
        newRow.querySelectorAll('input, select').forEach(el => el.value = '');

        // Kích hoạt nút xóa cho dòng mới
        const removeBtn = newRow.querySelector('.btn-remove-row');
        removeBtn.disabled = false;
        removeBtn.addEventListener('click', function() {
            newRow.remove();
        });

        container.appendChild(newRow);
    });
</script>
</body>
</html>