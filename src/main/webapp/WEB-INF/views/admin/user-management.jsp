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
        <a href="${pageContext.request.contextPath}/admin-dashboard" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/admin-dashboard" class="nav-link">Dashboard</a>
            <a href="${pageContext.request.contextPath}/admin/users" class="nav-link active">Users</a>
            <a href="#" class="nav-link">Records</a>
        </div>
    </div>
    <div class="d-flex align-items-center gap-3 text-muted">
        <i class="far fa-bell"></i>
        <i class="fas fa-cog"></i>
        <img src="https://ui-avatars.com/api/?name=Admin&background=0D8ABC&color=fff" alt="User" class="rounded-circle" width="28" height="28">
    </div>
</nav>

<div class="app-container">

    <aside class="sidebar">
        <div class="user-profile-sm">
            <img src="https://ui-avatars.com/api/?name=System+Admin&background=1e293b&color=fff" alt="Admin">
            <div>
                <div class="name">System Admin</div>
                <div class="role">IT Department</div>
            </div>
        </div>

        <ul class="sidebar-menu mt-3">
            <li><a href="${pageContext.request.contextPath}/admin-dashboard"><i class="fas fa-th-large"></i> Overview</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/users" class="active"><i class="fas fa-users-cog"></i> User Management</a></li>
        </ul>

        <div class="sidebar-footer">
            <ul class="sidebar-menu p-0 m-0">
                <li><a href="#"><i class="fas fa-sign-out-alt"></i> Sign Out</a></li>
            </ul>
        </div>
    </aside>

    <main class="main-content d-flex flex-column">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">User Management</h2>
                <p class="text-muted small mb-0">Quản lý tài khoản Bác sĩ, Y tá và Bệnh nhân.</p>
            </div>
            <div class="d-flex gap-2">
                <button class="btn btn-primary btn-sm fw-medium px-3 rounded-pill" data-bs-toggle="modal" data-bs-target="#addUserModal" style="background-color: var(--primary-blue); border:none;">
                    <i class="fas fa-plus me-1"></i> Add New User
                </button>
            </div>
        </div>

        <div class="custom-card flex-grow-1">

            <form id="searchFilterForm" action="${pageContext.request.contextPath}/admin/users" method="GET" class="d-flex justify-content-between align-items-center mb-4">
                <div class="d-flex gap-3">
                    <select id="roleSelect" name="role" class="form-select form-select-sm shadow-none" style="width: 150px;">
                        <option value="">Tất cả vai trò</option>
                        <option value="benh_nhan" ${selectedRole == 'benh_nhan' ? 'selected' : ''}>Bệnh nhân</option>
                        <option value="bac_si" ${selectedRole == 'bac_si' ? 'selected' : ''}>Bác sĩ</option>
                        <option value="y_ta" ${selectedRole == 'y_ta' ? 'selected' : ''}>Y tá</option>
                        <option value="quan_tri_vien" ${selectedRole == 'quan_tri_vien' ? 'selected' : ''}>Admin</option>
                    </select>
                    <select id="statusSelect" name="status" class="form-select form-select-sm shadow-none" style="width: 150px;">
                        <option value="">Trạng thái (Tất cả)</option>
                        <option value="1" ${selectedStatus == '1' ? 'selected' : ''}>Đang hoạt động</option>
                        <option value="0" ${selectedStatus == '0' ? 'selected' : ''}>Đã khóa</option>
                    </select>
                </div>
                <div class="d-flex gap-2">
                    <div class="input-group input-group-sm" style="width: 250px;">
                        <span class="input-group-text bg-white border-end-0 text-muted"><i class="fas fa-search"></i></span>
                        <input id="keywordInput" type="text" name="keyword" value="${searchKeyword}" class="form-control border-start-0 ps-0 shadow-none" placeholder="Tìm tên, email hoặc SĐT...">
                    </div>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table custom-table mb-0 align-middle">
                    <thead>
                    <tr>
                        <th>Họ và Tên</th>
                        <th>Liên hệ</th>
                        <th>Vai trò</th>
                        <th>Ngày tạo</th>
                        <th>Trạng thái</th>
                        <th class="text-end">Hành động</th>
                    </tr>
                    </thead>
                    <tbody id="userTableBody">
                    <c:forEach var="user" items="${userList}">
                        <tr>
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <div class="avatar-sm" style="background:#e0f2fe; color:#0369a1;">
                                        <i class="fas fa-user"></i>
                                    </div>
                                    <div class="fw-bold" style="font-size:0.85rem;"><c:out value="${user.hoTen}"/></div>
                                </div>
                            </td>
                            <td>
                                <div class="text-muted" style="font-size:0.75rem;"><i class="fas fa-envelope me-1"></i> <c:out value="${user.email}"/></div>
                                <div class="text-muted" style="font-size:0.75rem;"><i class="fas fa-phone me-1"></i> <c:out value="${user.soDienThoai != null ? user.soDienThoai : 'N/A'}"/></div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.vaiTro == 'quan_tri_vien'}"><span class="badge bg-dark rounded-pill">Admin</span></c:when>
                                    <c:when test="${user.vaiTro == 'bac_si'}"><span class="badge bg-info rounded-pill">Bác sĩ</span></c:when>
                                    <c:when test="${user.vaiTro == 'y_ta'}"><span class="badge bg-success rounded-pill">Y tá</span></c:when>
                                    <c:otherwise><span class="badge bg-secondary rounded-pill">Bệnh nhân</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-muted small"><c:out value="${user.ngayTao}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.kichHoat == 1}"><span class="badge-active">Hoạt động</span></c:when>
                                    <c:otherwise><span class="badge-inactive">Đã khóa</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <button class="btn btn-sm btn-light text-primary border-0 me-1 btn-edit-user"
                                        data-bs-toggle="modal"
                                        data-bs-target="#editUserModal"
                                        data-id="${user.id}"
                                        data-hoten="${user.hoTen}"
                                        data-email="${user.email}"
                                        data-sdt="${user.soDienThoai}"
                                        data-vaitro="${user.vaiTro}"
                                        title="Sửa">
                                    <i class="fas fa-edit"></i>
                                </button>

                                <form action="${pageContext.request.contextPath}/admin/users" method="POST" style="display:inline;">
                                    <input type="hidden" name="action" value="toggleStatus">
                                    <input type="hidden" name="id" value="${user.id}">
                                    <c:choose>
                                        <c:when test="${user.kichHoat == 1}">
                                            <input type="hidden" name="status" value="0">
                                            <button type="submit" class="btn btn-sm btn-light text-danger border-0" title="Khóa tài khoản"><i class="fas fa-lock"></i></button>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="hidden" name="status" value="1">
                                            <button type="submit" class="btn btn-sm btn-light text-success border-0" title="Mở khóa"><i class="fas fa-unlock"></i></button>
                                        </c:otherwise>
                                    </c:choose>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty userList}">
                        <tr>
                            <td colspan="6" class="text-center text-muted py-4">
                                <i class="fas fa-folder-open fs-3 mb-2"></i>
                                <p class="mb-0">Không tìm thấy tài khoản nào phù hợp.</p>
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <div id="paginationContainer">
                <c:if test="${totalPages > 1}">
                    <div class="d-flex justify-content-between align-items-center mt-4">
                        <span class="text-muted small">
                            Hiển thị trang ${currentPage} / ${totalPages} (Tổng số: ${totalRecords} tài khoản)
                        </span>
                        <ul class="pagination pagination-sm mb-0">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link shadow-none" href="${pageContext.request.contextPath}/admin/users?role=${selectedRole}&status=${selectedStatus}&keyword=${searchKeyword}&page=${currentPage - 1}">Trước</a>
                            </li>
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link shadow-none" href="${pageContext.request.contextPath}/admin/users?role=${selectedRole}&status=${selectedStatus}&keyword=${searchKeyword}&page=${i}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <a class="page-link shadow-none" href="${pageContext.request.contextPath}/admin/users?role=${selectedRole}&status=${selectedStatus}&keyword=${searchKeyword}&page=${currentPage + 1}">Sau</a>
                            </li>
                        </ul>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</div>

<div class="modal fade" id="addUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header border-bottom-0 pb-0">
                <h5 class="modal-title fw-bold">Thêm tài khoản mới</h5>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form action="${pageContext.request.contextPath}/admin/users" method="POST">
                    <input type="hidden" name="action" value="create">
                    <div class="mb-3">
                        <label class="form-label small fw-medium">Họ và Tên <span class="text-danger">*</span></label>
                        <input type="text" class="form-control shadow-none" name="hoTen" required placeholder="Nhập họ và tên...">
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label small fw-medium">Email <span class="text-danger">*</span></label>
                            <input type="email" class="form-control shadow-none" name="email" required placeholder="email@example.com">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label small fw-medium">Số điện thoại</label>
                            <input type="text" class="form-control shadow-none" name="soDienThoai" placeholder="09xxxxxxx">
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-medium">Mật khẩu <span class="text-danger">*</span></label>
                        <input type="password" class="form-control shadow-none" name="matKhau" required placeholder="Nhập mật khẩu khởi tạo">
                    </div>
                    <div class="mb-4">
                        <label class="form-label small fw-medium">Vai trò hệ thống <span class="text-danger">*</span></label>
                        <select class="form-select shadow-none" name="vaiTro" required>
                            <option value="benh_nhan" selected>Bệnh nhân</option>
                            <option value="bac_si">Bác sĩ</option>
                            <option value="y_ta">Y tá / Chuyên viên</option>
                            <option value="quan_tri_vien">Quản trị viên (Admin)</option>
                        </select>
                        <div class="form-text small text-muted mt-1"><i class="fas fa-info-circle"></i> Nếu chọn "Bệnh nhân", hệ thống sẽ tự động tạo hồ sơ bệnh án đi kèm.</div>
                    </div>
                    <div class="d-flex justify-content-end gap-2">
                        <button type="button" class="btn btn-light fw-medium px-4" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary fw-medium px-4" style="background-color: var(--primary-blue); border:none;">Tạo tài khoản</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="editUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header border-bottom-0 pb-0">
                <h5 class="modal-title fw-bold">Chỉnh sửa tài khoản</h5>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form action="${pageContext.request.contextPath}/admin/users" method="POST">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" id="editUserId">

                    <div class="mb-3">
                        <label class="form-label small fw-medium">Họ và Tên <span class="text-danger">*</span></label>
                        <input type="text" class="form-control shadow-none" name="hoTen" id="editHoTen" required placeholder="Nhập họ và tên...">
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label small fw-medium">Email <span class="text-danger">*</span></label>
                            <input type="email" class="form-control shadow-none" name="email" id="editEmail" required placeholder="email@example.com">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label small fw-medium">Số điện thoại</label>
                            <input type="text" class="form-control shadow-none" name="soDienThoai" id="editSoDienThoai" placeholder="09xxxxxxx">
                        </div>
                    </div>
                    <div class="mb-4">
                        <label class="form-label small fw-medium">Vai trò hệ thống <span class="text-danger">*</span></label>
                        <select class="form-select shadow-none" name="vaiTro" id="editVaiTro" required>
                            <option value="benh_nhan">Bệnh nhân</option>
                            <option value="bac_si">Bác sĩ</option>
                            <option value="y_ta">Y tá / Chuyên viên</option>
                            <option value="quan_tri_vien">Quản trị viên (Admin)</option>
                        </select>
                    </div>
                    <div class="d-flex justify-content-end gap-2">
                        <button type="button" class="btn btn-light fw-medium px-4" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary fw-medium px-4" style="background-color: var(--primary-blue); border:none;">Lưu thay đổi</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        const form = document.getElementById("searchFilterForm");
        const keywordInput = document.getElementById("keywordInput");
        const roleSelect = document.getElementById("roleSelect");
        const statusSelect = document.getElementById("statusSelect");
        const tableBody = document.getElementById("userTableBody");
        const paginationContainer = document.getElementById("paginationContainer");

        let debounceTimer;

        function doLiveSearch() {
            const formData = new FormData(form);
            const params = new URLSearchParams(formData);

            params.set("page", "1");

            const url = form.action + "?" + params.toString();

            window.history.pushState({}, "", url);

            fetch(url)
                .then(response => response.text())
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, "text/html");

                    tableBody.innerHTML = doc.getElementById("userTableBody").innerHTML;
                    paginationContainer.innerHTML = doc.getElementById("paginationContainer").innerHTML;
                })
                .catch(err => console.error("Lỗi Live Search:", err));
        }

        keywordInput.addEventListener("input", function() {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(doLiveSearch, 300);
        });

        roleSelect.addEventListener("change", doLiveSearch);
        statusSelect.addEventListener("change", doLiveSearch);

        document.addEventListener('click', function(e) {
            const editBtn = e.target.closest('.btn-edit-user');
            if (editBtn) {
                document.getElementById('editUserId').value = editBtn.getAttribute('data-id');
                document.getElementById('editHoTen').value = editBtn.getAttribute('data-hoten');
                document.getElementById('editEmail').value = editBtn.getAttribute('data-email');

                let sdt = editBtn.getAttribute('data-sdt');
                document.getElementById('editSoDienThoai').value = (sdt && sdt !== 'null') ? sdt : '';

                document.getElementById('editVaiTro').value = editBtn.getAttribute('data-vaitro');
            }
        });
    });
</script>

</body>
</html>