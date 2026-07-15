<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Quản lý người dùng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <a href="${pageContext.request.contextPath}/" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Bảng điều khiển</a>
            <a href="${pageContext.request.contextPath}/patient-manager" class="nav-link">Bệnh nhân</a>
            <a href="${pageContext.request.contextPath}/admin/users" class="nav-link active">Người dùng</a>
            <a href="#" class="nav-link">Báo cáo</a>
        </div>
    </div>
    <div class="d-flex align-items-center gap-3 text-muted">
        <i class="far fa-bell" style="cursor: pointer;"></i>
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.loginUser ? sessionScope.loginUser.hoTen : 'Admin'}&background=0D8ABC&color=fff" alt="Người dùng" class="rounded-circle" width="28" height="28">
    </div>
</nav>

<div class="app-container">

    <jsp:include page="/WEB-INF/views/admin/sidebar.jsp">
        <jsp:param name="activeMenu" value="users" />
    </jsp:include>

    <main class="main-content d-flex flex-column">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">Quản lý người dùng</h2>
                <p class="text-muted small mb-0">Quản lý quyền truy cập hệ thống, vai trò và tài khoản nhân viên.</p>
            </div>
            <div class="d-flex gap-2">
                <button class="btn btn-primary btn-sm fw-medium px-3 rounded-pill" style="background-color: var(--primary-blue); border:none;" data-bs-toggle="modal" data-bs-target="#addUserModal">
                    <i class="fas fa-plus me-1"></i> Thêm người dùng mới
                </button>
            </div>
        </div>

        <div class="custom-card flex-grow-1">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h5 class="h6 mb-0 fw-bold text-dark">Người dùng hệ thống</h5>

                <div class="d-flex gap-2">
                    <select class="form-select form-select-sm shadow-none border-light-subtle text-muted" style="width: 150px;">
                        <option value="all">Tất cả vai trò</option>
                        <option value="bac_si">Bác sĩ</option>
                        <option value="y_ta">Y tá</option>
                        <option value="quan_tri_vien">Quản trị viên</option>
                    </select>
                    <div class="input-group input-group-sm" style="width: 250px;">
                        <span class="input-group-text bg-white border-end-0 text-muted"><i class="fas fa-search"></i></span>
                        <input type="text" class="form-control border-start-0 ps-0 shadow-none" placeholder="Tìm kiếm người dùng...">
                    </div>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table custom-table mb-0">
                    <thead>
                    <tr>
                        <th>Thông tin người dùng</th>
                        <th>Liên hệ</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th class="text-end" style="min-width: 120px;">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${userList}">
                        <tr>
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <img src="https://ui-avatars.com/api/?name=${u.hoTen}&background=random&color=fff" class="rounded-circle" width="32" height="32" alt="${u.hoTen}">
                                    <div>
                                        <div class="fw-bold text-dark" style="font-size:0.85rem;"><c:out value="${u.hoTen}"/></div>
                                        <div class="text-muted" style="font-size:0.7rem;">ID: <c:out value="${u.id}"/></div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-muted small">
                                <div style="font-size: 0.75rem;"><i class="fas fa-envelope me-1"></i> <c:out value="${u.email}"/></div>
                                <c:if test="${not empty u.soDienThoai}">
                                    <div style="font-size: 0.75rem;"><i class="fas fa-phone-alt me-1"></i> <c:out value="${u.soDienThoai}"/></div>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.vaiTro == 'quan_tri_vien'}">
                                        <span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle rounded-pill px-2" style="font-size: 0.7rem;">Quản trị viên</span>
                                    </c:when>
                                    <c:when test="${u.vaiTro == 'bac_si'}">
                                        <span class="badge bg-primary bg-opacity-10 text-primary border border-primary-subtle rounded-pill px-2" style="font-size: 0.7rem;">Bác sĩ</span>
                                    </c:when>
                                    <c:when test="${u.vaiTro == 'y_ta'}">
                                        <span class="badge bg-info bg-opacity-10 text-info border border-info-subtle rounded-pill px-2" style="font-size: 0.7rem;">Y tá</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary bg-opacity-10 text-secondary border border-secondary-subtle rounded-pill px-2" style="font-size: 0.7rem;">Bệnh nhân</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.kichHoat == 1}">
                                        <span class="text-success small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-danger small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Đã khóa</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td class="text-end">
                                <button class="btn btn-sm btn-light text-primary me-1 btn-edit-user"
                                        title="Chỉnh sửa người dùng"
                                        data-bs-toggle="modal"
                                        data-bs-target="#editUserModal"
                                        data-id="${u.id}"
                                        data-hoten="<c:out value='${u.hoTen}'/>"
                                        data-email="<c:out value='${u.email}'/>"
                                        data-sdt="<c:out value='${u.soDienThoai}'/>"
                                        data-vaitro="${u.vaiTro}">
                                    <i class="fas fa-edit"></i>
                                </button>

                                <c:choose>
                                    <c:when test="${u.kichHoat == 1}">
                                        <form action="${pageContext.request.contextPath}/admin/users" method="POST" class="d-inline">
                                            <input type="hidden" name="action" value="toggleStatus">
                                            <input type="hidden" name="id" value="${u.id}">
                                            <input type="hidden" name="status" value="0">
                                            <button type="submit" class="btn btn-sm btn-light text-danger" title="Khóa người dùng" onclick="return confirm('Bạn có chắc chắn muốn khóa người dùng này không?');">
                                                <i class="fas fa-lock"></i>
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/admin/users" method="POST" class="d-inline">
                                            <input type="hidden" name="action" value="toggleStatus">
                                            <input type="hidden" name="id" value="${u.id}">
                                            <input type="hidden" name="status" value="1">
                                            <button type="submit" class="btn btn-sm btn-light text-success" title="Mở khóa người dùng">
                                                <i class="fas fa-unlock"></i>
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty userList}">
                        <tr>
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <div class="avatar-sm" style="background:#e0f2fe; color:#0284c7;">AD</div>
                                    <div>
                                        <div class="fw-bold text-dark" style="font-size:0.85rem;">Quản trị hệ thống (Demo)</div>
                                        <div class="text-muted" style="font-size:0.7rem;">ID: USR-0001</div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-muted small">
                                <div style="font-size: 0.75rem;"><i class="fas fa-envelope me-1"></i> admin@diabcare.vn</div>
                                <div style="font-size: 0.75rem;"><i class="fas fa-phone-alt me-1"></i> 0934-567-890</div>
                            </td>
                            <td><span class="badge bg-danger bg-opacity-10 text-danger border border-danger-subtle rounded-pill px-2" style="font-size: 0.7rem;">Quản trị viên</span></td>
                            <td><span class="text-success small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Hoạt động</span></td>
                            <td class="text-end">
                                <button class="btn btn-sm btn-light text-primary me-1"><i class="fas fa-edit"></i></button>
                                <button class="btn btn-sm btn-light text-danger"><i class="fas fa-lock"></i></button>
                            </td>
                        </tr>
                    </c:if>

                    </tbody>
                </table>
            </div>

            <div class="d-flex justify-content-between align-items-center mt-4">
                <span class="text-muted small">Hiển thị trang ${currentPage} trên ${totalPages} (Tổng cộng: ${totalRecords} người dùng)</span>
                <nav>
                    <ul class="pagination pagination-sm mb-0">
                        <c:if test="${currentPage > 1}">
                            <li class="page-item">
                                <a class="page-link" href="?page=${currentPage - 1}&role=${selectedRole}&status=${selectedStatus}&keyword=${searchKeyword}">Trước</a>
                            </li>
                        </c:if>

                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <li class="page-item ${currentPage == i ? 'active' : ''}">
                                <a class="page-link" href="?page=${i}&role=${selectedRole}&status=${selectedStatus}&keyword=${searchKeyword}">${i}</a>
                            </li>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <li class="page-item">
                                <a class="page-link" href="?page=${currentPage + 1}&role=${selectedRole}&status=${selectedStatus}&keyword=${searchKeyword}">Tiếp</a>
                            </li>
                        </c:if>
                    </ul>
                </nav>
            </div>
        </div>

    </main>
</div>

<div class="footer-bar mt-auto">
    <div>
        <span class="fw-bold" style="color: var(--primary-blue);">HealthAlert</span>
        <span class="ms-2">© 2026 Hệ thống HealthAlert. Đã đăng ký bản quyền.</span>
    </div>
</div>

<div class="modal fade" id="addUserModal" tabindex="-1" aria-labelledby="addUserModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/users" method="POST">
                <input type="hidden" name="action" value="create">
                <div class="modal-header">
                    <h5 class="modal-title" id="addUserModalLabel">Thêm người dùng mới</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Họ và tên</label>
                        <input type="text" name="hoTen" class="form-control form-control-sm" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Email</label>
                        <input type="email" name="email" class="form-control form-control-sm" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Số điện thoại</label>
                        <input type="text" name="soDienThoai" class="form-control form-control-sm">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Vai trò</label>
                        <select name="vaiTro" class="form-select form-select-sm" required>
                            <option value="bac_si">Bác sĩ</option>
                            <option value="y_ta">Y tá</option>
                            <option value="quan_tri_vien">Quản trị viên</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Mật khẩu khởi tạo</label>
                        <input type="password" name="matKhau" class="form-control form-control-sm" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary btn-sm">Thêm mới</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="editUserModal" tabindex="-1" aria-labelledby="editUserModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/users" method="POST">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" id="edit-id">

                <div class="modal-header">
                    <h5 class="modal-title" id="editUserModalLabel">Chỉnh sửa thông tin thành viên</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Họ và tên</label>
                        <input type="text" name="hoTen" id="edit-hoten" class="form-control form-control-sm" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Email</label>
                        <input type="email" name="email" id="edit-email" class="form-control form-control-sm" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Số điện thoại</label>
                        <input type="text" name="soDienThoai" id="edit-sdt" class="form-control form-control-sm">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Vai trò</label>
                        <select name="vaiTro" id="edit-vaitro" class="form-select form-select-sm" required>
                            <option value="bac_si">Bác sĩ</option>
                            <option value="y_ta">Y tá</option>
                            <option value="quan_tri_vien">Quản trị viên</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary btn-sm">Cập nhật</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/script.js"></script>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        const editButtons = document.querySelectorAll('.btn-edit-user');

        editButtons.forEach(button => {
            button.addEventListener('click', function() {
                const id = this.getAttribute('data-id');
                const hoTen = this.getAttribute('data-hoten');
                const email = this.getAttribute('data-email');
                const soDienThoai = this.getAttribute('data-sdt');
                const vaiTro = this.getAttribute('data-vaitro');

                document.getElementById('edit-id').value = id;
                document.getElementById('edit-hoten').value = hoTen;
                document.getElementById('edit-email').value = email;
                document.getElementById('edit-sdt').value = soDienThoai;
                document.getElementById('edit-vaitro').value = vaiTro;
            });
        });
    });
</script>

</body>
</html>