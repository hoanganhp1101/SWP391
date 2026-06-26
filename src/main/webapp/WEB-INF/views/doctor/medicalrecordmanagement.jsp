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
            overflow:visible;
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

        .date-range-picker {
            position: relative;
            width: 250px;
        }

        .date-display {
            height: 44px;           /* trước 48 hoặc lớn hơn */
            padding: 0 14px;

            display: flex;
            align-items: center;
            justify-content: space-between;

            background: #fff;
            border: 1px solid #ddd;
            border-radius: 22px;

            font-size: 14px;
            font-weight: 500;

            cursor: pointer;
        }

        .date-display:hover {
            border-color: #6c63ff;
        }

        .date-popup {
            position: absolute;
            top: 60px;
            left: 0;

            width: 340px;

            background: white;
            border-radius: 8px;

            padding: 20px;

            box-shadow: 0 10px 30px rgba(0,0,0,.15);

            display: none;
            z-index: 999;
        }

        .date-popup.show {
            display: block;
        }

        .date-fields {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .date-fields label {
            font-size: 14px;
            font-weight: 600;
        }

        .date-fields input {
            height: 40px;
            padding: 0 12px;

            border: 1px solid #ddd;
            border-radius: 6px;
        }

        .popup-actions {
            margin-top: 20px;

            display: flex;
            justify-content: flex-end;
            gap: 12px;
        }

        .popup-actions button {
            padding: 8px 16px;

            border: none;
            border-radius: 4px;

            cursor: pointer;
        }

        .popup-actions button[type="submit"] {
            background: #6c63ff;
            color: white;
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
                    placeholder="Tìm kiếm hồ sơ sức khỏe..."
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
                <h4>BS. Smith</h4>
            </div>
        </div>

        <nav class="menu">

            <a class="menu-item active">
                <i class="fa-solid fa-table-cells"></i>
                <span>Tổng quan</span>
            </a>

            <a href="patientmanagement.html" class="menu-item">
                <i class="fa-solid fa-users"></i>
                <span>Danh sách bệnh nhân</span>
            </a>

            <a class="menu-item">
                <i class="fa-regular fa-bell"></i>
                <span>Cảnh báo khẩn cấp</span>
            </a>

            <a href="medicalrecordmanagement.html" class="menu-item">
                <i class="fa-regular fa-clipboard"></i>
                <span>Hồ sơ sức khỏe</span>
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

        <div class="page-content">

            <c:if test="${param.success eq '1'}">
                <div style="background:#d1fae5;border:1px solid #6ee7b7;color:#065f46;padding:14px 20px;border-radius:12px;margin-bottom:20px;font-weight:500;">
                    <i class="fa-solid fa-circle-check"></i>
                    Tạo hồ sơ bệnh án thành công.
                </div>
            </c:if>
            <c:if test="${param.deleted eq '1'}">
                <div style="background:#d1fae5;border:1px solid #6ee7b7;color:#065f46;padding:14px 20px;border-radius:12px;margin-bottom:20px;font-weight:500;">
                    <i class="fa-solid fa-circle-check"></i>
                    Đã xóa hồ sơ bệnh án.
                </div>
            </c:if>

            <div class="page-header">
                <h1>Quản lý hồ sơ sức khỏe</h1>
                <p>Danh sách và quản lý hồ sơ theo dõi sức khỏe</p>
            </div>

            <div class="card">

                <div class="card-top">

                    <div class="record-search-box">
                        <i class="fa-solid fa-magnifying-glass"></i>

                        <form method="get"
                              action="${pageContext.request.contextPath}/doctor/patient-records">

                            <input
                                    type="text"
                                    name="keyword"
                                    value="${param.keyword}"
                                    placeholder="Tìm kiếm..."
                            >

                        </form>
                    </div>

                    <div class="actions">

                        <a href="${pageContext.request.contextPath}/doctor/export-patients?keyword=${param.keyword}&risk=${param.risk}"
                           class="btn btn-outline">

                            <i class="fa-solid fa-download"></i>
                            Xuất Excel
                        </a>

                        <a class="btn btn-primary"
                           href="${pageContext.request.contextPath}/medical-encounters/add">
                            <i class="fa-solid fa-plus"></i>
                            Thêm hồ sơ bệnh án
                        </a>

                    </div>

                </div>

                <div class="risk-filter">

                    <form action="${pageContext.request.contextPath}/doctor/patient-records"
                          method="get">

                        <div class="date-range-picker">

                            <div class="date-display" id="toggleDatePicker">
                <span id="dateText">
                    ${not empty param.startDate && not empty param.endDate
                            ? param.startDate.concat(' - ').concat(param.endDate)
                            : 'Chọn khoảng ngày'}
                </span>
                                <span>▼</span>
                            </div>

                            <div class="date-popup" id="datePopup">

                                <div class="date-fields">

                                    <label>Từ ngày</label>
                                    <input
                                            type="date"
                                            id="startDate"
                                            name="startDate"
                                            value="${param.startDate}"
                                    >

                                    <label>Đến ngày</label>
                                    <input
                                            type="date"
                                            id="endDate"
                                            name="endDate"
                                            value="${param.endDate}"
                                    >

                                </div>

                                <div class="popup-actions">
                                    <button type="button" id="cancelBtn">
                                        Cancel
                                    </button>

                                    <button type="submit">
                                        OK
                                    </button>
                                </div>

                            </div>

                        </div>

                    </form>

                </div>

                <div class="table-wrapper">

                    <table class="record-table">

                        <thead>
                        <tr>
                            <th>MÃ HỒ SƠ</th>
                            <th>MÃ BỆNH NHÂN</th>
                            <th>THỜI GIAN ĐO</th>
                            <th>ĐƯỜNG HUYẾT (mg/dL)</th>
                            <th>HbA1c (%)</th>
                            <th>BMI</th>
                            <th>CÂN NẶNG (kg)</th>
                            <th>SỐ NGÀY TỪ LẦN KHÁM GẦN NHẤT</th>
                            <th>THAO TÁC</th>
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

                            <td>
                                <%= record.getHealthRecordId() %>
                            </td>

                            <td>
                                <%= record.getPatient().getPatientCode() %>
                            </td>

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
                                <%= record.getDaysSinceLastVisit() %>
                            </td>

                            <td>

                                <a class="table-icon-btn edit-btn"
                                   href="${pageContext.request.contextPath}/doctor/record-detail?id=<%= record.getId() %>"
                                   title="Xem chi tiết">
                                    <i class="fa-solid fa-eye"></i>
                                </a>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/doctor/record-delete"
                                      style="display:inline;"
                                      onsubmit="return confirm('Xóa hồ sơ <%= record.getHealthRecordId() %>?');">
                                    <input type="hidden" name="id" value="<%= record.getId() %>">
                                    <button type="submit" class="table-icon-btn"
                                            style="background:#fef2f2;color:#dc2626;margin-left:6px;" title="Xóa">
                                        <i class="fa-solid fa-trash"></i>
                                    </button>
                                </form>

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
</body>
<script>
    document.addEventListener("DOMContentLoaded", function () {

        const toggleDatePicker = document.getElementById("toggleDatePicker");
        const datePopup = document.getElementById("datePopup");
        const startDate = document.getElementById("startDate");
        const endDate = document.getElementById("endDate");
        const dateText = document.getElementById("dateText");
        const cancelBtn = document.getElementById("cancelBtn");

        if (!toggleDatePicker || !datePopup) {
            console.error("Date picker not found");
            return;
        }

        toggleDatePicker.onclick = function (e) {
            e.stopPropagation();
            datePopup.classList.toggle("show");
        };

        if (cancelBtn) {
            cancelBtn.onclick = function () {
                datePopup.classList.remove("show");
            };
        }

        document.addEventListener("click", function (e) {

            if (
                !datePopup.contains(e.target) &&
                !toggleDatePicker.contains(e.target)
            ) {
                datePopup.classList.remove("show");
            }

        });

        function formatDate(value) {

            if (!value) return "";

            const date = new Date(value);

            return date.toLocaleDateString("vi-VN");
        }

        function updateText() {

            if (
                startDate &&
                endDate &&
                startDate.value &&
                endDate.value
            ) {

                dateText.textContent =
                    formatDate(startDate.value) +
                    " - " +
                    formatDate(endDate.value);

            } else {

                dateText.textContent = "Chọn khoảng ngày";
            }
        }

        if (startDate) {
            startDate.addEventListener("change", updateText);
        }

        if (endDate) {
            endDate.addEventListener("change", updateText);
        }

        updateText();
    });
</script>
</html>