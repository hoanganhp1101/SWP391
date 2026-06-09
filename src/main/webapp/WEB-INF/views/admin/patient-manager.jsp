<%--
  Created by IntelliJ IDEA.
  User: nguye
  Date: 07/06/2026
  Time: 11:30 CH
--%>
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

        /* Style riêng cho modal chi tiết */
        .modal-profile-header {
            background: linear-gradient(135deg, #0d6efd, #0dcaf0);
            color: white;
            padding: 2rem;
            text-align: center;
        }
        .info-label {
            font-size: 0.8rem;
            text-transform: uppercase;
            font-weight: 600;
            color: #6c757d;
            margin-bottom: 2px;
        }
        .info-value {
            font-size: 1rem;
            font-weight: 500;
            color: #212529;
            margin-bottom: 15px;
        }
    </style>
</head>
<body class="bg-light">

<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 sidebar d-none d-md-block px-3">
            <h5 class="text-center mb-4 text-uppercase font-weight-bold tracking-wide">Diabetes SysAdmin</h5>
            <ul class="nav flex-column">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin-dashboard.jsp"><i class="fas fa-tachometer-alt me-2"></i> Dashboard</a></li>
                <hr class="text-secondary my-2">
                <li class="nav-item"><a class="nav-link active" href="${pageContext.request.contextPath}/patient-manager"><i class="fas fa-notes-medical me-2"></i> Hồ sơ Bệnh nhân</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-user-md me-2"></i> Quản lý ca bác sĩ</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-pills me-2"></i> Danh mục thuốc</a></li>
            </ul>
        </div>

        <div class="col-md-10 p-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="h3 text-gray-800">Danh sách Bệnh nhân</h2>
            </div>

            <div class="card shadow-sm border-0">
                <div class="card-body p-0">
                    <table class="table table-hover mb-0 align-middle">
                        <thead class="table-light">
                        <tr>
                            <th>Tên Bệnh nhân</th>
                            <th>Ngày sinh</th>
                            <th>Liên hệ</th>
                            <th>Loại Tiểu đường</th>
                            <th>Bác sĩ phụ trách</th>
                            <th class="text-center">Hồ sơ chi tiết</th>
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
                                <td>
                                    <span class="badge bg-danger">${p.loaiTieuDuong}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty p.tenBacSi}">
                                            <i class="fas fa-user-md text-info"></i> BS. ${p.tenBacSi}
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-warning"><i class="fas fa-exclamation-triangle"></i> Chưa phân công</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                        <%-- ĐÃ CẢI TIẾN: Chuyển thành nút kích hoạt POPUP kèm gán dữ liệu động vào thẻ data-* --%>
                                    <button type="button"
                                            class="btn btn-sm btn-outline-info"
                                            data-bs-toggle="modal"
                                            data-bs-target="#patientDetailModal"
                                            data-id="${p.id}"
                                            data-name="${p.tenBenhNhan}"
                                            data-dob="${p.ngaySinh}"
                                            data-phone="${p.soDienThoai}"
                                            data-email="${p.email}"
                                            data-type="${p.loaiTieuDuong}"
                                            data-doctor="${not empty p.tenBacSi ? p.tenBacSi : 'Chưa phân công'}">
                                        <i class="fas fa-eye"></i> Xem Bệnh Án
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty patientList}">
                            <tr>
                                <td colspan="6" class="text-center py-4 text-muted">Không có dữ liệu bệnh nhân.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="patientDetailModal" tabindex="-1" aria-labelledby="patientDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg" style="border-radius: 12px; overflow: hidden;">

            <div class="modal-profile-header position-relative">
                <button type="button" class="btn-close btn-close-white position-absolute top-0 end-0 m-3" data-bs-dismiss="modal" aria-label="Close"></button>
                <img id="modal-avatar" src="" class="rounded-circle border border-4 border-white shadow-sm mb-2" style="width: 90px; height: 90px;" alt="Avatar">
                <h3 class="fw-bold mb-1" id="modal-name">N/A</h3>
                <p class="mb-0 opacity-75 small"><i class="fas fa-fingerprint me-1"></i> UUID: <span id="modal-id">N/A</span></p>
            </div>

            <div class="modal-body p-4 bg-white">
                <h6 class="text-primary fw-bold mb-3"><i class="fas fa-id-card me-2"></i>Thông tin hành chính & Lâm sàng</h6>
                <div class="row">
                    <div class="col-md-6">
                        <div class="info-label">Ngày sinh</div>
                        <div class="info-value" id="modal-dob">N/A</div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-label">Số điện thoại</div>
                        <div class="info-value"><i class="fas fa-phone text-muted me-1"></i> <span id="modal-phone">N/A</span></div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-label">Địa chỉ Email</div>
                        <div class="info-value"><i class="fas fa-envelope text-muted me-1"></i> <span id="modal-email">N/A</span></div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-label">Phân loại Tiểu đường</div>
                        <div class="info-value">
                            <span class="badge bg-danger rounded-pill px-3 py-1.5" id="modal-type">N/A</span>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-label">Bác sĩ phụ trách chính</div>
                        <div class="info-value text-info fw-bold"><i class="fas fa-user-md me-1"></i> <span id="modal-doctor">N/A</span></div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-label">Trạng thái giám sát</div>
                        <div class="info-value text-success fw-bold"><i class="fas fa-circle me-1" style="font-size: 8px;"></i> Active Realtime Monitoring</div>
                    </div>
                </div>

                <hr class="my-3 opacity-25">

                <h6 class="text-success fw-bold mb-2"><i class="fas fa-history me-2"></i>Nhận xét từ hệ thống AI (Giai đoạn thử nghiệm)</h6>
                <div class="p-3 bg-light rounded text-muted small fst-italic border">
                    Kết nối cơ sở dữ liệu thành công. Nhật ký đo nồng độ glucose và các phân tích phân hệ AI tự động sẽ tự động đồng bộ hóa tại vùng này trong các sprint tiếp theo.
                </div>
            </div>

            <div class="modal-footer bg-light border-top-0">
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Đóng cửa sổ</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const patientDetailModal = document.getElementById('patientDetailModal');
    if (patientDetailModal) {
        patientDetailModal.addEventListener('show.bs.modal', event => {
            const button = event.relatedTarget;

            // Trích xuất thông tin từ các thuộc tính data-* gán trên nút bấm
            const id = button.getAttribute('data-id');
            const name = button.getAttribute('data-name');
            const dob = button.getAttribute('data-dob');
            const phone = button.getAttribute('data-phone');
            const email = button.getAttribute('data-email');
            const type = button.getAttribute('data-type');
            const doctor = button.getAttribute('data-doctor');

            // Đổ dữ liệu tìm được vào các vùng thẻ HTML tương ứng bên trong modal
            patientDetailModal.querySelector('#modal-id').textContent = id;
            patientDetailModal.querySelector('#modal-name').textContent = name;
            patientDetailModal.querySelector('#modal-dob').textContent = dob;
            patientDetailModal.querySelector('#modal-phone').textContent = phone;
            patientDetailModal.querySelector('#modal-email').textContent = email;
            patientDetailModal.querySelector('#modal-type').textContent = type;
            patientDetailModal.querySelector('#modal-doctor').textContent = doctor;

            patientDetailModal.querySelector('#modal-avatar').src = `https://ui-avatars.com/api/?name=\${encodeURIComponent(name)}&background=ffffff&color=0d6efd&size=120`;
        });
    }
</script>
</body>
</html>