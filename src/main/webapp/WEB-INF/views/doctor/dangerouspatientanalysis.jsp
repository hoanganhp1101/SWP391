<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân tích hồ sơ nguy hiểm - ${detail.patientName}</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        *{margin:0;padding:0;box-sizing:border-box;font-family:Inter,sans-serif;}
        body{background:#f5f7fb;color:#111827;}
        .page{max-width:1100px;margin:0 auto;padding:32px 24px 48px;}
        .back-link{display:inline-flex;align-items:center;gap:8px;color:#2563eb;text-decoration:none;font-weight:600;margin-bottom:24px;}
        .hero{background:#fff;border:1px solid #e5e7eb;border-left:5px solid #ef4444;border-radius:18px;padding:28px;margin-bottom:24px;}
        .hero.high{border-left-color:#f59e0b;}
        .hero.medium{border-left-color:#3b82f6;}
        .hero-top{display:flex;justify-content:space-between;gap:20px;align-items:flex-start;}
        .patient-head{display:flex;gap:16px;align-items:center;}
        .avatar{width:64px;height:64px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:700;background:#fee2e2;color:#dc2626;}
        .hero.high .avatar{background:#fef3c7;color:#d97706;}
        .hero.medium .avatar{background:#dbeafe;color:#2563eb;}
        .hero h1{font-size:30px;margin-bottom:6px;}
        .hero .code{color:#9ca3af;font-size:16px;font-weight:500;}
        .hero .type{margin-top:8px;color:#6b7280;}
        .hero-vital{text-align:right;}
        .hero-vital .value{font-size:36px;font-weight:700;color:#dc2626;}
        .hero.high .hero-vital .value{color:#d97706;}
        .hero.medium .hero-vital .value{color:#2563eb;}
        .hero-vital .time{margin-top:8px;color:#9ca3af;font-size:14px;}
        .grid{display:grid;grid-template-columns:1.2fr 1fr;gap:24px;}
        .card{background:#fff;border:1px solid #e5e7eb;border-radius:18px;padding:24px;}
        .card h2{font-size:20px;margin-bottom:16px;display:flex;align-items:center;gap:10px;}
        .tags{display:flex;flex-wrap:wrap;gap:10px;margin-bottom:18px;}
        .tag{padding:8px 12px;border-radius:999px;background:#f3f4f6;font-size:13px;font-weight:500;}
        .tag.warn{background:#fef3c7;color:#b45309;}
        .tag.info{background:#eff6ff;color:#1d4ed8;}
        .tag.danger{background:#fef2f2;color:#b91c1c;}
        .reason-list,.recommend-list{padding-left:20px;line-height:1.8;color:#4b5563;}
        .reason-list li,.recommend-list li{margin-bottom:8px;}
        .ai-box{background:#f8fbff;border:1px solid #dbeafe;border-radius:14px;padding:18px;line-height:1.8;color:#374151;}
        .ai-box strong{color:#1d4ed8;}
        .status{margin-bottom:16px;padding:12px 14px;border-radius:12px;font-size:14px;}
        .status.ok{background:#ecfdf3;color:#027a48;}
        .status.warn{background:#fff7e8;color:#b54708;}
        .status.error{background:#fef3f2;color:#b42318;}
        table{width:100%;border-collapse:collapse;}
        th,td{padding:12px 10px;border-bottom:1px solid #f3f4f6;text-align:left;font-size:14px;}
        th{color:#6b7280;font-size:12px;text-transform:uppercase;}
        .urgent-badge{display:inline-flex;align-items:center;gap:8px;background:#fef2f2;color:#dc2626;padding:8px 12px;border-radius:999px;font-size:14px;font-weight:600;margin-top:16px;}
        @media (max-width:900px){.grid{grid-template-columns:1fr;}.hero-top{flex-direction:column;}}
    </style>
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
<div class="page">

    <a href="${pageContext.request.contextPath}/doctor-dashboard" class="back-link">
        <i class="fa-solid fa-arrow-left"></i> Quay lại bảng điều khiển
    </a>

    <div class="hero ${detail.riskLevel}">
        <div class="hero-top">
            <div class="patient-head">
                <div class="avatar">${detail.initials}</div>
                <div>
                    <h1>${detail.patientName} <span class="code">${detail.patientCode}</span></h1>
                    <div class="type">${detail.loaiTieuDuong eq 'Type 1' ? 'Tiểu đường týp 1' : (detail.loaiTieuDuong eq 'Type 2' ? 'Tiểu đường týp 2' : (not empty detail.loaiTieuDuong ? detail.loaiTieuDuong : 'Tiểu đường'))}</div>
                    <c:if test="${detail.needsUrgentReview}">
                        <div class="urgent-badge">
                            <i class="fa-solid fa-triangle-exclamation"></i> Cần xem xét ngay
                        </div>
                    </c:if>
                </div>
            </div>
            <div class="hero-vital">
                <div class="value">
                    <c:choose>
                        <c:when test="${detail.duongHuyetGanNhat != null}">
                            <fmt:formatNumber value="${detail.duongHuyetGanNhat}" maxFractionDigits="0"/> mg/dL
                        </c:when>
                        <c:when test="${detail.hba1cGanNhat != null}">
                            HbA1c <fmt:formatNumber value="${detail.hba1cGanNhat}" maxFractionDigits="1"/>%
                        </c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </div>
                <div class="time">
                    <i class="fa-regular fa-clock"></i> ${detail.timeAgo}
                </div>
            </div>
        </div>
    </div>

    <div class="grid">
        <div>
            <div class="card" style="margin-bottom:24px;">
                <h2><i class="fa-solid fa-sparkles"></i> Phân tích AI (Gemini)</h2>

                <c:choose>
                    <c:when test="${detail.geminiUsed}">
                        <div class="status ok">Gemini đã phân tích hồ sơ bệnh nhân này.</div>
                    </c:when>
                    <c:when test="${not empty detail.geminiError}">
                        <div class="status error">${detail.geminiError}</div>
                    </c:when>
                    <c:otherwise>
                        <div class="status warn">Đang dùng phân tích theo quy tắc y khoa.</div>
                    </c:otherwise>
                </c:choose>

                <div class="ai-box" style="margin-bottom:16px;">
                    <strong>Tóm tắt:</strong><br>
                    ${detail.aiSummary}
                </div>

                <div class="ai-box">
                    <strong>Phân tích chi tiết:</strong><br>
                    ${detail.aiDetailAnalysis}
                </div>
            </div>

            <div class="card">
                <h2><i class="fa-solid fa-lightbulb"></i> Khuyến nghị</h2>
                <ul class="recommend-list">
                    <c:forEach items="${detail.aiRecommendations}" var="rec">
                        <li>${rec}</li>
                    </c:forEach>
                </ul>
            </div>
        </div>

        <div>
            <div class="card" style="margin-bottom:24px;">
                <h2><i class="fa-solid fa-triangle-exclamation"></i> Dấu hiệu nguy hiểm</h2>
                <div class="tags">
                    <c:forEach items="${detail.metricTags}" var="tag">
                        <span class="tag ${tag.type == 'warning' || tag.type == 'trend' ? 'warn' : tag.type == 'glucose' || tag.type == 'hba1c' ? 'info' : tag.type == 'bp' ? 'danger' : ''}">
                            ${tag.label} · ${tag.value}
                        </span>
                    </c:forEach>
                </div>
                <ul class="reason-list">
                    <c:forEach items="${detail.riskReasons}" var="reason">
                        <li>${reason}</li>
                    </c:forEach>
                </ul>
            </div>

            <div class="card">
                <h2><i class="fa-solid fa-notes-medical"></i> Lịch sử đo gần đây</h2>
                <table>
                    <thead>
                    <tr>
                        <th>Thời gian</th>
                        <th>Đường huyết</th>
                        <th>HbA1c</th>
                        <th>Huyết áp</th>
                        <th>Insulin</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${detail.recentRecords}" var="r" begin="0" end="9">
                        <tr>
                            <td>
                                <c:if test="${r.thoiGianDo != null}">
                                    ${r.thoiGianDo.dayOfMonth}/${r.thoiGianDo.monthValue}/${r.thoiGianDo.year}
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${r.duongHuyetMgdl != null}">
                                    <fmt:formatNumber value="${r.duongHuyetMgdl}" maxFractionDigits="0"/>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${r.hba1cPercent != null}">
                                    <fmt:formatNumber value="${r.hba1cPercent}" maxFractionDigits="1"/>%
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${r.huyetApTamThu != null}">
                                    ${r.huyetApTamThu}/${r.huyetApTamTruong}
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${r.lieuLuongInsulinUi != null}">
                                    ${r.lieuLuongInsulinUi} UI
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</div>
</main>
</div>
</body>
</html>
