<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan sức khỏe - DiabCare</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <style>
        :root {
            --primary: #0a4aa8;
            --primary-light: #e6effc;
            --text-dark: #1e293b;
            --text-muted: #64748b;
            --bg-body: #f8fafc;
            --bg-white: #ffffff;
            --border: #e2e8f0;
            --danger: #ef4444;
            --danger-light: #fee2e2;
            --success: #10b981;
            --success-light: #d1fae5;
            --warning: #f59e0b;
            --warning-light: #fef3c7;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', sans-serif;
        }

        body {
            background-color: var(--bg-body);
            color: var(--text-dark);
        }

        /* Top Navigation */
        .top-nav {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background-color: var(--bg-white);
            border-bottom: 1px solid var(--border);
            padding: 0 2rem;
            height: 64px;
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 100;
        }

        .nav-left {
            display: flex;
            align-items: center;
            gap: 2rem;
        }

        .logo {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--primary);
        }

        .nav-links {
            display: flex;
            gap: 1.5rem;
        }

        .nav-links a {
            text-decoration: none;
            color: var(--text-muted);
            font-weight: 500;
            font-size: 0.875rem;
            padding: 1.25rem 0;
            position: relative;
        }

        .nav-links a.active {
            color: var(--primary);
        }

        .nav-links a.active::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 2px;
            background-color: var(--primary);
        }

        .nav-right {
            display: flex;
            align-items: center;
            gap: 1.5rem;
            color: var(--text-muted);
        }

        .nav-right i {
            cursor: pointer;
            font-size: 1.125rem;
        }

        .avatar-small {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background-color: #cbd5e1;
            background-image: url('https://ui-avatars.com/api/?name=${patientInfo.hoTen}&background=0D8ABC&color=fff');
            background-size: cover;
        }

        /* Main Layout */
        .app-container {
            display: flex;
            margin-top: 64px;
            min-height: calc(100vh - 64px);
        }

        /* Left Sidebar */
        .sidebar {
            width: 280px;
            background-color: var(--bg-white);
            border-right: 1px solid var(--border);
            padding: 2rem 1.5rem;
            display: flex;
            flex-direction: column;
            position: fixed;
            top: 64px;
            bottom: 0;
            overflow-y: auto;
        }

        .profile-card {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            margin-bottom: 2rem;
            padding-bottom: 2rem;
            border-bottom: 1px solid var(--border);
        }

        .profile-avatar {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            margin-bottom: 1rem;
            background-color: #cbd5e1;
            background-image: url('https://ui-avatars.com/api/?name=${patientInfo.hoTen}&background=0D8ABC&color=fff');
            background-size: cover;
        }

        .profile-name {
            font-weight: 600;
            font-size: 1.125rem;
            color: var(--text-dark);
        }

        .profile-role {
            font-size: 0.875rem;
            color: var(--text-muted);
        }

        .sidebar-menu {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
            flex-grow: 1;
        }

        .menu-btn {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 0.75rem 1rem;
            border-radius: 8px;
            color: var(--text-muted);
            text-decoration: none;
            font-weight: 500;
            font-size: 0.875rem;
            transition: all 0.2s;
            border: none;
            background: none;
            width: 100%;
            cursor: pointer;
            text-align: left;
        }

        .menu-btn i {
            width: 20px;
            text-align: center;
            font-size: 1rem;
        }

        .menu-btn:hover {
            background-color: var(--bg-body);
        }

        .menu-btn.active {
            background-color: var(--primary);
            color: var(--bg-white);
        }

        .sidebar-bottom {
            margin-top: auto;
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }

        .btn-new {
            background-color: var(--primary);
            color: white;
            border: none;
            border-radius: 8px;
            padding: 0.75rem;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            transition: background-color 0.2s;
        }

        .btn-new:hover {
            background-color: #083c8a;
        }

        /* Main Content */
        .content {
            margin-left: 280px;
            padding: 2rem;
            flex-grow: 1;
            width: calc(100% - 280px);
        }

        .page-title {
            font-size: 1.5rem;
            font-weight: 700;
            margin-bottom: 1.5rem;
        }

        /* Cards Row 1 */
        .row-top {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr;
            gap: 1.5rem;
            margin-bottom: 1.5rem;
        }

        .metric-card {
            background-color: var(--bg-white);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 1.5rem;
            position: relative;
        }

        .metric-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }

        .metric-title {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--text-muted);
            text-transform: uppercase;
        }

        .metric-title.red i { color: var(--danger); }
        .metric-title.brown i { color: #8b5a2b; }

        .badge {
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: 600;
        }

        .badge.success { background-color: var(--success-light); color: var(--success); }
        .badge.warning { background-color: var(--warning-light); color: var(--warning); }
        .badge.danger { background-color: var(--danger-light); color: var(--danger); }

        .metric-value {
            font-size: 2.5rem;
            font-weight: 700;
            display: flex;
            align-items: baseline;
            gap: 0.25rem;
        }

        .metric-unit {
            font-size: 1rem;
            font-weight: 500;
            color: var(--text-muted);
        }

        .metric-desc {
            font-size: 0.875rem;
            color: var(--danger);
            font-weight: 500;
            margin-top: 0.5rem;
        }

        .metric-card.red-border {
            border-left: 4px solid var(--danger);
        }
        
        .progress-bar-bg {
            height: 16px; 
            background: #e2e8f0; 
            border-radius: 8px; 
            margin-top: 1rem;
            overflow: hidden;
            display: flex;
        }
        
        .progress-bar-fill {
            height: 100%;
            width: 70%;
            background-color: #93c5fd;
        }

        .alerts-card {
            background-color: var(--bg-white);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 1.5rem;
        }

        .alerts-title {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 1rem;
            font-weight: 600;
            margin-bottom: 1rem;
        }

        .alert-item {
            display: flex;
            gap: 1rem;
            margin-bottom: 1rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--border);
        }

        .alert-item:last-child {
            margin-bottom: 0;
            padding-bottom: 0;
            border-bottom: none;
        }

        .alert-icon {
            font-size: 1.25rem;
        }
        .alert-icon.danger { color: var(--danger); }
        .alert-icon.muted { color: var(--text-muted); }

        .alert-content p {
            font-size: 0.875rem;
            font-weight: 600;
            margin-bottom: 0.25rem;
        }

        .alert-content span {
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        /* Middle Row */
        .row-middle {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 1.5rem;
            margin-bottom: 1.5rem;
        }

        .card {
            background-color: var(--bg-white);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 1.5rem;
        }

        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 1.5rem;
        }

        .card-title h3 {
            font-size: 1.125rem;
            font-weight: 600;
        }

        .card-title p {
            font-size: 0.875rem;
            color: var(--text-muted);
            margin-top: 0.25rem;
        }

        .chart-controls {
            display: flex;
            gap: 0.5rem;
            align-items: center;
        }

        .chart-controls button {
            padding: 0.25rem 0.75rem;
            border: 1px solid var(--border);
            background: var(--bg-white);
            border-radius: 16px;
            font-size: 0.75rem;
            font-weight: 500;
            cursor: pointer;
        }
        .chart-controls button.active {
            background-color: var(--primary);
            color: white;
            border-color: var(--primary);
        }

        .date-picker {
            padding: 0.25rem 0.5rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 0.75rem;
            color: var(--text-muted);
            outline: none;
            cursor: pointer;
            background: var(--bg-white);
        }
        
        .date-picker:focus {
            border-color: var(--primary);
        }

        .chart-container {
            height: 300px;
            position: relative;
        }

        .apt-item {
            display: flex;
            gap: 1rem;
            margin-bottom: 1rem;
            background-color: var(--bg-body);
            padding: 0.75rem;
            border-radius: 8px;
        }

        .apt-date {
            background-color: #e2e8f0;
            border-radius: 8px;
            padding: 0.5rem;
            text-align: center;
            min-width: 60px;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .apt-date span:first-child {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--text-dark);
        }

        .apt-date span:last-child {
            font-size: 0.75rem;
            font-weight: 600;
            color: var(--text-muted);
            text-transform: uppercase;
        }

        .apt-info h4 {
            font-size: 0.875rem;
            font-weight: 600;
            margin-bottom: 0.25rem;
        }

        .apt-info p {
            font-size: 0.75rem;
            color: var(--text-muted);
            display: flex;
            align-items: center;
            gap: 0.25rem;
            margin-bottom: 0.125rem;
        }

        .btn-outline {
            display: block;
            width: 100%;
            padding: 0.75rem;
            border: 1px solid var(--primary);
            background: transparent;
            color: var(--primary);
            border-radius: 8px;
            font-weight: 600;
            text-align: center;
            text-decoration: none;
            margin-top: 1rem;
            cursor: pointer;
            transition: all 0.2s;
        }
        .btn-outline:hover {
            background-color: var(--primary-light);
        }

        /* Table Section */
        .table-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1.5rem;
        }

        .search-box {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.5rem 1rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            background-color: var(--bg-white);
            width: 250px;
        }

        .search-box input {
            border: none;
            outline: none;
            font-size: 0.875rem;
            width: 100%;
        }
        
        .filter-icon {
            padding: 0.5rem 0.75rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            cursor: pointer;
            color: var(--text-muted);
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid var(--border);
        }

        th {
            font-size: 0.75rem;
            font-weight: 600;
            color: var(--text-muted);
            text-transform: uppercase;
        }

        td {
            font-size: 0.875rem;
        }

        .record-type {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            color: var(--primary);
            font-weight: 500;
        }

        .status-badge {
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: 600;
            background-color: #e2e8f0;
            color: var(--text-muted);
        }
        
        .status-badge.action {
            background-color: var(--primary-light);
            color: var(--primary);
        }

        .action-link {
            color: var(--primary);
            text-decoration: none;
            font-weight: 600;
            font-size: 0.875rem;
        }

        /* Footer */
        .footer {
            margin-top: 2rem;
            padding-top: 1rem;
            border-top: 1px solid var(--border);
            display: flex;
            justify-content: space-between;
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        .footer-links {
            display: flex;
            gap: 1rem;
        }
        .footer-links a {
            color: var(--text-muted);
            text-decoration: none;
        }
        
        /* Modal Styles */
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.3s;
        }

        .modal-overlay.active {
            opacity: 1;
            pointer-events: auto;
        }

        .modal {
            background: var(--bg-white);
            border-radius: 12px;
            width: 100%;
            max-width: 500px;
            padding: 2rem;
            box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
            transform: translateY(-20px);
            transition: transform 0.3s;
        }

        .modal-overlay.active .modal {
            transform: translateY(0);
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1.5rem;
        }

        .modal-title {
            font-size: 1.25rem;
            font-weight: 600;
        }

        .close-btn {
            background: none;
            border: none;
            font-size: 1.25rem;
            cursor: pointer;
            color: var(--text-muted);
        }

        .form-group {
            margin-bottom: 1rem;
        }

        .form-group label {
            display: block;
            font-size: 0.875rem;
            font-weight: 500;
            margin-bottom: 0.5rem;
            color: var(--text-dark);
        }

        .form-control {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 1px solid var(--border);
            border-radius: 8px;
            font-size: 0.875rem;
            outline: none;
        }

        .form-control:focus {
            border-color: var(--primary);
        }

        .form-row {
            display: flex;
            gap: 1rem;
        }

        .form-row .form-group {
            flex: 1;
        }

        .modal-footer {
            margin-top: 2rem;
            display: flex;
            justify-content: flex-end;
            gap: 1rem;
        }
        
        .btn {
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            border: none;
            font-size: 0.875rem;
        }

        .btn-cancel {
            background: var(--bg-body);
            color: var(--text-dark);
        }

        .btn-save {
            background: var(--primary);
            color: white;
        }
    </style>
</head>
<body>

    <!-- Top Navigation -->
    <nav class="top-nav">
        <div class="nav-left">
            <div class="logo">HealthAlert</div>
            <div class="nav-links">
                <a href="#" class="active">Tổng quan</a>
                <a href="#">Hồ sơ sức khỏe</a>
                <a href="#">Lịch hẹn</a>
                <a href="#">Báo cáo</a>
            </div>
        </div>
        <div class="nav-right">
            <i class="far fa-bell"></i>
            <i class="fas fa-cog"></i>
            <div class="avatar-small"></div>
        </div>
    </nav>

    <div class="app-container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="profile-card">
                <div class="profile-avatar"></div>
                <div class="profile-name">${patientInfo.hoTen != null ? patientInfo.hoTen : 'Bệnh nhân'}</div>
                <div class="profile-role">Bệnh nhân - ĐTĐ ${patientInfo.loaiTieuDuong != null ? patientInfo.loaiTieuDuong : 'Type 2'}</div>
            </div>

            <nav class="sidebar-menu">
                <a href="#" class="menu-btn active"><i class="fas fa-file-medical"></i> Xem bệnh án cá nhân</a>
                <a href="#" class="menu-btn"><i class="far fa-calendar-alt"></i> Xem lịch khám</a>
                <a href="#" class="menu-btn"><i class="fas fa-pills"></i> Đơn thuốc</a>
                <a href="#" class="menu-btn"><i class="fas fa-chart-line"></i> Biểu đồ tiến triển</a>
                <a href="#" class="menu-btn"><i class="fas fa-history"></i> Lịch sử cảnh báo</a>
            </nav>

            <div class="sidebar-bottom">
                <button class="btn-new"><i class="fas fa-plus"></i> Thêm bản ghi mới</button>
                <a href="#" class="menu-btn"><i class="far fa-question-circle"></i> Hỗ trợ</a>
                <a href="#" class="menu-btn"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
            </div>
        </aside>

        <!-- Main Content -->
        <main class="content">
            <h1 class="page-title">Tổng quan sức khỏe</h1>

            <!-- Top Cards -->
            <div class="row-top">
                <!-- Heart Rate -->
                <div class="metric-card">
                    <div class="metric-header">
                        <div class="metric-title red"><i class="far fa-heart"></i> NHỊP TIM</div>
                        <span class="badge success">BÌNH THƯỜNG</span>
                    </div>
                    <div class="metric-value">
                        ${latestHeartRate != null ? latestHeartRate : '--'} <span class="metric-unit">BPM</span>
                    </div>
                    <div class="progress-bar-bg">
                        <div class="progress-bar-fill"></div>
                    </div>
                </div>

                <!-- Blood Pressure -->
                <div class="metric-card red-border">
                    <div class="metric-header">
                        <div class="metric-title brown"><i class="fas fa-stethoscope"></i> HUYẾT ÁP</div>
                        <span class="badge danger">CẢNH BÁO</span>
                    </div>
                    <div class="metric-value">
                        ${latestSystolic != null ? latestSystolic : '--'}/${latestDiastolic != null ? latestDiastolic : '--'} <span class="metric-unit">mmHg</span>
                    </div>
                    <div class="metric-desc">Cao hơn so với mức cơ sở.</div>
                </div>

                <!-- Recent Alerts -->
                <div class="alerts-card">
                    <div class="alerts-title">
                        <i class="far fa-bell" style="color: var(--danger);"></i> Cảnh báo gần đây
                    </div>
                    <c:choose>
                        <c:when test="${not empty alerts}">
                            <c:forEach var="alert" items="${alerts}">
                                <div class="alert-item">
                                    <div class="alert-icon ${alert.mucDo == 'nguy_hiem' || alert.mucDo == 'cao' ? 'danger' : 'muted'}">
                                        <i class="fas ${alert.mucDo == 'nguy_hiem' || alert.mucDo == 'cao' ? 'fa-exclamation-triangle' : 'fa-info-circle'}"></i>
                                    </div>
                                    <div class="alert-content">
                                        <p>${alert.tieuDe}</p>
                                        <span>${alert.noiDung}</span>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="alert-item">
                                <div class="alert-content"><p>Không có cảnh báo nào gần đây.</p></div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Middle Cards -->
            <div class="row-middle">
                <!-- Health Trends Chart -->
                <div class="card">
                    <div class="card-header">
                        <div class="card-title">
                            <h3>Biểu đồ theo dõi chỉ số</h3>
                            <p>Chi tiết các lần đo: Đường huyết, Nhịp tim, Huyết áp</p>
                        </div>
                        <div class="chart-controls">
                            <button class="active" id="btn-7days">7 Ngày</button>
                            <button id="btn-30days">30 Ngày</button>
                            <input type="date" class="date-picker" id="datePicker" title="Chọn ngày cụ thể">
                        </div>
                    </div>
                    <div class="chart-container" style="height: 350px;">
                        <canvas id="trendsChart"></canvas>
                    </div>
                </div>

                <!-- Appointments -->
                <div class="card">
                    <div class="card-header" style="margin-bottom: 1rem;">
                        <div class="card-title">
                            <h3><i class="far fa-calendar-alt" style="color: var(--primary);"></i> Lịch hẹn</h3>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${not empty appointments}">
                            <c:forEach var="appt" items="${appointments}">
                                <div class="apt-item">
                                    <div class="apt-date">
                                        <span>${appt.thoiGianHen.toString().substring(8,10)}</span>
                                        <span>THG ${appt.thoiGianHen.toString().substring(5,7)}</span>
                                    </div>
                                    <div class="apt-info">
                                        <h4>${appt.tieuDe}</h4>
                                        <p><i class="far fa-clock"></i> ${appt.thoiGianHen.toString().substring(11, 16)}</p>
                                        <p><i class="fas fa-map-marker-alt"></i> ${appt.diaDiem}</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="padding: 1rem; color: var(--text-muted);">Không có lịch hẹn nào sắp tới.</p>
                        </c:otherwise>
                    </c:choose>

                    <button class="btn-outline">Xem toàn bộ lịch</button>
                </div>
            </div>

            <!-- Bottom Table -->
            <div class="card">
                <div class="table-header">
                    <h3>Lịch sử khám bệnh</h3>
                    <div style="display: flex; gap: 0.5rem;">
                        <div class="search-box">
                            <i class="fas fa-search" style="color: var(--text-muted);"></i>
                            <input type="text" placeholder="Tìm kiếm hồ sơ...">
                        </div>
                        <div class="filter-icon">
                            <i class="fas fa-filter"></i>
                        </div>
                    </div>
                </div>
                
                <table>
                    <thead>
                        <tr>
                            <th>NGÀY</th>
                            <th>LOẠI HỒ SƠ</th>
                            <th>BÁC SĨ</th>
                            <th>TRẠNG THÁI</th>
                            <th>THAO TÁC</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty medicalDocuments}">
                                <c:forEach var="doc" items="${medicalDocuments}">
                                    <tr>
                                        <td>${doc.ngayThucHien}</td>
                                        <td>
                                            <div class="record-type">
                                                <i class="far fa-file-pdf"></i> ${doc.loaiTaiLieu}
                                            </div>
                                        </td>
                                        <td>Bác sĩ</td>
                                        <td>
                                            <span class="status-badge ${doc.trangThai == 'hoan_thanh' ? '' : 'action'}">
                                                ${doc.trangThai == 'hoan_thanh' ? 'HOÀN THÀNH' : (doc.trangThai == 'can_xu_ly' ? 'CẦN XỬ LÝ' : 'ĐÃ HỦY')}
                                            </span>
                                        </td>
                                        <td><a href="#" class="action-link">Chi tiết</a></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align: center;">Không có tài liệu nào.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- Footer -->
            <div class="footer">
                <div>© 2024 DiabCare. All rights reserved. Confidential Medical Data.</div>
                <div class="footer-links">
                    <a href="#">Chính sách bảo mật</a>
                    <a href="#">Điều khoản dịch vụ</a>
                    <a href="#">Tuân thủ HIPAA</a>
                    <a href="#">Liên hệ hỗ trợ</a>
                </div>
            </div>

        </main>
    </div>

    <!-- Modal Thêm bản ghi mới -->
    <div class="modal-overlay" id="recordModal">
        <div class="modal">
            <div class="modal-header">
                <h3 class="modal-title">Ghi chỉ số sức khỏe</h3>
                <button class="close-btn" id="closeModalBtn"><i class="fas fa-times"></i></button>
            </div>
            <form action="logData" method="POST">
                <div class="form-row">
                    <div class="form-group">
                        <label>Đường huyết (mg/dL)</label>
                        <input type="number" step="0.1" name="duong_huyet" class="form-control" placeholder="VD: 110">
                    </div>
                    <div class="form-group">
                        <label>Nhịp tim (BPM)</label>
                        <input type="number" name="nhip_tim" class="form-control" placeholder="VD: 75">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>H/áp Tâm thu (mmHg)</label>
                        <input type="number" name="huyet_ap_thu" class="form-control" placeholder="VD: 120">
                    </div>
                    <div class="form-group">
                        <label>H/áp Tâm trương (mmHg)</label>
                        <input type="number" name="huyet_ap_truong" class="form-control" placeholder="VD: 80">
                    </div>
                </div>
                <div class="form-group">
                    <label>Thời điểm đo</label>
                    <select class="form-control" name="thoi_diem">
                        <option value="luc_doi">Lúc đói (Sáng sớm)</option>
                        <option value="sau_an_1h">Sau ăn 1 giờ</option>
                        <option value="sau_an_2h">Sau ăn 2 giờ</option>
                        <option value="truoc_ngu">Trước khi ngủ</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Ghi chú thêm</label>
                    <textarea class="form-control" name="ghi_chu" rows="2" placeholder="Cảm thấy mệt mỏi, vừa tập thể dục xong..."></textarea>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-cancel" id="cancelModalBtn">Hủy</button>
                    <button type="submit" class="btn btn-save">Lưu bản ghi</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        // Chart.js configuration for Health Trends
        const ctx = document.getElementById('trendsChart').getContext('2d');
        
        // Dữ liệu thật từ backend (DB)
        const dbData = ${chartDataJson != null ? chartDataJson : '[]'};

        function processData(dataList) {
            const result = {
                labels: [],
                glucose: [],
                heartRate: [],
                systolic: [],
                diastolic: []
            };
            dataList.forEach(item => {
                if(item.time) {
                    const d = new Date(item.time);
                    result.labels.push(d.toLocaleDateString('vi-VN', {day:'2-digit', month:'2-digit'}) + ' ' + d.getHours() + 'h');
                } else {
                    result.labels.push('');
                }
                // Nếu null sẽ vẽ đứt quãng hoặc không vẽ
                result.glucose.push(item.glucose);
                result.heartRate.push(item.hr);
                result.systolic.push(item.sys);
                result.diastolic.push(item.dia);
            });
            return result;
        }

        const realChartData = processData(dbData);

        let trendsChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: realChartData.labels,
                datasets: [
                    {
                        label: 'Đường huyết (mg/dL)',
                        data: realChartData.glucose,
                        borderColor: '#0a4aa8', // Primary blue
                        backgroundColor: '#0a4aa8',
                        spanGaps: true, // Nối liền các điểm null
                        tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                    },
                    {
                        label: 'Nhịp tim (BPM)',
                        data: realChartData.heartRate,
                        borderColor: '#ef4444', // Danger red
                        backgroundColor: '#ef4444',
                        spanGaps: true,
                        tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                    },
                    {
                        label: 'Huyết áp tâm thu (mmHg)',
                        data: realChartData.systolic,
                        borderColor: '#f59e0b', // Warning orange
                        backgroundColor: '#f59e0b',
                        spanGaps: true,
                        borderDash: [5, 5],
                        tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                    },
                    {
                        label: 'Huyết áp tâm trương (mmHg)',
                        data: realChartData.diastolic,
                        borderColor: '#10b981', // Success green
                        backgroundColor: '#10b981',
                        spanGaps: true,
                        borderDash: [5, 5],
                        tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            boxWidth: 8
                        }
                    },
                    tooltip: {
                        enabled: true,
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false,
                            drawBorder: false
                        },
                        ticks: {
                            display: true,
                            font: { size: 11 }
                        }
                    },
                    y: {
                        grid: {
                            color: '#e2e8f0',
                            borderDash: [3, 3]
                        },
                        ticks: {
                            display: true,
                            stepSize: 20
                        },
                        min: 40,
                        max: 180
                    }
                }
            }
        });

        // Interactivity for controls
        const btn7 = document.getElementById('btn-7days');
        const btn30 = document.getElementById('btn-30days');
        const datePicker = document.getElementById('datePicker');

        function updateChart(dataset) {
            trendsChart.data.labels = dataset.labels;
            trendsChart.data.datasets[0].data = dataset.glucose;
            trendsChart.data.datasets[1].data = dataset.heartRate;
            trendsChart.data.datasets[2].data = dataset.systolic;
            trendsChart.data.datasets[3].data = dataset.diastolic;
            trendsChart.update();
        }

        btn7.addEventListener('click', () => {
            btn7.classList.add('active');
            btn30.classList.remove('active');
            datePicker.value = '';
            // updateChart(realChartData); // For now realChartData has all points
        });

        btn30.addEventListener('click', () => {
            btn30.classList.add('active');
            btn7.classList.remove('active');
            datePicker.value = '';
            // updateChart(realChartData);
        });

        datePicker.addEventListener('change', (e) => {
            if(e.target.value) {
                btn7.classList.remove('active');
                btn30.classList.remove('active');
                // Lọc dữ liệu theo ngày trong tương lai nếu muốn
            }
        });

        // Modal Logic
        const recordModal = document.getElementById('recordModal');
        const btnNewRecord = document.querySelector('.btn-new');
        const closeModalBtn = document.getElementById('closeModalBtn');
        const cancelModalBtn = document.getElementById('cancelModalBtn');

        function openModal() {
            recordModal.classList.add('active');
        }

        function closeModal() {
            recordModal.classList.remove('active');
        }

        if (btnNewRecord) btnNewRecord.addEventListener('click', openModal);
        if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
        if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeModal);
        
        // Đóng modal khi bấm ra ngoài
        recordModal.addEventListener('click', (e) => {
            if (e.target === recordModal) {
                closeModal();
            }
        });
    </script>
</body>
</html>
