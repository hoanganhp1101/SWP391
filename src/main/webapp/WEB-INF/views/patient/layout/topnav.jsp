<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeTop" value="${param.activeTop}"/>
<c:set var="avatarUrl" value="${not empty patientInfo.anhDaiDien ? patientInfo.anhDaiDien : 'https://ui-avatars.com/api/?name=Benh+Nhan&background=0D8ABC&color=fff'}"/>

<nav class="top-nav">
    <div class="nav-left">
        <a class="logo" href="${ctx}/patient-dashboard">DiabCare</a>
        <div class="nav-links">
            <a href="${ctx}/patient-dashboard"
               class="${activeTop == 'dashboard' ? 'active' : ''}">Bảng điều khiển</a>
            <a href="${ctx}/patient-medical-profile"
               class="${activeTop == 'profile' ? 'active' : ''}">Hồ sơ y tế</a>
            <a href="${ctx}/patient-appointments"
               class="${activeTop == 'appointments' ? 'active' : ''}">Lịch khám</a>
            <a href="${ctx}/patient-iot"
               class="${activeTop == 'iot' ? 'active' : ''}">IoT mô phỏng</a>
            <a href="${ctx}/patient-diet"
               class="${activeTop == 'diet' ? 'active' : ''}">Thực đơn</a>
        </div>
    </div>
    <div class="nav-right">
        <jsp:include page="../notifications.jsp"/>
        <a href="${ctx}/ai-chat" title="Chat AI" style="color:inherit"><i class="fa-regular fa-comments"></i></a>
        <a href="#" class="avatar-link" data-open-profile-modal title="Hồ sơ">
            <div class="avatar-small editable" style="background-image:url('${avatarUrl}')"></div>
        </a>
    </div>
</nav>
