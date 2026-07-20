<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý hồ sơ bệnh án - HealthAlert</title>
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
        .btn-delete {
            background: none;
            border: none;
            color: #dc3545;
            cursor: pointer;
            font-size: 18px;
            padding: 4px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            transition: color .2s ease;
        }

        .btn-delete:hover {
            color: #b02a37;
        }

        /* Filter dùng chung: xem /css/filters.css */

    </style>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<c:if test="${empty doctor}">
    <c:set var="doctor" value="${sessionScope.user}"/>
</c:if>
<jsp:include page="/WEB-INF/views/doctor/layout/topbar.jsp"/>
<div class="layout">
    <jsp:include page="/WEB-INF/views/doctor/layout/sidebar.jsp"/>
    <main class="main-content">


        <div class="page-content">

            <c:if test="${param.success eq '1'}">
                <div style="background:#d1fae5;border:1px solid #6ee7b7;color:#065f46;padding:14px 20px;border-radius:12px;margin-bottom:20px;font-weight:500;">
                    <i class="fa-solid fa-circle-check"></i>
                    Tạo hồ sơ khám bệnh thành công.
                </div>
            </c:if>
            <c:if test="${param.deleted eq '1'}">
                <div style="background:#d1fae5;border:1px solid #6ee7b7;color:#065f46;padding:14px 20px;border-radius:12px;margin-bottom:20px;font-weight:500;">
                    <i class="fa-solid fa-circle-check"></i>
                    Đã xóa hồ sơ khám bệnh.
                </div>
            </c:if>

            <div class="page-header">
                <h1>Quản lý hồ sơ khám bệnh</h1>
                <p>Danh sách và quản lý medical encounter</p>
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

                            <input type="hidden" name="type" value="${param.type}">
                            <input type="hidden" name="status" value="${param.status}">
                            <input type="hidden" name="startDate" value="${param.startDate}">
                            <input type="hidden" name="endDate" value="${param.endDate}">
                            <input type="hidden" name="patientId" value="${param.patientId}">

                        </form>
                    </div>

                    <div class="actions">

                        <form method="post"
                              action="${pageContext.request.contextPath}/doctor/patient-records"
                              style="display:inline;">
                            <input type="hidden" name="action" value="add">

                            <button type="submit" class="btn btn-primary">
                                <i class="fa-solid fa-plus"></i>
                                Thêm hồ sơ khám bệnh
                            </button>
                        </form>

                    </div>

                </div>

                <c:set var="typeLabel" value="${param.type eq 'tai_kham_noi_tiet' ? 'Bệnh án tái khám Nội tiết'
                        : (param.type eq 'mau_tong_quat' ? 'Kết quả xét nghiệm máu tổng quát'
                        : (param.type eq 'sinh_hoa_mau' ? 'Kết quả sinh hóa máu' : 'Loại hồ sơ'))}"/>
                <c:set var="statusLabel" value="${param.status eq 'da_kham' ? 'Đã khám' : 'Trạng thái'}"/>

                <div class="filter-bar">

                    <!-- Chọn khoảng ngày -->
                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${not empty param.startDate && not empty param.endDate
                                    ? param.startDate.concat(' → ').concat(param.endDate)
                                    : 'Chọn khoảng ngày'}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-popup">
                            <form action="${pageContext.request.contextPath}/doctor/patient-records" method="get">
                                <input type="hidden" name="keyword" value="${param.keyword}">
                                <input type="hidden" name="type" value="${param.type}">
                                <input type="hidden" name="status" value="${param.status}">
                                <input type="hidden" name="patientId" value="${param.patientId}">
                                <div class="filter-fields">
                                    <label>Từ ngày</label>
                                    <input type="date" name="startDate" value="${param.startDate}">
                                    <label>Đến ngày</label>
                                    <input type="date" name="endDate" value="${param.endDate}">
                                </div>
                                <div class="filter-actions">
                                    <button type="submit" class="btn-apply">Áp dụng</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Loại hồ sơ -->
                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${typeLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.type ? 'active' : ''}"
                               href="?type=&status=${param.status}&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&patientId=${param.patientId}">
                                <i class="fa-solid fa-check filter-check"></i> Tất cả
                            </a>
                            <a class="filter-item ${param.type eq 'tai_kham_noi_tiet' ? 'active' : ''}"
                               href="?type=tai_kham_noi_tiet&status=${param.status}&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&patientId=${param.patientId}">
                                <i class="fa-solid fa-check filter-check"></i> Bệnh án tái khám Nội tiết
                            </a>
                            <a class="filter-item ${param.type eq 'mau_tong_quat' ? 'active' : ''}"
                               href="?type=mau_tong_quat&status=${param.status}&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&patientId=${param.patientId}">
                                <i class="fa-solid fa-check filter-check"></i> Kết quả xét nghiệm máu tổng quát
                            </a>
                            <a class="filter-item ${param.type eq 'sinh_hoa_mau' ? 'active' : ''}"
                               href="?type=sinh_hoa_mau&status=${param.status}&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&patientId=${param.patientId}">
                                <i class="fa-solid fa-check filter-check"></i> Kết quả sinh hóa máu
                            </a>
                        </div>
                    </div>

                    <!-- Trạng thái -->
                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${statusLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.status ? 'active' : ''}"
                               href="?type=${param.type}&status=&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&patientId=${param.patientId}">
                                <i class="fa-solid fa-check filter-check"></i> Tất cả
                            </a>
                            <a class="filter-item ${param.status eq 'da_kham' ? 'active' : ''}"
                               href="?type=${param.type}&status=da_kham&keyword=${param.keyword}&startDate=${param.startDate}&endDate=${param.endDate}&patientId=${param.patientId}">
                                <i class="fa-solid fa-check filter-check"></i> Đã khám
                            </a>
                        </div>
                    </div>

                </div>

                <div class="table-wrapper">

                    <table class="record-table">

                        <thead>
                        <tr>
                            <th>MÃ ENCOUNTER</th>
                            <th>LOẠI HỒ SƠ</th>
                            <th>BỆNH NHÂN</th>
                            <th>BÁC SĨ</th>
                            <th>NGÀY KHÁM</th>
                            <th>TRẠNG THÁI</th>
                            <th>THỜI GIAN TẠO</th>
                            <th>THAO TÁC</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach var="record" items="${records}">
                        <tr>
                            <td><c:out value="${record.displayCode}"/></td>
                            <td><c:out value="${record.encounterTypeLabel}"/></td>
                            <td><c:out value="${record.patientDisplayName}"/></td>
                            <td><c:out value="${record.doctorDisplayName}"/></td>
                            <td><c:out value="${record.ngayKhamDisplay}"/></td>
                            <td><c:out value="${record.statusLabel}"/></td>
                            <td><c:out value="${record.ngayTaoDisplay}"/></td>
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/doctor/patient-records" style="display:inline;">
                                    <input type="hidden" name="action" value="detail">
                                    <input type="hidden" name="id" value="${record.id}">
                                    <button type="submit" class="table-icon-btn edit-btn" title="Xem chi tiết">
                                        <i class="fa-solid fa-eye"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                        </c:forEach>
                        </tbody>

                    </table>

                </div>

            </div>

        </div>
</main>
</div>

</body>
</html>