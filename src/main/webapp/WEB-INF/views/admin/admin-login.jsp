<%--
  Created by IntelliJ IDEA.
  User: nguye
  Date: 23/06/2026
  Time: 8:42 CH
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Portal | HealthAlert</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body {
            background-color: #f8fafc;
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Inter', sans-serif;
        }
        .login-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.05);
            width: 100%;
            max-width: 420px;
            overflow: hidden;
        }
        .login-header {
            background: var(--primary-blue, #0d6efd);
            padding: 2rem 1.5rem;
            text-align: center;
            color: white;
        }
        #togglePassword {
            border-left: none;
            transition: all 0.2s;
        }
        #togglePassword:hover {
            color: #0d6efd !important;
        }
    </style>
</head>
<body>

<div class="login-card">
    <div class="login-header">
        <i class="fas fa-shield-alt fa-3x mb-3 opacity-75"></i>
        <h3 class="fw-bold mb-0">HealthAlert Admin</h3>
        <p class="mb-0 mt-1 small opacity-75">System Management Portal</p>
    </div>
    <div class="p-4 p-md-5">

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger d-flex align-items-center py-2 px-3 small rounded-3" role="alert">
                <i class="fas fa-exclamation-circle me-2"></i>
                <div><c:out value="${errorMessage}"/></div>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/admin/login" method="post">

            <div class="mb-3">
                <label class="form-label text-muted fw-bold small">Email / Tài khoản</label>
                <div class="input-group">
                    <span class="input-group-text bg-light border-end-0 text-muted"><i class="fas fa-envelope"></i></span>
                    <input type="text" name="email" class="form-control border-start-0 ps-0 shadow-none bg-light"
                           placeholder="admin@healthalert.vn" value="${cookie.adminEmail.value}" required>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label text-muted fw-bold small">Mật khẩu</label>
                <div class="input-group">
                    <span class="input-group-text bg-light border-end-0 text-muted"><i class="fas fa-lock"></i></span>

                    <%-- Thêm id="loginPassword" và bỏ border-end để liền mạch với nút mắt --%>
                    <input type="password" name="password" id="loginPassword" class="form-control border-start-0 border-end-0 ps-0 shadow-none bg-light"
                           placeholder="••••••••" value="${cookie.adminPass.value}" required>

                    <button class="btn bg-light border border-start-0 text-muted" type="button" id="togglePassword" style="z-index: 10;">
                        <i class="fas fa-eye" id="eyeIcon"></i>
                    </button>
                </div>
            </div>

            <div class="mb-4 form-check">
                <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe" value="ON"
                ${not empty cookie.adminEmail.value ? 'checked' : ''}>
                <label class="form-check-label text-muted small" for="rememberMe">Ghi nhớ đăng nhập</label>
            </div>

            <button type="submit" class="btn btn-primary w-100 fw-bold py-2 rounded-3" style="background-color: var(--primary-blue, #0d6efd); border:none;">
                Đăng Nhập Quản Trị
            </button>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        const togglePassword = document.getElementById('togglePassword');
        const passwordInput = document.getElementById('loginPassword');
        const eyeIcon = document.getElementById('eyeIcon');

        togglePassword.addEventListener('click', function () {
            // Kiểm tra xem type hiện tại đang là password hay text
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // Đổi icon tương ứng khi ẩn/hiện
            if (type === 'text') {
                eyeIcon.classList.remove('fa-eye');
                eyeIcon.classList.add('fa-eye-slash');
            } else {
                eyeIcon.classList.remove('fa-eye-slash');
                eyeIcon.classList.add('fa-eye');
            }
        });
    });
</script>
</body>
</html>