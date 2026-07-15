<%@ page import="com.example.diabetesmanage.model.Appointment" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
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

        .status-tabs{
            display:flex;
            gap:10px;
            margin-bottom:24px;
            flex-wrap:wrap;
        }

        .status-tab{
            padding:10px 18px;
            border-radius:999px;
            border:1px solid #e5e7eb;
            background:#fff;
            color:#374151;
            text-decoration:none;
            font-size:14px;
            font-weight:500;
        }

        .status-tab.active{
            background:#1554c7;
            color:#fff;
            border-color:#1554c7;
        }

        .status-badge{
            display:inline-block;
            padding:6px 12px;
            border-radius:999px;
            font-size:12px;
            font-weight:600;
        }

        .status-cho_kham{ background:#fef3c7; color:#92400e; }
        .status-da_kham{ background:#d1fae5; color:#065f46; }
        .status-huy, .status-da_huy{ background:#fee2e2; color:#991b1b; }

        .btn-status{
            padding:8px 12px;
            border-radius:8px;
            border:none;
            font-size:12px;
            font-weight:600;
            cursor:pointer;
            margin-right:6px;
        }

        .btn-complete{ background:#059669; color:#fff; }
        .btn-cancel{ background:#fef2f2; color:#dc2626; }

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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty doctor}">
    <c:set var="doctor" value="${sessionScope.user}"/>
</c:if>
<jsp:include page="/WEB-INF/views/doctor/layout/topbar.jsp"/>
<div class="layout">
    <jsp:include page="/WEB-INF/views/doctor/layout/sidebar.jsp"/>
    <main class="main-content">


        <div class="page-content">

            <c:if test="${param.updated eq '1'}">
                <div style="background:#d1fae5;border:1px solid #6ee7b7;color:#065f46;padding:14px 20px;border-radius:12px;margin-bottom:20px;font-weight:500;">
                    <i class="fa-solid fa-circle-check"></i>
                    <c:choose>
                        <c:when test="${param.encounterCreated eq '1'}">
                            Đã đánh dấu đã khám và tạo hồ sơ lần khám mới.
                        </c:when>
                        <c:otherwise>
                            Đã cập nhật trạng thái lịch khám.
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <div class="page-header">
                <h1>Quản lý lịch khám</h1>
                <p>Danh sách lịch hẹn và cập nhật trạng thái khám</p>
            </div>

            <div class="card">

                <div class="card-top">

                    <div class="record-search-box">
                        <i class="fa-solid fa-magnifying-glass"></i>

                        <form method="get"
                              action="${pageContext.request.contextPath}/doctor/medical-encounters">

                            <input type="hidden" name="status" value="${param.status}">

                            <input
                                    type="text"
                                    name="keyword"
                                    value="${param.keyword}"
                                    placeholder="Tìm kiếm bệnh nhân, nội dung khám..."
                            >

                        </form>
                    </div>

                </div>

                <div class="status-tabs">
                    <a class="status-tab ${empty param.status ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/doctor/medical-encounters?keyword=${param.keyword}">
                        Tất cả
                    </a>
                    <a class="status-tab ${param.status eq 'cho_kham' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/doctor/medical-encounters?status=cho_kham&keyword=${param.keyword}">
                        Chờ khám
                    </a>
                    <a class="status-tab ${param.status eq 'da_kham' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/doctor/medical-encounters?status=da_kham&keyword=${param.keyword}">
                        Đã khám
                    </a>
                    <a class="status-tab ${param.status eq 'da_huy' or param.status eq 'huy' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/doctor/medical-encounters?status=da_huy&keyword=${param.keyword}">
                        Hủy
                    </a>
                </div>

                <div class="table-wrapper">

                    <table class="record-table">

                        <thead>
                        <tr>
                            <th>TÊN BỆNH NHÂN</th>
                            <th>NỘI DUNG KHÁM</th>
                            <th>THỜI GIAN HẸN</th>
                            <th>ĐỊA ĐIỂM</th>
                            <th>TRẠNG THÁI</th>
                            <th>THAO TÁC</th>
                        </tr>
                        </thead>

                        <tbody>

                        <%
                            List<Appointment> appointments =
                                    (List<Appointment>) request.getAttribute("appointments");
                            DateTimeFormatter timeFmt =
                                    DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Locale.US);

                            if (appointments != null) {
                                for (Appointment appt : appointments) {
                                    String statusClass = "status-" + appt.getTrangThai();
                                    String timeText = appt.getThoiGianHen() != null
                                            ? appt.getThoiGianHen().toLocalDateTime().format(timeFmt)
                                            : "—";
                        %>

                        <tr>

                            <td><%= appt.getPatientName() %></td>
                            <td><%= appt.getNoiDungKham() %></td>
                            <td><%= timeText %></td>
                            <td><%= appt.getDiaDiem() %></td>
                            <td>
                                <span class="status-badge <%= statusClass %>">
                                    <%= appt.getTrangThaiLabel() %>
                                </span>
                            </td>
                            <td>
                                <% if (Appointment.STATUS_CHO_KHAM.equals(appt.getTrangThai())) { %>
                                <form method="post"
                                      action="${pageContext.request.contextPath}/doctor/appointments/status"
                                      style="display:inline;"
                                      onsubmit="return confirm('Đánh dấu đã khám và tạo hồ sơ lần khám?');">
                                    <input type="hidden" name="id" value="<%= appt.getId() %>">
                                    <input type="hidden" name="status" value="da_kham">
                                    <input type="hidden" name="filterStatus" value="${param.status}">
                                    <button type="submit" class="btn-status btn-complete">
                                        Đánh dấu đã khám
                                    </button>
                                </form>
                                <form method="post"
                                      action="${pageContext.request.contextPath}/doctor/appointments/status"
                                      style="display:inline;"
                                      onsubmit="return confirm('Hủy lịch hẹn này?');">
                                    <input type="hidden" name="id" value="<%= appt.getId() %>">
                                    <input type="hidden" name="status" value="da_huy">
                                    <input type="hidden" name="filterStatus" value="${param.status}">
                                    <button type="submit" class="btn-status btn-cancel">
                                        Hủy lịch
                                    </button>
                                </form>
                                <% } else { %>
                                —
                                <% } %>
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
</html>