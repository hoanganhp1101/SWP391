<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty doctor}">
    <c:set var="doctor" value="${sessionScope.user}"/>
</c:if>
<c:if test="${empty activeTopNav}">
    <c:set var="activeTopNav" value="${activeMenu}"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<%-- Shell + filter dùng chung cho mọi trang doctor --%>
<link rel="stylesheet" href="${ctx}/assets/css/doctor-layout.css">
<link rel="stylesheet" href="${ctx}/assets/css/filters.css">
<script src="${ctx}/assets/js/filters.js" defer></script>

<header class="topbar">
    <div class="logo">HealthAlert</div>

    <div class="top-nav">
        <a href="${ctx}/doctor-dashboard"
           class="${activeTopNav == 'dashboard' ? 'active' : ''}">Bảng điều khiển</a>
        <a href="${ctx}/doctor/patient-list"
           class="${activeTopNav == 'patients' ? 'active' : ''}">Bệnh nhân</a>
        <a href="${ctx}/doctor/patient-records"
           class="${activeTopNav == 'records' ? 'active' : ''}">Hồ sơ</a>
        <a href="${ctx}/doctor/appointments"
           class="${activeTopNav == 'appointments' ? 'active' : ''}">Lịch khám</a>
        <a href="${ctx}/doctor/alerts"
           class="${activeTopNav == 'alerts' ? 'active' : ''}">Cảnh báo</a>
        <a href="${ctx}/doctor/analytics"
           class="${activeTopNav == 'analytics' ? 'active' : ''}">Phân tích</a>
    </div>

    <div class="top-actions">
        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" placeholder="Tìm kiếm hồ sơ y tế...">
        </div>
        <a href="${ctx}/doctor/alerts" title="Cảnh báo" class="icon-btn-link">
            <i class="fa-regular fa-bell icon-btn"></i>
        </a>
        <img class="topbar-avatar avatar"
             src="${not empty doctor.anhDaiDien ? doctor.anhDaiDien : 'https://i.pravatar.cc/40'}"
             alt="">
    </div>
</header>
