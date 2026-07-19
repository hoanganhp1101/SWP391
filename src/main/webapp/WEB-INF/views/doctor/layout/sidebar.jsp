<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty doctor}">
    <c:set var="doctor" value="${sessionScope.user}"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<aside class="sidebar">
    <div class="doctor-profile">
        <img src="${not empty doctor.anhDaiDien ? doctor.anhDaiDien : 'https://i.pravatar.cc/60'}" alt="">
        <div>
            <h4>${not empty doctor.hoTen ? doctor.hoTen : 'Bác sĩ'}</h4>
            <p>${not empty doctor.vaiTro ? doctor.vaiTro : 'Bác sĩ điều trị'}</p>
        </div>
    </div>

    <nav class="menu">
        <a href="${ctx}/doctor-dashboard"
           class="menu-item ${activeMenu == 'dashboard' ? 'active' : ''}">
            <i class="fa-solid fa-table-cells"></i>
            <span>Tổng quan</span>
        </a>

        <a href="${ctx}/doctor/patient-list"
           class="menu-item ${activeMenu == 'patients' ? 'active' : ''}">
            <i class="fa-solid fa-users"></i>
            <span>Danh sách bệnh nhân</span>
        </a>

        <a class="menu-item ${activeMenu == 'alerts' ? 'active' : ''}">
            <i class="fa-regular fa-bell"></i>
            <span>Cảnh báo khẩn cấp</span>
        </a>

        <a href="${ctx}/doctor/patient-records"
           class="menu-item ${activeMenu == 'records' ? 'active' : ''}">
            <i class="fa-regular fa-clipboard"></i>
            <span>Hồ sơ khám bệnh</span>
        </a>

        <a href="${ctx}/doctor/appointments"
           class="menu-item ${activeMenu == 'appointments' ? 'active' : ''}">
            <i class="fa-solid fa-calendar-days"></i>
            <span>Quản lý lịch khám</span>
        </a>

        <a class="menu-item ${activeMenu == 'analytics' ? 'active' : ''}">
            <i class="fa-solid fa-chart-column"></i>
            <span>Phân tích dữ liệu</span>
        </a>
    </nav>

    <div class="sidebar-bottom">
        <form method="post" action="${ctx}/doctor/patient-records" style="margin:0;">
            <input type="hidden" name="action" value="add">
            <button type="submit" class="new-record">
                <i class="fa-solid fa-plus"></i>
                Tạo hồ sơ mới
            </button>
        </form>

        <a class="bottom-link" href="#">
            <i class="fa-regular fa-circle-question"></i>
            Hỗ trợ
        </a>

        <a class="bottom-link" href="${ctx}/Logincontroller?service=logout">
            <i class="fa-solid fa-arrow-right-from-bracket"></i>
            Đăng xuất
        </a>
    </div>
</aside>
