<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <title>Patient Health Insights</title>

    <style>
        *{
            margin:0;
            padding:0;
            box-sizing:border-box;
            font-family:'Segoe UI',sans-serif;
        }

        body{
            background:#f8fafc;
            color:#1f2937;
        }

        /* LAYOUT */

        .layout{
            display:flex;
            min-height:100vh;
        }

        /* SIDEBAR */

        .sidebar{
            width:280px;
            background:#fff;
            border-right:1px solid #e5e7eb;
            position:fixed;
            top:0;
            left:0;
            height:100vh;
            overflow-y:auto;
        }

        .logo{
            padding:30px;
            border-bottom:1px solid #f1f5f9;
        }

        .logo h2{
            font-size:34px;
            font-weight:700;
        }

        .logo p{
            margin-top:6px;
            color:#64748b;
        }

        .sidebar nav{
            padding:20px;
        }

        .sidebar nav a{
            display:flex;
            align-items:center;
            gap:14px;

            padding:14px 18px;
            margin-bottom:10px;

            border-radius:14px;

            text-decoration:none;

            color:#374151;
            font-weight:500;

            transition:.2s;
        }

        .sidebar nav a:hover{
            background:#f3f4f6;
        }

        .sidebar nav a.active{
            background:#eef2ff;
            color:#2563eb;
            font-weight:600;
        }

        .sidebar nav a i{
            width:20px;
            font-size:18px;
        }

        /* MAIN */

        .main-content{
            flex:1;
            margin-left:280px;
            min-height:100vh;
        }

        /* TOPBAR */

        .topbar{
            height:90px;

            background:#fff;

            border-bottom:1px solid #e5e7eb;

            display:flex;
            justify-content:flex-end;
            align-items:center;

            padding:0 40px;

            position:sticky;
            top:0;
            z-index:100;
        }

        .profile{
            display:flex;
            align-items:center;
            gap:14px;
        }

        .profile-info{
            display:flex;
            flex-direction:column;
            align-items:flex-end;
        }

        .profile-info span{
            font-weight:600;
        }

        .profile-info small{
            color:#64748b;
            font-size:13px;
        }

        .profile-avatar{
            width:46px;
            height:46px;

            border-radius:50%;

            background:#2563eb;
            color:#fff;

            display:flex;
            align-items:center;
            justify-content:center;

            font-size:18px;
        }

        .top-header{
            display:flex;
            justify-content:space-between;
            align-items:flex-start;
            margin-bottom:30px;
        }

        .header-left{
            display:flex;
            gap:18px;
        }

        .back-btn{
            width:54px;
            height:54px;
            border-radius:16px;
            border:1px solid #dbe2ea;
            background:#fff;
            cursor:pointer;
            font-size:18px;
        }

        .top-header h1{
            font-size:42px;
            margin-bottom:10px;
        }

        .top-header p{
            color:#64748b;
        }

        .header-actions{
            display:flex;
            gap:14px;
        }

        .outline-btn{
            border:1px solid #dbe2ea;
            background:#fff;
            padding:14px 22px;
            border-radius:14px;
            cursor:pointer;
            font-weight:600;
            display:flex;
            align-items:center;
            gap:10px;
        }

        /* SUMMARY */

        .summary-card{
            background:#fff;
            border:1px solid #e5e7eb;
            border-radius:24px;
            padding:30px;
            margin-bottom:30px;
        }

        .summary-card h2{
            margin-bottom:30px;
        }

        .summary-grid{
            display:grid;
            grid-template-columns:1fr 1fr;
            gap:40px;
        }

        .summary-left{
            display:flex;
            gap:30px;
        }

        .patient-avatar{
            width:110px;
            height:110px;
            border-radius:50%;
            background:#dbeafe;
            color:#2563eb;
            font-size:42px;
            display:flex;
            align-items:center;
            justify-content:center;
        }

        .summary-info{
            display:flex;
            flex-direction:column;
            gap:24px;
        }

        .info-group span{
            color:#6b7280;
            display:block;
            margin-bottom:10px;
        }

        .info-group h3{
            font-size:18px;
            font-weight:600;
            line-height:1.5;
        }

        .info-group i{
            color:#94a3b8;
            margin-right:8px;
        }

        .metrics{
            margin-top:34px;
            border-top:1px solid #e5e7eb;
            padding-top:30px;
            display:grid;
            grid-template-columns:repeat(4,1fr);
            text-align:center;
        }

        .metric-item h2{
            font-size:44px;
            margin-bottom:8px;
        }

        .metric-item p{
            color:#64748b;
        }

        .blue{
            color:#2563eb;
        }

        .green{
            color:#16a34a;
        }

        .orange{
            color:#f97316;
        }

        .purple{
            color:#9333ea;
        }

        /* CHARTS */


        .chart-stats{
            margin-top:20px;
            display:flex;
            justify-content:space-between;
            color:#64748b;
        }

        .red-text{
            color:red;
        }

        .green-text{
            color:#16a34a;
        }

        /* BAR CHART */

        /* CHARTS */

        .charts-grid{
            display:grid;
            grid-template-columns:1fr 1fr;
            gap:24px;
            margin-bottom:30px;
        }

        .chart-card{
            background:#fff;
            border:1px solid #e5e7eb;
            border-radius:24px;
            padding:28px;
        }

        .chart-header{
            display:flex;
            justify-content:space-between;
            align-items:flex-start;
            gap:20px;
            margin-bottom:26px;
        }

        .chart-title h2{
            font-size:20px;
            margin-bottom:8px;
        }

        .chart-title p{
            color:#64748b;
            line-height:1.6;
        }

        .legend-top{
            display:flex;
            align-items:center;
            gap:22px;
            flex-wrap:wrap;
        }

        .legend-item{
            display:flex;
            align-items:center;
            gap:10px;
            color:#64748b;
            font-size:15px;
        }

        .legend-dot{
            width:14px;
            height:14px;
            border-radius:50%;
            display:inline-block;
            flex-shrink:0;
        }

        .legend-line{
            width:20px;
            height:3px;
            background:#ef4444;
            border-radius:999px;
        }

        .blue-dot{
            background:#2563eb;
        }

        .green-dot{
            background:#16a34a;
        }

        /* LINE CHART */

        .fake-chart{
            position:relative;
            height:260px;
            overflow:hidden;
        }

        .target-line{
            position:absolute;
            left:0;
            top:50%;
            width:100%;
            border-top:2px dashed #ef4444;
        }

        .glucose-svg{
            width:100%;
            height:100%;
        }

        .glucose-line{
            stroke:#2563eb;
            stroke-width:4;
            stroke-linecap:round;
            stroke-linejoin:round;
        }

        .fake-chart circle{
            fill:#2563eb;
        }

        /* STATS */

        .chart-stats{
            margin-top:24px;
            display:flex;
            justify-content:space-between;
            flex-wrap:wrap;
            gap:14px;
            color:#64748b;
            line-height:1.6;
        }

        .chart-stats strong{
            color:#111827;
        }

        .red-text{
            color:#ef4444 !important;
        }

        .green-text{
            color:#16a34a !important;
        }

        /* BAR CHART */

        .bars-chart{
            height:260px;
            display:flex;
            align-items:flex-end;
            justify-content:space-between;
            gap:14px;
            padding-top:20px;
            border-bottom:1px solid #e5e7eb;
        }

        .bar-item{
            flex:1;
            display:flex;
            flex-direction:column;
            align-items:center;
        }

        .bar-fill{
            width:100%;
            max-width:58px;
            background:#16a34a;
            border-radius:14px 14px 0 0;
            transition:0.25s;
        }

        .bar-fill:hover{
            opacity:0.9;
            transform:translateY(-3px);
        }

        .h1{height:180px;}
        .h2{height:168px;}
        .h3{height:156px;}
        .h4{height:148px;}
        .h5{height:140px;}
        .h6{height:136px;}

        .bar-item span{
            margin-top:12px;
            color:#64748b;
            font-size:14px;
        }

        .chart-container{
            height:320px;
            margin-top:10px;
        }

        .chart-title h2{
            margin-bottom:8px;
        }

        .chart-title p{
            color:#64748b;
        }

        .chart-stats{
            margin-top:24px;
            display:flex;
            justify-content:space-between;
            flex-wrap:wrap;
            gap:12px;
            color:#64748b;
        }

        .red-text{
            color:#ef4444;
        }

        .green-text{
            color:#16a34a;
        }
        /* RESPONSIVE */

        @media(max-width:1200px){

            .charts-grid{
                grid-template-columns:1fr;
            }

        }

        @media(max-width:768px){

            .chart-header{
                flex-direction:column;
            }

            .chart-stats{
                flex-direction:column;
            }

        }

        /* BOTTOM */

        .bottom-grid{
            display:grid;
            grid-template-columns:1fr 1fr;
            gap:24px;
        }

        .ai-card,
        .recommend-card{
            background:#fff;
            border:1px solid #e5e7eb;
            border-radius:24px;
            padding:28px;
        }

        .section-header{
            display:flex;
            justify-content:space-between;
            margin-bottom:26px;
        }

        .section-header p{
            color:#64748b;
            margin-top:8px;
        }

        .ai-btn{
            background:#eff6ff;
            color:#2563eb;
            border:none;
            padding:12px 16px;
            border-radius:14px;
            font-weight:600;
        }

        /* RISK ITEM */

        .risk-item{
            margin-bottom:28px;
        }

        .risk-top{
            display:flex;
            justify-content:space-between;
            margin-bottom:16px;
        }

        .risk-left{
            display:flex;
            gap:16px;
        }

        .risk-left p{
            color:#64748b;
            margin-top:6px;
        }

        .risk-icon{
            width:54px;
            height:54px;
            border-radius:16px;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size:20px;
        }

        .orange-bg{
            background:#fff7ed;
            color:#f97316;
        }

        .green-bg{
            background:#dcfce7;
            color:#16a34a;
        }

        .blue-bg{
            background:#dbeafe;
            color:#2563eb;
        }

        .badge{
            padding:8px 14px;
            border-radius:999px;
            font-size:14px;
            font-weight:600;
            height:fit-content;
        }

        .medium{
            background:#ffedd5;
            color:#ea580c;
        }

        .low{
            background:#dcfce7;
            color:#16a34a;
        }

        .progress{
            width:100%;
            height:10px;
            background:#e5e7eb;
            border-radius:999px;
            overflow:hidden;
        }

        .progress-fill{
            height:100%;
            border-radius:999px;
        }

        .orange-fill{
            background:#f97316;
        }

        .green-fill{
            background:#22c55e;
        }

        .w45{
            width:45%;
        }

        .w25{
            width:25%;
        }

        .w52{
            width:52%;
        }

        .percent{
            display:block;
            text-align:right;
            margin-top:8px;
            color:#64748b;
        }

        /* NOTE */

        .summary-note{
            background:#eff6ff;
            border:1px solid #bfdbfe;
            border-radius:18px;
            padding:22px;
            display:flex;
            gap:18px;
            margin-top:34px;
        }

        .note-icon{
            width:42px;
            height:42px;
            border-radius:12px;
            background:#2563eb;
            color:#fff;
            display:flex;
            align-items:center;
            justify-content:center;
        }

        .summary-note p{
            color:#2563eb;
            margin-top:8px;
            line-height:1.6;
        }

        /* RECOMMEND */

        .recommend-box{
            border:1px solid #e5e7eb;
            border-radius:20px;
            padding:24px;
            margin-bottom:22px;
        }

        .recommend-title{
            display:flex;
            align-items:center;
            gap:16px;
            margin-bottom:18px;
        }

        .recommend-icon{
            width:54px;
            height:54px;
            border-radius:16px;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size:20px;
        }

        .recommend-box ul{
            padding-left:24px;
            color:#374151;
            line-height:1.9;
        }

        .page-content {
            padding: 32px 40px;
        }

        /* HEADER */
        .header{
            margin-bottom:28px;
        }
    </style>

    <link
            rel="stylesheet"
            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
    />
</head>

<body>

<div class="container">

    <!-- SIDEBAR -->

    <aside class="sidebar">

        <div class="logo">
            <h2>MediCare</h2>
            <p>Doctor Portal</p>
        </div>

        <nav>

            <a href="doctordashboard.jsp" class="active">
                <i class="fa-solid fa-table-columns"></i>
                Dashboard
            </a>

            <a href="patientmanagement.jsp">
                <i class="fa-solid fa-users"></i>
                Patient List
            </a>

            <a href="medicalrecordmanagement.jsp">
                <i class="fa-regular fa-file-lines"></i>
                Medical Records
            </a>

            <a>
                <i class="fa-solid fa-pills"></i>
                Prescriptions
            </a>

            <a href="doctorappointment.jsp">
                <i class="fa-regular fa-calendar-check"></i>
                Appointments
            </a>

            <a>
                <i class="fa-solid fa-flask-vial"></i>
                Laboratory Results
            </a>

            <a>
                <i class="fa-solid fa-triangle-exclamation"></i>
                AI Alerts
            </a>

            <a href="highriskdashboard.jsp">
                <i class="fa-solid fa-heart-pulse"></i>
                High Risk Dashboard
            </a>

        </nav>

    </aside>

    <!-- MAIN -->

    <main class="main-content">

        <!-- HEADER -->
        <header class="topbar">


            <div class="profile">

                <div class="profile-info">
                    <span>Dr. Sarah Johnson</span>
                    <small>Endocrinologist</small>
                </div>

                <div class="profile-avatar">
                    <i class="fa-solid fa-user-doctor"></i>
                </div>

            </div>

        </header>
        <div class="page-content">
            <div class="top-header">

                <div class="header-left">

                    <button class="back-btn">
                        <i class="fa-solid fa-arrow-left"></i>
                    </button>

                    <div>

                        <h1>Medical Record Detail</h1>

                        <p>
                            Xem chi tiết thông tin sức khỏe và khuyến nghị điều trị
                        </p>

                    </div>

                </div>

                <div class="header-actions">

                    <button class="outline-btn">
                        <i class="fa-regular fa-file-pdf"></i>
                        Xuất PDF
                    </button>

                    <button class="outline-btn">
                        <i class="fa-solid fa-print"></i>
                        In
                    </button>

                </div>

            </div>

            <!-- SUMMARY -->

            <section class="summary-card">

                <h2>Patient</h2>

                <div class="summary-grid">

                    <!-- LEFT -->

                    <div class="summary-left">

                        <div class="patient-avatar">
                            <i class="fa-regular fa-user"></i>
                        </div>

                        <div class="summary-info">
                            <div class="info-group">
                                <span>Tên bệnh nhân</span>
                                <h3>Nguyễn Văn An</h3>
                            </div>
                        </div>
                    </div>

                    <!-- RIGHT -->

                    <div class="summary-right">
                        <div class="info-group">
                            <span>Mã bệnh nhân</span>
                            <h3>P0001</h3>
                        </div>

                        <div class="info-group">
                            <span>Chẩn đoán</span>
                            <h3>Tiểu đường type 2</h3>
                        </div>
                    </div>
                </div>

            </section>

            <!-- CHARTS -->

            <div class="charts-grid">

                <!-- GLUCOSE -->
                <div class="chart-card">

                    <div class="chart-header">

                        <div class="chart-title">
                            <h2>Glucose Trend Chart</h2>
                            <p>Xu hướng đường huyết 10 ngày gần nhất</p>
                        </div>

                    </div>

                    <div class="chart-container">
                        <canvas id="glucoseChart"></canvas>
                    </div>

                    <div class="chart-stats">
                        <span>Trung bình: <strong>133.8 mg/dL</strong></span>

                        <span>
                                    Cao nhất:
                                    <strong class="red-text">
                                        145 mg/dL
                                    </strong>
                                </span>

                        <span>
                                    Thấp nhất:
                                    <strong class="green-text">
                                        126 mg/dL
                                    </strong>
                                </span>
                    </div>

                </div>

                <!-- HBA1C -->
                <div class="chart-card">

                    <div class="chart-header">

                        <div class="chart-title">
                            <h2>HbA1c Trend Chart</h2>
                            <p>Xu hướng HbA1c 6 tháng gần nhất</p>
                        </div>

                    </div>

                    <div class="chart-container">
                        <canvas id="hba1cChart"></canvas>
                    </div>

                    <div class="chart-stats">

                                <span>
                                    Hiện tại:
                                    <strong class="green-text">
                                        7.1%
                                    </strong>
                                </span>

                        <span>
                                    Mục tiêu:
                                    <strong>
                                        &lt; 7.0%
                                    </strong>
                                </span>

                        <span>
                                    Xu hướng:
                                    <strong class="green-text">
                                        ↓ Giảm dần
                                    </strong>
                                </span>

                    </div>

                </div>

            </div>

            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

            <script>

                // GLUCOSE

                const glucoseCtx =
                    document.getElementById('glucoseChart');

                new Chart(glucoseCtx, {

                    type: 'line',

                    data: {

                        labels: [
                            '01/05',
                            '02/05',
                            '03/05',
                            '04/05',
                            '05/05',
                            '06/05',
                            '07/05',
                            '08/05',
                            '10/05'
                        ],

                        datasets: [

                            {
                                label: 'Glucose',

                                data: [
                                    142,
                                    138,
                                    145,
                                    132,
                                    128,
                                    135,
                                    130,
                                    126,
                                    133
                                ],

                                borderColor: '#2563eb',
                                backgroundColor: '#2563eb',
                                tension: 0.45,
                                borderWidth: 4,
                                pointRadius: 5,
                                pointHoverRadius: 7,
                                fill: false
                            },

                            {
                                label: 'Target',

                                data: [
                                    130, 130, 130, 130, 130,
                                    130, 130, 130, 130
                                ],

                                borderColor: '#ef4444',
                                borderDash: [6, 6],
                                borderWidth: 2,
                                pointRadius: 0
                            }

                        ]

                    },

                    options: {

                        responsive: true,
                        maintainAspectRatio: false,

                        plugins: {
                            legend: {
                                position: 'top',
                                labels: {
                                    usePointStyle: true,
                                    padding: 24,
                                    font: {
                                        size: 14
                                    }
                                }
                            }
                        },

                        scales: {

                            y: {
                                min: 100,
                                max: 160,

                                grid: {
                                    color: '#e5e7eb',
                                    drawBorder: false
                                },

                                ticks: {
                                    stepSize: 15,
                                    color: '#64748b'
                                }
                            },

                            x: {

                                grid: {
                                    color: '#f1f5f9',
                                    drawBorder: false
                                },

                                ticks: {
                                    color: '#64748b'
                                }
                            }

                        }

                    }

                });

                // HBA1C

                const hba1cCtx =
                    document.getElementById('hba1cChart');

                new Chart(hba1cCtx, {

                    type: 'bar',

                    data: {

                        labels: [
                            'T01',
                            'T02',
                            'T03',
                            'T04',
                            'T05',
                            'T06'
                        ],

                        datasets: [

                            {
                                label: 'HbA1c',

                                data: [
                                    8.2,
                                    7.9,
                                    7.6,
                                    7.4,
                                    7.2,
                                    7.1
                                ],

                                backgroundColor: '#16a34a',
                                borderRadius: 12
                            },

                            {
                                type: 'line',

                                label: 'Target',

                                data: [7, 7, 7, 7, 7, 7],

                                borderColor: '#ef4444',
                                borderDash: [6, 6],
                                borderWidth: 2,
                                pointRadius: 0
                            }

                        ]

                    },

                    options: {

                        responsive: true,
                        maintainAspectRatio: false,

                        plugins: {
                            legend: {
                                position: 'top',
                                labels: {
                                    usePointStyle: true,
                                    padding: 24,
                                    font: {
                                        size: 14
                                    }
                                }
                            }
                        },

                        scales: {

                            y: {
                                min: 0,
                                max: 10,

                                grid: {
                                    color: '#e5e7eb',
                                    drawBorder: false
                                },

                                ticks: {
                                    color: '#64748b',
                                    stepSize: 2
                                }
                            },

                            x: {

                                grid: {
                                    display: false
                                },

                                ticks: {
                                    color: '#64748b'
                                }
                            }

                        }

                    }

                });

            </script>

            <!-- BOTTOM -->

            <div class="bottom-grid">

                <!-- AI -->

                <div class="ai-card">

                    <div class="section-header">

                        <div>

                            <h2>AI Risk Assessment</h2>

                            <p>
                                Đánh giá rủi ro biến chứng bằng AI
                            </p>

                        </div>

                        <button class="ai-btn">
                            AI Powered
                        </button>

                    </div>

                    <!-- RISK ITEM -->

                    <div class="risk-item">

                        <div class="risk-top">

                            <div class="risk-left">

                                <div class="risk-icon orange-bg">
                                    <i class="fa-solid fa-triangle-exclamation"></i>
                                </div>

                                <div>

                                    <h3>Biến chứng võng mạc</h3>
                                    <p>Mức độ rủi ro</p>

                                </div>

                            </div>

                            <span class="badge medium">
                                        Trung bình
                                    </span>

                        </div>

                        <div class="progress">

                            <div class="progress-fill orange-fill w45"></div>

                        </div>

                        <span class="percent">45%</span>

                    </div>

                    <!-- ITEM -->

                    <div class="risk-item">

                        <div class="risk-top">

                            <div class="risk-left">

                                <div class="risk-icon green-bg">
                                    <i class="fa-solid fa-chart-line"></i>
                                </div>

                                <div>

                                    <h3>Biến chứng thận</h3>
                                    <p>Mức độ rủi ro</p>

                                </div>

                            </div>

                            <span class="badge low">
                                        Thấp
                                    </span>

                        </div>

                        <div class="progress">

                            <div class="progress-fill green-fill w25"></div>

                        </div>

                        <span class="percent">25%</span>

                    </div>

                    <!-- ITEM -->

                    <div class="risk-item">

                        <div class="risk-top">

                            <div class="risk-left">

                                <div class="risk-icon orange-bg">
                                    <i class="fa-solid fa-heart-pulse"></i>
                                </div>

                                <div>

                                    <h3>Biến chứng tim mạch</h3>
                                    <p>Mức độ rủi ro</p>

                                </div>

                            </div>

                            <span class="badge medium">
                                        Trung bình
                                    </span>

                        </div>

                        <div class="progress">

                            <div class="progress-fill orange-fill w52"></div>

                        </div>

                        <span class="percent">52%</span>

                    </div>

                    <!-- NOTE -->

                    <div class="summary-note">

                        <div class="note-icon">
                            <i class="fa-solid fa-circle-info"></i>
                        </div>

                        <div>

                            <h3>Tổng quan đánh giá</h3>

                            <p>
                                Bệnh nhân có mức độ rủi ro trung bình về biến chứng
                                võng mạc và tim mạch. Cần theo dõi chặt chẽ và tuân thủ
                                các khuyến nghị điều trị.
                            </p>

                        </div>

                    </div>

                </div>

                <!-- RECOMMEND -->

                <div class="recommend-card">

                    <div class="section-header">

                        <div>

                            <h2>Recommendations</h2>

                            <p>
                                Khuyến nghị điều trị và theo dõi
                            </p>

                        </div>

                    </div>

                    <!-- BLOCK -->

                    <div class="recommend-box">

                        <div class="recommend-title">

                            <div class="recommend-icon blue-bg">
                                <i class="fa-solid fa-capsules"></i>
                            </div>

                            <h3>Thuốc</h3>

                        </div>

                        <ul>

                            <li>Tiếp tục sử dụng Metformin 500mg x 2 lần/ngày</li>

                            <li>
                                Theo dõi tác dụng phụ của thuốc
                                (buồn nôn, rối loạn tiêu hóa)
                            </li>

                        </ul>

                    </div>

                    <!-- BLOCK -->

                    <div class="recommend-box">

                        <div class="recommend-title">

                            <div class="recommend-icon green-bg">
                                <i class="fa-solid fa-heart-pulse"></i>
                            </div>

                            <h3>Hoạt động thể chất</h3>

                        </div>

                        <ul>

                            <li>Tập thể dục nhẹ nhàng 30 phút mỗi ngày</li>
                            <li>Đi bộ hoặc bơi lội ít nhất 5 ngày/tuần</li>
                            <li>Tránh vận động quá sức</li>

                        </ul>

                    </div>

                    <!-- BLOCK -->

                    <div class="recommend-box">

                        <div class="recommend-title">

                            <div class="recommend-icon orange-bg">
                                <i class="fa-solid fa-apple-whole"></i>
                            </div>

                            <h3>Chế độ ăn</h3>

                        </div>

                        <ul>

                            <li>Giảm lượng carbohydrate tinh chế</li>
                            <li>Tăng cường rau xanh và chất xơ</li>
                            <li>Ăn nhiều bữa nhỏ trong ngày</li>

                        </ul>

                    </div>

                </div>

            </div>
        </div>
    </main>

</div>


</body>
</html>