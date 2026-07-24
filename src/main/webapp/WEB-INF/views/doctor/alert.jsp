<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý cảnh báo sức khỏe</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        :root {
            --primary: #1557d5;
            --danger: #dc2626;
            --warning: #f59e0b;
            --success: #16a34a;
            --ink: #1f2937;
            --muted: #6b7280;
            --line: #e5e7eb;
            --bg: #f5f7fb;
        }

        * { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: "Segoe UI", Inter, Arial, sans-serif;
            background: var(--bg);
            color: var(--ink);
            height: 100vh;
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }

        /* ===== TOPBAR ===== */
        .topbar {
            height: 72px;
            flex-shrink: 0;
            background: #fff;
            display: flex;
            align-items: center;
            padding: 0 32px;
            border-bottom: 1px solid var(--line);
            z-index: 50;
        }

        .logo { font-size: 20px; font-weight: 700; color: #0d4bb5; }

        .top-nav { display: flex; gap: 32px; margin-left: 40px; }

        .top-nav a {
            color: #555;
            font-size: 15px;
            text-decoration: none;
            cursor: pointer;
        }

        .top-nav a.active { color: var(--primary); font-weight: 600; }

        .top-actions { display: flex; align-items: center; gap: 20px; margin-left: auto; }

        .search-box {
            width: 260px;
            height: 40px;
            display: flex;
            align-items: center;
            padding: 0 14px;
            border: 1px solid #d1d5db;
            border-radius: 10px;
            background: #fff;
        }

        .search-box i { color: #777; }
        .search-box input { border: none; outline: none; width: 100%; margin-left: 10px; font-size: 14px; }

        .top-icon { font-size: 20px; color: #4b5563; cursor: pointer; }

        .avatar { width: 38px; height: 38px; border-radius: 50%; object-fit: cover; }

        /* ===== LAYOUT ===== */
        .layout { display: flex; flex: 1; min-height: 0; overflow: hidden; }

        .sidebar {
            width: 240px;
            overflow-y: auto;
            background: #fff;
            border-right: 1px solid var(--line);
            display: flex;
            flex-direction: column;
            flex-shrink: 0;
        }

        .doctor-profile { padding: 24px 20px; display: flex; align-items: center; gap: 12px; }
        .doctor-profile img { width: 42px; height: 42px; border-radius: 10px; object-fit: cover; }
        .doctor-profile h4 { font-size: 15px; color: #1554c7; }
        .doctor-profile p { font-size: 12px; color: #666; }

        .menu { padding: 0 14px; }

        .menu-item {
            display: flex;
            align-items: center;
            gap: 14px;
            height: 48px;
            margin-bottom: 6px;
            padding: 0 16px;
            border-radius: 12px;
            color: #374151;
            text-decoration: none;
            cursor: pointer;
            font-size: 14px;
        }

        .menu-item i { font-size: 17px; width: 20px; text-align: center; }
        .menu-item.active { background: var(--primary); color: #fff; font-weight: 600; }
        .menu-item:not(.active):hover { background: #f1f5ff; }

        .sidebar-bottom { margin-top: auto; padding: 18px 16px; }

        .new-record {
            width: 100%;
            height: 46px;
            border: none;
            border-radius: 10px;
            background: #0d4bb5;
            color: #fff;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            margin-bottom: 8px;
        }

        .bottom-link {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            text-decoration: none;
            color: #374151;
            cursor: pointer;
            font-size: 14px;
        }

        .main-content { flex: 1; padding: 26px 30px; overflow-y: auto; min-width: 0; }

        /* ===== HEADER + FILTER ===== */
        .page-title h1 { font-size: 26px; margin-bottom: 4px; }
        .page-title p { color: var(--muted); margin-bottom: 18px; }
        .result-count { color: var(--primary); font-weight: 700; }

        .filter-panel {
            background: #fff;
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 16px;
            margin-bottom: 18px;
        }

        .filter-form {
            display: grid;
            grid-template-columns: repeat(4, minmax(140px, 1fr)) minmax(200px, 1.4fr) auto auto;
            gap: 12px;
            align-items: end;
        }

        .field { min-width: 0; }
        .field label { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 700; color: #374151; }

        select, input[type="search"] {
            width: 100%;
            height: 42px;
            border: 1px solid #cfd8e3;
            border-radius: 8px;
            padding: 0 12px;
            background: #fff;
            color: var(--ink);
            font-size: 14px;
            outline: none;
        }

        select:focus, input[type="search"]:focus, textarea:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(21, 87, 213, 0.12);
        }

        .btn {
            height: 42px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 8px;
            padding: 0 16px;
            border: 1px solid transparent;
            font-weight: 700;
            text-decoration: none;
            cursor: pointer;
            white-space: nowrap;
            font-size: 14px;
        }

        .btn-primary { background: var(--primary); color: #fff; }
        .btn-secondary { background: #fff; border-color: #cfd8e3; color: #374151; }

        /* ===== FLASH ===== */
        .flash-banner { margin-bottom: 16px; padding: 12px 16px; border-radius: 8px; font-weight: 700; }
        .flash-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .flash-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

        /* ===== WORKSPACE: DETAIL (left) + LIST (right) ===== */
        .workspace {
            display: grid;
            grid-template-columns: minmax(0, 1fr) 360px;
            gap: 18px;
            align-items: start;
        }

        .list-col { display: flex; flex-direction: column; gap: 12px; }

        .list-head { font-size: 13px; font-weight: 700; color: var(--muted); text-transform: uppercase; letter-spacing: .04em; }

        .alert-list { display: flex; flex-direction: column; gap: 10px; }

        .alert-item {
            background: #fff;
            border: 1px solid var(--line);
            border-left: 5px solid #9ca3af;
            border-radius: 10px;
            padding: 12px 14px;
            cursor: pointer;
            transition: box-shadow .15s, border-color .15s;
        }

        .alert-item:hover { box-shadow: 0 4px 14px rgba(15, 23, 42, 0.08); }
        .alert-item.active { border-color: var(--primary); box-shadow: 0 6px 18px rgba(21, 87, 213, 0.16); }
        .alert-item.severity-danger { border-left-color: var(--danger); }
        .alert-item.severity-high { border-left-color: var(--warning); }
        .alert-item.severity-medium { border-left-color: var(--success); }

        .item-name { font-weight: 800; font-size: 14px; display: flex; flex-wrap: wrap; align-items: baseline; gap: 8px; }
        .item-phone { font-weight: 600; font-size: 12px; color: #1d4ed8; }
        .item-title { color: #4b5563; font-size: 13px; margin: 4px 0 8px; line-height: 1.35; }
        .item-meta { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }

        .pill {
            display: inline-flex;
            align-items: center;
            min-height: 24px;
            border-radius: 999px;
            padding: 3px 9px;
            font-size: 11px;
            font-weight: 800;
        }

        .pill.severity-danger { background: #fee2e2; color: #991b1b; }
        .pill.severity-high { background: #fef3c7; color: #92400e; }
        .pill.severity-medium { background: #dcfce7; color: #166534; }
        .status-unread { background: #dbeafe; color: #1d4ed8; }
        .status-processing { background: #fef3c7; color: #92400e; }
        .status-resolved { background: #dcfce7; color: #166534; }
        .timestamp, .alert-type { color: var(--muted); font-size: 12px; }

        /* ===== DETAIL PANEL ===== */
        .detail-col { position: sticky; top: 0; align-self: start; }

        .detail-panel {
            background: #fff;
            border: 1px solid var(--line);
            border-radius: 14px;
            padding: 24px;
            box-shadow: 0 2px 10px rgba(15, 23, 42, 0.05);
        }

        .detail-empty {
            text-align: center;
            color: var(--muted);
            padding: 60px 20px;
        }

        .detail-empty i { font-size: 40px; color: #cbd5e1; margin-bottom: 14px; }

        .alert-detail { display: none; }
        .alert-detail.active { display: block; }

        .detail-patient { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 6px; }
        .detail-patient .name { font-size: 20px; font-weight: 800; }
        .patient-phone {
            font-size: 15px;
            font-weight: 600;
            color: #1d4ed8;
            white-space: nowrap;
        }
        .patient-phone.muted { color: var(--muted); font-weight: 500; }
        .patient-code { color: var(--muted); font-size: 13px; margin-bottom: 8px; }

        .detail-title { font-size: 18px; font-weight: 800; margin: 14px 0 8px; }
        .detail-content { color: #4b5563; line-height: 1.55; margin-bottom: 14px; }

        .detail-meta { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-bottom: 14px; }

        .detail-times {
            display: grid;
            gap: 6px;
            font-size: 13px;
            color: #374151;
            background: #f9fafb;
            border-radius: 8px;
            padding: 12px 14px;
            margin-bottom: 16px;
        }

        .detail-times span.label { color: var(--muted); margin-right: 6px; }

        .note-preview {
            font-size: 13px;
            color: #374151;
            background: #f1f5ff;
            border: 1px solid #dbe4ff;
            border-radius: 8px;
            padding: 10px 12px;
            margin-bottom: 16px;
            white-space: pre-line;
        }

        .quick-action-box { border-top: 1px dashed var(--line); padding-top: 16px; }
        .quick-action-box h4 { font-size: 14px; margin-bottom: 10px; }

        .quick-action-box textarea {
            width: 100%;
            min-height: 84px;
            border: 1px solid #cfd8e3;
            border-radius: 8px;
            padding: 10px 12px;
            font-family: inherit;
            font-size: 14px;
            resize: vertical;
        }

        .preset-chips { display: flex; flex-wrap: wrap; gap: 6px; margin: 10px 0; }

        .preset-chips button {
            border: 1px solid #dbeafe;
            background: #eff6ff;
            color: #1d4ed8;
            border-radius: 999px;
            padding: 4px 10px;
            font-size: 12px;
            font-weight: 700;
            cursor: pointer;
        }

        .resolve-check { display: flex; align-items: center; gap: 8px; font-size: 13px; margin: 6px 0 12px; }

        .detail-buttons { display: flex; gap: 10px; flex-wrap: wrap; }
        .detail-buttons .btn { flex: 1; min-width: 140px; }

        /* ===== PAGINATION ===== */
        .pagination-bar {
            display: flex;
            flex-direction: column;
            gap: 10px;
            margin-top: 6px;
            padding: 14px;
            background: #fff;
            border: 1px solid var(--line);
            border-radius: 10px;
        }

        .pagination-info { color: var(--muted); font-size: 13px; }
        .pagination-controls { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }

        .page-btn {
            min-width: 36px;
            height: 36px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 8px;
            border: 1px solid #cfd8e3;
            background: #fff;
            color: #374151;
            text-decoration: none;
            font-weight: 700;
            font-size: 13px;
            padding: 0 10px;
        }

        .page-btn:hover { border-color: var(--primary); color: var(--primary); }
        .page-btn.active { background: var(--primary); border-color: var(--primary); color: #fff; }
        .page-btn.disabled { opacity: 0.45; pointer-events: none; }

        .empty-state {
            background: #fff;
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 42px;
            text-align: center;
            color: var(--muted);
        }

        @media (max-width: 1100px) {
            .workspace { grid-template-columns: 1fr; }
            .detail-col { position: static; order: -1; }
            .filter-form { grid-template-columns: repeat(2, minmax(150px, 1fr)); }
        }

        @media (max-width: 760px) {
            .sidebar { display: none; }
            .top-nav, .search-box { display: none; }
            .filter-form { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<!-- TOPBAR -->
<header class="topbar">
    <div class="logo">HealthAlert</div>
    <nav class="top-nav">
        <a href="${pageContext.request.contextPath}/doctor-dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/doctor/patient-list">Patients</a>
        <a href="${pageContext.request.contextPath}/doctor/patient-records">Records</a>
        <a class="active">Alerts</a>
    </nav>
    <div class="top-actions">
        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" placeholder="Search medical records...">
        </div>
        <i class="fa-regular fa-bell top-icon"></i>
        <i class="fa-solid fa-gear top-icon"></i>
        <img class="avatar" src="https://i.pravatar.cc/40" alt="">
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
            <a href="${pageContext.request.contextPath}/doctor-dashboard" class="menu-item">
                <i class="fa-solid fa-table-cells"></i><span>Overview</span>
            </a>
            <a href="${pageContext.request.contextPath}/doctor/patient-list" class="menu-item">
                <i class="fa-solid fa-users"></i><span>Patient List</span>
            </a>
            <a href="${pageContext.request.contextPath}/doctor/alerts" class="menu-item active">
                <i class="fa-regular fa-bell"></i><span>Emergency Alerts</span>
            </a>
            <a href="${pageContext.request.contextPath}/doctor/patient-records" class="menu-item">
                <i class="fa-regular fa-clipboard"></i><span>Medical History</span>
            </a>
            <a href="${pageContext.request.contextPath}/doctor/analytics" class="menu-item">
                <i class="fa-solid fa-chart-column"></i><span>Analytics</span>
            </a>
            <a href="${pageContext.request.contextPath}/doctor/threshold-settings" class="menu-item">
                <i class="fa-solid fa-sliders"></i><span>Threshold Settings</span>
            </a>
            <a href="${pageContext.request.contextPath}/doctor/ai-recommendations" class="menu-item">
                <i class="fa-solid fa-robot"></i><span>AI Recommendations</span>
            </a>
        </nav>
        <div class="sidebar-bottom">
            <button class="new-record"><i class="fa-solid fa-plus"></i> New Record</button>
            <a class="bottom-link"><i class="fa-regular fa-circle-question"></i> Support</a>
            <a class="bottom-link"><i class="fa-solid fa-arrow-right-from-bracket"></i> Sign Out</a>
        </div>
    </aside>

    <!-- MAIN -->
    <main class="main-content">
        <div class="page-title">
            <h1>Danh sách cảnh báo</h1>
            <p>
                <span class="result-count">${totalAlerts}</span> cảnh báo phù hợp
                <c:if test="${totalAlerts > 0}"> — hiển thị ${fromIndex}–${toIndex}</c:if>
            </p>
        </div>

        <div class="filter-panel">
            <form class="filter-form" method="get" action="${pageContext.request.contextPath}/doctor/alerts">
                <div class="field">
                    <label for="severity">Mức độ</label>
                    <select id="severity" name="severity">
                        <option value="all" ${severityFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="danger" ${severityFilter == 'danger' ? 'selected' : ''}>Nguy hiểm - Đỏ</option>
                        <option value="high" ${severityFilter == 'high' ? 'selected' : ''}>Cao - Vàng</option>
                        <option value="medium" ${severityFilter == 'medium' ? 'selected' : ''}>Trung bình - Xanh</option>
                    </select>
                </div>
                <div class="field">
                    <label for="status">Trạng thái</label>
                    <select id="status" name="status">
                        <option value="all" ${statusFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="unread" ${statusFilter == 'unread' ? 'selected' : ''}>Chưa xem</option>
                        <option value="processing" ${statusFilter == 'processing' ? 'selected' : ''}>Đang xử lý</option>
                        <option value="resolved" ${statusFilter == 'resolved' ? 'selected' : ''}>Đã giải quyết</option>
                    </select>
                </div>
                <div class="field">
                    <label for="type">Loại cảnh báo</label>
                    <select id="type" name="type">
                        <option value="all" ${typeFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="glucose" ${typeFilter == 'glucose' ? 'selected' : ''}>Đường huyết cao/thấp</option>
                        <option value="missed_measurement" ${typeFilter == 'missed_measurement' ? 'selected' : ''}>Quên đo chỉ số</option>
                        <option value="missed_medication" ${typeFilter == 'missed_medication' ? 'selected' : ''}>Bỏ thuốc</option>
                        <option value="abnormal_trend" ${typeFilter == 'abnormal_trend' ? 'selected' : ''}>Xu hướng tăng bất thường</option>
                    </select>
                </div>
                <div class="field">
                    <label for="keyword">Tìm kiếm</label>
                    <input id="keyword" type="search" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Tên hoặc số điện thoại">
                </div>
                <button class="btn btn-primary" type="submit">Lọc</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doctor/alerts">Xóa lọc</a>
            </form>
        </div>

        <c:if test="${param.saved == '1'}">
            <div class="flash-banner flash-success">Đã lưu xử lý nhanh và cập nhật thời gian xử lý.</div>
        </c:if>
        <c:if test="${param.error == '1'}">
            <div class="flash-banner flash-error">Không thể lưu ghi chú. Vui lòng thử lại.</div>
        </c:if>

        <c:choose>
            <c:when test="${not empty listAlerts}">
                <div class="workspace">
                    <!-- DETAIL (bên trái) -->
                    <section class="detail-col">
                        <div class="detail-panel">
                            <div class="detail-empty" id="detailEmpty">
                                <i class="fa-regular fa-hand-pointer"></i>
                                <div>Chọn một cảnh báo ở danh sách bên phải để xem chi tiết và xử lý.</div>
                            </div>

                            <c:forEach items="${listAlerts}" var="a">
                                <div class="alert-detail" id="detail-${a.id}">
                                    <div class="detail-patient">
                                        <span class="name">
                                            <c:choose>
                                                <c:when test="${not empty a.hoTenBenhNhan}"><c:out value="${a.hoTenBenhNhan}" /></c:when>
                                                <c:otherwise>Chưa có tên bệnh nhân</c:otherwise>
                                            </c:choose>
                                        </span>
                                        <c:choose>
                                            <c:when test="${not empty a.soDienThoaiBenhNhan}">
                                                <span class="patient-phone"><c:out value="${a.soDienThoaiBenhNhan}" /></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="patient-phone muted">Chưa có SĐT</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <c:if test="${not empty a.patientId}">
                                        <div class="patient-code">Mã bệnh nhân: <c:out value="${a.patientId}" /></div>
                                    </c:if>

                                    <div class="detail-title"><c:out value="${a.tieuDe}" /></div>
                                    <div class="detail-content"><c:out value="${a.noiDung}" /></div>

                                    <div class="detail-meta">
                                        <span class="pill ${a.mucDoCss}"><c:out value="${a.mucDo}" /></span>
                                        <span class="pill ${a.trangThaiCss}"><c:out value="${a.trangThaiXuLy}" /></span>
                                        <c:if test="${not empty a.loaiCanhBao}">
                                            <span class="alert-type"><c:out value="${a.loaiCanhBao}" /></span>
                                        </c:if>
                                    </div>

                                    <div class="detail-times">
                                        <div><span class="label">Thời gian tạo:</span><c:out value="${a.thoiGianTao}" /></div>
                                        <div>
                                            <span class="label">Thời gian xử lý:</span>
                                            <c:choose>
                                                <c:when test="${not empty a.thoiGianXuLy}"><c:out value="${a.thoiGianXuLy}" /></c:when>
                                                <c:otherwise>Chưa xử lý</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <c:if test="${not empty a.ghiChuXuLy}">
                                        <div class="note-preview"><strong>Ghi chú xử lý:</strong>
<c:out value="${a.ghiChuXuLy}" /></div>
                                    </c:if>

                                    <div class="quick-action-box">
                                        <h4>Xử lý nhanh</h4>
                                        <form method="post" action="${pageContext.request.contextPath}/doctor/alerts/quick-action">
                                            <input type="hidden" name="alertId" value="${a.id}">
                                            <input type="hidden" name="severity" value="${severityFilter}">
                                            <input type="hidden" name="status" value="${statusFilter}">
                                            <input type="hidden" name="type" value="${typeFilter}">
                                            <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}">
                                            <input type="hidden" name="page" value="${currentPage}">
                                            <textarea name="ghiChu" rows="3" required maxlength="500"
                                                      placeholder="VD: Đã gọi điện nhắc bệnh nhân, Đã chỉnh liều"></textarea>
                                            <div class="preset-chips">
                                                <button type="button" data-text="Đã gọi điện nhắc bệnh nhân">Đã gọi điện</button>
                                                <button type="button" data-text="Đã chỉnh liều thuốc">Đã chỉnh liều</button>
                                                <button type="button" data-text="Đã nhắc bệnh nhân đo chỉ số">Đã nhắc đo chỉ số</button>
                                            </div>
                                            <label class="resolve-check">
                                                <input type="checkbox" name="markResolved" value="1">
                                                Đánh dấu đã giải quyết
                                            </label>
                                            <div class="detail-buttons">
                                                <button type="submit" class="btn btn-primary">Lưu xử lý</button>
                                                <a href="${pageContext.request.contextPath}/doctor/patient-records?patientId=${a.patientId}"
                                                   class="btn btn-secondary">Xem bệnh án chi tiết</a>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>

                    <!-- LIST (bên phải) -->
                    <aside class="list-col">
                        <div class="list-head">Cảnh báo (${fromIndex}–${toIndex} / ${totalAlerts})</div>
                        <div class="alert-list">
                            <c:forEach items="${listAlerts}" var="a">
                                <div class="alert-item ${a.mucDoCss}" data-detail-id="${a.id}">
                                    <div class="item-name">
                                        <c:choose>
                                            <c:when test="${not empty a.hoTenBenhNhan}"><c:out value="${a.hoTenBenhNhan}" /></c:when>
                                            <c:otherwise>Chưa có tên bệnh nhân</c:otherwise>
                                        </c:choose>
                                        <c:if test="${not empty a.soDienThoaiBenhNhan}">
                                            <span class="item-phone"><c:out value="${a.soDienThoaiBenhNhan}" /></span>
                                        </c:if>
                                    </div>
                                    <div class="item-title"><c:out value="${a.tieuDe}" /></div>
                                    <div class="item-meta">
                                        <span class="pill ${a.mucDoCss}"><c:out value="${a.mucDo}" /></span>
                                        <span class="pill ${a.trangThaiCss}"><c:out value="${a.trangThaiXuLy}" /></span>
                                        <span class="timestamp"><c:out value="${a.thoiGianTao}" /></span>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <c:if test="${totalPages > 1}">
                            <div class="pagination-bar">
                                <div class="pagination-info">Trang ${currentPage} / ${totalPages}</div>
                                <div class="pagination-controls">
                                    <c:url var="prevUrl" value="/doctor/alerts">
                                        <c:param name="severity" value="${severityFilter}"/>
                                        <c:param name="status" value="${statusFilter}"/>
                                        <c:param name="type" value="${typeFilter}"/>
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                                        <c:param name="page" value="${currentPage - 1}"/>
                                    </c:url>
                                    <c:url var="nextUrl" value="/doctor/alerts">
                                        <c:param name="severity" value="${severityFilter}"/>
                                        <c:param name="status" value="${statusFilter}"/>
                                        <c:param name="type" value="${typeFilter}"/>
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                                        <c:param name="page" value="${currentPage + 1}"/>
                                    </c:url>

                                    <a class="page-btn ${currentPage <= 1 ? 'disabled' : ''}" href="${currentPage <= 1 ? '#' : prevUrl}">Trước</a>
                                    <c:forEach begin="1" end="${totalPages}" var="p">
                                        <c:url var="pageUrl" value="/doctor/alerts">
                                            <c:param name="severity" value="${severityFilter}"/>
                                            <c:param name="status" value="${statusFilter}"/>
                                            <c:param name="type" value="${typeFilter}"/>
                                            <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                                            <c:param name="page" value="${p}"/>
                                        </c:url>
                                        <a class="page-btn ${p == currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a>
                                    </c:forEach>
                                    <a class="page-btn ${currentPage >= totalPages ? 'disabled' : ''}" href="${currentPage >= totalPages ? '#' : nextUrl}">Sau</a>
                                </div>
                            </div>
                        </c:if>
                    </aside>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">Không có cảnh báo phù hợp với bộ lọc hiện tại.</div>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<script>
    (function () {
        var items = document.querySelectorAll('.alert-item');
        var details = document.querySelectorAll('.alert-detail');
        var emptyHint = document.getElementById('detailEmpty');

        function showDetail(id) {
            details.forEach(function (d) { d.classList.remove('active'); });
            items.forEach(function (it) { it.classList.remove('active'); });

            var target = document.getElementById('detail-' + id);
            var item = document.querySelector('.alert-item[data-detail-id="' + id + '"]');
            if (target) {
                if (emptyHint) { emptyHint.style.display = 'none'; }
                target.classList.add('active');
            }
            if (item) { item.classList.add('active'); }
        }

        items.forEach(function (item) {
            item.addEventListener('click', function () {
                showDetail(item.getAttribute('data-detail-id'));
            });
        });

        // Tự chọn cảnh báo đầu tiên khi mở trang
        if (items.length > 0) {
            showDetail(items[0].getAttribute('data-detail-id'));
        }

        document.querySelectorAll('.preset-chips button').forEach(function (chip) {
            chip.addEventListener('click', function () {
                var form = chip.closest('form');
                var textarea = form.querySelector('textarea');
                textarea.value = chip.getAttribute('data-text');
                textarea.focus();
            });
        });
    })();
</script>
</body>
</html>
