<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Nội dung giáo dục</title>
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
            <a href="${pageContext.request.contextPath}/admin/educational-content" class="nav-link active">Giáo dục</a>
        </div>
    </div>
    <div class="d-flex align-items-center gap-3 text-muted">
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.adminUser ? sessionScope.adminUser.hoTen : 'Admin'}&background=0D8ABC&color=fff"
             alt="Admin" class="rounded-circle" width="28" height="28">
        <a href="${pageContext.request.contextPath}/admin/logout" class="text-danger ms-2" title="Đăng xuất" style="text-decoration: none;">
            <i class="fas fa-sign-out-alt fs-5"></i>
        </a>
    </div>
</nav>

<div class="app-container">
    <jsp:include page="/WEB-INF/views/admin/sidebar.jsp">
        <jsp:param name="activeMenu" value="educational-content" />
    </jsp:include>

    <main class="main-content d-flex flex-column">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h4 mb-0 fw-bold">Quản lý nội dung giáo dục</h2>
                <p class="text-muted small mb-0">Biên soạn kiến thức dinh dưỡng, thuốc, insulin và tự chăm sóc cho bệnh nhân tiểu đường.</p>
            </div>
            <button class="btn btn-primary btn-sm rounded-pill px-3" data-bs-toggle="modal" data-bs-target="#contentModal" onclick="openAddModal()">
                <i class="fas fa-plus me-1"></i> Thêm nội dung
            </button>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-3"><div class="custom-card"><div class="stat-card-title">Tổng nội dung</div><h3 class="stat-card-value mb-0">${totalContents}</h3></div></div>
            <div class="col-md-3"><div class="custom-card"><div class="stat-card-title">Đang hiển thị</div><h3 class="stat-card-value text-success mb-0">${activeContents}</h3></div></div>
            <div class="col-md-3"><div class="custom-card"><div class="stat-card-title">Dinh dưỡng</div><h3 class="stat-card-value text-warning mb-0">${nutritionContents}</h3></div></div>
            <div class="col-md-3"><div class="custom-card"><div class="stat-card-title">Thuốc & insulin</div><h3 class="stat-card-value text-primary mb-0">${medicationContents}</h3></div></div>
        </div>

        <c:if test="${not empty flashMessage}">
            <div class="alert alert-${flashType} alert-dismissible fade show py-2 small" role="alert">
                <c:out value="${flashMessage}"/>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
            </div>
        </c:if>

        <div class="custom-card flex-grow-1">
            <div class="d-flex justify-content-between align-items-center mb-4 gap-3">
                <h5 class="h6 mb-0 fw-bold">Thư viện nội dung</h5>
                <form action="${pageContext.request.contextPath}/admin/educational-content" method="get" class="d-flex gap-2">
                    <select name="category" class="form-select form-select-sm shadow-none" style="width: 165px;">
                        <option value="">Tất cả chủ đề</option>
                        <option value="dinh_duong" ${selectedCategory == 'dinh_duong' ? 'selected' : ''}>Dinh dưỡng</option>
                        <option value="thuoc_insulin" ${selectedCategory == 'thuoc_insulin' ? 'selected' : ''}>Thuốc & insulin</option>
                        <option value="van_dong" ${selectedCategory == 'van_dong' ? 'selected' : ''}>Vận động</option>
                        <option value="tu_cham_soc" ${selectedCategory == 'tu_cham_soc' ? 'selected' : ''}>Tự chăm sóc</option>
                    </select>
                    <select name="status" class="form-select form-select-sm shadow-none" style="width: 145px;">
                        <option value="">Tất cả trạng thái</option>
                        <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Đang hiển thị</option>
                        <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Đã ẩn</option>
                    </select>
                    <div class="input-group input-group-sm" style="width: 250px;">
                        <span class="input-group-text bg-white border-end-0 text-muted"><i class="fas fa-search"></i></span>
                        <input type="text" name="keyword" value="${fn:escapeXml(searchKeyword)}" class="form-control border-start-0 ps-0 shadow-none" placeholder="Tìm tiêu đề hoặc nội dung...">
                    </div>
                    <button class="btn btn-sm btn-primary px-3" type="submit">Lọc</button>
                    <a href="${pageContext.request.contextPath}/admin/educational-content" class="btn btn-sm btn-light border px-3">Reset</a>
                </form>
            </div>

            <div class="table-responsive">
                <table class="table custom-table align-middle mb-0">
                    <thead>
                    <tr>
                        <th>Nội dung</th>
                        <th>Chủ đề</th>
                        <th>Đối tượng</th>
                        <th>Thứ tự</th>
                        <th>Trạng thái</th>
                        <th class="text-end">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${contentList}">
                        <tr>
                            <td style="min-width: 360px;">
                                <div class="fw-bold text-dark"><c:out value="${item.title}"/></div>
                                <div class="text-muted small"><c:out value="${item.summary}" default="Chưa có mô tả ngắn"/></div>
                            </td>
                            <td><span class="badge bg-info text-dark rounded-pill"><c:out value="${item.category}"/></span></td>
                            <td class="text-muted small"><c:out value="${item.targetAudience}"/></td>
                            <td>${item.displayOrder}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.active}"><span class="text-success small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Hiển thị</span></c:when>
                                    <c:otherwise><span class="text-secondary small fw-medium"><i class="fas fa-circle me-1" style="font-size:8px;"></i> Đã ẩn</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <button class="btn btn-sm btn-light text-primary btn-edit-content"
                                        data-bs-toggle="modal"
                                        data-bs-target="#contentModal"
                                        data-id="${item.id}"
                                        data-title="${fn:escapeXml(item.title)}"
                                        data-category="${item.category}"
                                        data-summary="${fn:escapeXml(item.summary)}"
                                        data-content="${fn:escapeXml(item.content)}"
                                        data-target="${item.targetAudience}"
                                        data-order="${item.displayOrder}"
                                        data-active="${item.active}">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-light text-danger" data-bs-toggle="modal" data-bs-target="#deleteModal" onclick="document.getElementById('deleteId').value='${item.id}'">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty contentList}">
                        <tr><td colspan="6" class="text-center text-muted py-5">Chưa có nội dung giáo dục phù hợp.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<div class="modal fade" id="contentModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/educational-content" method="post">
                <input type="hidden" name="action" id="contentAction" value="add">
                <input type="hidden" name="id" id="contentId">
                <div class="modal-header">
                    <h5 class="modal-title" id="contentModalTitle">Thêm nội dung giáo dục</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-bold small">Tiêu đề</label>
                        <input type="text" name="title" id="contentTitle" class="form-control" required>
                    </div>
                    <div class="row g-3 mb-3">
                        <div class="col-md-4">
                            <label class="form-label fw-bold small">Chủ đề</label>
                            <select name="category" id="contentCategory" class="form-select" required>
                                <option value="dinh_duong">Dinh dưỡng</option>
                                <option value="thuoc_insulin">Thuốc & insulin</option>
                                <option value="van_dong">Vận động</option>
                                <option value="tu_cham_soc">Tự chăm sóc</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold small">Đối tượng</label>
                            <select name="targetAudience" id="contentTarget" class="form-select">
                                <option value="benh_nhan">Bệnh nhân</option>
                                <option value="nguoi_cham_soc">Người chăm sóc</option>
                                <option value="tat_ca">Tất cả</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold small">Thứ tự hiển thị</label>
                            <input type="number" name="displayOrder" id="contentOrder" class="form-control" value="0">
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold small">Mô tả ngắn</label>
                        <textarea name="summary" id="contentSummary" class="form-control" rows="2"></textarea>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold small">Nội dung chi tiết</label>
                        <textarea name="content" id="contentBody" class="form-control" rows="7" required></textarea>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" id="contentActive" name="active" checked>
                        <label class="form-check-label" for="contentActive">Hiển thị cho người dùng</label>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary">Lưu nội dung</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="deleteModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content text-center p-4">
            <i class="fas fa-exclamation-triangle text-danger fa-3x mb-3"></i>
            <h5 class="mb-3">Xóa nội dung này?</h5>
            <form action="${pageContext.request.contextPath}/admin/educational-content" method="post">
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
        document.getElementById('contentModalTitle').innerText = 'Thêm nội dung giáo dục';
        document.getElementById('contentAction').value = 'add';
        document.getElementById('contentId').value = '';
        document.getElementById('contentTitle').value = '';
        document.getElementById('contentCategory').value = 'dinh_duong';
        document.getElementById('contentSummary').value = '';
        document.getElementById('contentBody').value = '';
        document.getElementById('contentTarget').value = 'benh_nhan';
        document.getElementById('contentOrder').value = '0';
        document.getElementById('contentActive').checked = true;
    }

    document.querySelectorAll('.btn-edit-content').forEach(button => {
        button.addEventListener('click', function() {
            document.getElementById('contentModalTitle').innerText = 'Chỉnh sửa nội dung giáo dục';
            document.getElementById('contentAction').value = 'update';
            document.getElementById('contentId').value = this.dataset.id;
            document.getElementById('contentTitle').value = this.dataset.title || '';
            document.getElementById('contentCategory').value = this.dataset.category || 'dinh_duong';
            document.getElementById('contentSummary').value = this.dataset.summary || '';
            document.getElementById('contentBody').value = this.dataset.content || '';
            document.getElementById('contentTarget').value = this.dataset.target || 'benh_nhan';
            document.getElementById('contentOrder').value = this.dataset.order || '0';
            document.getElementById('contentActive').checked = this.dataset.active === 'true';
        });
    });
</script>
</body>
</html>
