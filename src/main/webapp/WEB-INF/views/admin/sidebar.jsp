<%--
  Created by IntelliJ IDEA.
  User: nguye
  Date: 17/06/2026
  Time: 8:57 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<aside class="sidebar">
    <div class="user-profile-sm">
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.loginUser ? sessionScope.loginUser.hoTen : 'User'}&background=1e293b&color=fff" alt="Ảnh đại diện">
        <div>
            <div class="name">
                <c:out value="${not empty sessionScope.adminUser.hoTen ? sessionScope.adminUser.hoTen : 'Admin'}"/>
            </div>
            <div class="role text-capitalize">
                <c:choose>
                    <c:when test="${sessionScope.loginUser.vaiTro == 'bac_si'}">Bác sĩ</c:when>
                    <c:when test="${sessionScope.loginUser.vaiTro == 'quan_tri_vien'}">Quản trị viên</c:when>
                    <c:otherwise><c:out value="${sessionScope.loginUser.vaiTro}"/></c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <ul class="sidebar-menu">
        <li><a href="${pageContext.request.contextPath}/dashboard" class="${param.activeMenu == 'dashboard' ? 'active' : ''}"><i class="fas fa-th-large"></i> Tổng quan</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/users" class="${param.activeMenu == 'users' ? 'active' : ''}"><i class="fas fa-users-cog"></i> Quản lý người dùng</a></li>
        <li><a href="${pageContext.request.contextPath}/patient-manager" class="${param.activeMenu == 'patients' ? 'active' : ''}"><i class="fas fa-user-injured"></i> Danh sách bệnh nhân</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/patient-assignments" class="${param.activeMenu == 'assignments' ? 'active' : ''}"><i class="fas fa-user-md"></i> Gán bác sĩ phụ trách</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/high-risk-patients" class="${param.activeMenu == 'high-risk-patients' ? 'active' : ''}"><i class="fas fa-triangle-exclamation"></i> Bệnh nhân nguy cơ cao</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/reports" class="${param.activeMenu == 'reports' ? 'active' : ''}"><i class="fas fa-chart-line"></i> Báo cáo hệ thống</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/core-medical-data" class="${param.activeMenu == 'core-medical-data' ? 'active' : ''}"><i class="fas fa-database"></i> Dữ liệu y khoa lõi</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/educational-content" class="${param.activeMenu == 'educational-content' ? 'active' : ''}"><i class="fas fa-book-medical"></i> Nội dung giáo dục</a></li>

        <li><a href="${pageContext.request.contextPath}/admin/master-foods" class="${param.activeMenu == 'master-foods' ? 'active' : ''}"><i class="fas fa-apple-alt"></i> Dữ liệu Thực phẩm</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/master-medications" class="${param.activeMenu == 'master-medications' ? 'active' : ''}"><i class="fas fa-pills"></i> Dữ liệu Thuốc</a></li>
    </ul>
</aside>
