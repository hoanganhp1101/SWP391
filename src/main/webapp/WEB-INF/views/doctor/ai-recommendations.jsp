<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khuyến nghị AI</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        :root { --primary:#1557d5; --danger:#dc2626; --muted:#6b7280; --line:#e5e7eb; --bg:#f5f7fb; }
        body { margin:0; font-family:"Segoe UI",Inter,Arial,sans-serif; background:var(--bg); color:#1f2937; }
        .page-title h1 { font-size:26px; margin:0 0 4px; }
        .page-title p { color:var(--muted); margin:0 0 18px; }
        .flash { padding:12px 16px; border-radius:10px; margin-bottom:16px; font-weight:600; font-size:14px; }
        .flash.ok { background:#dcfce7; color:#166534; }
        .flash.err { background:#fee2e2; color:#991b1b; }
        .toolbar { display:flex; gap:12px; flex-wrap:wrap; align-items:end; margin-bottom:18px; }
        .field label { display:block; font-size:12px; font-weight:700; margin-bottom:4px; }
        .field select, .field input { height:40px; border:1px solid #cfd8e3; border-radius:10px; padding:0 10px; min-width:140px; }
        .btn { height:40px; padding:0 16px; border-radius:10px; border:none; font-weight:700; cursor:pointer; text-decoration:none; display:inline-flex; align-items:center; gap:8px; }
        .btn-primary { background:var(--primary); color:#fff; }
        .btn-secondary { background:#fff; border:1px solid var(--line); color:#374151; }
        table { width:100%; border-collapse:collapse; background:#fff; border-radius:14px; overflow:hidden; border:1px solid var(--line); }
        th, td { padding:12px 14px; border-bottom:1px solid var(--line); text-align:left; font-size:14px; }
        th { background:#f8fafc; color:#475569; font-size:12px; text-transform:uppercase; }
        .badge { display:inline-block; padding:4px 10px; border-radius:999px; font-size:12px; font-weight:700; }
        .badge.danger { background:#fee2e2; color:#991b1b; }
        .badge.high { background:#fef3c7; color:#92400e; }
        .badge.mid { background:#dbeafe; color:#1e40af; }
        .badge.ok { background:#dcfce7; color:#166534; }
        .empty { padding:40px; text-align:center; color:var(--muted); background:#fff; border:1px dashed var(--line); border-radius:14px; }
        .pager { display:flex; gap:8px; margin-top:16px; align-items:center; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/doctor/layout/topbar.jsp"/>
<div class="layout">
    <jsp:include page="/WEB-INF/views/doctor/layout/sidebar.jsp"/>
    <main class="main-content">
        <div class="page-title">
            <h1>Khuyến nghị AI</h1>
            <p><span>${total}</span> khuyến nghị — Gemini phân tích theo ngưỡng của bạn</p>
        </div>

        <c:if test="${param.synced == '1'}">
            <div class="flash ok">Đã đồng bộ: tạo ${param.created}, cập nhật ${param.refreshed}, Gemini ${param.gemini}/${param.scanned} BN.</div>
        </c:if>
        <c:if test="${param.error == '1'}">
            <div class="flash err">Đồng bộ gặp lỗi. <c:if test="${not empty param.errmsg}">${fn:escapeXml(param.errmsg)}</c:if></div>
        </c:if>

        <form class="toolbar" method="get" action="${pageContext.request.contextPath}/doctor/ai-recommendations">
            <div class="field">
                <label>Mức</label>
                <select name="level">
                    <option value="all" ${levelFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                    <option value="nguy_hiem" ${levelFilter == 'nguy_hiem' ? 'selected' : ''}>Nguy hiểm</option>
                    <option value="cao" ${levelFilter == 'cao' ? 'selected' : ''}>Cao</option>
                    <option value="trung_binh" ${levelFilter == 'trung_binh' ? 'selected' : ''}>Trung bình</option>
                </select>
            </div>
            <div class="field">
                <label>Trạng thái</label>
                <select name="status">
                    <option value="all" ${statusFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                    <option value="chua_xem" ${statusFilter == 'chua_xem' ? 'selected' : ''}>Chưa xem</option>
                    <option value="da_xem" ${statusFilter == 'da_xem' ? 'selected' : ''}>Đã xem</option>
                    <option value="da_ap_dung" ${statusFilter == 'da_ap_dung' ? 'selected' : ''}>Đã áp dụng</option>
                    <option value="bo_qua" ${statusFilter == 'bo_qua' ? 'selected' : ''}>Bỏ qua</option>
                </select>
            </div>
            <div class="field">
                <label>Tìm kiếm</label>
                <input type="search" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Tên bệnh nhân">
            </div>
            <button class="btn btn-primary" type="submit">Lọc</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doctor/ai-recommendations">Xóa lọc</a>
        </form>

        <form method="post" action="${pageContext.request.contextPath}/doctor/ai-recommendations" style="margin-bottom:16px;">
            <input type="hidden" name="sync" value="1">
            <button class="btn btn-primary" type="submit"
                    onclick="return confirm('Đồng bộ lại toàn bộ bằng Gemini? Có thể mất vài phút.');">
                <i class="fa-solid fa-arrows-rotate"></i> Đồng bộ Gemini
            </button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doctor/threshold-settings">
                <i class="fa-solid fa-sliders"></i> Ngưỡng
            </a>
        </form>

        <c:choose>
            <c:when test="${empty list}">
                <div class="empty">Chưa có khuyến nghị. Bấm “Đồng bộ Gemini” hoặc mở lại trang khi bệnh nhân đã có chỉ số.</div>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                    <tr>
                        <th>Bệnh nhân</th>
                        <th>Mức</th>
                        <th>Điểm</th>
                        <th>Trạng thái</th>
                        <th>Thời gian</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${list}">
                        <tr>
                            <td>
                                <div><c:out value="${item.hoTenBenhNhan}"/></div>
                                <div style="font-size:12px;color:#6b7280;">Mã BN: <c:out value="${item.maBenhNhanDisplay}"/></div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.mucCanhBao == 'nguy_hiem'}"><span class="badge danger">Nguy hiểm</span></c:when>
                                    <c:when test="${item.mucCanhBao == 'cao'}"><span class="badge high">Cao</span></c:when>
                                    <c:when test="${item.mucCanhBao == 'trung_binh'}"><span class="badge mid">Trung bình</span></c:when>
                                    <c:otherwise><span class="badge ok"><c:out value="${item.mucCanhBaoLabel}"/></span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${item.diemNguyCo}"/></td>
                            <td><c:out value="${item.trangThaiLabel}"/></td>
                            <td><c:out value="${item.thoiGianPhanTichDisplay}"/></td>
                            <td><a href="${pageContext.request.contextPath}/doctor/ai-recommendations?id=${item.id}">Chi tiết</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <c:if test="${totalPages > 1}">
                    <div class="pager">
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <a class="btn ${p == currentPage ? 'btn-primary' : 'btn-secondary'}"
                               href="${pageContext.request.contextPath}/doctor/ai-recommendations?page=${p}&level=${levelFilter}&status=${statusFilter}&keyword=${fn:escapeXml(keyword)}">${p}</a>
                        </c:forEach>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </main>
</div>
</body>
</html>
