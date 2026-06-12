<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Hồ sơ Bệnh nhân | Diabetes Manage</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        .sidebar { min-height: 100vh; background: #212529; color: white; padding-top: 20px; }
        .nav-item .nav-link { color: #adb5bd; margin-bottom: 5px; }
        .nav-item .nav-link:hover, .nav-item .nav-link.active { color: #fff; background: rgba(255,255,255,0.1); border-radius: 5px; }
        .table-hover tbody tr:hover { background-color: #f8f9fa; }
        .modal-profile-header { background: linear-gradient(135deg, #0d6efd, #0dcaf0); color: white; padding: 2rem; text-align: center; }
        .info-label { font-size: 0.8rem; text-transform: uppercase; font-weight: 600; color: #6c757d; margin-bottom: 2px; }
        .info-value { font-size: 1rem; font-weight: 500; color: #212529; margin-bottom: 15px; }
        .action-btns .btn { padding: 0.25rem 0.5rem; font-size: 0.875rem; margin-right: 3px; }
    </style>
</head>
<body class="bg-light">

<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 sidebar d-none d-md-block px-3">
            <h5 class="text-center mb-4 text-uppercase font-weight-bold tracking-wide">Diabetes SysAdmin</h5>
            <ul class="nav flex-column">
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-tachometer-alt me-2"></i> Dashboard</a></li>
                <hr class="text-secondary my-2">
                <li class="nav-item"><a class="nav-link active" href="${pageContext.request.contextPath}/patient-manager"><i class="fas fa-notes-medical me-2"></i> Hồ sơ Bệnh nhân</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-user-md me-2"></i> Quản lý bác sĩ</a></li>
            </ul>
        </div>

        <div class="col-md-10 p-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="h3 text-gray-800">Danh sách Bệnh nhân</h2>
                <button class="btn btn-primary shadow-sm" data-bs-toggle="modal" data-bs-target="#formModal" onclick="openAddModal()">
                    <i class="fas fa-plus-circle me-1"></i> Thêm Bệnh nhân
                </button>
            </div>

            <div class="card shadow-sm border-0">
                <div class="card-body p-0">
                    <table class="table table-hover mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Tên Bệnh nhân</th>
                            <th>Ngày sinh</th>
                            <th>Liên hệ</th>
                            <th>Loại Bệnh</th>
                            <th>Bác sĩ</th>
                            <th class="text-center">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${patientList}">
                            <tr>
                                <td class="fw-bold text-primary">${p.tenBenhNhan}</td>
                                <td>${p.ngaySinh}</td>
                                <td>
                                    <small><i class="fas fa-phone-alt text-muted"></i> ${p.soDienThoai}</small><br>
                                    <small><i class="fas fa-envelope text-muted"></i> ${p.email}</small>
                                </td>
                                <td><span class="badge bg-danger">${p.loaiTieuDuong}</span></td>
                                <td>${not empty p.tenBacSi ? p.tenBacSi : '<span class="text-warning">Chưa xếp</span>'}</td>
                                <td class="text-center action-btns">
                                    <button class="btn btn-outline-info" title="Xem chi tiết"
                                            data-bs-toggle="modal" data-bs-target="#patientDetailModal"
                                            data-id="${p.id}" data-name="${p.tenBenhNhan}" data-dob="${p.ngaySinh}"
                                            data-phone="${p.soDienThoai}" data-email="${p.email}"
                                            data-type="${p.loaiTieuDuong}" data-doctor="${not empty p.tenBacSi ? p.tenBacSi : 'Chưa phân công'}">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                    <button class="btn btn-outline-warning" title="Sửa thông tin"
                                            data-bs-toggle="modal" data-bs-target="#formModal"
                                            onclick="openEditModal('${p.id}', '${p.tenBenhNhan}', '${p.email}', '${p.soDienThoai}', '${p.ngaySinh}', '${p.loaiTieuDuong}')">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn btn-outline-danger" title="Xóa hồ sơ"
                                            data-bs-toggle="modal" data-bs-target="#deleteModal"
                                            onclick="document.getElementById('deleteId').value = '${p.id}'">
                                        <i class="fas fa-trash-alt"></i>
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
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

                <div class="modal-header bg-primary text-white">
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
                    <button type="submit" class="btn btn-primary" id="formSubmitBtn">Lưu hồ sơ</button>
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
            detailModal.querySelector('#modal-avatar').src = `https://ui-avatars.com/api/?name=\${encodeURIComponent(name)}&background=ffffff&color=0d6efd&size=120`;
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