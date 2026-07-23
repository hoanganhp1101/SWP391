<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý bệnh nhân - HealthAlert</title>
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

            overflow:visible;
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
            overflow-y:visible;
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

        .edit-btn{
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

        /* Filter dùng chung: xem /assets/css/filters.css */
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

            <div class="header">
                <h1>Quản lý bệnh nhân</h1>
                <p>Danh sách và quản lý thông tin bệnh nhân</p>
            </div>

            <div class="table-card">

                <div class="table-top">

                    <div class="patient-search-box">
                        <i class="fa-solid fa-magnifying-glass"></i>

                        <form method="get"
                              action="${pageContext.request.contextPath}/doctor/patient-list">

                            <input
                                    type="text"
                                    name="keyword"
                                    value="${param.keyword}"
                                    placeholder="Tìm kiếm..."
                            >

                            <input type="hidden" name="glucose" value="${param.glucose}">
                            <input type="hidden" name="hba1c" value="${param.hba1c}">
                            <input type="hidden" name="bmi" value="${param.bmi}">
                            <input type="hidden" name="bloodPressure" value="${param.bloodPressure}">
                            <input type="hidden" name="age" value="${param.age}">
                            <input type="hidden" name="gender" value="${param.gender}">
                            <input type="hidden" name="diabetesType" value="${param.diabetesType}">
                            <input type="hidden" name="action" value="${param.action}">
                        </form>
                    </div>
</div>

                <c:set var="basePath" value="${pageContext.request.contextPath}/doctor/patient-list"/>

                <c:choose>
                    <c:when test="${param.glucose eq 'normal'}"><c:set var="glucoseLabel" value="Bình thường"/></c:when>
                    <c:when test="${param.glucose eq 'high'}"><c:set var="glucoseLabel" value="Cao"/></c:when>
                    <c:when test="${param.glucose eq 'critical'}"><c:set var="glucoseLabel" value="Rất cao"/></c:when>
                    <c:when test="${param.glucose eq 'missing'}"><c:set var="glucoseLabel" value="Chưa đo"/></c:when>
                    <c:otherwise><c:set var="glucoseLabel" value="Glucose"/></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${param.hba1c eq 'normal'}"><c:set var="hba1cLabel" value="Bình thường"/></c:when>
                    <c:when test="${param.hba1c eq 'prediabetes'}"><c:set var="hba1cLabel" value="Tiền tiểu đường"/></c:when>
                    <c:when test="${param.hba1c eq 'high'}"><c:set var="hba1cLabel" value="Cao"/></c:when>
                    <c:when test="${param.hba1c eq 'missing'}"><c:set var="hba1cLabel" value="Chưa làm"/></c:when>
                    <c:otherwise><c:set var="hba1cLabel" value="HbA1c"/></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${param.bmi eq 'normal'}"><c:set var="bmiLabel" value="Bình thường"/></c:when>
                    <c:when test="${param.bmi eq 'overweight'}"><c:set var="bmiLabel" value="Thừa cân"/></c:when>
                    <c:when test="${param.bmi eq 'obese'}"><c:set var="bmiLabel" value="Béo phì"/></c:when>
                    <c:when test="${param.bmi eq 'missing'}"><c:set var="bmiLabel" value="Chưa đo"/></c:when>
                    <c:otherwise><c:set var="bmiLabel" value="BMI"/></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${param.action eq 'no-update'}"><c:set var="actionLabel" value="Chưa cập nhật 7 ngày"/></c:when>
                    <c:when test="${param.action eq 'no-followup'}"><c:set var="actionLabel" value="Chưa tái khám 30 ngày"/></c:when>
                    <c:otherwise><c:set var="actionLabel" value="Hành động"/></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${param.bloodPressure eq 'normal'}"><c:set var="bloodPressureLabel" value="Bình thường"/></c:when>
                    <c:when test="${param.bloodPressure eq 'high'}"><c:set var="bloodPressureLabel" value="Tăng huyết áp"/></c:when>
                    <c:when test="${param.bloodPressure eq 'low'}"><c:set var="bloodPressureLabel" value="Hạ huyết áp"/></c:when>
                    <c:when test="${param.bloodPressure eq 'missing'}"><c:set var="bloodPressureLabel" value="Chưa đo"/></c:when>
                    <c:otherwise><c:set var="bloodPressureLabel" value="Huyết áp"/></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${param.age eq 'child'}"><c:set var="ageLabel" value="Dưới 18 tuổi"/></c:when>
                    <c:when test="${param.age eq 'adult'}"><c:set var="ageLabel" value="18–39 tuổi"/></c:when>
                    <c:when test="${param.age eq 'middle'}"><c:set var="ageLabel" value="40–59 tuổi"/></c:when>
                    <c:when test="${param.age eq 'senior'}"><c:set var="ageLabel" value="Từ 60 tuổi"/></c:when>
                    <c:otherwise><c:set var="ageLabel" value="Tuổi"/></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${param.gender eq 'nam'}"><c:set var="genderLabel" value="Nam"/></c:when>
                    <c:when test="${param.gender eq 'nu'}"><c:set var="genderLabel" value="Nữ"/></c:when>
                    <c:when test="${param.gender eq 'khac'}"><c:set var="genderLabel" value="Khác"/></c:when>
                    <c:otherwise><c:set var="genderLabel" value="Giới tính"/></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${param.diabetesType eq 'Type 1'}"><c:set var="diabetesTypeLabel" value="Tiểu đường týp 1"/></c:when>
                    <c:when test="${param.diabetesType eq 'Type 2'}"><c:set var="diabetesTypeLabel" value="Tiểu đường týp 2"/></c:when>
                    <c:when test="${param.diabetesType eq 'Thai kỳ'}"><c:set var="diabetesTypeLabel" value="Thai kỳ"/></c:when>
                    <c:when test="${param.diabetesType eq 'Khác'}"><c:set var="diabetesTypeLabel" value="Khác"/></c:when>
                    <c:otherwise><c:set var="diabetesTypeLabel" value="Loại tiểu đường"/></c:otherwise>
                </c:choose>

                <div class="filter-bar">

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${glucoseLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.glucose ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.glucose eq 'normal' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=normal&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Bình thường</a>
                            <a class="filter-item ${param.glucose eq 'high' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=high&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Cao</a>
                            <a class="filter-item ${param.glucose eq 'critical' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=critical&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Rất cao</a>
                            <a class="filter-item ${param.glucose eq 'missing' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=missing&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Chưa đo</a>
                        </div>
                    </div>

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${hba1cLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.hba1c ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.hba1c eq 'normal' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=normal&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Bình thường</a>
                            <a class="filter-item ${param.hba1c eq 'prediabetes' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=prediabetes&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tiền tiểu đường</a>
                            <a class="filter-item ${param.hba1c eq 'high' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=high&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Cao</a>
                            <a class="filter-item ${param.hba1c eq 'missing' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=missing&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Chưa làm</a>
                        </div>
                    </div>

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${bmiLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.bmi ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.bmi eq 'normal' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=normal&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Bình thường</a>
                            <a class="filter-item ${param.bmi eq 'overweight' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=overweight&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Thừa cân</a>
                            <a class="filter-item ${param.bmi eq 'obese' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=obese&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Béo phì</a>
                            <a class="filter-item ${param.bmi eq 'missing' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=missing&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Chưa đo</a>
                        </div>
                    </div>

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${bloodPressureLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.bloodPressure ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.bloodPressure eq 'normal' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=normal&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Bình thường</a>
                            <a class="filter-item ${param.bloodPressure eq 'high' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=high&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tăng huyết áp</a>
                            <a class="filter-item ${param.bloodPressure eq 'low' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=low&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Hạ huyết áp</a>
                            <a class="filter-item ${param.bloodPressure eq 'missing' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=missing&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Chưa đo</a>
                        </div>
                    </div>

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${ageLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.age ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.age eq 'child' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=child&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Dưới 18 tuổi</a>
                            <a class="filter-item ${param.age eq 'adult' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=adult&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> 18–39 tuổi</a>
                            <a class="filter-item ${param.age eq 'middle' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=middle&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> 40–59 tuổi</a>
                            <a class="filter-item ${param.age eq 'senior' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=senior&gender=${param.gender}&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Từ 60 tuổi</a>
                        </div>
                    </div>

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${genderLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.gender ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.gender eq 'nam' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=nam&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Nam</a>
                            <a class="filter-item ${param.gender eq 'nu' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=nu&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Nữ</a>
                            <a class="filter-item ${param.gender eq 'khac' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=khac&diabetesType=${param.diabetesType}&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Khác</a>
                        </div>
                    </div>

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${diabetesTypeLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.diabetesType ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.diabetesType eq 'Type 1' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=Type%201&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tiểu đường týp 1</a>
                            <a class="filter-item ${param.diabetesType eq 'Type 2' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=Type%202&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Tiểu đường týp 2</a>
                            <a class="filter-item ${param.diabetesType eq 'Thai kỳ' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=Thai%20k%E1%BB%B3&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Thai kỳ</a>
                            <a class="filter-item ${param.diabetesType eq 'Khác' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=Kh%C3%A1c&action=${param.action}"><i class="fa-solid fa-check filter-check"></i> Khác</a>
                        </div>
                    </div>

                    <div class="filter-dropdown">
                        <button type="button" class="filter-button">
                            <span class="filter-label">${actionLabel}</span>
                            <i class="fa-solid fa-chevron-down"></i>
                        </button>
                        <div class="filter-menu">
                            <a class="filter-item ${empty param.action ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action="><i class="fa-solid fa-check filter-check"></i> Tất cả</a>
                            <a class="filter-item ${param.action eq 'no-update' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=no-update"><i class="fa-solid fa-check filter-check"></i> Chưa cập nhật 7 ngày</a>
                            <a class="filter-item ${param.action eq 'no-followup' ? 'active' : ''}" href="${basePath}?keyword=${param.keyword}&glucose=${param.glucose}&hba1c=${param.hba1c}&bmi=${param.bmi}&bloodPressure=${param.bloodPressure}&age=${param.age}&gender=${param.gender}&diabetesType=${param.diabetesType}&action=no-followup"><i class="fa-solid fa-check filter-check"></i> Chưa tái khám 30 ngày</a>
                        </div>
                    </div>

                </div>

                <div class="table-wrapper">

                    <table>

                        <thead>
                        <tr>
                            <th>MÃ BỆNH NHÂN</th>
                            <th>HỌ VÀ TÊN</th>
                            <th>TUỔI</th>
                            <th>GIỚI TÍNH</th>
                            <th>EMAIL</th>
                            <th>LOẠI TIỂU ĐƯỜNG</th>
                            <th>NGÀY CẬP NHẬT GẦN NHẤT</th>
                            <th>THAO TÁC</th>
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

                                <td>${p.loaiTieuDuong eq 'Type 1' ? 'Tiểu đường týp 1' : (p.loaiTieuDuong eq 'Type 2' ? 'Tiểu đường týp 2' : p.loaiTieuDuong)}</td>

                                <td>${p.ngayCapNhat}</td>

                                <td>
                                    <div class="action-buttons">

                                        <form method="post"
                                              action="${pageContext.request.contextPath}/doctor/patient-list"
                                              style="display:inline;">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button type="submit" class="edit-btn" title="Xem chi tiết">
                                                <i class="fa-solid fa-eye"></i>
                                            </button>
                                        </form>

                                    </div>

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