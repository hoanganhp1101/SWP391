<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Doctor Dashboard</title>

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

        /* DASHBOARD */

        .dashboard{
            padding:40px;
        }

        .dashboard h1{
            font-size:42px;
            margin-bottom:10px;
        }

        .dashboard p{
            color:#64748b;
        }

        /* KPI CARDS */

        .cards{
            display:grid;
            grid-template-columns:repeat(4,1fr);
            gap:24px;

            margin-top:34px;
        }

        .card{
            background:#fff;

            border:1px solid #e5e7eb;
            border-radius:22px;

            padding:28px;

            display:flex;
            justify-content:space-between;
            align-items:center;
        }

        .card h4{
            color:#64748b;
            font-size:15px;
            font-weight:500;
        }

        .card h2{
            margin-top:12px;
            font-size:40px;
        }

        .card-icon{
            width:64px;
            height:64px;

            border-radius:18px;

            display:flex;
            justify-content:center;
            align-items:center;

            font-size:24px;
        }

        .patient-icon{
            background:#dbeafe;
            color:#2563eb;
        }

        .appointment-icon{
            background:#dcfce7;
            color:#16a34a;
        }

        .alert-icon{
            background:#f3e8ff;
            color:#9333ea;
        }

        .risk-icon{
            background:#fee2e2;
            color:#dc2626;
        }

        /* TABLE CARD */

        .table-card{
            margin-top:34px;

            background:#fff;

            border:1px solid #e5e7eb;
            border-radius:24px;

            overflow:hidden;
        }

        .table-header{
            display:flex;
            justify-content:space-between;
            align-items:center;

            padding:26px 30px;

            border-bottom:1px solid #f1f5f9;
        }

        .table-header h2{
            font-size:30px;
        }

        .table-header a{
            color:#2563eb;
            text-decoration:none;
            font-weight:600;
        }

        table{
            width:100%;
            border-collapse:collapse;
        }

        thead{
            background:#f8fafc;
        }

        th{
            text-align:left;

            padding:18px 24px;

            font-size:14px;
            color:#64748b;

            border-bottom:1px solid #e5e7eb;
        }

        td{
            padding:22px 24px;

            border-bottom:1px solid #f1f5f9;
        }

        tr:hover{
            background:#fafafa;
        }

        /* BADGES */

        .risk,
        .badge,
        .status{
            display:inline-flex;
            align-items:center;
            justify-content:center;

            padding:8px 14px;

            border-radius:999px;

            font-size:13px;
            font-weight:600;
        }

        .high{
            background:#fee2e2;
            color:#dc2626;
        }

        .critical{
            background:#fee2e2;
            color:#dc2626;
        }

        .elevated{
            background:#ffedd5;
            color:#ea580c;
        }

        .normal{
            background:#dcfce7;
            color:#16a34a;
        }

        /* EXTRA GRID */

        .extra-grid{
            margin-left:280px;

            display:grid;
            grid-template-columns:1.1fr 1fr;

            gap:30px;

            padding:0 40px 40px;
        }

        /* SECTIONS */

        .alert-section,
        .lab-section{
            background:#fff;

            border:1px solid #e5e7eb;
            border-radius:24px;

            overflow:hidden;
        }

        /* SECTION HEADER */

        .section-header{
            display:flex;
            justify-content:space-between;
            align-items:center;

            padding:28px 30px;

            border-bottom:1px solid #f1f5f9;
        }

        .section-title{
            display:flex;
            align-items:center;
            gap:16px;
        }

        .section-title h2{
            font-size:30px;
        }

        .section-icon{
            width:52px;
            height:52px;

            border-radius:16px;

            display:flex;
            align-items:center;
            justify-content:center;

            font-size:22px;
        }

        .section-icon.purple{
            background:#f3e8ff;
            color:#9333ea;
        }

        .section-icon.green{
            background:#dcfce7;
            color:#059669;
        }

        .alert-count{
            background:#f3e8ff;
            color:#9333ea;

            padding:10px 18px;

            border-radius:999px;

            font-weight:600;
        }

        /* ALERT ITEM */

        .alert-item{
            margin:24px;

            border:1px solid #e5e7eb;
            border-radius:20px;

            padding:24px;
        }

        .alert-top{
            display:flex;
            justify-content:space-between;
            align-items:flex-start;
        }

        .alert-top h3{
            font-size:24px;
            margin-bottom:8px;
        }

        .alert-top span{
            color:#64748b;
        }

        .alert-item p{
            margin-top:18px;

            color:#374151;
            line-height:1.6;
        }

        .alert-footer{
            display:flex;
            justify-content:space-between;
            align-items:center;

            margin-top:24px;
        }

        .alert-footer span{
            color:#94a3b8;
            font-size:14px;
        }

        .alert-footer button{
            border:none;

            background:#2563eb;
            color:#fff;

            padding:12px 20px;

            border-radius:12px;

            font-weight:600;
            cursor:pointer;
        }

        /* LAB GRID */

        .lab-grid{
            padding:24px;

            display:grid;
            grid-template-columns:1fr 1fr;

            gap:22px;
        }

        .lab-card{
            border:1px solid #e5e7eb;

            border-radius:20px;

            padding:24px;
        }

        .lab-top{
            display:flex;
            justify-content:space-between;
            align-items:center;
        }

        .lab-top h3{
            font-size:22px;
        }

        .lab-value{
            margin-top:18px;

            font-size:44px;
            font-weight:700;
        }

        .lab-value span{
            font-size:20px;
            color:#64748b;
        }

        .lab-card p{
            margin-top:12px;
            color:#64748b;
        }

        .lab-bottom{
            margin-top:20px;

            display:flex;
            justify-content:space-between;
            align-items:center;
        }

        /* ICON COLORS */

        .success{
            color:#22c55e;
        }

        .danger{
            color:#ef4444;
        }

        .neutral{
            color:#64748b;
        }

        .critical-icon{
            color:#ef4444;
        }

        .high-icon{
            color:#f97316;
        }

        /* RESPONSIVE */

        @media(max-width:1400px){

            .cards{
                grid-template-columns:repeat(2,1fr);
            }

            .extra-grid{
                grid-template-columns:1fr;
            }
        }

        @media(max-width:992px){

            .sidebar{
                position:relative;
                width:100%;
                height:auto;
            }

            .layout{
                flex-direction:column;
            }

            .main-content,
            .extra-grid{
                margin-left:0;
            }

            .cards{
                grid-template-columns:1fr;
            }

            .lab-grid{
                grid-template-columns:1fr;
            }
        }
    </style>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
</head>

<body>

<div class="layout">

    <!-- SIDEBAR -->
    <aside class="sidebar">

        <div class="logo">
            <h2>MediCare</h2>
            <p>Doctor Portal</p>
        </div>

        <nav>

            <a href="doctordashboard.html" class="active">
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

        <!-- CONTENT -->

        <section class="dashboard">

            <h1>Dashboard Overview</h1>
            <p>Welcome back, Dr. Sarah Johnson</p>

            <!-- KPI -->
            <div class="cards">

                <div class="card">

                    <div>
                        <h4>Total Patients</h4>
                        <h2>248</h2>
                    </div>

                    <div class="card-icon patient-icon">
                        <i class="fa-solid fa-users"></i>
                    </div>

                </div>

                <div class="card">

                    <div>
                        <h4>Today's Appointments</h4>
                        <h2>12</h2>
                    </div>

                    <div class="card-icon appointment-icon">
                        <i class="fa-regular fa-calendar-check"></i>
                    </div>

                </div>

                <div class="card">

                    <div>
                        <h4>New AI Alerts</h4>
                        <h2>8</h2>
                    </div>

                    <div class="card-icon alert-icon">
                        <i class="fa-regular fa-bell"></i>
                    </div>

                </div>

                <div class="card">

                    <div>
                        <h4>High-Risk Patients</h4>
                        <h2>23</h2>
                    </div>

                    <div class="card-icon risk-icon">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                    </div>

                </div>

            </div>

            <!-- TABLE -->

            <div class="table-card">

                <div class="table-header">

                    <h2>High-Risk Patient Dashboard</h2>

                    <a href="#">View All</a>

                </div>

                <table>

                    <thead>
                    <tr>
                        <th>Patient Name</th>
                        <th>Age</th>
                        <th>Glucose</th>
                        <th>HbA1c</th>
                        <th>Risk Score</th>
                        <th>Risk Level</th>
                        <th>Status</th>
                    </tr>
                    </thead>

                    <tbody>

                    <tr>
                        <td>John Anderson</td>
                        <td>58</td>
                        <td>185</td>
                        <td>8.2</td>
                        <td>87</td>
                        <td>
                                        <span class="risk high">
                                            High
                                        </span>
                        </td>
                        <td>Needs Review</td>
                    </tr>

                    <tr>
                        <td>Maria Garcia</td>
                        <td>62</td>
                        <td>172</td>
                        <td>7.8</td>
                        <td>82</td>
                        <td>
                                        <span class="risk high">
                                            High
                                        </span>
                        </td>
                        <td>Under Treatment</td>
                    </tr>

                    </tbody>

                </table>

            </div>

        </section>

    </main>

</div>
<!-- EXTRA DASHBOARD SECTION -->

<div class="extra-grid">

    <!-- AI ALERT CENTER -->

    <div class="alert-section">

        <div class="section-header">

            <div class="section-title">

                <div class="section-icon purple">
                    <i class="fa-solid fa-robot"></i>
                </div>

                <h2>AI Alert Center</h2>

            </div>

            <span class="alert-count">
                        4 Active Alerts
                    </span>

        </div>

        <!-- ALERT ITEM -->

        <div class="alert-item">

            <div class="alert-top">

                <div>

                    <h3>
                        <i class="fa-solid fa-circle-exclamation critical-icon"></i>
                        Patricia Williams
                    </h3>

                    <span>Glucose Spike</span>

                </div>

                <div class="badge critical">
                    Critical
                </div>

            </div>

            <p>
                Severe hyperglycemia detected - immediate intervention required
            </p>

            <div class="alert-footer">

                <span>2026-05-23 • 09:15 AM</span>

                <button>
                    View Details
                </button>

            </div>

        </div>

        <!-- ALERT ITEM -->

        <div class="alert-item">

            <div class="alert-top">

                <div>

                    <h3>
                        <i class="fa-solid fa-triangle-exclamation high-icon"></i>
                        John Anderson
                    </h3>

                    <span>Kidney Function</span>

                </div>

                <div class="badge high">
                    High
                </div>

            </div>

            <p>
                Abnormal creatinine levels - schedule nephrology consult
            </p>

            <div class="alert-footer">

                <span>2026-05-23 • 08:42 AM</span>

                <button>
                    View Details
                </button>

            </div>

        </div>

    </div>

    <!-- LAB RESULTS -->

    <div class="lab-section">

        <div class="section-header">

            <div class="section-title">

                <div class="section-icon green">
                    <i class="fa-solid fa-flask"></i>
                </div>

                <h2>Laboratory Results Summary</h2>

            </div>

            <a href="#">
                View All Tests
            </a>

        </div>

        <div class="lab-grid">

            <div class="lab-card">

                <div class="lab-top">

                    <h3>HbA1c</h3>

                    <i class="fa-solid fa-arrow-trend-down success"></i>

                </div>

                <div class="lab-value">
                    7.2 <span>%</span>
                </div>

                <p>Normal: &lt; 5.7</p>

                <div class="lab-bottom">

                    <span>2 days ago</span>

                    <div class="status elevated">
                        elevated
                    </div>

                </div>

            </div>

            <div class="lab-card">

                <div class="lab-top">

                    <h3>Creatinine</h3>

                    <i class="fa-solid fa-arrow-trend-up danger"></i>

                </div>

                <div class="lab-value">
                    1.3 <span>mg/dL</span>
                </div>

                <p>Normal: 0.7 - 1.2</p>

                <div class="lab-bottom">

                    <span>1 week ago</span>

                    <div class="status elevated">
                        elevated
                    </div>

                </div>

            </div>

            <div class="lab-card">

                <div class="lab-top">

                    <h3>Total Cholesterol</h3>

                    <i class="fa-solid fa-minus neutral"></i>

                </div>

                <div class="lab-value">
                    195 <span>mg/dL</span>
                </div>

                <p>Normal: &lt; 200</p>

                <div class="lab-bottom">

                    <span>3 days ago</span>

                    <div class="status normal">
                        normal
                    </div>

                </div>

            </div>

            <div class="lab-card">

                <div class="lab-top">

                    <h3>LDL Cholesterol</h3>

                    <i class="fa-solid fa-arrow-trend-down success"></i>

                </div>

                <div class="lab-value">
                    118 <span>mg/dL</span>
                </div>

                <p>Normal: &lt; 100</p>

                <div class="lab-bottom">

                    <span>3 days ago</span>

                    <div class="status elevated">
                        elevated
                    </div>

                </div>

            </div>

        </div>

    </div>

</div>

</body>
</html>