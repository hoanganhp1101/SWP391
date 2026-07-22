<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <c:set var="ctx" value="${pageContext.request.contextPath}" />
        <c:set var="active" value="${param.activeMenu}" />

        <aside class="sidebar">
            <div class="profile-card">
                <div class="profile-avatar editable" title="Nhấn để cập nhật hồ sơ" data-open-profile-modal></div>
                <div class="profile-name">${patientInfo.hoTen != null ? patientInfo.hoTen : 'Bệnh nhân'}</div>
                <div class="profile-role">Bệnh nhân - ĐTĐ ${patientInfo.loaiTieuDuong != null ?
                    patientInfo.loaiTieuDuong : 'Type 2'}</div>
                <div class="profile-help-text">Nhấn vào ảnh đại diện để chỉnh sửa hồ sơ</div>
            </div>

            <nav class="sidebar-menu">
                <a href="${ctx}/patient-dashboard" class="menu-btn ${active == 'dashboard' ? 'active' : ''}">
                    <i class="fas fa-chart-pie"></i> Tổng quan
                </a>
                <a href="${ctx}/patient-medical-profile" class="menu-btn ${active == 'profile' ? 'active' : ''}">
                    <i class="fas fa-file-medical"></i> Xem bệnh án cá nhân
                </a>
                <a href="${ctx}/patient-appointments" class="menu-btn ${active == 'appointments' ? 'active' : ''}">
                    <i class="far fa-calendar-alt"></i> Xem lịch khám
                </a>
                <a href="${ctx}/patient-prescriptions" class="menu-btn ${active == 'prescriptions' ? 'active' : ''}">
                    <i class="fas fa-pills"></i> Đơn thuốc
                </a>
                <a href="${ctx}/patient-diet" class="menu-btn ${active == 'diet' ? 'active' : ''}">
                    <i class="fas fa-utensils"></i> Thực đơn AI
                </a>
                <a href="${ctx}/patient-dashboard#charts" class="menu-btn ${active == 'charts' ? 'active' : ''}">
                    <i class="fas fa-chart-line"></i> Biểu đồ tiến triển
                </a>
                <a href="${ctx}/patient-notifications" class="menu-btn ${active == 'notifications' ? 'active' : ''}">
                    <i class="fas fa-bell"></i> Lịch sử thông báo
                </a>
                <a href="${ctx}/patient-medical-history" class="menu-btn ${active == 'history' ? 'active' : ''}">
                    <i class="fas fa-file-pdf"></i> Lịch sử khám bệnh
                </a>
            </nav>

            <div class="sidebar-bottom">
                <c:if test="${active == 'dashboard'}">
                    <button type="button" class="btn-new"><i class="fas fa-plus"></i> Thêm bản ghi mới</button>
                </c:if>
                <a href="#" class="menu-btn"><i class="far fa-question-circle"></i> Hỗ trợ</a>
                <a href="${ctx}/logout" class="menu-btn"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
            </div>
        </aside>