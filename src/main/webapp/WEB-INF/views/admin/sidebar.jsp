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
        <img src="https://ui-avatars.com/api/?name=${not empty sessionScope.loginUser ? sessionScope.loginUser.hoTen : 'User'}&background=1e293b&color=fff" alt="User Avatar">
        <div>
            <div class="name">
                <c:out value="${not empty sessionScope.loginUser.hoTen ? sessionScope.loginUser.hoTen : 'Guest'}"/>
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

    <div class="px-3 pb-3">
        <button class="btn btn-primary w-100 rounded-1" style="background-color: var(--primary-blue); border:none;">
            <i class="fas fa-plus me-1"></i> New Patient
        </button>
    </div>

    <ul class="sidebar-menu">
        <li><a href="${pageContext.request.contextPath}/dashboard" class="${param.activeMenu == 'dashboard' ? 'active' : ''}"><i class="fas fa-th-large"></i> Overview</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/users" class="${param.activeMenu == 'users' ? 'active' : ''}"><i class="fas fa-users-cog"></i> User Management</a></li>
        <li><a href="${pageContext.request.contextPath}/patient-manager" class="${param.activeMenu == 'patients' ? 'active' : ''}"><i class="fas fa-user-injured"></i> Patient List</a></li>
    </ul>

    <div class="sidebar-footer">
        <ul class="sidebar-menu p-0 m-0">
            <li><a href="#"><i class="far fa-question-circle"></i> Support</a></li>
            <li><a href="${pageContext.request.contextPath}/logout"><i class="fas fa-sign-out-alt"></i> Sign Out</a></li>
        </ul>
    </div>
</aside>