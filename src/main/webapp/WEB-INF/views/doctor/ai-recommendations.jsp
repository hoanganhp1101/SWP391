<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Recommendations</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        :root {
            --primary: #1557d5; --danger: #dc2626; --warning: #f59e0b; --success: #16a34a;
            --ink: #1f2937; --muted: #6b7280; --line: #e5e7eb; --bg: #f5f7fb;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: "Segoe UI", Inter, Arial, sans-serif; background: var(--bg); color: var(--ink);
            height: 100vh; overflow: hidden; display: flex; flex-direction: column;
        }
        .topbar {
            height: 72px; flex-shrink: 0; background: #fff; display: flex; align-items: center;
            padding: 0 32px; border-bottom: 1px solid var(--line);
        }
        .logo { font-size: 20px; font-weight: 700; color: #0d4bb5; }
        .top-nav { display: flex; gap: 28px; margin-left: 40px; }
        .top-nav a { color: #555; text-decoration: none; font-size: 15px; }
        .top-nav a.active { color: var(--primary); font-weight: 600; }
        .top-actions { margin-left: auto; display: flex; align-items: center; gap: 16px; }
        .avatar { width: 38px; height: 38px; border-radius: 50%; }
        .layout { display: flex; flex: 1; min-height: 0; overflow: hidden; }
        .sidebar {
            width: 240px; background: #fff; border-right: 1px solid var(--line);
            display: flex; flex-direction: column; flex-shrink: 0; overflow-y: auto;
        }
        .doctor-profile { padding: 24px 20px; display: flex; gap: 12px; align-items: center; }
        .doctor-profile img { width: 42px; height: 42px; border-radius: 10px; }
        .doctor-profile h4 { font-size: 15px; color: #1554c7; }
        .doctor-profile p { font-size: 12px; color: #666; }
        .menu { padding: 0 14px; }
        .menu-item {
            display: flex; align-items: center; gap: 14px; height: 48px; margin-bottom: 6px;
            padding: 0 16px; border-radius: 12px; color: #374151; text-decoration: none; font-size: 14px;
        }
        .menu-item i { width: 20px; text-align: center; }
        .menu-item.active { background: var(--primary); color: #fff; font-weight: 600; }
        .menu-item:not(.active):hover { background: #f1f5ff; }
        .main-content { flex: 1; padding: 26px 30px; overflow-y: auto; min-width: 0; }
        .page-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; margin-bottom: 18px; flex-wrap: wrap; }
        .page-head h1 { font-size: 26px; margin-bottom: 4px; }
        .page-head p { color: var(--muted); font-size: 14px; }
        .flash { padding: 12px 16px; border-radius: 10px; margin-bottom: 16px; font-size: 14px; font-weight: 600; }
        .flash.ok { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .flash.err { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
        .card { background: #fff; border: 1px solid var(--line); border-radius: 16px; padding: 18px; }
        .filters { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 16px; align-items: end; }
        .filters label { display: block; font-size: 12px; font-weight: 700; margin-bottom: 4px; color: #374151; }
        .filters select, .filters input {
            height: 40px; border: 1px solid #cfd8e3; border-radius: 10px; padding: 0 12px; min-width: 160px;
        }
        .btn {
            height: 40px; padding: 0 16px; border-radius: 10px; border: none; font-weight: 700;
            cursor: pointer; display: inline-flex; align-items: center; gap: 8px; text-decoration: none; font-size: 14px;
        }
        .btn-primary { background: var(--primary); color: #fff; }
        .btn-secondary { background: #fff; color: #374151; border: 1px solid #cfd8e3; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px 10px; border-bottom: 1px solid var(--line); text-align: left; font-size: 14px; vertical-align: top; }
        th { color: var(--muted); font-size: 12px; letter-spacing: .02em; }
        .badge {
            display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 700;
        }
        .level-high { background: #fee2e2; color: #991b1b; }
        .level-medium { background: #ffedd5; color: #9a3412; }
        .level-low { background: #dcfce7; color: #166534; }
        .status { font-size: 12px; font-weight: 700; color: #374151; }
        .score { font-weight: 800; font-size: 16px; }
        .pager { display: flex; gap: 8px; align-items: center; margin-top: 16px; flex-wrap: wrap; }
        .pager a, .pager span {
            min-width: 36px; height: 36px; display: inline-flex; align-items: center; justify-content: center;
            border-radius: 8px; border: 1px solid var(--line); text-decoration: none; color: #374151; background: #fff;
        }
        .pager a.active { background: var(--primary); color: #fff; border-color: var(--primary); }
        .empty { text-align: center; color: var(--muted); padding: 40px 0; }
        @media (max-width: 960px) { .sidebar, .top-nav { display: none; } }
    </style>
</head>
<body>
<header class="topbar">
    <div class="logo">HealthAlert</div>
    <nav class="top-nav">
        <a href="${pageContext.request.contextPath}/doctor-dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/doctor/alerts">Alerts</a>
        <a href="${pageContext.request.contextPath}/doctor/analytics">Analytics</a>
        <a class="active">AI Recommendations</a>
    </nav>
    <div class="top-actions">
        <img class="avatar" src="https://i.pravatar.cc/40" alt="">
    </div>
</header>

<div class="layout">
    <aside class="sidebar">
        <div class="doctor-profile">
            <img src="https://i.pravatar.cc/60" alt="">
            <div><h4>Dr. Smith</h4><p>Endocrinologist</p></div>
        </div>
        <nav class="menu">
            <a href="${pageContext.request.contextPath}/doctor-dashboard" class="menu-item"><i class="fa-solid fa-table-cells"></i><span>Overview</span></a>
            <a href="${pageContext.request.contextPath}/doctor/patient-list" class="menu-item"><i class="fa-solid fa-users"></i><span>Patient List</span></a>
            <a href="${pageContext.request.contextPath}/doctor/alerts" class="menu-item"><i class="fa-regular fa-bell"></i><span>Emergency Alerts</span></a>
            <a href="${pageContext.request.contextPath}/doctor/patient-records" class="menu-item"><i class="fa-regular fa-clipboard"></i><span>Medical History</span></a>
            <a href="${pageContext.request.contextPath}/doctor/analytics" class="menu-item"><i class="fa-solid fa-chart-column"></i><span>Analytics</span></a>
            <a href="${pageContext.request.contextPath}/doctor/ai-recommendations" class="menu-item active"><i class="fa-solid fa-robot"></i><span>AI Recommendations</span></a>
            <a href="${pageContext.request.contextPath}/doctor/threshold-settings" class="menu-item"><i class="fa-solid fa-sliders"></i><span>Threshold Settings</span></a>
        </nav>
    </aside>

    <main class="main-content">
        <div class="page-head">
            <div>
                <h1>AI Recommendations</h1>
                <p>UC 16 — Khuyến nghị do Gemini sinh từ chỉ số sức khỏe + ngưỡng bác sĩ</p>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/doctor/ai-recommendations"
                  onsubmit="this.querySelector('button').disabled=true; this.querySelector('button').innerHTML='Đang gọi Gemini cho tất cả BN…';">
                <input type="hidden" name="sync" value="1">
                <button class="btn btn-primary" type="submit"
                        title="Gọi Gemini lần lượt cho mọi bệnh nhân (có nghỉ giữa các lần để tránh hết quota). Có thể mất 1–2 phút.">
                    <i class="fa-solid fa-wand-magic-sparkles"></i> Đồng bộ Gemini (tất cả BN)
                </button>
            </form>
        </div>

        <c:if test="${param.synced == '1'}">
            <div class="flash ok">
                Gemini đã xử lý <strong>${param.scanned}</strong> bệnh nhân:
                tạo mới <strong>${param.created}</strong>,
                viết lại <strong>${empty param.refreshed ? 0 : param.refreshed}</strong>,
                bỏ qua <strong>${param.skipped}</strong> (đã có bản Gemini hôm nay).
                <br/>Số bản Gemini thành công: <strong>${empty param.gemini ? 0 : param.gemini}</strong>
                · config: <strong>${param.geminiOn == '1' ? 'BẬT' : 'TẮT'}</strong>.
                <br/><small>Mở trang chỉ đọc danh sách đã lưu. Bấm nút để Gemini tạo/ghi đè lại toàn bộ (có thể mất 1–2 phút).</small>
                <c:if test="${param.geminiOn != '1'}">
                    <br/>Bật <code>gemini.enabled=true</code> + key trong <code>gemini.properties</code> rồi restart.
                </c:if>
                <c:if test="${not empty param.geminiErr}">
                    <br/><code style="white-space:pre-wrap;"><c:out value="${param.geminiErr}"/></code>
                </c:if>
                <c:if test="${fn:contains(param.geminiErr, '429') || fn:contains(param.geminiErr, 'quota') || fn:contains(param.geminiErr, 'Quota')}">
                    <br/><strong>Hết quota free (429)</strong> — đợi RPM hết đỏ trên AI Studio rồi bấm lại.
                </c:if>
                <c:if test="${param.scanned == '0'}">
                    <br/>Chưa có bệnh nhân gắn <code>bac_si_id</code>.
                </c:if>
            </div>
        </c:if>
        <c:if test="${param.error == '1'}">
            <div class="flash err">
                Đồng bộ lỗi (scanned=${param.scanned}, insertFailed=${param.failed}).
                <c:if test="${not empty param.errmsg}"><br/><code><c:out value="${param.errmsg}"/></code></c:if>
                <br/>Chạy script <code>ensure-ai-analysis.sql</code> rồi thử lại. Nếu vẫn lỗi, gửi dòng <code>errmsg</code> ở trên.
            </div>
        </c:if>

        <div class="card">
            <form class="filters" method="get" action="${pageContext.request.contextPath}/doctor/ai-recommendations">
                <div>
                    <label>Mức</label>
                    <select name="level">
                        <option value="all" ${levelFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="nguy_hiem" ${levelFilter == 'nguy_hiem' || levelFilter == 'high' ? 'selected' : ''}>Nguy hiểm</option>
                        <option value="cao" ${levelFilter == 'cao' ? 'selected' : ''}>Cao</option>
                        <option value="trung_binh" ${levelFilter == 'trung_binh' || levelFilter == 'medium' ? 'selected' : ''}>Trung bình</option>
                    </select>
                </div>
                <div>
                    <label>Trạng thái</label>
                    <select name="status">
                        <option value="all" ${statusFilter eq 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="chua_xem" ${statusFilter eq 'chua_xem' ? 'selected' : ''}>Chưa xem</option>
                        <option value="da_xem" ${statusFilter eq 'da_xem' ? 'selected' : ''}>Đã xem</option>
                        <option value="da_ap_dung" ${statusFilter eq 'da_ap_dung' ? 'selected' : ''}>Đã áp dụng</option>
                        <option value="bo_qua" ${statusFilter eq 'bo_qua' ? 'selected' : ''}>Bỏ qua</option>
                    </select>
                </div>
                <div>
                    <label>Tìm kiếm</label>
                    <input type="text" name="keyword" value="${keyword}" placeholder="Tên BN / nội dung...">
                </div>
                <button class="btn btn-primary" type="submit"><i class="fa-solid fa-filter"></i> Lọc</button>
            </form>

            <c:choose>
                <c:when test="${empty list}">
                    <div class="empty">Chưa có khuyến nghị. Bấm <strong>Đồng bộ Gemini (tất cả BN)</strong> để tạo danh sách (cần API key + còn quota).</div>
                </c:when>
                <c:otherwise>
                    <p style="color:var(--muted);font-size:13px;margin-bottom:10px;">Hiển thị ${fromIndex}–${toIndex} / ${total}</p>
                    <table>
                        <thead>
                        <tr>
                            <th>Thời gian</th>
                            <th>Bệnh nhân</th>
                            <th>Điểm</th>
                            <th>Mức</th>
                            <th>Khuyến nghị</th>
                            <th>Trạng thái</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${list}">
                            <tr>
                                <td><c:out value="${item.thoiGianPhanTichDisplay}"/></td>
                                <td><c:out value="${item.hoTenBenhNhan}"/></td>
                                <td class="score"><c:out value="${item.diemNguyCo}"/></td>
                                <td><span class="badge ${item.mucCanhBaoCss}"><c:out value="${item.mucCanhBaoLabel}"/></span></td>
                                <td><c:out value="${item.khuyenNghiShort}"/></td>
                                <td class="status"><c:out value="${item.trangThaiLabel}"/></td>
                                <td>
                                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doctor/ai-recommendations?id=${item.id}">
                                        Chi tiết
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>

                    <c:if test="${totalPages > 1}">
                        <div class="pager">
                            <c:forEach begin="1" end="${totalPages}" var="p">
                                <c:url var="pageUrl" value="/doctor/ai-recommendations">
                                    <c:param name="page" value="${p}"/>
                                    <c:param name="level" value="${levelFilter}"/>
                                    <c:param name="status" value="${statusFilter}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                </c:url>
                                <a class="${p == currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a>
                            </c:forEach>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</div>
</body>
</html>
