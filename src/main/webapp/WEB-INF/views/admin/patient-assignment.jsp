<%--
  Created by IntelliJ IDEA.
  User: nguye
  Date: 27/06/2026
  Time: 5:03 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert | Phân công Điều trị</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <a href="${pageContext.request.contextPath}/" class="brand">HealthAlert</a>
        <div class="d-flex">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">Dashboard</a>
            <a href="${pageContext.request.contextPath}/admin/patient-assignments" class="nav-link active">Phân công</a>
        </div>
    </div>
</nav>

<div class="app-container">
    <jsp:include page="/WEB-INF/views/admin/sidebar.jsp">
        <jsp:param name="activeMenu" value="assignments" />
    </jsp:include>

    <main class="main-content d-flex flex-column">
        <div class="mb-4">
            <h2 class="h4 mb-0 fw-bold text-gray-800">Phân công Bác sĩ Điều trị</h2>
            <p class="text-muted small mb-0">Quản lý và chỉ định bác sĩ phụ trách cho từng bệnh nhân.</p>
        </div>

        <div class="custom-card flex-grow-1 p-0 overflow-hidden">
            <div class="table-responsive">
                <table class="table table-hover custom-table mb-0 align-middle">
                    <thead class="table-light">
                    <tr>
                        <th class="ps-4">ID Bệnh nhân</th>
                        <th>Họ tên Bệnh nhân</th>
                        <th>Bác sĩ phụ trách hiện tại</th>
                        <th class="text-end pe-4">Thao tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="patient" items="${patientList}">
                        <tr>
                            <td class="ps-4 text-muted">${patient.id}</td>
                            <td class="fw-bold">${patient.hoTen}</td>

                            <td>
                                <c:choose>
                                    <c:when test="${not empty activeAssignments[patient.id]}">
                                        <span class="badge bg-primary bg-opacity-10 text-primary rounded-pill px-3 py-2">
                                            <i class="fas fa-user-md me-1"></i> Bs. ${activeAssignments[patient.id]}
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary bg-opacity-10 text-secondary rounded-pill px-3 py-2">
                                            Chưa phân công
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td class="text-end pe-4">
                                <button class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                        data-bs-toggle="modal" data-bs-target="#assignModal"
                                        onclick="openAssignModal('${patient.id}', '${patient.hoTen}')">
                                    <i class="fas fa-exchange-alt me-1"></i> Phân công
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<div class="modal fade" id="assignModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/admin/patient-assignments" method="post">
                <div class="modal-header text-white" style="background-color: var(--primary-blue, #0d6efd);">
                    <h5 class="modal-title">Chỉ định Bác sĩ</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body p-4">
                    <p class="text-muted mb-3">Bạn đang phân công bác sĩ cho bệnh nhân: <strong id="displayPatientName" class="text-dark"></strong></p>

                    <input type="hidden" name="patientId" id="inputPatientId">

                    <div class="mb-3">
                        <label class="form-label text-muted fw-bold">Chọn Bác sĩ phụ trách <span class="text-danger">*</span></label>
                        <select class="form-select" name="doctorId" required>
                            <option value="" selected disabled>-- Vui lòng chọn Bác sĩ --</option>
                            <c:forEach var="doctor" items="${doctorList}">
                                <option value="${doctor.id}">Bs. ${doctor.hoTen}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Hàm đẩy dữ liệu (ID và Tên) bệnh nhân vào Modal khi click nút Phân công
    function openAssignModal(patientId, patientName) {
        document.getElementById('inputPatientId').value = patientId;
        document.getElementById('displayPatientName').innerText = patientName;
    }
</script>
</body>
</html>
