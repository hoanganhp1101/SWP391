<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Bảng Điều Khiển Admin | Diabetes Manage</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        .card-counter { box-shadow: 0 4px 8px rgba(0,0,0,0.1); border: none; border-radius: 10px; transition: transform 0.2s;}
        .card-counter:hover { transform: translateY(-5px); }
        .sidebar { min-height: 100vh; background: #212529; color: white; padding-top: 20px; }
        .nav-item .nav-link { color: #adb5bd; margin-bottom: 5px; }
        .nav-item .nav-link:hover, .nav-item .nav-link.active { color: #fff; background: rgba(255,255,255,0.1); border-radius: 5px; }
    </style>
</head>
<body class="bg-light">

<div class="container-fluid">
    <div class="row">
        <div class="col-md-2 sidebar d-none d-md-block px-3">
            <h5 class="text-center mb-4 text-uppercase font-weight-bold tracking-wide">Diabetes SysAdmin</h5>
            <ul class="nav flex-column">
                <li class="nav-item"><a class="nav-link active" href="#"><i class="fas fa-tachometer-alt me-2"></i> Dashboard</a></li>
                <hr class="text-secondary my-2">
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-users-cog me-2"></i> Quản lý tài khoản</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-user-md me-2"></i> Quản lý ca bác sĩ</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-pills me-2"></i> Danh mục thuốc</a></li>
                <hr class="text-secondary my-2">
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-brain me-2"></i> Cấu hình AI</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-check-double me-2"></i> Kiểm duyệt dữ liệu</a></li>
                <hr class="text-secondary my-2">
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-clipboard-list me-2"></i> Theo dõi Log</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-database me-2"></i> Sao lưu dữ liệu</a></li>
                <li class="nav-item"><a class="nav-link" href="#"><i class="fas fa-bullhorn me-2"></i> Gửi thông báo</a></li>
            </ul>
        </div>

        <div class="col-md-10 p-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="h3 text-gray-800">Tổng quan hệ thống</h2>
                <div>
                    <span class="badge bg-primary p-2 me-2"><i class="fas fa-shield-alt"></i> Phiên: Admin</span>
                    <button class="btn btn-outline-danger btn-sm"><i class="fas fa-sign-out-alt"></i> Đăng xuất</button>
                </div>
            </div>

            <div class="row mb-4">
                <div class="col-md-3">
                    <div class="card card-counter bg-info text-white p-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-uppercase mb-1">Tổng User (Hệ thống)</h6>
                                <h3 class="mb-0">${totalUsers}</h3>
                            </div>
                            <i class="fas fa-users fa-2x opacity-50"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card card-counter bg-success text-white p-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-uppercase mb-1">Dự đoán AI (Hôm nay)</h6>
                                <h3 class="mb-0">${aiPredictions}</h3>
                            </div>
                            <i class="fas fa-robot fa-2x opacity-50"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card card-counter bg-warning text-dark p-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-uppercase mb-1">Dữ liệu chờ duyệt</h6>
                                <h3 class="mb-0">${pendingModeration}</h3>
                            </div>
                            <i class="fas fa-tasks fa-2x opacity-50"></i>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card card-counter bg-danger text-white p-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-uppercase mb-1">Lỗi hệ thống (Log)</h6>
                                <h3 class="mb-0">${systemErrors}</h3>
                            </div>
                            <i class="fas fa-exclamation-triangle fa-2x opacity-50"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card shadow-sm border-0">
                <div class="card-header bg-white font-weight-bold d-flex justify-content-between align-items-center">
                    <span><i class="fas fa-list me-2 text-primary"></i>Theo dõi Log hệ thống gần đây</span>
                    <div>
                        <a href="ai-report" class="btn btn-sm btn-outline-success me-2"><i class="fas fa-robot"></i> Báo cáo AI</a>
                        <a href="#" class="btn btn-sm btn-primary">Xem tất cả báo cáo</a>
                    </div>
                </div>
                <div class="card-body p-0">
                    <table class="table table-hover mb-0">
                        <thead class="table-light">
                        <tr>
                            <th>Thời gian</th>
                            <th>Mức độ</th>
                            <th>Hành động / Sự kiện</th>
                            <th>Người thực hiện</th>
                            <th>IP</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            List<String[]> systemLogs = (List<String[]>) request.getAttribute("systemLogs");
                            if(systemLogs != null) {
                                for(String[] log : systemLogs) {
                        %>
                        <tr>
                            <td class="text-muted"><small><%= log[0] %></small></td>
                            <td>
                                <% if(log[1].equals("ERROR") || log[1].equals("CRITICAL")) { %>
                                <span class="badge bg-danger"><%= log[1] %></span>
                                <% } else if(log[1].equals("WARNING")) { %>
                                <span class="badge bg-warning text-dark"><%= log[1] %></span>
                                <% } else { %>
                                <span class="badge bg-success"><%= log[1] %></span>
                                <% } %>
                            </td>
                            <td><%= log[2] %></td>
                            <td><%= log[3] %></td>
                            <td class="text-muted"><small><%= log[4] %></small></td>
                        </tr>
                        <% } } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>