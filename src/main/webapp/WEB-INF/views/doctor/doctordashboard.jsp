<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
            /* =========================
               GLOBAL
            ========================= */

        *{
            margin:0;
            padding:0;
            box-sizing:border-box;
            font-family:Inter, sans-serif;
        }

        body{
            background:#f5f7fb;
        }

        /* =========================
           LAYOUT
        ========================= */

        .layout{
            display:flex;
            height:calc(100vh - 80px);
        }

        .main-content{
            flex:1;
            background:#f5f7fb;
            padding:28px;
            overflow:auto;
        }

        /* =========================
           TOPBAR
        ========================= */

        .topbar{
            height:80px;
            background:#fff;
            display:flex;
            align-items:center;
            padding:0 48px;
            border-bottom:1px solid #e5e7eb;
        }

        .logo{
            font-size:20px;
            font-weight:700;
            color:#0d4bb5;
        }

        .top-nav{
            display:flex;
            gap:36px;
            margin-left:40px;
        }

        .top-nav a{
            color:#555;
            text-decoration:none;
        }

        .top-nav .active{
            color:#1557d5;
            font-weight:600;
            position:relative;
        }

        .top-nav .active::after{
            content:"";
            position:absolute;
            left:0;
            bottom:-28px;
            width:100%;
            height:3px;
            background:#1557d5;
        }

        .top-actions{
            margin-left:auto;
            display:flex;
            align-items:center;
            gap:22px;
        }

        .search-box{
            width:290px;
            height:42px;
            display:flex;
            align-items:center;
            padding:0 16px;
            border:1px solid #d1d5db;
            border-radius:10px;
            background:white;
        }

        .search-box input{
            border:none;
            outline:none;
            width:100%;
            margin-left:10px;
        }

        .icon-btn{
            font-size:22px;
            color:#4b5563;
            cursor:pointer;
        }

        .topbar-avatar{
            width:38px;
            height:38px;
            border-radius:50%;
            object-fit:cover;
        }

        /* =========================
           SIDEBAR
        ========================= */

        .sidebar{
            width:240px;
            background:white;
            border-right:1px solid #e5e7eb;
            display:flex;
            flex-direction:column;
        }

        .doctor-profile{
            padding:28px 20px;
            display:flex;
            align-items:center;
            gap:12px;
        }

        .doctor-profile img{
            width:42px;
            height:42px;
            border-radius:10px;
        }

        .doctor-profile h4{
            color:#1554c7;
        }

        .doctor-profile p{
            font-size:12px;
            color:#666;
        }

        .menu{
            padding:0 16px;
        }

        .menu-item{
            display:flex;
            align-items:center;
            gap:14px;
            height:52px;
            padding:0 16px;
            margin-bottom:8px;
            border-radius:12px;
            color:#374151;
            text-decoration:none;
        }

        .menu-item.active{
            background:#1557d5;
            color:white;
            font-weight:600;
        }

        .sidebar-bottom{
            margin-top:auto;
            padding:20px 16px;
        }

        .new-record{
            width:100%;
            height:48px;
            border:none;
            border-radius:10px;
            background:#0d4bb5;
            color:white;
            font-weight:600;
            cursor:pointer;
        }

        .bottom-link{
            display:flex;
            align-items:center;
            gap:12px;
            padding:14px 12px;
            text-decoration:none;
            color:#374151;
        }

        /* =========================
           DASHBOARD HEADER
        ========================= */

        .dashboard-header h1{
            font-size:42px;
            margin-bottom:8px;
        }

        .dashboard-header p{
            color:#6b7280;
            font-size:18px;
        }

        /* =========================
           STATS CARDS
        ========================= */

        .stats-grid{
            margin-top:30px;
            display:grid;
            grid-template-columns:repeat(4,1fr);
            gap:24px;
        }

        .stat-card{
            background:white;
            border:1px solid #e5e7eb;
            border-radius:18px;
            padding:24px;
        }

        .card-top{
            display:flex;
            justify-content:space-between;
            align-items:flex-start;
        }

        .card-title{
            color:#6b7280;
        }

        .stat-card h2{
            margin-top:10px;
            font-size:42px;
        }

        .icon{
            width:56px;
            height:56px;
            border-radius:14px;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size:24px;
        }

        .icon.blue{
            background:#e8f0ff;
            color:#1557d5;
        }

        .icon.green{
            background:#e9faf3;
            color:#10b981;
        }

        .icon.yellow{
            background:#fff7e8;
            color:#f59e0b;
        }

        .icon.red{
            background:#feecec;
            color:#ef4444;
        }

        /* =========================
           RISK DISTRIBUTION
        ========================= */

        .risk-distribution-card{
            margin-top:30px;
            background:white;
            border:1px solid #e5e7eb;
            border-radius:20px;
            padding:25px;
        }

        .chart-container{
            width:320px;
            margin:auto;
        }

        .stats{
            margin-top:40px;
            display:grid;
            grid-template-columns:repeat(4,1fr);
            gap:20px;
        }

        .stat-item{
            text-align:center;
        }

        .value{
            display:flex;
            justify-content:center;
            align-items:center;
            gap:10px;
            font-size:42px;
            font-weight:700;
        }

        .dot{
            width:14px;
            height:14px;
            border-radius:50%;
        }

        .low{background:#10b981;}
        .medium{background:#f59e0b;}
        .high{background:#f97316;}
        .critical{background:#ef4444;}

        /* =========================
           BOTTOM SECTION
        ========================= */

        .dashboard-row{
            margin-top:30px;
            display:grid;
            grid-template-columns:2fr 1fr;
            gap:24px;
        }

        .urgent-card,
        .ai-card{
            background:white;
            border:1px solid #e5e7eb;
            border-radius:18px;
            padding:30px;
        }

        .card-header{
            display:flex;
            justify-content:space-between;
            align-items:center;
            margin-bottom:25px;
        }

        .title{
            display:flex;
            align-items:center;
            gap:12px;
        }

        .badge{
            background:#fde2e2;
            color:#b42318;
            padding:8px 14px;
            border-radius:999px;
        }

        .patient-card{
            display:flex;
            justify-content:space-between;
            align-items:center;
            gap:20px;
            padding:18px;
            border:1px solid #d7dce4;
            border-radius:14px;
            margin-bottom:18px;
        }

        .patient-card.critical{
            background:#fff8f8;
            border-color:#f1c5c5;
        }

        .patient-left{
            display:flex;
            align-items:center;
            gap:16px;
        }

        .patient-avatar{
            width:50px;
            height:50px;
            border-radius:50%;
            display:flex;
            justify-content:center;
            align-items:center;
        }

        .patient-avatar.red{
            background:#fde8e8;
            color:#c81e1e;
        }

        .patient-avatar.blue{
            background:#e8efff;
            color:#1d4ed8;
        }

        .btn-danger{
            background:#d92d20;
            color:white;
            border:none;
            padding:14px 22px;
            border-radius:10px;
        }

        .btn-outline{
            background:white;
            border:1px solid #9ca3af;
            padding:14px 22px;
            border-radius:10px;
        }

        .alert-box{
            background:#eef4ff;
            color:#1e40af;
            padding:18px;
            border-radius:12px;
            margin-bottom:24px;
        }

        .analytics-btn{
            width:100%;
            padding:14px;
            background:white;
            color:#1d4ed8;
            border:2px solid #1d4ed8;
            border-radius:12px;
        }

    </style>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<!-- TOPBAR -->
<!-- TOPBAR -->
<header class="topbar">

    <div class="logo">
        HealthAlert
    </div>

    <div class="top-nav">
        <a class="active">Bảng điều khiển</a>
        <a>Bệnh nhân</a>
        <a>Hồ sơ</a>
        <a>Báo cáo</a>
    </div>

    <div class="top-actions">

        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input
                    type="text"
                    placeholder="Tìm kiếm hồ sơ y tế..."
            >
        </div>

        <i class="fa-regular fa-bell icon-btn"></i>
        <i class="fa-solid fa-gear icon-btn"></i>

        <img
                class="topbar-avatar"
                src="https://i.pravatar.cc/40"
                alt=""
        >

    </div>

</header>
<div class="layout">

    <!-- SIDEBAR -->
    <aside class="sidebar">

        <div class="doctor-profile">
            <img src="https://i.pravatar.cc/60" alt="">
            <div>
                <h4>BS. Smith</h4>
                <p>Trưởng khoa phẫu thuật</p>
            </div>
        </div>

        <nav class="menu">

            <a class="menu-item active">
                <i class="fa-solid fa-table-cells"></i>
                <span>Tổng quan</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/patient-list" class="menu-item">
                <i class="fa-solid fa-users"></i>
                <span>Danh sách bệnh nhân</span>
            </a>

            <a class="menu-item">
                <i class="fa-regular fa-bell"></i>
                <span>Cảnh báo khẩn cấp</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/patient-records" class="menu-item">
                <i class="fa-regular fa-clipboard"></i>
                <span>Tiền sử bệnh án</span>
            </a>

            <a class="menu-item">
                <i class="fa-solid fa-chart-column"></i>
                <span>Phân tích dữ liệu</span>
            </a>

        </nav>

        <div class="sidebar-bottom">

            <button class="new-record">
                <i class="fa-solid fa-plus"></i>
                Tạo hồ sơ mới
            </button>

            <a class="bottom-link">
                <i class="fa-regular fa-circle-question"></i>
                Hỗ trợ
            </a>

            <a class="bottom-link">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
                Đăng xuất
            </a>

        </div>

    </aside>

    <!-- MAIN -->
    <main class="main-content">

        <div class="dashboard-container">

            <div class="dashboard-header">
                <h1>Tổng quan hệ thống</h1>
                <p>Theo dõi các chỉ số sức khỏe và mức độ rủi ro của bệnh nhân</p>
            </div>

            <!-- STATS -->
            <div class="stats-grid">

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Tổng số bệnh nhân</span>
                            <h2>1,248</h2>
                        </div>

                        <div class="icon blue">
                            <i class="fa-solid fa-users"></i>
                        </div>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Cảnh báo đang hoạt động</span>
                            <h2>23</h2>
                        </div>

                        <div class="icon yellow">
                            <i class="fa-regular fa-bell"></i>
                        </div>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Hồ sơ sức khỏe hôm nay</span>
                            <h2>156</h2>
                        </div>

                        <div class="icon green">
                            <i class="fa-regular fa-file-lines"></i>
                        </div>
                    </div>
                </div>

            </div>

            <!-- RISK DISTRIBUTION -->
            <div class="risk-distribution-card">

                <h2>Phân bố mức độ rủi ro</h2>

                <div class="chart-container">
                    <canvas id="riskChart"></canvas>
                </div>

                <div class="stats">

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot low"></span>
                            642
                        </div>
                        <p>Rủi ro thấp</p>
                    </div>

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot medium"></span>
                            389
                        </div>
                        <p>Rủi ro trung bình</p>
                    </div>

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot high"></span>
                            130
                        </div>
                        <p>Rủi ro cao</p>
                    </div>

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot critical"></span>
                            87
                        </div>
                        <p>Rủi ro nghiêm trọng</p>
                    </div>

                </div>

            </div>

            <!-- BOTTOM SECTION -->
            <div class="dashboard-row">

                <!-- URGENT CARD -->
                <div class="urgent-card">

                    <div class="card-header">

                        <div class="title">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <h2>Cần chú ý khẩn cấp</h2>
                        </div>

                        <div class="badge">
                            3 ca ưu tiên mức 1
                        </div>

                    </div>

                    <div class="patient-card critical">

                        <div class="patient-left">
                            <div class="patient-avatar red">
                                <i class="fa-solid fa-heart-circle-plus"></i>
                            </div>

                            <div>
                                <h3>Bệnh nhân #001</h3>
                                <p>Khu điều trị C-12</p>
                            </div>
                        </div>

                        <div class="patient-right">
                            <div class="vital">
                                SPO2: 88%
                            </div>

                            <div class="time">
                                Phát hiện cách đây 4 phút
                            </div>
                        </div>

                        <button class="btn-danger">
                            Xem chỉ số
                        </button>

                    </div>

                    <div class="patient-card">

                        <div class="patient-left">
                            <div class="patient-avatar blue">
                                <i class="fa-solid fa-notes-medical"></i>
                            </div>

                            <div>
                                <h3>Bệnh nhân #002</h3>
                                <p>Khoa Hồi sức tích cực</p>
                            </div>
                        </div>

                        <div class="patient-right">
                            <div class="vital">
                                Huyết áp: 165/110
                            </div>

                            <div class="time">
                                Phát hiện cách đây 12 phút
                            </div>
                        </div>

                        <button class="btn-outline">
                            Xem chỉ số
                        </button>

                    </div>

                </div>

                <!-- AI CARD -->
                <div class="ai-card">

                    <div class="title">
                        <i class="fa-solid fa-sparkles"></i>
                        <h2>Phân tích AI</h2>
                    </div>

                    <div class="alert-box">
                        <strong>Cảnh báo:</strong>
                        Hệ thống phát hiện mối tương quan cao giữa tăng đường huyết buổi sáng
                        và thời điểm dùng thuốc ở 12 bệnh nhân.
                    </div>

                    <ul style="padding-left:20px; line-height:1.8;">
                        <li>Nguy cơ tái nhập viện tăng khoảng 3% trong tuần này.</li>
                        <li>Phát hiện nhịp tim bất thường trong dữ liệu lịch sử.</li>
                    </ul>

                    <br>

                    <button class="analytics-btn">
                        Xem toàn bộ phân tích
                    </button>

                </div>

            </div>

        </div>

    </main>

</div>
</body>
<script>
    const ctx = document.getElementById('riskChart');

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: [
                'Rủi ro thấp',
                'Rủi ro trung bình',
                'Rủi ro cao',
                'Rủi ro nghiêm trọng'
            ],
            datasets: [{
                data: [642, 389, 130, 87],
                backgroundColor: [
                    '#10b981',
                    '#f59e0b',
                    '#f97316',
                    '#ef4444'
                ],
                borderColor: '#ffffff',
                borderWidth: 4
            }]
        },
        options: {
            responsive: true,
            cutout: '60%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        boxWidth: 10,
                        font: {
                            size: 14
                        }
                    }
                }
            }
        }
    });
</script>
</html>