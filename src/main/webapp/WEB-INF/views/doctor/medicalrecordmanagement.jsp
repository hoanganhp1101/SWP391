<%@ page import="com.example.diabetesmanage.model.HealthRecord" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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

        /* tránh đè search-box của topbar */
        .record-search-box{
            flex:1;
            position:relative;
        }

        .record-search-box i{
            position:absolute;
            top:50%;
            left:16px;
            transform:translateY(-50%);
            color:#94a3b8;
        }

        .record-search-box input{
            width:100%;
            padding:16px 18px 16px 48px;
            border:1px solid #dbe2ea;
            border-radius:14px;
            outline:none;
            font-size:15px;
        }

        .actions{
            display:flex;
            gap:14px;
        }

        .btn{
            border:none;
            padding:14px 22px;
            border-radius:14px;
            font-size:15px;
            font-weight:600;
            cursor:pointer;
        }

        .btn-outline{
            background:white;
            border:1px solid #dbe2ea;
        }

        .btn-primary{
            background:#2563eb;
            color:white;
        }

        .table-wrapper{
            overflow-x:auto;
        }

        .record-table{
            width:100%;
            border-collapse:collapse;
        }

        .record-table th{
            text-align:left;
            padding:20px;
            font-size:13px;
            color:#64748b;
            background:#f8fafc;
        }

        .record-table td{
            padding:20px;
            border-bottom:1px solid #eef2f7;
        }

        .record-table tbody tr:hover{
            background:#f8fafc;
        }

        .action-buttons{
            display:flex;
            gap:10px;
        }

        .table-icon-btn{
            width:40px;
            height:40px;
            border:none;
            border-radius:12px;
            cursor:pointer;
        }

        .edit-btn{
            background:#eff6ff;
            color:#2563eb;
        }

        .delete-btn{
            background:#fef2f2;
            color:#dc2626;
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

            <div class="page-header">
                <h1>Quản lý hồ sơ y tế</h1>
                <p>Danh sách và quản lý hồ sơ khám bệnh</p>
            </div>

            <div class="card">

                <div class="card-top">

                    <div class="record-search-box">
                        <i class="fa-solid fa-magnifying-glass"></i>

                        <input
                                type="text"
                                id="searchInput"
                                placeholder="Tìm kiếm theo tên bệnh nhân, mã BN, bác sĩ..."
                        >
                    </div>

                    <div class="actions">

                        <button class="btn btn-outline">
                            <i class="fa-solid fa-download"></i>
                            Xuất Excel
                        </button>

                        <button class="btn btn-primary">
                            <i class="fa-solid fa-plus"></i>
                            Thêm hồ sơ
                        </button>

                    </div>

                </div>

                <div class="table-wrapper">

                    <table class="record-table">

                        <thead>
                        <tr>
                            <th>RECORD ID</th>
                            <th>MEASUREMENT TIME</th>
                            <th>BLOOD GLUCOSE (mg/dL)</th>
                            <th>HbA1c (%)</th>
                            <th>BMI</th>
                            <th>WEIGHT (kg)</th>
                            <th>ACTION</th>
                        </tr>
                        </thead>

                        <tbody>

                        <%
                            List<HealthRecord> records =
                                    (List<com.example.diabetesmanage.model.HealthRecord>)
                                            request.getAttribute("records");

                            if(records != null){
                                for(var record : records){
                        %>

                        <tr>

                            <td><%= record.getId() %></td>

                            <td>
                                <%= record.getThoiGianDo() %>
                            </td>

                            <td>
                                <%= record.getDuongHuyetMgdl() %>
                            </td>

                            <td>
                                <%= record.getHba1cPercent() %>
                            </td>

                            <td>
                                <%= record.getBmi() %>
                            </td>

                            <td>
                                <%= record.getCanNangKg() %>
                            </td>

                            <td>

                                <a class="table-icon-btn edit-btn"
                                   href="${pageContext.request.contextPath}/doctor/record-detail?id=<%= record.getId() %>">

                                    <i class="fa-solid fa-eye"></i>

                                </a>

                            </td>

                        </tr>

                        <%
                                }
                            }
                        %>

                        </tbody>

                    </table>

                </div>

            </div>

        </div>


    </main>

</div>
<script>

    document
        .getElementById("searchInput")
        .addEventListener("keyup", function () {

            const keyword = this.value.toLowerCase();

            const filtered = medicalRecords.filter(record =>
                record.patientName.toLowerCase().includes(keyword) ||
                record.patientId.toLowerCase().includes(keyword) ||
                record.doctor.toLowerCase().includes(keyword) ||
                record.diagnosis.toLowerCase().includes(keyword)
            );

            renderTable(filtered);

        });

</script>
</body>
</html>