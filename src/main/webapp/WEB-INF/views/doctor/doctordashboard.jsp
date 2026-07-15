<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
            /* =========================
               GLOBAL
            ========================= */

        *{
            margin:0;
            padding:0;
            box-sizing:border-box;
            font-family:Inter, sans-serif;
        }

        body{
            background:#f5f7fb;
        }

        /* =========================
           LAYOUT
        ========================= */

        .layout{
            display:flex;
            height:calc(100vh - 80px);
        }

        .main-content{
            flex:1;
            background:#f5f7fb;
            padding:28px;
            overflow:auto;
        }

        /* =========================
           TOPBAR
        ========================= */

        .topbar{
            height:80px;
            background:#fff;
            display:flex;
            align-items:center;
            padding:0 48px;
            border-bottom:1px solid #e5e7eb;
        }

        .logo{
            font-size:20px;
            font-weight:700;
            color:#0d4bb5;
        }

        .top-nav{
            display:flex;
            gap:36px;
            margin-left:40px;
        }

        .top-nav a{
            color:#555;
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

        .top-actions{
            margin-left:auto;
            display:flex;
            align-items:center;
            gap:22px;
        }

        .search-box{
            width:290px;
            height:42px;
            display:flex;
            align-items:center;
            padding:0 16px;
            border:1px solid #d1d5db;
            border-radius:10px;
            background:white;
        }

        .search-box input{
            border:none;
            outline:none;
            width:100%;
            margin-left:10px;
        }

        .icon-btn{
            font-size:22px;
            color:#4b5563;
            cursor:pointer;
        }

        .topbar-avatar{
            width:38px;
            height:38px;
            border-radius:50%;
            object-fit:cover;
        }

        /* =========================
           SIDEBAR
        ========================= */

        .sidebar{
            width:240px;
            background:white;
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
        }

        .doctor-profile h4{
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
            padding:0 16px;
            margin-bottom:8px;
            border-radius:12px;
            color:#374151;
            text-decoration:none;
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
            font-weight:600;
            cursor:pointer;
        }

        .bottom-link{
            display:flex;
            align-items:center;
            gap:12px;
            padding:14px 12px;
            text-decoration:none;
            color:#374151;
        }

        /* =========================
           DASHBOARD HEADER
        ========================= */

        .dashboard-header h1{
            font-size:42px;
            margin-bottom:8px;
        }

        .dashboard-header p{
            color:#6b7280;
            font-size:18px;
        }

        /* =========================
           STATS CARDS
        ========================= */

        .stats-grid{
            margin-top:30px;
            display:grid;
            grid-template-columns:repeat(4,1fr);
            gap:24px;
        }

        .stat-card{
            background:white;
            border:1px solid #e5e7eb;
            border-radius:18px;
            padding:24px;
        }

        .card-top{
            display:flex;
            justify-content:space-between;
            align-items:flex-start;
        }

        .card-title{
            color:#6b7280;
        }

        .stat-card h2{
            margin-top:10px;
            font-size:42px;
        }

        .icon{
            width:56px;
            height:56px;
            border-radius:14px;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size:24px;
        }

        .icon.blue{
            background:#e8f0ff;
            color:#1557d5;
        }

        .icon.green{
            background:#e9faf3;
            color:#10b981;
        }

        .icon.yellow{
            background:#fff7e8;
            color:#f59e0b;
        }

        .icon.red{
            background:#feecec;
            color:#ef4444;
        }

        /* =========================
           RISK DISTRIBUTION
        ========================= */

        .risk-distribution-card{
            margin-top:30px;
            background:white;
            border:1px solid #e5e7eb;
            border-radius:20px;
            padding:25px;
        }

        .chart-container{
            width:320px;
            margin:auto;
        }

        .stats{
            margin-top:40px;
            display:grid;
            grid-template-columns:repeat(4,1fr);
            gap:20px;
        }

        .stat-item{
            text-align:center;
        }

        .value{
            display:flex;
            justify-content:center;
            align-items:center;
            gap:10px;
            font-size:42px;
            font-weight:700;
        }

        .dot{
            width:14px;
            height:14px;
            border-radius:50%;
        }

        .low{background:#10b981;}
        .medium{background:#f59e0b;}
        .high{background:#f97316;}
        .critical{background:#ef4444;}

        /* =========================
           BOTTOM SECTION
        ========================= */

        .dashboard-row{
            margin-top:30px;
        }

        .danger-section{
            background:white;
            border:1px solid #e5e7eb;
            border-radius:18px;
            padding:30px;
        }

        .card-header{
            display:flex;
            justify-content:space-between;
            align-items:center;
            margin-bottom:25px;
        }

        .title{
            display:flex;
            align-items:center;
            gap:12px;
        }

        .badge{
            background:#fde2e2;
            color:#b42318;
            padding:8px 14px;
            border-radius:999px;
            font-size:14px;
            font-weight:600;
        }

        .gemini-status{
            margin-bottom:20px;
            padding:14px 16px;
            border-radius:12px;
            font-size:14px;
        }

        .gemini-status.ok{
            background:#ecfdf3;
            color:#027a48;
        }

        .gemini-status.warn{
            background:#fff7e8;
            color:#b54708;
        }

        .gemini-status.error{
            background:#fef3f2;
            color:#b42318;
        }

        .danger-list{
            display:flex;
            flex-direction:column;
            gap:16px;
        }

        .danger-card{
            border:1px solid #e5e7eb;
            border-left:4px solid #d1d5db;
            border-radius:16px;
            padding:20px 22px;
            background:#fff;
        }

        .danger-card.critical{
            border-left-color:#ef4444;
            background:#fffbfb;
        }

        .danger-card.high{
            border-left-color:#f59e0b;
            background:#fffdf8;
        }

        .danger-card.medium{
            border-left-color:#3b82f6;
        }

        .danger-card-top{
            display:flex;
            justify-content:space-between;
            align-items:flex-start;
            gap:20px;
        }

        .danger-patient-info{
            display:flex;
            gap:14px;
            align-items:flex-start;
        }

        .danger-avatar{
            width:48px;
            height:48px;
            border-radius:50%;
            display:flex;
            align-items:center;
            justify-content:center;
            font-weight:700;
            font-size:15px;
            flex-shrink:0;
        }

        .danger-card.critical .danger-avatar{
            background:#fee2e2;
            color:#dc2626;
        }

        .danger-card.high .danger-avatar{
            background:#fef3c7;
            color:#d97706;
        }

        .danger-card.medium .danger-avatar{
            background:#dbeafe;
            color:#2563eb;
        }

        .danger-name{
            font-size:18px;
            font-weight:700;
            color:#111827;
        }

        .danger-code{
            color:#9ca3af;
            font-weight:500;
            font-size:15px;
            margin-left:8px;
        }

        .danger-type{
            display:inline-flex;
            align-items:center;
            gap:6px;
            margin-top:6px;
            font-size:13px;
            color:#6b7280;
        }

        .danger-type .dot{
            width:8px;
            height:8px;
            border-radius:50%;
            background:#9ca3af;
        }

        .danger-card.critical .danger-type .dot{background:#ef4444;}
        .danger-card.high .danger-type .dot{background:#f59e0b;}
        .danger-card.medium .danger-type .dot{background:#3b82f6;}

        .danger-vital{
            text-align:right;
            flex-shrink:0;
        }

        .danger-vital .value{
            font-size:28px;
            font-weight:700;
            line-height:1.1;
        }

        .danger-card.critical .danger-vital .value{color:#dc2626;}
        .danger-card.high .danger-vital .value{color:#d97706;}
        .danger-card.medium .danger-vital .value{color:#2563eb;}

        .danger-vital .time{
            margin-top:6px;
            font-size:13px;
            color:#9ca3af;
            display:flex;
            align-items:center;
            justify-content:flex-end;
            gap:6px;
        }

        .danger-tags{
            display:flex;
            flex-wrap:wrap;
            gap:10px;
            margin-top:16px;
        }

        .danger-tag{
            display:inline-flex;
            align-items:center;
            gap:8px;
            padding:8px 12px;
            border-radius:999px;
            font-size:13px;
            font-weight:500;
            background:#f3f4f6;
            color:#374151;
        }

        .danger-tag.glucose{background:#eff6ff;color:#1d4ed8;}
        .danger-tag.hba1c{background:#f5f3ff;color:#7c3aed;}
        .danger-tag.bp{background:#fef2f2;color:#b91c1c;}
        .danger-tag.bmi{background:#fff7ed;color:#c2410c;}
        .danger-tag.insulin{background:#ecfeff;color:#0e7490;}
        .danger-tag.warning,.danger-tag.trend{background:#fef3c7;color:#b45309;}

        .danger-ai{
            margin-top:16px;
            font-size:14px;
            line-height:1.7;
            color:#4b5563;
        }

        .danger-ai i{
            color:#2563eb;
            margin-right:6px;
        }

        .danger-card-footer{
            margin-top:16px;
            padding-top:14px;
            border-top:1px solid #f3f4f6;
            display:flex;
            justify-content:space-between;
            align-items:center;
        }

        .danger-urgent{
            display:flex;
            align-items:center;
            gap:8px;
            color:#dc2626;
            font-size:14px;
            font-weight:600;
        }

        .danger-view-link{
            color:#2563eb;
            text-decoration:none;
            font-weight:600;
            font-size:14px;
        }

        .danger-view-link:hover{
            text-decoration:underline;
        }

        .empty-danger{
            color:#6b7280;
            padding:24px 0;
            text-align:center;
        }

        .alert-box{
            background:#eef4ff;
            color:#1e40af;
            padding:18px;
            border-radius:12px;
            margin-bottom:24px;
        }

        .analytics-btn{
            width:100%;
            padding:14px;
            background:white;
            color:#1d4ed8;
            border:2px solid #1d4ed8;
            border-radius:12px;
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


        <div class="dashboard-container">

            <div class="dashboard-header">
                <h1>Tổng quan hệ thống</h1>
                <p>Theo dõi các chỉ số sức khỏe và mức độ rủi ro của bệnh nhân</p>
            </div>

            <!-- STATS -->
            <div class="stats-grid">

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Tổng số bệnh nhân</span>
                            <h2><fmt:formatNumber value="${stats.totalPatients}" groupingUsed="true"/></h2>
                        </div>

                        <div class="icon blue">
                            <i class="fa-solid fa-users"></i>
                        </div>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Cảnh báo đang hoạt động</span>
                            <h2><fmt:formatNumber value="${stats.activeAlerts}" groupingUsed="true"/></h2>
                        </div>

                        <div class="icon yellow">
                            <i class="fa-regular fa-bell"></i>
                        </div>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="card-top">
                        <div>
                            <span class="card-title">Hồ sơ khám bệnh hôm nay</span>
                            <h2><fmt:formatNumber value="${stats.todayHealthRecords}" groupingUsed="true"/></h2>
                        </div>

                        <div class="icon green">
                            <i class="fa-regular fa-file-lines"></i>
                        </div>
                    </div>
                </div>

            </div>

            <!-- RISK DISTRIBUTION -->
            <div class="risk-distribution-card">

                <h2>Phân bố mức độ rủi ro</h2>

                <div class="chart-container">
                    <canvas id="riskChart"></canvas>
                </div>

                <div class="stats">

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot low"></span>
                            ${stats.riskLow}
                        </div>
                        <p>Rủi ro thấp</p>
                    </div>

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot medium"></span>
                            ${stats.riskMedium}
                        </div>
                        <p>Rủi ro trung bình</p>
                    </div>

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot high"></span>
                            ${stats.riskHigh}
                        </div>
                        <p>Rủi ro cao</p>
                    </div>

                    <div class="stat-item">
                        <div class="value">
                            <span class="dot critical"></span>
                            ${stats.riskCritical}
                        </div>
                        <p>Rủi ro nghiêm trọng</p>
                    </div>

                </div>

            </div>

            <!-- DANGEROUS PATIENTS -->
            <div class="dashboard-row">

                <div class="danger-section" id="danger-section">

                    <div class="card-header">
                        <div class="title">
                            <i class="fa-solid fa-circle-exclamation"></i>
                            <h2>Hồ sơ nguy hiểm</h2>
                        </div>
                        <div class="badge">
                            ${analysisResult.totalDangerousCount} hồ sơ cần xem xét
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${analysisResult.geminiUsed}">
                            <div class="gemini-status ok">
                                <i class="fa-solid fa-sparkles"></i>
                                Gemini đang phân tích từng bệnh nhân có hồ sơ bất thường.
                            </div>
                        </c:when>
                        <c:when test="${analysisResult.geminiConfigured && not empty analysisResult.geminiError}">
                            <div class="gemini-status error">
                                <strong>Gemini lỗi:</strong> ${analysisResult.geminiError}
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="gemini-status warn">
                                Chưa kết nối Gemini — đang dùng phân tích theo quy tắc y khoa cho từng bệnh nhân.
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="danger-list">
                        <c:choose>
                            <c:when test="${empty urgentPatients}">
                                <div class="empty-danger">
                                    Không có hồ sơ bệnh án nguy hiểm.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${urgentPatients}" var="alert">
                                    <div class="danger-card ${alert.riskLevel}">
                                        <div class="danger-card-top">
                                            <div class="danger-patient-info">
                                                <div class="danger-avatar">${alert.initials}</div>
                                                <div>
                                                    <div>
                                                        <span class="danger-name">${alert.patientName}</span>
                                                        <span class="danger-code">${alert.patientCode}</span>
                                                    </div>
                                                    <div class="danger-type">
                                                        <span class="dot"></span>
                                                        <span>${not empty alert.loaiTieuDuong ? alert.loaiTieuDuong : 'Tiểu đường'}</span>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="danger-vital">
                                                <div class="value">${alert.vitalDisplay}</div>
                                                <div class="time">
                                                    <i class="fa-regular fa-clock"></i>
                                                    ${alert.timeAgo}
                                                </div>
                                            </div>
                                        </div>

                                        <c:if test="${not empty alert.metricTags}">
                                            <div class="danger-tags">
                                                <c:forEach items="${alert.metricTags}" var="tag">
                                                    <span class="danger-tag ${tag.type}">
                                                        <c:if test="${tag.type == 'glucose'}">
                                                            <i class="fa-solid fa-droplet"></i>
                                                        </c:if>
                                                        <c:if test="${tag.type == 'hba1c'}">
                                                            <i class="fa-solid fa-chart-line"></i>
                                                        </c:if>
                                                        <c:if test="${tag.type == 'bp'}">
                                                            <i class="fa-solid fa-heart-pulse"></i>
                                                        </c:if>
                                                        <c:if test="${tag.trending}">
                                                            <i class="fa-solid fa-arrow-trend-up"></i>
                                                        </c:if>
                                                        ${tag.label} · ${tag.value}
                                                    </span>
                                                </c:forEach>
                                            </div>
                                        </c:if>

                                        <div class="danger-ai">
                                            <i class="fa-solid fa-sparkles"></i>
                                            ${not empty alert.aiSummary
                                                ? alert.aiSummary
                                                : 'Cần theo dõi thêm các chỉ số sức khỏe của bệnh nhân.'}
                                        </div>

                                        <div class="danger-card-footer">
                                            <div>
                                                <c:if test="${alert.needsUrgentReview}">
                                                    <span class="danger-urgent">
                                                        <i class="fa-solid fa-triangle-exclamation"></i>
                                                        Cần xem xét ngay
                                                    </span>
                                                </c:if>
                                            </div>
                                            <a href="${pageContext.request.contextPath}/doctor/dangerous-patient-analysis?id=${alert.patientId}"
                                               class="danger-view-link">
                                                Xem hồ sơ <i class="fa-solid fa-chevron-right"></i>
                                            </a>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>

            </div>

        </div>
</main>
</div>

</body>
<script>
    const ctx = document.getElementById('riskChart');

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: [
                'Rủi ro thấp',
                'Rủi ro trung bình',
                'Rủi ro cao',
                'Rủi ro nghiêm trọng'
            ],
            datasets: [{
                data: [${stats.riskLow}, ${stats.riskMedium}, ${stats.riskHigh}, ${stats.riskCritical}],
                backgroundColor: [
                    '#10b981',
                    '#f59e0b',
                    '#f97316',
                    '#ef4444'
                ],
                borderColor: '#ffffff',
                borderWidth: 4
            }]
        },
        options: {
            responsive: true,
            cutout: '60%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        boxWidth: 10,
                        font: {
                            size: 14
                        }
                    }
                }
            }
        }
    });
</script>
</html>