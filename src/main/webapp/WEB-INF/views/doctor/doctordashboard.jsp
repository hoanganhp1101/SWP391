<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert Dashboard</title>
    <style>
        *{
            margin:0;
            padding:0;
            box-sizing:border-box;
            font-family:Inter, sans-serif;
        }

        body{
            background:#f5f6fa;
        }

        .layout{
            display:flex;
            height:calc(100vh - 80px);
        }

        .topbar{
            height:80px;
            background:white;

            display:flex;
            align-items:center;

            padding:0 48px;

            border-bottom:1px solid #e5e7eb;
        }

        .sidebar{
            width:240px;
            background:#fff;
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
            object-fit:cover;
        }

        .doctor-profile h4{
            font-size:16px;
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

            margin-bottom:8px;
            padding:0 16px;

            border-radius:12px;

            color:#374151;
            text-decoration:none;

            cursor:pointer;
        }

        .menu-item i{
            font-size:18px;
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

            font-size:15px;
            font-weight:600;

            cursor:pointer;
        }

        .new-record i{
            margin-right:8px;
        }

        .bottom-link{
            display:flex;
            align-items:center;
            gap:12px;

            padding:14px 12px;

            text-decoration:none;
            color:#374151;

            cursor:pointer;
        }

        /* ==========================
           TOPBAR
        ========================== */


        .logo{
            font-size:20px;
            font-weight:700;
            color:#0d4bb5;
        }

        .top-nav{
            display:flex;
            gap:36px;

            margin-left:40px; /* chỉnh số này */
        }

        .top-actions{
            display:flex;
            align-items:center;
            gap:22px;

            margin-left:auto;
        }

        .top-nav a{
            color:#555;
            cursor:pointer;
            font-size:16px;
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

        .search-box{
            width:290px;
            height:42px;

            display:flex;
            align-items:center;

            padding:0 16px;

            border:1px solid #d1d5db;
            border-radius:10px;

            background:#fff;
        }

        .search-box i{
            color:#777;
        }

        .search-box input{
            border:none;
            outline:none;
            width:100%;
            margin-left:10px;
            font-size:14px;
        }

        .icon-btn{
            font-size:22px;
            color:#4b5563;
            cursor:pointer;
        }

        .avatar{
            width:38px;
            height:38px;
            border-radius:50%;
            object-fit:cover;
        }
        .page-content{
            padding:32px;
        }

        .page-header{
            margin-bottom:28px;
        }

        .page-header h1{
            font-size:38px;
            font-weight:700;
            margin-bottom:10px;
        }

        .page-header p{
            color:#64748b;
        }

        .card{
            background:white;
            border:1px solid #e5e7eb;
            border-radius:24px;
            overflow:hidden;
        }

        .card-top{
            padding:26px;
            display:flex;
            justify-content:space-between;
            align-items:center;
            gap:20px;
            border-bottom:1px solid #e5e7eb;
        }
        .main-content{
            flex:1;
            background:#f5f7fb;
            padding:28px;
            overflow:auto;
        }

        .dashboard-header h1{
            font-size:42px;
            font-weight:700;
            margin-bottom:8px;
        }

        .dashboard-header p{
            color:#6b7280;
            font-size:18px;
        }

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
            font-size:16px;
        }

        .stat-card h2{
            margin-top:10px;
            font-size:42px;
            font-weight:700;
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

        .icon.red{
            background:#feecec;
            color:#ef4444;
        }

        .icon.yellow{
            background:#fff7e8;
            color:#f59e0b;
        }

        .icon.green{
            background:#e9faf3;
            color:#10b981;
        }

        .trend{
            margin-top:18px;
            font-size:15px;
        }

        .positive{
            color:#10b981;
        }

        .negative{
            color:#ef4444;
        }

        .risk-card{
            margin-top:30px;
            background:white;
            border:1px solid #e5e7eb;
            border-radius:20px;
            padding:28px;
        }

        .risk-card h3{
            font-size:28px;
            margin-bottom:30px;
        }

        .risk-content{
            display:flex;
            flex-direction:column;
            align-items:center;
        }

        .donut-chart{
            width:260px;
            height:260px;
            border-radius:50%;

            background:
                    conic-gradient(
                            #10b981 0% 52%,
                            #f59e0b 52% 83%,
                            #f97316 83% 93%,
                            #ef4444 93% 100%
                    );

            position:relative;
        }

        .donut-chart::after{
            content:"";
            position:absolute;

            width:140px;
            height:140px;

            background:white;
            border-radius:50%;

            top:50%;
            left:50%;

            transform:translate(-50%,-50%);
        }

        .risk-legend{
            margin-top:24px;

            display:flex;
            gap:24px;

            font-size:18px;
        }

        .legend-item{
            display:flex;
            align-items:center;
            gap:8px;
        }

        .dot{
            width:14px;
            height:14px;
            border-radius:50%;
            display:inline-block;
        }

        .green{
            background:#10b981;
        }

        .yellow{
            background:#f59e0b;
        }

        .orange{
            background:#f97316;
        }

        .red{
            background:#ef4444;
        }

        .risk-stats{
            margin-top:50px;

            width:100%;

            display:grid;
            grid-template-columns:repeat(4,1fr);
            gap:20px;
        }

        .risk-box{
            text-align:center;
        }

        .risk-box h2{
            font-size:42px;
            margin:10px 0;
        }

        .risk-box p{
            color:#6b7280;
        }
    </style>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<!-- TOPBAR -->
<header class="topbar">

    <div class="logo">
        HealthAlert
    </div>

    <div class="top-nav">
        <a class="active">Dashboard</a>
        <a>Patients</a>
        <a>Records</a>
        <a>Reports</a>
    </div>

    <div class="top-actions">

        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input
                    type="text"
                    placeholder="Search medical records..."
            >
        </div>

        <i class="fa-regular fa-bell icon-btn"></i>
        <i class="fa-solid fa-gear icon-btn"></i>

        <img
                class="avatar"
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
                <h4>Dr. Smith</h4>
                <p>Chief Surgeon</p>
            </div>
        </div>

        <nav class="menu">

            <a class="menu-item active">
                <i class="fa-solid fa-table-cells"></i>
                <span>Overview</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/patient-list" class="menu-item">
                <i class="fa-solid fa-users"></i>
                <span>Patient List</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/alerts" class="menu-item">
                <i class="fa-regular fa-bell"></i>
                <span>Emergency Alerts</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/patient-records" class="menu-item">
                <i class="fa-regular fa-clipboard"></i>
                <span>Medical History</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/analytics" class="menu-item">
                <i class="fa-solid fa-chart-column"></i>
                <span>Analytics</span>
            </a>

        </nav>

        <div class="sidebar-bottom">

            <button class="new-record">
                <i class="fa-solid fa-plus"></i>
                New Record
            </button>

            <a class="bottom-link">
                <i class="fa-regular fa-circle-question"></i>
                Support
            </a>

            <a class="bottom-link">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
                Sign Out
            </a>

        </div>

    </aside>

    <!-- MAIN -->
    <main class="main-content">

        <div class="dashboard-container">

            <div class="dashboard-header">
                <h1>Dashboard Overview</h1>
                <p>Monitor patient health metrics and risk levels</p>
            </div>

            <!-- Stats Cards -->
            <div class="stats-grid">

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Total Patients</span>
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
                            <span class="card-title">High Risk Patients</span>
                            <h2>87</h2>
                        </div>
                        <div class="icon red">
                            <i class="fa-solid fa-triangle-exclamation"></i>
                        </div>
                    </div>


                </div>

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Active Alerts</span>
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
                            <span class="card-title">Health Records Today</span>
                            <h2>156</h2>
                        </div>
                        <div class="icon green">
                            <i class="fa-regular fa-file-lines"></i>
                        </div>
                    </div>

                </div>

            </div>

            <!-- Risk Distribution -->
            <div class="risk-card">

                <h3>Risk Level Distribution</h3>

                <div class="risk-content">

                    <div class="donut-chart"></div>

                    <div class="risk-legend">

                        <div class="legend-item">
                            <span class="dot green"></span>
                            Low Risk
                        </div>

                        <div class="legend-item">
                            <span class="dot yellow"></span>
                            Medium Risk
                        </div>

                        <div class="legend-item">
                            <span class="dot orange"></span>
                            High Risk
                        </div>

                        <div class="legend-item">
                            <span class="dot red"></span>
                            Critical Risk
                        </div>

                    </div>

                    <div class="risk-stats">

                        <div class="risk-box">
                            <span class="dot green"></span>
                            <h2>642</h2>
                            <p>Low Risk</p>
                        </div>

                        <div class="risk-box">
                            <span class="dot yellow"></span>
                            <h2>389</h2>
                            <p>Medium Risk</p>
                        </div>

                        <div class="risk-box">
                            <span class="dot orange"></span>
                            <h2>130</h2>
                            <p>High Risk</p>
                        </div>

                        <div class="risk-box">
                            <span class="dot red"></span>
                            <h2>87</h2>
                            <p>Critical Risk</p>
                        </div>

                    </div>

                </div>

            </div>

        </div>

    </main>

</div>

</body>
</html>
