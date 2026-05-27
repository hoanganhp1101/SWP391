<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <title>Medical Dashboard</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link
            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
            rel="stylesheet"
    >

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
        /* TOP CARDS */

        .top-cards{
            display:grid;
            grid-template-columns:repeat(4,1fr);
            gap:20px;
            margin-bottom:30px;
        }

        .card{
            background:#fff;
            border:1px solid #e5e7eb;
            border-radius:24px;
            padding:28px;
            display:flex;
            justify-content:space-between;
            align-items:center;
        }

        .card h4{
            color:#64748b;
            margin-bottom:12px;
            font-size:18px;
        }

        .card h1{
            font-size:54px;
        }

        .icon-box{
            width:72px;
            height:72px;
            border-radius:22px;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size:30px;
        }

        .blue{
            background:#dbeafe;
        }

        .yellow{
            background:#fef3c7;
        }

        .green{
            background:#dcfce7;
        }

        .red{
            background:#fee2e2;
        }

        /* WARNING */

        .warning-box{
            background:#faf5ff;
            border:1px solid #e9d5ff;
            border-left:6px solid #a855f7;
            border-radius:28px;
            padding:34px;
            margin-bottom:30px;
        }

        .warning-header{
            display:flex;
            justify-content:space-between;
            align-items:flex-start;
            margin-bottom:28px;
        }

        .warning-title{
            display:flex;
            gap:18px;
        }

        .brain{
            width:56px;
            height:56px;
            border-radius:18px;
            background:#f3e8ff;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size:24px;
        }

        .warning-title h2{
            font-size:48px;
            margin-bottom:10px;
        }

        .warning-title p{
            color:#64748b;
            font-size:22px;
        }

        .details-btn{
            border:1px solid #d8b4fe;
            background:#fff;
            padding:16px 26px;
            border-radius:18px;
            font-weight:600;
            cursor:pointer;
        }

        /* ALERT */

        .alert-list{
            display:grid;
            grid-template-columns:1fr 1fr;
            gap:20px;
        }

        .alert-card{
            background:#fff;
            border:1px solid #e9d5ff;
            border-radius:20px;
            padding:22px;
        }

        .alert-card h3{
            margin-bottom:12px;
            font-size:20px;
        }

        .alert-card p{
            color:#64748b;
            line-height:1.7;
        }

        /* MAIN GRID */

        .main-grid{
            display:grid;
            grid-template-columns:2fr 1fr;
            gap:24px;
        }

        /* TABLE */

        .table-card,
        .side-card{
            background:#fff;
            border:1px solid #e5e7eb;
            border-radius:24px;
            padding:28px;
        }

        .table-top{
            display:flex;
            justify-content:space-between;
            align-items:center;
            margin-bottom:28px;
        }

        .table-top h2{
            font-size:44px;
            margin-bottom:10px;
        }

        .table-top p{
            color:#64748b;
            font-size:20px;
        }

        .search{
            width:320px;
            padding:18px;
            border-radius:16px;
            border:1px solid #dbe2ea;
            outline:none;
        }

        .table-wrapper{
            overflow-x:auto;
        }

        table{
            width:100%;
            border-collapse:collapse;
            min-width:760px;
        }

        th{
            text-align:left;
            padding:18px 12px;
            color:#64748b;
            border-bottom:1px solid #e5e7eb;
        }

        td{
            padding:18px 12px;
            border-bottom:1px solid #eef2f7;
        }

        /* PATIENT */

        .patient{
            display:flex;
            align-items:center;
            gap:14px;
        }

        .avatar{
            width:48px;
            height:48px;
            border-radius:50%;
            background:#e8efff;
            color:#2563eb;
            display:flex;
            align-items:center;
            justify-content:center;
            font-weight:700;
        }

        .patient-name{
            font-weight:600;
        }

        .patient-age{
            color:#64748b;
            font-size:14px;
            margin-top:4px;
        }

        /* BADGES */

        .badge{
            padding:8px 14px;
            border-radius:999px;
            font-size:13px;
            font-weight:600;
        }

        .high{
            background:#fee2e2;
            color:#dc2626;
        }

        .low{
            background:#dcfce7;
            color:#059669;
        }

        .medium{
            background:#fef3c7;
            color:#d97706;
        }

        .pending{
            background:#dbeafe;
            color:#2563eb;
        }

        .approved{
            background:#dcfce7;
            color:#059669;
        }

        /* BUTTONS */

        .btn{
            padding:10px 16px;
            border-radius:14px;
            border:none;
            cursor:pointer;
            font-weight:600;
            margin-right:8px;
        }

        .approve{
            background:#ecfdf5;
            color:#059669;
            border:1px solid #86efac;
        }

        .reject{
            background:#fff1f2;
            color:#dc2626;
            border:1px solid #fecdd3;
        }

        .view{
            background:#f3f4f6;
        }

        /* RIGHT */

        .right-side{
            display:flex;
            flex-direction:column;
            gap:24px;
        }

        .side-card h3{
            font-size:34px;
            margin-bottom:10px;
        }

        .side-card p{
            color:#64748b;
            margin-bottom:24px;
        }

        .stat{
            display:flex;
            justify-content:space-between;
            margin-bottom:24px;
            font-size:18px;
        }

        .priority-card{
            border:1px solid #e5e7eb;
            border-radius:18px;
            padding:20px;
        }

        .priority-top{
            display:flex;
            justify-content:space-between;
            margin-bottom:14px;
        }

        .priority-name{
            font-weight:700;
            font-size:18px;
        }

        .priority-desc{
            color:#64748b;
            margin-bottom:16px;
            line-height:1.7;
        }

        .vital{
            display:flex;
            justify-content:space-between;
            margin-bottom:10px;
        }

        .page-content {
            padding: 32px 40px;
        }

        /* HEADER */
        .header{
            margin-bottom:28px;
        }
        .action-buttons{
            display:flex;
            align-items:center;
            gap:10px;
        }

        .icon-btn{
            width:38px;
            height:38px;

            border:none;
            border-radius:12px;

            display:flex;
            align-items:center;
            justify-content:center;

            cursor:pointer;

            transition:0.2s ease;

            font-size:15px;
        }

        /* VIEW */

        .view-btn{
            background:#eff6ff;
            color:#2563eb;
        }

        .view-btn:hover{
            background:#dbeafe;
        }

        /* APPROVE */

        .approve-btn{
            background:#dcfce7;
            color:#16a34a;
        }

        .approve-btn:hover{
            background:#bbf7d0;
        }

        /* REJECT */

        .reject-btn{
            background:#fee2e2;
            color:#dc2626;
        }

        .reject-btn:hover{
            background:#fecaca;
        }

    </style>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
</head>

<body>

<div class="dashboard-layout">

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

            <a href="doctorappointment.html">
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
        <!-- TOP CARDS -->
        <div class="page-content">
            <div class="top-cards">

                <div class="card">
                    <div>
                        <h4>Total Appointments</h4>
                        <h1>5</h1>
                    </div>

                    <div class="icon-box blue">
                        📅
                    </div>
                </div>

                <div class="card">
                    <div>
                        <h4>Pending Approval</h4>
                        <h1>3</h1>
                    </div>

                    <div class="icon-box yellow">
                        ⏰
                    </div>
                </div>

                <div class="card">
                    <div>
                        <h4>Confirmed Today</h4>
                        <h1>2</h1>
                    </div>

                    <div class="icon-box green">
                        ✔
                    </div>
                </div>

                <div class="card">
                    <div>
                        <h4>High Risk Patients</h4>
                        <h1>1</h1>
                    </div>

                    <div class="icon-box red">
                        ⚠
                    </div>
                </div>

            </div>

            <!-- WARNING -->

            <div class="warning-box">

                <div class="warning-header">

                    <div class="warning-title">

                        <div class="brain">
                            🧠
                        </div>

                        <div>
                            <h2>AI Early Warning System</h2>

                            <p>
                                2 patients show concerning patterns that may require immediate attention
                            </p>
                        </div>

                    </div>

                    <button class="details-btn">
                        View Details
                    </button>

                </div>

                <div class="alert-list">

                    <div class="alert-card">
                        <h3>⚠ John Anderson</h3>

                        <p>
                            Blood glucose trending upward over 7 days.
                            Possible insulin resistance.
                        </p>
                    </div>

                    <div class="alert-card">
                        <h3>⚠ Robert Chen</h3>

                        <p>
                            New neuropathy symptoms.
                            Recommend immediate foot examination.
                        </p>
                    </div>

                </div>

            </div>

            <!-- MAIN GRID -->

            <div class="main-grid">

                <!-- TABLE -->

                <div class="table-card">

                    <div class="table-top">

                        <div>
                            <h2>Today's Schedule</h2>

                            <p>
                                Manage patient appointments and consultations
                            </p>
                        </div>

                        <input
                                type="text"
                                class="search"
                                placeholder="Search patients..."
                        >

                    </div>

                    <div class="table-wrapper">

                        <table>

                            <thead>

                            <tr>
                                <th>Patient</th>
                                <th>Time</th>
                                <th>Type</th>
                                <th>Risk</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>

                            </thead>

                            <tbody>

                            <tr>

                                <td>
                                    <div class="patient">

                                        <div class="avatar">
                                            JA
                                        </div>

                                        <div>
                                            <div class="patient-name">
                                                John Anderson
                                            </div>

                                            <div class="patient-age">
                                                58 years old
                                            </div>
                                        </div>

                                    </div>
                                </td>

                                <td>9:00 AM</td>
                                <td>
                                                <span class="appointment-type phone">
                                                    <i class="fa-solid fa-phone"></i>
                                                    Phone Call
                                                </span>
                                </td>

                                <td>
                                                <span class="badge high">
                                                    HIGH
                                                </span>
                                </td>

                                <td>
                                                <span class="badge pending">
                                                    pending
                                                </span>
                                </td>

                                <td class="action-buttons">

                                    <button class="icon-btn approve-btn" title="Approve">
                                        <i class="fa-solid fa-check"></i>
                                    </button>

                                    <button class="icon-btn reject-btn" title="Reject">
                                        <i class="fa-solid fa-xmark"></i>
                                    </button>

                                    <button class="icon-btn view-btn"
                                            title="View Details"
                                            onclick="window.location.href = 'doctorappointdetail.html'">
                                        <i class="fa-solid fa-eye"></i>
                                    </button>

                                </td>

                            </tr>

                            <tr>

                                <td>
                                    <div class="patient">

                                        <div class="avatar">
                                            MG
                                        </div>

                                        <div>
                                            <div class="patient-name">
                                                Maria Garcia
                                            </div>

                                            <div class="patient-age">
                                                45 years old
                                            </div>
                                        </div>

                                    </div>
                                </td>

                                <td>10:00 AM</td>
                                <td>
                                                <span class="appointment-type phone">
                                                    <i class="fa-solid fa-phone"></i>
                                                    Phone Call
                                                </span>
                                </td>

                                <td>
                                                <span class="badge low">
                                                    LOW
                                                </span>
                                </td>

                                <td>
                                                <span class="badge approved">
                                                    approved
                                                </span>
                                </td>

                                <td class="action-buttons">

                                    <button class="icon-btn view-btn"
                                            title="View Details"
                                            onclick="window.location.href = 'doctorappointdetail.html'">
                                        <i class="fa-solid fa-eye"></i>
                                    </button>

                                </td>

                            </tr>
                            <tr>

                                <td>
                                    <div class="patient">

                                        <div class="avatar">
                                            JA
                                        </div>

                                        <div>
                                            <div class="patient-name">
                                                John Anderson
                                            </div>

                                            <div class="patient-age">
                                                58 years old
                                            </div>
                                        </div>

                                    </div>
                                </td>

                                <td>9:00 AM</td>

                                <!-- DIRECT VISIT -->
                                <td>
                                                <span class="appointment-type followup">
                                                    <i class="fa-solid fa-hospital"></i>
                                                    Follow-up
                                                </span>
                                </td>

                                <td>
                                                <span class="badge high">
                                                    HIGH
                                                </span>
                                </td>

                                <td>
                                                <span class="badge approved">
                                                    confirmed
                                                </span>
                                </td>

                                <td class="action-buttons">

                                    <button class="icon-btn view-btn"
                                            title="View Details"
                                            onclick="window.location.href = 'doctorappointdetail.html'">
                                        <i class="fa-solid fa-eye"></i>
                                    </button>

                                </td>

                            </tr>

                            <!-- ONLINE MEETING -->

                            <tr>

                                <td>
                                    <div class="patient">

                                        <div class="avatar">
                                            MG
                                        </div>

                                        <div>
                                            <div class="patient-name">
                                                Maria Garcia
                                            </div>

                                            <div class="patient-age">
                                                45 years old
                                            </div>
                                        </div>

                                    </div>
                                </td>

                                <td>10:00 AM</td>

                                <!-- ONLINE -->
                                <td>
                                                <span class="appointment-type online">
                                                    <i class="fa-solid fa-video"></i>
                                                    Online
                                                </span>
                                </td>

                                <td>
                                                <span class="badge low">
                                                    LOW
                                                </span>
                                </td>

                                <td>
                                                <span class="badge approved">
                                                    confirmed
                                                </span>
                                </td>

                                <td class="action-buttons">

                                    <button class="icon-btn view-btn"
                                            title="View Details"
                                            onclick="window.location.href = 'doctorappointdetail.html'">
                                        <i class="fa-solid fa-eye"></i>
                                    </button>

                                </td>

                            </tr>

                            <!-- PHONE CALL -->

                            <tr>

                                <td>
                                    <div class="patient">

                                        <div class="avatar">
                                            RC
                                        </div>

                                        <div>
                                            <div class="patient-name">
                                                Robert Chen
                                            </div>

                                            <div class="patient-age">
                                                52 years old
                                            </div>
                                        </div>

                                    </div>
                                </td>

                                <td>11:30 AM</td>

                                <!-- PHONE -->
                                <td>
                                                <span class="appointment-type phone">
                                                    <i class="fa-solid fa-phone"></i>
                                                    Phone Call
                                                </span>
                                </td>

                                <td>
                                                <span class="badge medium">
                                                    MEDIUM
                                                </span>
                                </td>

                                <td>
                                                <span class="badge pending">
                                                    pending
                                                </span>
                                </td>

                                <td class="action-buttons">

                                    <button class="icon-btn approve-btn" title="Approve">
                                        <i class="fa-solid fa-check"></i>
                                    </button>

                                    <button class="icon-btn reject-btn" title="Reject">
                                        <i class="fa-solid fa-xmark"></i>
                                    </button>

                                    <button class="icon-btn view-btn"
                                            title="View Details"
                                            onclick="window.location.href = 'doctorappointdetail.html'">
                                        <i class="fa-solid fa-eye"></i>
                                    </button>

                                </td>
                            </tbody>

                        </table>

                    </div>

                </div>

                <!-- RIGHT -->

                <div class="right-side">

                    <div class="side-card">

                        <h3>Quick Stats</h3>

                        <p>
                            Today's performance
                        </p>

                        <div class="stat">
                            <span>Appointments</span>
                            <strong>8/12</strong>
                        </div>

                        <div class="stat">
                            <span>Patients Seen</span>
                            <strong>15</strong>
                        </div>

                    </div>

                    <div class="side-card">

                        <h3>High Priority</h3>

                        <p>
                            Patients requiring attention
                        </p>

                        <div class="priority-card">

                            <div class="priority-top">

                                <div class="priority-name">
                                    John Anderson
                                </div>

                                <span class="badge high">
                                            HIGH
                                        </span>

                            </div>

                            <div class="priority-desc">
                                Irregular blood glucose readings
                            </div>

                            <div class="vital">
                                <span>Blood Sugar:</span>
                                <strong>180 mg/dL</strong>
                            </div>

                            <div class="vital">
                                <span>BP:</span>
                                <strong>145/92</strong>
                            </div>

                        </div>

                    </div>

                </div>

            </div>
        </div>
    </main>

</div>

</body>
</html>
