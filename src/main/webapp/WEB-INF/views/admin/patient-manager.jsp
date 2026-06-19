<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Patient Management</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">

    <style>
        .table-hover tbody tr:hover { background-color: #f8f9fa; }
        .modal-profile-header { background: linear-gradient(135deg, var(--primary-blue, #0d6efd), #0dcaf0); color: white; padding: 2rem; text-align: center; }
        .info-label { font-size: 0.8rem; text-transform: uppercase; font-weight: 600; color: #6c757d; margin-bottom: 2px; }
        .info-value { font-size: 1rem; font-weight: 500; color: #212529; margin-bottom: 15px; }
        .action-btns .btn { padding: 0.25rem 0.5rem; font-size: 0.875rem; margin-right: 3px; }
    </style>
</head>
<body>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <a href="${pageContext.request.contextPath}/" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a>
            <a href="${pageContext.request.contextPath}/patient-manager" class="nav-link active">Patients</a>
            <a href="${pageContext.request.contextPath}/admin/users" class="nav-link">Users</a>
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
        <jsp:param name="activeMenu" value="patients" />
    </jsp:include>

    <main class="main-content d-flex flex-column">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold text-gray-800">Danh sách Bệnh nhân</h2>
                <p class="text-muted small mb-0">Quản lý và cập nhật hồ sơ bệnh nhân.</p>
            </div>
            <button class="btn btn-primary shadow-sm rounded-pill px-4" style="background-color: var(--primary-blue); border:none;" data-bs-toggle="modal" data-bs-target="#formModal" onclick="openAddModal()">
                <i class="fas fa-plus me-1"></i> Thêm Bệnh nhân
            </button>
        </div>

        <div class="custom-card flex-grow-1 p-0 overflow-hidden">
            <div class="table-responsive">
                <table class="table table-hover custom-table mb-0 align-middle">
                    <thead class="table-light">
                    <tr>
                        <th class="ps-4">Tên Bệnh nhân</th>
                        <th>Ngày sinh</th>
                        <th>Liên hệ</th>
                        <th>Loại Bệnh</th>
                        <th>Bác sĩ</th>
                        <th class="text-end pe-4">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${patientList}">
                        <tr>
                            <td class="ps-4 fw-bold text-primary">${p.tenBenhNhan}</td>
                            <td>${p.ngaySinh}</td>
                            <td>
                                <div style="font-size: 0.8rem;"><i class="fas fa-phone-alt text-muted me-1"></i> ${p.soDienThoai}</div>
                                <div style="font-size: 0.8rem;"><i class="fas fa-envelope text-muted me-1"></i> ${p.email}</div>
                            </td>
                            <td>
                                <span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle rounded-pill px-2">${p.loaiTieuDuong}</span>
                            </td>
                            <td>${not empty p.tenBacSi ? p.tenBacSi : '<span class="text-warning fst-italic">Chưa xếp</span>'}</td>
                            <td class="text-end pe-4 action-btns">
                                <button class="btn btn-light text-info" title="Xem chi tiết"
                                        data-bs-toggle="modal" data-bs-target="#patientDetailModal"
                                        data-id="${p.id}" data-name="${p.tenBenhNhan}" data-dob="${p.ngaySinh}"
                                        data-phone="${p.soDienThoai}" data-email="${p.email}"
                                        data-type="${p.loaiTieuDuong}" data-doctor="${not empty p.tenBacSi ? p.tenBacSi : 'Chưa phân công'}">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="btn btn-light text-warning" title="Sửa thông tin"
                                        data-bs-toggle="modal" data-bs-target="#formModal"
                                        onclick="openEditModal('${p.id}', '${p.tenBenhNhan}', '${p.email}', '${p.soDienThoai}', '${p.ngaySinh}', '${p.loaiTieuDuong}')">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-light text-danger" title="Xóa hồ sơ"
                                        data-bs-toggle="modal" data-bs-target="#deleteModal"
                                        onclick="document.getElementById('deleteId').value = '${p.id}'">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty patientList}">
                        <tr>
                            <td colspan="6" class="text-center text-muted py-4">Chưa có hồ sơ bệnh nhân nào.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
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

<div class="modal fade" id="patientDetailModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg" style="border-radius: 12px; overflow: hidden;">
            <div class="modal-profile-header position-relative">
                <button type="button" class="btn-close btn-close-white position-absolute top-0 end-0 m-3" data-bs-dismiss="modal"></button>
                <img id="modal-avatar" src="" class="rounded-circle border border-4 border-white shadow-sm mb-2" style="width: 90px; height: 90px;">
                <h3 class="fw-bold mb-1" id="modal-name">N/A</h3>
                <p class="mb-0 opacity-75 small">UUID: <span id="modal-id">N/A</span></p>
            </div>
            <div class="modal-body p-4 bg-white">
                <div class="row">
                    <div class="col-md-6"><div class="info-label">Ngày sinh</div><div class="info-value" id="modal-dob">N/A</div></div>
                    <div class="col-md-6"><div class="info-label">Số điện thoại</div><div class="info-value" id="modal-phone">N/A</div></div>
                    <div class="col-md-6"><div class="info-label">Email</div><div class="info-value" id="modal-email">N/A</div></div>
                    <div class="col-md-6"><div class="info-label">Loại Tiểu đường</div><div class="info-value text-danger fw-bold" id="modal-type">N/A</div></div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="formModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/patient-manager" method="post">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="id" id="formId">

                <div class="modal-header text-white" style="background-color: var(--primary-blue);">
                    <h5 class="modal-title" id="formModalTitle">Thêm Bệnh nhân mới</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label text-muted fw-bold">Họ và tên</label>
                        <input type="text" class="form-control" name="hoTen" id="inputName" required>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label text-muted fw-bold">Số điện thoại</label>
                            <input type="text" class="form-control" name="soDienThoai" id="inputPhone">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted fw-bold">Ngày sinh</label>
                            <input type="date" class="form-control" name="ngaySinh" id="inputDob" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-muted fw-bold">Email</label>
                        <input type="email" class="form-control" name="email" id="inputEmail" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-muted fw-bold">Phân loại bệnh</label>
                        <select class="form-select" name="loaiTieuDuong" id="inputType">
                            <option value="Type 1">Type 1</option>
                            <option value="Type 2">Type 2</option>
                            <option value="Thai kỳ">Thai kỳ</option>
                            <option value="Khác">Khác</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary" id="formSubmitBtn" style="background-color: var(--primary-blue); border:none;">Lưu hồ sơ</button>
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
            <p class="text-muted small">Hành động này sẽ xóa vĩnh viễn hồ sơ bệnh nhân khỏi hệ thống.</p>
            <form action="${pageContext.request.contextPath}/patient-manager" method="post">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" id="deleteId">
                <div class="d-flex justify-content-center gap-2 mt-3">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger">Xóa ngay</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>
<script>
    const detailModal = document.getElementById('patientDetailModal');
    if (detailModal) {
        detailModal.addEventListener('show.bs.modal', e => {
            const btn = e.relatedTarget;
            const name = btn.getAttribute('data-name');
            detailModal.querySelector('#modal-id').textContent = btn.getAttribute('data-id');
            detailModal.querySelector('#modal-name').textContent = name;
            detailModal.querySelector('#modal-dob').textContent = btn.getAttribute('data-dob');
            detailModal.querySelector('#modal-phone').textContent = btn.getAttribute('data-phone');
            detailModal.querySelector('#modal-email').textContent = btn.getAttribute('data-email');
            detailModal.querySelector('#modal-type').textContent = btn.getAttribute('data-type');
            detailModal.querySelector('#modal-avatar').src = `https://ui-avatars.com/api/?name=\${encodeURIComponent(name)}&background=ffffff&color=0D8ABC&size=120`;
        });
    }

    function openAddModal() {
        document.getElementById('formModalTitle').innerText = "Thêm Bệnh nhân mới";
        document.getElementById('formAction').value = "add";
        document.getElementById('formSubmitBtn').innerText = "Thêm mới";

        document.getElementById('inputName').value = "";
        document.getElementById('inputPhone').value = "";
        document.getElementById('inputDob').value = "";
        document.getElementById('inputEmail').value = "";
        document.getElementById('inputType').value = "Type 2";
    }

    function openEditModal(id, name, email, phone, dob, type) {
        document.getElementById('formModalTitle').innerText = "Chỉnh sửa Hồ sơ";
        document.getElementById('formAction').value = "update";
        document.getElementById('formSubmitBtn').innerText = "Cập nhật";

        document.getElementById('formId').value = id;
        document.getElementById('inputName').value = name;
        document.getElementById('inputEmail').value = email;
        document.getElementById('inputPhone').value = phone;
        document.getElementById('inputDob').value = dob;
        document.getElementById('inputType').value = type;
    }
</script>
</body>
</html>