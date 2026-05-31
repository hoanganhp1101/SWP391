<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
           MAIN
        ========================== */

        .main-content{
            flex:1;
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
            margin-bottom:24px;
        }

        .page-header h1{
            font-size:32px;
            font-weight:700;
            color:#111827;
            margin-bottom:8px;
        }

        .page-header p{
            color:#6b7280;
        }

        .patient-card{
            background:white;
            border-radius:20px;
            border:1px solid #e5e7eb;
            overflow:hidden;
        }

        .card-header{
            padding:24px 32px;
            border-bottom:1px solid #e5e7eb;
        }

        .card-header h2{
            font-size:22px;
            color:#111827;
        }

        .form-container{
            padding:32px;
            display:grid;
            grid-template-columns:1fr 1fr;
            gap:24px;
        }

        .form-group{
            display:flex;
            flex-direction:column;
        }

        .form-group label{
            margin-bottom:10px;
            font-weight:600;
            color:#374151;
        }

        .form-group input,
        .form-group select{
            height:56px;
            border:1px solid #d1d5db;
            border-radius:14px;
            padding:0 18px;
            font-size:15px;
            outline:none;
        }

        .form-group input:focus,
        .form-group select:focus{
            border-color:#1557d5;
        }

        .full-width{
            grid-column:span 2;
        }

        .button-group{
            grid-column:span 2;
            display:flex;
            justify-content:flex-end;
            gap:16px;
            margin-top:8px;
        }

        .cancel-btn{
            height:52px;
            padding:0 24px;
            border:1px solid #d1d5db;
            background:white;
            border-radius:12px;
            cursor:pointer;
            font-weight:600;
        }

        .submit-btn{
            height:52px;
            padding:0 24px;
            border:none;
            background:#1557d5;
            color:white;
            border-radius:12px;
            cursor:pointer;
            font-weight:600;
        }

        .submit-btn:hover{
            background:#0f4cc7;
        }

        @media(max-width:768px){

            .form-container{
                grid-template-columns:1fr;
            }

            .full-width,
            .button-group{
                grid-column:span 1;
            }
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

            <a href="patientmanagement.html" class="menu-item">
                <i class="fa-solid fa-users"></i>
                <span>Patient List</span>
            </a>

            <a class="menu-item">
                <i class="fa-regular fa-bell"></i>
                <span>Emergency Alerts</span>
            </a>

            <a href="medicalrecordmanagement.html" class="menu-item">
                <i class="fa-regular fa-clipboard"></i>
                <span>Medical History</span>
            </a>

            <a class="menu-item">
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

        <div class="page-content">

            <div class="patient-card">

                <div class="card-header">
                    <h2>Patient Information</h2>
                </div>

                <form class="form-container">

                    <div class="form-group">
                        <label>Patient ID</label>
                        <input type="text" value="${patient.id}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Full Name</label>
                        <input type="text" value="${patient.user.hoTen}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Date of Birth</label>
                        <input type="date" value="${patient.ngaySinh}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Gender</label>
                        <input type="text" value="${patient.gioiTinh}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Phone Number</label>
                        <input type="text" value="${patient.user.soDienThoai}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" value="${patient.user.email}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Address</label>
                        <input type="text" value="${patient.diaChi}" readonly>
                    </div>

                </form>

            </div>

        </div>

    </main>

</div>

</body>
</html>