<%--
  Created by IntelliJ IDEA.
  User: nguye
  Date: 27/06/2026
  Time: 4:30 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Quản lý Thực phẩm gốc</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
    <style>
        .table-hover tbody tr:hover { background-color: #f8f9fa; }
        .action-btns .btn { padding: 0.25rem 0.5rem; font-size: 0.875rem; margin-right: 3px; }
    </style>
</head>
<body>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <a href="${pageContext.request.contextPath}/" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a>
            <a href="${pageContext.request.contextPath}/patient-manager" class="nav-link">Patients</a>
            <a href="${pageContext.request.contextPath}/admin/master-foods" class="nav-link active">Thực phẩm</a>
            <a href="${pageContext.request.contextPath}/admin/master-medications" class="nav-link">Thuốc</a>
        </div>
    </div>
    <div class="d-flex align-items-center gap-3 text-muted">
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.loginUser ? sessionScope.loginUser.hoTen : 'Admin'}&background=0D8ABC&color=fff" alt="User" class="rounded-circle" width="28" height="28">
    </div>
</nav>

<div class="app-container">
    <jsp:include page="/WEB-INF/views/admin/sidebar.jsp">
        <jsp:param name="activeMenu" value="master-foods" />
    </jsp:include>

    <main class="main-content d-flex flex-column">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold text-gray-800">Kho dữ liệu Thực phẩm</h2>
                <p class="text-muted small mb-0">Quản lý danh mục thực phẩm mẫu cho bệnh nhân tham khảo.</p>
            </div>
            <button class="btn btn-primary shadow-sm rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#formModal" onclick="openAddModal()">
                <i class="fas fa-plus me-1"></i> Thêm Thực phẩm
            </button>
        </div>

        <div class="custom-card flex-grow-1 p-0 overflow-hidden">
            <div class="table-responsive">
                <table class="table table-hover custom-table mb-0 align-middle">
                    <thead class="table-light">
                    <tr>
                        <th class="ps-4">Tên Thực phẩm</th>
                        <th>Khẩu phần</th>
                        <th>Carbs (g)</th>
                        <th>Calo (kcal)</th>
                        <th>Chỉ số GI</th>
                        <th>Trạng thái</th>
                        <th class="text-end pe-4">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="f" items="${foodList}">
                        <tr>
                            <td class="ps-4 fw-bold text-primary">${f.tenThucPham}</td>
                            <td>${f.donViKhauPhan}</td>
                            <td>${f.carbsG}</td>
                            <td>${not empty f.caloKcal ? f.caloKcal : '-'}</td>
                            <td>${not empty f.chiSoGI ? f.chiSoGI : '-'}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${f.trangThai}">
                                        <span class="badge bg-success bg-opacity-10 text-success border border-success-subtle rounded-pill">Hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary bg-opacity-10 text-secondary border border-secondary-subtle rounded-pill">Đã ẩn</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end pe-4 action-btns">
                                <button class="btn btn-light text-warning" title="Sửa"
                                        data-bs-toggle="modal" data-bs-target="#formModal"
                                        onclick="openEditModal('${f.id}', '${f.tenThucPham}', '${f.donViKhauPhan}', '${f.carbsG}', '${f.caloKcal}', '${f.chiSoGI}', ${f.trangThai})">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-light text-danger" title="Xóa"
                                        data-bs-toggle="modal" data-bs-target="#deleteModal"
                                        onclick="document.getElementById('deleteId').value = '${f.id}'">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty foodList}">
                        <tr><td colspan="7" class="text-center text-muted py-4">Chưa có dữ liệu thực phẩm.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<div class="modal fade" id="formModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/master-foods" method="post">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="formId">

                <div class="modal-header text-white" style="background-color: var(--primary-blue, #0d6efd);">
                    <h5 class="modal-title" id="formModalTitle">Thêm Thực phẩm</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label text-muted fw-bold">Tên thực phẩm</label>
                        <input type="text" class="form-control" name="tenThucPham" id="inputTen" required>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label text-muted fw-bold">Đơn vị khẩu phần</label>
                            <input type="text" class="form-control" name="donViKhauPhan" id="inputDonVi" placeholder="VD: 1 bát con, 100g" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted fw-bold">Lượng Carbs (g)</label>
                            <input type="number" step="0.1" class="form-control" name="carbsG" id="inputCarbs" required>
                        </div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label text-muted fw-bold">Calo (kcal)</label>
                            <input type="number" step="0.1" class="form-control" name="caloKcal" id="inputCalo">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted fw-bold">Chỉ số GI</label>
                            <input type="number" step="0.1" class="form-control" name="chiSoGI" id="inputGI">
                        </div>
                    </div>
                    <div class="form-check form-switch mt-3">
                        <input class="form-check-input" type="checkbox" role="switch" id="inputTrangThai" name="trangThai" checked>
                        <label class="form-check-label text-muted fw-bold" for="inputTrangThai">Hiển thị/Hoạt động</label>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary" id="formSubmitBtn">Lưu lại</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="deleteModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content text-center p-4">
            <i class="fas fa-exclamation-triangle text-danger fa-3x mb-3"></i>
            <h5 class="mb-3">Xác nhận xóa?</h5>
            <form action="${pageContext.request.contextPath}/admin/master-foods" method="post">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" id="deleteId">
                <div class="d-flex justify-content-center gap-2 mt-3">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger">Xóa</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function openAddModal() {
        document.getElementById('formModalTitle').innerText = "Thêm Thực phẩm";
        document.getElementById('formAction').value = "add";
        document.getElementById('formSubmitBtn').innerText = "Thêm mới";
        document.getElementById('inputTen').value = "";
        document.getElementById('inputDonVi').value = "";
        document.getElementById('inputCarbs').value = "";
        document.getElementById('inputCalo').value = "";
        document.getElementById('inputGI').value = "";
        document.getElementById('inputTrangThai').checked = true;
    }

    function openEditModal(id, ten, donVi, carbs, calo, gi, trangThai) {
        document.getElementById('formModalTitle').innerText = "Chỉnh sửa Thực phẩm";
        document.getElementById('formAction').value = "update";
        document.getElementById('formSubmitBtn').innerText = "Cập nhật";
        document.getElementById('formId').value = id;
        document.getElementById('inputTen').value = ten;
        document.getElementById('inputDonVi').value = donVi;
        document.getElementById('inputCarbs').value = carbs !== 'null' ? carbs : '';
        document.getElementById('inputCalo').value = calo !== 'null' ? calo : '';
        document.getElementById('inputGI').value = gi !== 'null' ? gi : '';
        document.getElementById('inputTrangThai').checked = trangThai;
    }
</script>
</body>
</html>
