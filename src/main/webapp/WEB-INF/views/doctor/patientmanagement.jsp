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

        .header{
            margin-bottom:28px;
        }

        .header h1{
            font-size:38px;
            font-weight:700;
            margin-bottom:10px;
        }

        .header p{
            color:#64748b;
        }

        .table-card{
            background:white;
            border:1px solid #e5e7eb;
            border-radius:24px;
            overflow:hidden;
        }

        .table-top{
            display:flex;
            justify-content:space-between;
            align-items:center;
            padding:26px;
            border-bottom:1px solid #e5e7eb;
            gap:20px;
        }

        .patient-search-box{
            flex:1;
            position:relative;
        }

        .patient-search-box i{
            position:absolute;
            left:16px;
            top:50%;
            transform:translateY(-50%);
            color:#94a3b8;
        }

        .patient-search-box input{
            width:100%;
            padding:16px 16px 16px 48px;
            border:1px solid #dbe2ea;
            border-radius:14px;
            outline:none;
        }

        .table-actions{
            display:flex;
            gap:14px;
        }

        .btn{
            border:none;
            padding:14px 22px;
            border-radius:14px;
            cursor:pointer;
            font-weight:600;
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

        table{
            width:100%;
            border-collapse:collapse;
        }

        thead{
            background:#f8fafc;
        }

        th{
            text-align:left;
            padding:20px;
            color:#64748b;
            font-size:13px;
        }

        td{
            padding:20px;
            border-bottom:1px solid #eef2f7;
        }

        tbody tr:hover{
            background:#f8fafc;
        }

        .action-buttons{
            display:flex;
            gap:10px;
        }

        .edit-btn,
        .delete-btn{
            width:40px;
            height:40px;
            border:none;
            border-radius:10px;
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

        .confirm-toast{
            position:fixed;
            top:30px;
            right:30px;
            width:340px;
            background:white;
            border-radius:20px;
            padding:20px;
            box-shadow:0 10px 30px rgba(0,0,0,.15);

            opacity:0;
            visibility:hidden;
            transform:translateY(-20px);

            transition:.3s;
        }

        .confirm-toast.show{
            opacity:1;
            visibility:visible;
            transform:translateY(0);
        }

        .confirm-content{
            display:flex;
            gap:15px;
        }

        .confirm-actions{
            margin-top:20px;
            display:flex;
            justify-content:flex-end;
            gap:10px;
        }

        .cancel-btn,
        .confirm-btn{
            border:none;
            padding:12px 18px;
            border-radius:10px;
            cursor:pointer;
        }

        .confirm-btn{
            background:#ef4444;
            color:white;
        }

        .cancel-btn{
            background:#e5e7eb;
        }

        .toast{
            position:fixed;
            bottom:30px;
            right:30px;
            background:#16a34a;
            color:white;
            padding:15px 22px;
            border-radius:12px;

            opacity:0;
            visibility:hidden;

            transition:.3s;
        }

        .toast.show{
            opacity:1;
            visibility:visible;
        }
        /* ==========================
RISK FILTER
========================== */

        .risk-filter{
            display:flex;
            gap:12px;
            padding:14px 26px;
            border-bottom:1px solid #e5e7eb;
            flex-wrap:wrap;
        }

        .filter-btn{
            border:1px solid #dbe2ea;
            background:#fff;
            padding:10px 20px;
            border-radius:999px;
            font-size:14px;
            font-weight:600;
            cursor:pointer;
            transition:.2s;
        }

        .filter-btn.active{
            background:#eef2ff;
        }

        .filter-low{
            border-color:#22c55e;
            color:#16a34a;
        }

        .filter-medium{
            border-color:#f59e0b;
            color:#d97706;
        }

        .filter-high{
            border-color:#f97316;
            color:#ea580c;
        }

        .filter-critical{
            border-color:#ef4444;
            color:#dc2626;
        }

        /* ==========================
           RISK BADGE
        ========================== */

        .risk-badge{
            display:inline-block;
            padding:6px 12px;
            border-radius:999px;
            font-size:13px;
            font-weight:600;
        }

        .risk-low{
            background:#dcfce7;
            color:#16a34a;
        }

        .risk-medium{
            background:#fef3c7;
            color:#d97706;
        }

        .risk-high{
            background:#ffedd5;
            color:#ea580c;
        }

        .risk-critical{
            background:#fee2e2;
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

            <div class="header">
                <h1>Quản lý bệnh nhân</h1>
                <p>Danh sách và quản lý thông tin bệnh nhân</p>
            </div>

            <div class="table-card">

                <div class="table-top">

                    <div class="patient-search-box">
                        <i class="fa-solid fa-magnifying-glass"></i>

                        <input
                                type="text"
                                id="searchInput"
                                placeholder="Tìm kiếm theo tên, email, số điện thoại..."
                        />
                    </div>

                    <div class="table-actions">

                        <button class="btn btn-outline">
                            <i class="fa-solid fa-download"></i>
                            Xuất Excel
                        </button>

                        <button class="btn btn-primary">
                            <i class="fa-solid fa-plus"></i>
                            Thêm bệnh nhân
                        </button>


                    </div>


                </div>
                <div class="risk-filter">

                    <a href="${pageContext.request.contextPath}/doctor/patient-list" class="filter-btn ${empty param.risk ? 'active' : ''}">
                        All
                    </a>

                    <a href="${pageContext.request.contextPath}/doctor/patient-list?risk=low"
                       class="filter-btn filter-low ${param.risk == 'low' ? 'active' : ''}">
                        Low
                    </a>

                    <a href="${pageContext.request.contextPath}/doctor/patient-list?risk=medium"
                       class="filter-btn filter-medium ${param.risk == 'medium' ? 'active' : ''}">
                        Medium
                    </a>

                    <a href="${pageContext.request.contextPath}/doctor/patient-list?risk=high"
                       class="filter-btn filter-high ${param.risk == 'high' ? 'active' : ''}">
                        High
                    </a>

                    <a href="${pageContext.request.contextPath}/doctor/patient-list?risk=critical"
                       class="filter-btn filter-critical ${param.risk == 'critical' ? 'active' : ''}">
                        Critical
                    </a>

                </div>

                <div class="table-wrapper">

                    <table>

                        <thead>
                        <tr>
                            <th>PATIENT ID</th>
                            <th>FULL NAME</th>
                            <th>AGE</th>
                            <th>GENDER</th>
                            <th>EMAIL</th>
                            <th>DIABETES TYPE</th>
                            <th>LAST MEASUREMENT DATE</th>
                            <th>ACTIONS</th>
                        </tr>
                        </thead>

                        <tbody id="patientTable">

                        <c:forEach items="${patients}" var="p">

                            <tr>
                                <td>${p.patientCode}</td>

                                <td>${p.user.hoTen}</td>

                                <td>${p.tuoi}</td>

                                <td>${p.gioiTinh}</td>

                                <td>${p.user.email}</td>

                                <td>${p.loaiTieuDuong}</td>

                                <td>
                                        ${p.ngayCapNhat}
                                </td>

                                <td>
                                    <div class="action-buttons">

                                        <a href="${pageContext.request.contextPath}/doctor/patient-detail?id=${p.id}"
                                           class="edit-btn">

                                            <i class="fa-solid fa-eye"></i>

                                        </a>

                                    </div>
                                </td>

                            </tr>

                        </c:forEach>

                        </tbody>

                    </table>

                </div>

            </div>

        </div>


        <div id="toast" class="toast"></div>


    </main>

</div>
<script>

    const searchInput =
        document.getElementById("searchInput");

    searchInput.addEventListener("keyup", function () {

        const keyword =
            this.value.toLowerCase();

        const rows =
            document.querySelectorAll(
                "#patientTable tr"
            );

        rows.forEach(row => {

            const text =
                row.innerText.toLowerCase();

            if (text.includes(keyword)) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }

        });

    });

    let selectedRow = null;

    function showToast(message) {

        const toast = document.getElementById("toast");

        toast.innerText = message;

        toast.classList.add("show");

        setTimeout(() => {
            toast.classList.remove("show");
        }, 2500);
    }

</script>
</body>
</html>