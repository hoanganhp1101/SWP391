<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân tích hồ sơ nguy hiểm - HealthAlert</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        /* =============================
           Dangerous Patient — redesign (UI only)
           - Không đụng backend
           - Màu chính: blue #2563eb, đỏ #ef4444, vàng #f59e0b
           ============================= */
        :root{
            --blue:#2563eb;
            --red:#ef4444;
            --yellow:#f59e0b;
            --text:#111827;
            --muted:#6b7280;
            --card:#ffffff;
            --border:#e5e7eb;
            --shadow: 0 10px 24px rgba(0,0,0,.06);
        }

        *{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: Inter, sans-serif;
        }

        body{
            background: #f5f7fb;
            color: var(--text);
            font-size: 15px;
            font-weight: 400;
            line-height: 1.5;
        }

        .danger-page{
            width: 100%;
            max-width: none;
            margin: 0;
        }

        .danger-hero{
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 20px;
            box-shadow: var(--shadow);
            padding: 22px 24px;
            display: flex;
            gap: 18px;
            justify-content: space-between;
            align-items: flex-start;
        }

        .hero-left{
            display: flex;
            gap: 16px;
            align-items: flex-start;
        }

        .hero-avatar{
            width: 74px;
            height: 74px;
            border-radius: 20px;
            background: #eef2ff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            font-weight: 600;
            color: var(--blue);
            flex-shrink: 0;
        }

        .risk-badge{
            display: inline-flex;
            align-items: center;
            gap: 10px;
            padding: 7px 12px;
            border-radius: 999px;
            background: rgba(239,68,68,.08);
            color: var(--red);
            font-weight: 600;
            font-size: 13px;
            margin-top: 10px;
        }
        .risk-dot{
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: var(--red);
            box-shadow: 0 0 0 6px rgba(239,68,68,.08);
        }

        .hero-meta h1{
            margin: 0;
            font-size: 23px;
            font-weight: 600;
            color: var(--text);
            line-height: 1.3;
        }
        .hero-meta .code{
            color: var(--muted);
            font-weight: 500;
            margin-left: 8px;
            font-size: 14px;
        }

        .hero-sub{
            margin-top: 6px;
            color: var(--muted);
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            font-size: 14px;
            font-weight: 400;
            line-height: 1.5;
        }

        .hero-right{
            min-width: 260px;
            display: flex;
            justify-content: flex-end;
        }

        .vital-card{
            background: #fff;
            border: 1px solid #ffe4e6;
            border-radius: 18px;
            padding: 16px 18px;
            width: 100%;
            max-width: 300px;
        }

        .vital-title{
            color: #6b7280;
            font-weight: 600;
            font-size: 12px;
            letter-spacing: .05em;
        }
        .vital-value{
            margin-top: 8px;
            font-size: 32px;
            font-weight: 600;
            color: var(--red);
            line-height: 1.2;
        }

        .vital-unit-inline{
            margin-left: 8px;
            font-size: 14px;
            font-weight: 500;
            color: var(--red);
            vertical-align: middle;
        }
        .vital-time{
            margin-top: 12px;
            color: #9ca3af;
            font-size: 14px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .metric-grid{
            margin-top: 14px;
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 12px;
        }

        .metric-card{
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 20px;
            box-shadow: var(--shadow);
            padding: 14px 14px;
            min-height: 98px;
        }

        .metric-label{
            color: #6b7280;
            font-weight: 500;
            font-size: 14px;
            line-height: 1.5;
        }

        .metric-value-row{
            margin-top: 10px;
            display: flex;
            align-items: flex-end;
            justify-content: space-between;
            gap: 10px;
            flex-wrap: wrap;
        }

        .metric-value-block{
            min-width: 0;
        }

        .metric-value{
            font-weight: 600;
            color: #111827;
            font-size: 26px;
            line-height: 1.2;
        }
        .metric-unit{
            margin-top: 4px;
            color: #6b7280;
            font-weight: 400;
            font-size: 13px;
            line-height: 1.5;
        }

        .metric-level-badge{
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 5px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 500;
            line-height: 1.4;
            white-space: nowrap;
            flex-shrink: 0;
        }

        .metric-badge-normal{
            background: rgba(34, 197, 94, .12);
            color: #16a34a;
        }

        .metric-badge-high{
            background: rgba(245, 158, 11, .14);
            color: #b45309;
        }

        .metric-badge-danger{
            background: rgba(249, 115, 22, .14);
            color: #ea580c;
        }

        .metric-badge-very-high{
            background: rgba(239, 68, 68, .12);
            color: #dc2626;
        }

        .metric-badge-nodata{
            background: #f3f4f6;
            color: #6b7280;
        }

        .ai-analysis{
            margin-top: 0;
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 20px;
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        /* =============================
           Full-width dashboard layout
           70% (AI) / 30% (Summary)
           ============================= */
        .dashboard-grid{
            margin-top: 16px;
            display: grid;
            grid-template-columns: 2.25fr 1fr;
            gap: 18px;
            align-items: start;
        }

        .dashboard-left{
            min-width: 0;
        }

        .dashboard-right{
            min-width: 0;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .summary-right-card{
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 20px;
            box-shadow: var(--shadow);
            padding: 16px 16px;
        }

        .summary-right-title{
            color: #2563eb;
            font-weight: 600;
            font-size: 17px;
            line-height: 1.4;
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 10px;
        }

        .summary-right-subtitle{
            margin-top: 14px;
            margin-bottom: 10px;
            font-weight: 600;
            letter-spacing: .02em;
            color: #6b7280;
            font-size: 15px;
            line-height: 1.5;
        }

        @media (max-width: 950px){
            .dashboard-grid{
                grid-template-columns: 1fr;
            }
        }

        .ai-header{
            padding: 16px 18px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 14px;
            background: #ffffff;
        }

        .ai-title{
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .ai-title .top{
            display: inline-flex;
            align-items: center;
            gap: 10px;
            font-weight: 600;
            color: #2563eb;
            font-size: 18px;
            line-height: 1.4;
        }
        .ai-title .sub{
            color: #64748b;
            font-weight: 500;
            font-size: 14px;
            line-height: 1.5;
        }

        .ai-gemini-btn{
            border: 1px solid #dbeafe;
            background: #fff;
            color: #2563eb;
            padding: 10px 14px;
            border-radius: 14px;
            cursor: pointer;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            white-space: nowrap;
        }

        .ai-tabs{
            padding: 0 18px;
            border-top: 1px solid #f1f5f9;
            border-bottom: 1px solid #f1f5f9;
            background: #fff;
            display: flex;
            gap: 36px;
            overflow-x: auto;
        }

        .ai-tab{
            padding: 14px 0;
            border: none;
            background: transparent;
            cursor: pointer;
            font-weight: 600;
            color: #64748b;
            font-size: 14px;
            border-bottom: 3px solid transparent;
            white-space: nowrap;
        }
        .ai-tab.active{
            color: #2563eb;
            border-bottom-color: #2563eb;
        }

        .ai-panel{
            display: none;
            padding: 18px 18px 22px;
        }
        .ai-panel.active{
            display: block;
        }

        .section-title{
            margin-top: 6px;
            margin-bottom: 10px;
            font-weight: 600;
            letter-spacing: .02em;
            color: #6b7280;
            font-size: 15px;
            line-height: 1.5;
        }

        .section-title-gap{
            margin-top: 18px;
        }

        .w72{ width: 72%; }
        .w58{ width: 58%; }
        .w45{ width: 45%; }
        .w34{ width: 34%; }

        .ai-summary{
            color: #374151;
            line-height: 1.65;
            font-size: 15px;
            background: #f8fbff;
            border: 1px solid #dbeafe;
            border-radius: 16px;
            padding: 14px 14px;
        }

        .red-bullets{
            margin: 0;
            padding-left: 20px;
        }
        .red-bullets li{
            color: #ef4444;
            margin-bottom: 8px;
            font-weight: 600;
            line-height: 1.6;
        }

        .detail-cards{
            display: grid;
            gap: 12px;
        }

        .detail-subcard{
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 18px;
            padding: 14px 14px;
            color: #374151;
            line-height: 1.65;
            font-size: 14px;
            box-shadow: 0 6px 16px rgba(0,0,0,.04);
        }

        .js-hidden{ display:none; }

        .factor-list{
            display: grid;
            gap: 12px;
        }

        .factor-card{
            background: #fff;
            border-radius: 18px;
            border: 1px solid #e5e7eb;
            padding: 14px 14px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
        }

        .factor-left{
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .factor-dot{
            width: 18px;
            height: 18px;
            border-radius: 50%;
        }

        .factor-main .label{
            font-weight: 600;
            color: #111827;
            font-size: 14px;
        }
        .factor-main .value{
            margin-top: 6px;
            color: #6b7280;
            font-weight: 700;
            font-size: 14px;
        }

        .factor-arrow{
            color: #94a3b8;
            font-size: 16px;
        }

        .factor-glucose{ border-color: rgba(239,68,68,.25); background: rgba(239,68,68,.05); }
        .factor-glucose .factor-dot{ background: #ef4444; }

        .factor-bp{ border-color: rgba(239,68,68,.25); background: rgba(239,68,68,.05); }
        .factor-bp .factor-dot{ background: #ef4444; }

        .factor-hba1c{ border-color: rgba(245,158,11,.30); background: rgba(245,158,11,.08); }
        .factor-hba1c .factor-dot{ background: #f97316; }

        .factor-bmi{ border-color: rgba(245,158,11,.30); background: rgba(245,158,11,.08); }
        .factor-bmi .factor-dot{ background: #f59e0b; }

        .factor-warn{ border-color: rgba(245,158,11,.25); background: rgba(245,158,11,.06); }
        .factor-warn .factor-dot{ background: #f59e0b; }

        .factor-info{ border-color: rgba(37,99,235,.20); background: rgba(37,99,235,.06); }
        .factor-info .factor-dot{ background: #2563eb; }

        .progress-list{
            display: grid;
            gap: 14px;
        }

        .progress-item{
            display: grid;
            gap: 8px;
        }

        .progress-title{
            display: flex;
            justify-content: space-between;
            align-items: baseline;
            gap: 10px;
            font-weight: 600;
            color: #111827;
        }

        .progress-bar{
            height: 12px;
            border-radius: 999px;
            background: #e5e7eb;
            overflow: hidden;
        }

        .progress-fill{
            height: 100%;
            width: 50%;
            border-radius: 999px;
        }

        .fill-red{ background: var(--red); }
        .fill-yellow{ background: var(--yellow); }

        .rec-list{
            display: grid;
            gap: 12px;
        }

        .rec-card{
            background: #f7fbff;
            border: 1px solid #dbeafe;
            border-radius: 18px;
            padding: 14px 14px;
            display: flex;
            gap: 12px;
            align-items: flex-start;
        }

        .rec-dot{
            width: 22px;
            height: 22px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            margin-top: 2px;
            border: 3px solid transparent;
        }

        .dot-red{ background: rgba(239,68,68,.10); border-color: rgba(239,68,68,.20); }
        .dot-red i{ color: var(--red); }

        .dot-yellow{ background: rgba(245,158,11,.10); border-color: rgba(245,158,11,.20); }
        .dot-yellow i{ color: var(--yellow); }

        .dot-blue{ background: rgba(37,99,235,.10); border-color: rgba(37,99,235,.20); }
        .dot-blue i{ color: var(--blue); }

        .rec-content{
            flex: 1;
        }

        .rec-top{
            display: flex;
            gap: 10px;
            align-items: baseline;
            justify-content: space-between;
            flex-wrap: wrap;
        }

        .rec-badge{
            padding: 5px 10px;
            border-radius: 999px;
            font-weight: 600;
            font-size: 12px;
            white-space: nowrap;
        }
        .badge-danger{ background: rgba(239,68,68,.10); color: var(--red); border: 1px solid rgba(239,68,68,.25); }
        .badge-warning{ background: rgba(245,158,11,.12); color: #b45309; border: 1px solid rgba(245,158,11,.25); }
        .badge-info{ background: rgba(37,99,235,.10); color: var(--blue); border: 1px solid rgba(37,99,235,.25); }

        .rec-title{
            font-weight: 600;
            color: #111827;
        }
        .rec-text{
            margin-top: 8px;
            color: #374151;
            font-weight: 600;
            line-height: 1.6;
        }

        @media (max-width: 950px){
            .danger-hero{ flex-direction: column; }
            .hero-right{ min-width: 100%; justify-content: flex-start; }
            .metric-grid{ grid-template-columns: repeat(2, minmax(0, 1fr)); }
        }

        @media (max-width: 520px){
            .metric-grid{ grid-template-columns: 1fr; }
            .ai-tabs{ gap: 18px; }
        }

        /* =============================
           Lịch sử theo dõi (timeline)
           ============================= */
        .follow-history{
            margin-top: 14px;
            background: #fff;
            border: 1px solid var(--border);
            border-radius: 20px;
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        .follow-history-head{
            padding: 18px 18px 12px;
            background: #fff;
        }

        .follow-history-title-row{
            display:flex;
            align-items:flex-start;
            gap: 12px;
        }

        .follow-history-icon{
            width: 42px;
            height: 42px;
            border-radius: 14px;
            background: rgba(37,99,235,.08);
            border: 1px solid rgba(37,99,235,.20);
            display:flex;
            align-items:center;
            justify-content:center;
            color: var(--blue);
            flex-shrink: 0;
        }

        .follow-history-title{
            font-weight: 600;
            color: #111827;
            font-size: 18px;
            letter-spacing: .01em;
            line-height: 1.3;
        }

        .follow-history-sub{
            margin-top: 6px;
            color: #64748b;
            font-weight: 500;
            font-size: 14px;
            line-height: 1.5;
        }

        .timeline-list{
            padding: 0 18px 18px;
            display:flex;
            flex-direction: column;
            gap: 12px;
        }

        .timeline-item{
            display:flex;
            gap: 14px;
            border-radius: 18px;
            border: 1px solid #e5e7eb;
            background: #fff;
            padding: 16px 14px;
            position: relative;
        }

        .timeline-item-first{
            background: rgba(239,68,68,.06);
            border-color: rgba(239,68,68,.25);
        }

        .timeline-marker{
            width: 40px;
            position: relative;
            display:flex;
            flex-direction: column;
            align-items:center;
            flex-shrink: 0;
        }

        .timeline-marker::after{
            content:"";
            position:absolute;
            top: 44px;
            bottom: -12px;
            left: 50%;
            width: 2px;
            background: #e5e7eb;
            transform: translateX(-50%);
        }

        .timeline-item:last-child .timeline-marker::after{
            display:none;
        }

        .timeline-circle{
            width: 28px;
            height: 28px;
            border-radius: 999px;
            background: #eef2ff;
            border: 1px solid rgba(37,99,235,.22);
            display:flex;
            align-items:center;
            justify-content:center;
            color: var(--blue);
        }

        .timeline-content{
            flex: 1;
            min-width: 0;
        }

        .timeline-top{
            display:flex;
            gap: 12px;
            justify-content: space-between;
            align-items: flex-start;
            flex-wrap: wrap;
        }

        .timeline-date{
            font-weight: 600;
            color: #111827;
            font-size: 15px;
        }

        .timeline-badges{
            display:flex;
            gap: 8px;
            align-items:center;
            flex-wrap: wrap;
            justify-content: flex-end;
        }

        .badge-risk{
            padding: 7px 12px;
            border-radius: 999px;
            font-weight: 600;
            font-size: 13px;
            border: 1px solid transparent;
            white-space: nowrap;
        }

        .badge-risk-danger{
            background: rgba(239,68,68,.10);
            border-color: rgba(239,68,68,.20);
            color: #dc2626;
        }

        .badge-risk-high{
            background: rgba(245,158,11,.12);
            border-color: rgba(245,158,11,.24);
            color: #d97706;
        }

        .badge-risk-medium{
            background: rgba(37,99,235,.10);
            border-color: rgba(37,99,235,.22);
            color: #2563eb;
        }

        .badge-risk-low{
            background: rgba(34,197,94,.10);
            border-color: rgba(34,197,94,.20);
            color: #16a34a;
        }

        .badge-nearest{
            padding: 7px 12px;
            border-radius: 999px;
            font-weight: 600;
            font-size: 13px;
            background: rgba(37,99,235,.10);
            border: 1px solid rgba(37,99,235,.22);
            color: #2563eb;
            white-space: nowrap;
        }

        .timeline-metrics{
            margin-top: 12px;
            display:grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 16px;
        }

        .timeline-metric{
            display:flex;
            flex-direction: column;
            gap: 6px;
            min-width: 0;
        }

        .timeline-metric .label{
            font-weight: 600;
            font-size: 13px;
            color: #6b7280;
        }

        .timeline-metric .value{
            font-weight: 600;
            font-size: 16px;
            color: #111827;
            line-height: 1.2;
            word-break: break-word;
        }

        .timeline-metric .unit{
            font-weight: 600;
            font-size: 12px;
            color: #6b7280;
        }

        @media (max-width: 950px){
            .timeline-metrics{
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 520px){
            .timeline-metrics{
                grid-template-columns: 1fr;
            }
        }
    </style>
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
            <div class="danger-page">

                <!-- Hero -->
                <div class="danger-hero">
                    <div class="hero-left">
                        <div class="hero-avatar">${detail.initials}</div>

                        <div class="hero-meta">
                            <h1>
                                ${detail.patientName}
                                <span class="code">${detail.patientCode}</span>
                            </h1>

                            <div>
                                <div class="risk-badge">
                                    <span class="risk-dot"></span>
                                    <span>
                                        <c:choose>
                                            <c:when test="${detail.riskLevel eq 'critical'}">Nguy Hiểm</c:when>
                                            <c:when test="${detail.riskLevel eq 'high'}">Nguy Hiểm</c:when>
                                            <c:otherwise>Nguy Cơ</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>

                            <div class="hero-sub">
                                <span>
                                    Giới tính:
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty detail.recentRecords && detail.recentRecords.size() > 0 && detail.recentRecords[0].patient != null && not empty detail.recentRecords[0].patient.gioiTinh}">
                                                ${detail.recentRecords[0].patient.gioiTinh}
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </span>
                                <span>
                                    Tuổi:
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty detail.recentRecords && detail.recentRecords.size() > 0 && detail.recentRecords[0].patient != null && detail.recentRecords[0].patient.tuoi != null}">
                                                ${detail.recentRecords[0].patient.tuoi}
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </span>
                                <span>
                                    Loại tiểu đường:
                                    <strong>
                                        ${detail.loaiTieuDuong eq 'Type 1' ? 'Tiểu đường týp 1' : (detail.loaiTieuDuong eq 'Type 2' ? 'Tiểu đường týp 2' : (not empty detail.loaiTieuDuong ? detail.loaiTieuDuong : 'Tiểu đường'))}
                                    </strong>
                                </span>
                                <span>
                                    Cập nhật:
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty detail.recentRecords && detail.recentRecords.size() > 0 && detail.recentRecords[0].thoiGianDo != null}">
                                                ${detail.recentRecords[0].thoiGianDo.hour}:${detail.recentRecords[0].thoiGianDo.minute}
                                                · ${detail.recentRecords[0].thoiGianDo.dayOfMonth}/${detail.recentRecords[0].thoiGianDo.monthValue}/${detail.recentRecords[0].thoiGianDo.year}
                                            </c:when>
                                            <c:otherwise>${detail.timeAgo}</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </span>
                                <span>
                                    Bác sĩ phụ trách:
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty detail.recentRecords && detail.recentRecords.size() > 0 && detail.recentRecords[0].nhapBoi != null && not empty detail.recentRecords[0].nhapBoi.hoTen}">
                                                ${detail.recentRecords[0].nhapBoi.hoTen}
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </span>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- Metric cards -->
                <div class="metric-grid">
                    <div class="metric-card">
                        <div class="metric-label">Đường huyết</div>
                        <div class="metric-value-row">
                            <div class="metric-value-block">
                                <div class="metric-value">
                                    <c:choose>
                                        <c:when test="${detail.duongHuyetGanNhat != null}">
                                            <fmt:formatNumber value="${detail.duongHuyetGanNhat}" maxFractionDigits="0"/>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="metric-unit">mg/dL</div>
                            </div>
                            <c:if test="${glucoseLevel != null}">
                                <span class="metric-level-badge ${glucoseLevel.cssClass}">
                                    <c:if test="${not empty glucoseLevel.emoji}">${glucoseLevel.emoji} </c:if>${glucoseLevel.label}
                                </span>
                            </c:if>
                        </div>
                    </div>

                    <div class="metric-card">
                        <div class="metric-label">Huyết áp</div>
                        <div class="metric-value-row">
                            <div class="metric-value-block">
                                <div class="metric-value">
                                    <c:choose>
                                        <c:when test="${detail.huyetApTamThu != null && detail.huyetApTamTruong != null}">
                                            ${detail.huyetApTamThu}/${detail.huyetApTamTruong}
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="metric-unit">mmHg</div>
                            </div>
                            <c:if test="${bpLevel != null}">
                                <span class="metric-level-badge ${bpLevel.cssClass}">
                                    <c:if test="${not empty bpLevel.emoji}">${bpLevel.emoji} </c:if>${bpLevel.label}
                                </span>
                            </c:if>
                        </div>
                    </div>

                    <div class="metric-card">
                        <div class="metric-label">BMI</div>
                        <div class="metric-value-row">
                            <div class="metric-value-block">
                                <div class="metric-value">
                                    <c:choose>
                                        <c:when test="${detail.bmiGanNhat != null}">
                                            <fmt:formatNumber value="${detail.bmiGanNhat}" maxFractionDigits="1"/>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="metric-unit">kg/m²</div>
                            </div>
                            <c:if test="${bmiLevel != null}">
                                <span class="metric-level-badge ${bmiLevel.cssClass}">
                                    <c:if test="${not empty bmiLevel.emoji}">${bmiLevel.emoji} </c:if>${bmiLevel.label}
                                </span>
                            </c:if>
                        </div>
                    </div>

                    <div class="metric-card">
                        <div class="metric-label">HbA1c</div>
                        <div class="metric-value-row">
                            <div class="metric-value-block">
                                <div class="metric-value">
                                    <c:choose>
                                        <c:when test="${detail.hba1cGanNhat != null}">
                                            <fmt:formatNumber value="${detail.hba1cGanNhat}" maxFractionDigits="1"/>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="metric-unit">%</div>
                            </div>
                            <c:if test="${hba1cLevel != null}">
                                <span class="metric-level-badge ${hba1cLevel.cssClass}">
                                    <c:if test="${not empty hba1cLevel.emoji}">${hba1cLevel.emoji} </c:if>${hba1cLevel.label}
                                </span>
                            </c:if>
                        </div>
                    </div>
                </div>

                <div class="dashboard-grid">
                    <div class="dashboard-left">

                        <!-- AI Analysis -->
                        <div class="ai-analysis">
                    <div class="ai-header">
                        <div class="ai-title">
                            <div class="top">
                                <i class="fa-solid fa-brain"></i>
                                <span>Phân Tích AI</span>
                            </div>
                            <div class="sub">Gemini • Cập nhật ${detail.timeAgo}</div>
                        </div>
                        <button class="ai-gemini-btn" type="button">
                            <i class="fa-solid fa-wand-magic-sparkles"></i>
                            AI Gemini
                        </button>
                    </div>

                    <div class="ai-tabs" role="tablist" aria-label="AI Analysis Tabs">
                        <button class="ai-tab active" type="button" data-ai-tab="overview">Tổng Quan</button>
                        <button class="ai-tab" type="button" data-ai-tab="detail">Phân Tích Chi Tiết</button>
                        <button class="ai-tab" type="button" data-ai-tab="factors">Yếu Tố Nguy Cơ</button>
                        <button class="ai-tab" type="button" data-ai-tab="forecast">Dự Báo</button>
                        <button class="ai-tab" type="button" data-ai-tab="recommend">Khuyến Nghị</button>
                    </div>

                    <!-- Tổng Quan -->
                    <div class="ai-panel active" id="tab-overview" role="tabpanel">
                        <div class="section-title">TÓM TẮT</div>
                        <div class="ai-summary">${detail.aiSummary}</div>

                        <div class="section-title section-title-gap">PHÁT HIỆN CHÍNH</div>
                        <ul class="red-bullets">
                            <c:forEach items="${detail.riskReasons}" var="reason">
                                <li>${reason}</li>
                            </c:forEach>
                            <c:if test="${empty detail.riskReasons}">
                                <li>—</li>
                            </c:if>
                        </ul>
                    </div>

                    <!-- Phân Tích Chi Tiết -->
                    <div class="ai-panel" id="tab-detail" role="tabpanel">
                        <div class="section-title">PHÂN TÍCH CHI TIẾT</div>
                        <div class="detail-cards" id="aiDetailCards"></div>
                        <div id="aiDetailRaw" class="js-hidden">${detail.aiDetailAnalysis}</div>
                    </div>

                    <!-- Yếu Tố Nguy Cơ -->
                    <div class="ai-panel" id="tab-factors" role="tabpanel">
                        <div class="section-title">CÁC YẾU TỐ NGUY CƠ</div>
                        <div class="factor-list">
                            <c:forEach items="${detail.metricTags}" var="tag">
                                <div
                                    class="factor-card ${tag.type eq 'bp' ? 'factor-bp' :
                                                        (tag.type eq 'glucose' ? 'factor-glucose' :
                                                        (tag.type eq 'hba1c' ? 'factor-hba1c' :
                                                        (tag.type eq 'bmi' ? 'factor-bmi' :
                                                        (tag.type eq 'warning' or tag.type eq 'trend' ? 'factor-warn' : 'factor-info'))))}">
                                    <div class="factor-left">
                                        <div class="factor-dot"></div>
                                        <div class="factor-main">
                                            <div class="label">${tag.label}</div>
                                            <div class="value">${tag.value}</div>
                                        </div>
                                    </div>
                                    <div class="factor-arrow">
                                        <i class="fa-solid fa-chevron-right"></i>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty detail.metricTags}">
                                <div class="detail-subcard">—</div>
                            </c:if>
                        </div>
                    </div>

                    <!-- Dự Báo -->
                    <div class="ai-panel" id="tab-forecast" role="tabpanel">
                        <div class="section-title">DỰ BÁO RỦI RO (AI GEMINI)</div>
                        <div class="progress-list">
                            <div class="progress-item">
                                <div class="progress-title">
                                    <span>Biến chứng thận</span>
                                    <span>72%</span>
                                </div>
                                <div class="progress-bar">
                                    <div class="progress-fill fill-red w72"></div>
                                </div>
                            </div>
                            <div class="progress-item">
                                <div class="progress-title">
                                    <span>Bệnh tim mạch</span>
                                    <span>58%</span>
                                </div>
                                <div class="progress-bar">
                                    <div class="progress-fill fill-red w58"></div>
                                </div>
                            </div>
                            <div class="progress-item">
                                <div class="progress-title">
                                    <span>Tổn thương võng mạc</span>
                                    <span>45%</span>
                                </div>
                                <div class="progress-bar">
                                    <div class="progress-fill fill-yellow w45"></div>
                                </div>
                            </div>
                            <div class="progress-item">
                                <div class="progress-title">
                                    <span>Nhiễm trùng chi dưới</span>
                                    <span>34%</span>
                                </div>
                                <div class="progress-bar">
                                    <div class="progress-fill fill-yellow w34"></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Khuyến Nghị -->
                    <div class="ai-panel" id="tab-recommend" role="tabpanel">
                        <div class="section-title">KHUYẾN NGHỊ</div>
                        <div class="rec-list">
                            <c:forEach items="${detail.aiRecommendations}" var="rec" varStatus="st">
                                <c:choose>
                                    <c:when test="${st.index == 0}">
                                        <div class="rec-card">
                                            <div class="rec-dot dot-red">
                                                <i class="fa-solid fa-triangle-exclamation"></i>
                                            </div>
                                            <div class="rec-content">
                                                <div class="rec-top">
                                                    <div class="rec-title">${rec}</div>
                                                    <span class="rec-badge badge-danger">Khẩn cấp</span>
                                                </div>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:when test="${st.index == 1}">
                                        <div class="rec-card">
                                            <div class="rec-dot dot-yellow">
                                                <i class="fa-solid fa-clipboard-check"></i>
                                            </div>
                                            <div class="rec-content">
                                                <div class="rec-top">
                                                    <div class="rec-title">${rec}</div>
                                                    <span class="rec-badge badge-warning">Cao</span>
                                                </div>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="rec-card">
                                            <div class="rec-dot dot-blue">
                                                <i class="fa-solid fa-heart"></i>
                                            </div>
                                            <div class="rec-content">
                                                <div class="rec-top">
                                                    <div class="rec-title">${rec}</div>
                                                    <span class="rec-badge badge-info">Thường xuyên</span>
                                                </div>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:if test="${empty detail.aiRecommendations}">
                                <div class="detail-subcard">—</div>
                            </c:if>
                        </div>
                    </div>
                </div>
                        </div>

                    <div class="dashboard-right">
                        <div class="summary-right-card">
                            <div class="summary-right-title">
                                <i class="fa-solid fa-shield-heart" style="color:#ef4444;"></i>
                                TÓM TẮT & NGUY CƠ
                            </div>

                            <div class="ai-summary">${detail.aiSummary}</div>

                            <div class="summary-right-subtitle">PHÁT HIỆN CHÍNH</div>
                            <ul class="red-bullets">
                                <c:forEach items="${detail.riskReasons}" var="reason">
                                    <li>${reason}</li>
                                </c:forEach>
                                <c:if test="${empty detail.riskReasons}">
                                    <li>—</li>
                                </c:if>
                            </ul>
                        </div>
                    </div>
                </div>

            <!-- Lịch sử theo dõi -->
            <c:choose>
                <c:when test="${empty detail.recentRecords}">
                    <div class="follow-history">
                        <div class="follow-history-head">
                            <div class="follow-history-title-row">
                                <div class="follow-history-icon">
                                    <i class="fa-regular fa-calendar-days"></i>
                                </div>
                                <div>
                                    <div class="follow-history-title">Lịch Sử Theo Dõi</div>
                                    <div class="follow-history-sub">Mới nhất trước · 0 lần đo</div>
                                </div>
                            </div>
                        </div>
                        <div class="timeline-list">
                            <div class="detail-subcard">—</div>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="follow-history">
                        <div class="follow-history-head">
                            <div class="follow-history-title-row">
                                <div class="follow-history-icon">
                                    <i class="fa-regular fa-calendar-days"></i>
                                </div>
                                <div>
                                    <div class="follow-history-title">Lịch Sử Theo Dõi</div>
                                    <div class="follow-history-sub">
                                        Mới nhất trước · ${detail.recentRecords.size()} lần đo
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="timeline-list">
                            <c:forEach items="${detail.recentRecords}" var="r" varStatus="st" begin="0" end="4">
                                <div class="timeline-item ${st.index == 0 ? 'timeline-item-first' : ''}">
                                    <div class="timeline-marker">
                                        <div class="timeline-circle">
                                            <i class="fa-solid fa-clock-rotate-left"></i>
                                        </div>
                                    </div>

                                    <div class="timeline-content">
                                        <div class="timeline-top">
                                            <div class="timeline-date">
                                                <c:choose>
                                                    <c:when test="${r.thoiGianDo != null}">
                                                        ${r.thoiGianDo.dayOfMonth}/${r.thoiGianDo.monthValue}/${r.thoiGianDo.year}
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div class="timeline-badges">
                                                <c:choose>
                                                    <c:when test="${r.duongHuyetMgdl != null && r.duongHuyetMgdl >= 250}">
                                                        <span class="badge-risk badge-risk-danger">Nguy Hiểm</span>
                                                    </c:when>
                                                    <c:when test="${r.duongHuyetMgdl != null && r.duongHuyetMgdl >= 190}">
                                                        <span class="badge-risk badge-risk-high">Cao</span>
                                                    </c:when>
                                                    <c:when test="${r.duongHuyetMgdl != null && r.duongHuyetMgdl >= 180}">
                                                        <span class="badge-risk badge-risk-medium">Trung Bình</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-risk badge-risk-low">Thấp</span>
                                                    </c:otherwise>
                                                </c:choose>

                                                <c:if test="${st.index == 0}">
                                                    <span class="badge-nearest">Lần đo gần nhất</span>
                                                </c:if>
                                            </div>
                                        </div>

                                        <div class="timeline-metrics">
                                            <div class="timeline-metric">
                                                <div class="label">Đường huyết</div>
                                                <div class="value">
                                                    <c:choose>
                                                        <c:when test="${r.duongHuyetMgdl != null}">
                                                            <fmt:formatNumber value="${r.duongHuyetMgdl}" maxFractionDigits="0"/>
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <c:if test="${r.duongHuyetMgdl != null}">
                                                    <div class="unit">mg/dL</div>
                                                </c:if>
                                            </div>

                                            <div class="timeline-metric">
                                                <div class="label">Huyết áp</div>
                                                <div class="value">
                                                    <c:choose>
                                                        <c:when test="${r.huyetApTamThu != null && r.huyetApTamTruong != null}">
                                                            ${r.huyetApTamThu}/${r.huyetApTamTruong}
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <c:if test="${r.huyetApTamThu != null && r.huyetApTamTruong != null}">
                                                    <div class="unit">mmHg</div>
                                                </c:if>
                                            </div>

                                            <div class="timeline-metric">
                                                <div class="label">HbA1c</div>
                                                <div class="value">
                                                    <c:choose>
                                                        <c:when test="${r.hba1cPercent != null}">
                                                            <fmt:formatNumber value="${r.hba1cPercent}" maxFractionDigits="1"/>%
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>

                                            <div class="timeline-metric">
                                                <div class="label">BMI</div>
                                                <div class="value">
                                                    <c:choose>
                                                        <c:when test="${r.bmi != null}">
                                                            <fmt:formatNumber value="${r.bmi}" maxFractionDigits="1"/>
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <c:if test="${r.bmi != null}">
                                                    <div class="unit">kg/m²</div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>
</div>
<script>
    (function () {
        "use strict";
        function activateTab(tabName) {
            var buttons = document.querySelectorAll("[data-ai-tab]");
            var panels = document.querySelectorAll(".ai-panel");
            buttons.forEach(function (btn) {
                btn.classList.toggle("active", btn.getAttribute("data-ai-tab") === tabName);
            });
            panels.forEach(function (panel) {
                var id = panel.getAttribute("id") || "";
                panel.classList.toggle("active", id === "tab-" + tabName);
            });
        }

        var tabButtons = document.querySelectorAll("[data-ai-tab]");
        tabButtons.forEach(function (btn) {
            btn.addEventListener("click", function () {
                activateTab(btn.getAttribute("data-ai-tab"));
            });
        });

        // Split aiDetailAnalysis by paragraphs/newlines into cards
        var raw = document.getElementById("aiDetailRaw");
        var cards = document.getElementById("aiDetailCards");
        if (raw && cards) {
            var text = raw.textContent || "";
            var parts = text
                .split(/\n{2,}|\r?\n/)
                .map(function (s) { return s.trim(); })
                .filter(function (s) { return s.length > 0; });

            if (parts.length === 0) {
                parts = ["—"];
            }

            cards.innerHTML = "";
            parts.forEach(function (part) {
                var div = document.createElement("div");
                div.className = "detail-subcard";
                div.textContent = part;
                cards.appendChild(div);
            });

            raw.classList.add("js-hidden");
        }
    })();
</script>
</body>
</html>
