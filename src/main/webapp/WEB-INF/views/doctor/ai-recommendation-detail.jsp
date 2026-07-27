<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết khuyến nghị AI</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        :root { --primary:#1557d5; --muted:#6b7280; --line:#e5e7eb; --bg:#f5f7fb; }
        body { margin:0; font-family:"Segoe UI",Inter,Arial,sans-serif; background:var(--bg); color:#1f2937; }
        .card { background:#fff; border:1px solid var(--line); border-radius:16px; padding:24px; margin-bottom:16px; }
        .meta { color:var(--muted); font-size:14px; margin-bottom:12px; }
        .section h3 { margin:0 0 8px; font-size:16px; }
        .section p, .section pre { white-space:pre-wrap; line-height:1.5; margin:0; font-family:inherit; font-size:14px; }
        .actions { display:flex; gap:10px; flex-wrap:wrap; align-items:end; }
        .field label { display:block; font-size:12px; font-weight:700; margin-bottom:4px; }
        .field select, .field textarea { width:100%; border:1px solid #cfd8e3; border-radius:10px; padding:10px; box-sizing:border-box; }
        .btn { height:40px; padding:0 16px; border-radius:10px; border:none; font-weight:700; cursor:pointer; text-decoration:none; display:inline-flex; align-items:center; }
        .btn-primary { background:var(--primary); color:#fff; }
        .btn-secondary { background:#fff; border:1px solid var(--line); color:#374151; }
        .flash { padding:12px 16px; border-radius:10px; margin-bottom:16px; font-weight:600; }
        .flash.ok { background:#dcfce7; color:#166534; }
        .flash.err { background:#fee2e2; color:#991b1b; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/doctor/layout/topbar.jsp"/>
<div class="layout">
    <jsp:include page="/WEB-INF/views/doctor/layout/sidebar.jsp"/>
    <main class="main-content">
        <c:if test="${param.saved == '1'}"><div class="flash ok">Đã cập nhật trạng thái.</div></c:if>
        <c:if test="${param.error == '1'}"><div class="flash err">Không cập nhật được. ${fn:escapeXml(param.errmsg)}</div></c:if>

        <div class="card">
            <h1 style="margin:0 0 8px;">${fn:escapeXml(detail.hoTenBenhNhan)}</h1>
            <div class="meta">
                Mức: <strong>${detail.mucCanhBao}</strong> · Điểm: <strong>${detail.diemNguyCo}</strong>
                · ${detail.thoiGianPhanTich} · Model: ${detail.modelVersion}
            </div>
        </div>

        <div class="card section">
            <h3>Phân tích</h3>
            <p>${fn:escapeXml(detail.phanTichChiTiet)}</p>
        </div>
        <div class="card section">
            <h3>Yếu tố nguy cơ</h3>
            <pre>${fn:escapeXml(detail.yeuToNguyCo)}</pre>
        </div>
        <div class="card section">
            <h3>Khuyến nghị</h3>
            <pre>${fn:escapeXml(detail.khuyenNghi)}</pre>
        </div>

        <div class="card">
            <form method="post" action="${pageContext.request.contextPath}/doctor/ai-recommendations">
                <input type="hidden" name="id" value="${detail.id}">
                <div class="field" style="margin-bottom:12px;">
                    <label for="status">Trạng thái xử lý</label>
                    <select id="status" name="status">
                        <option value="chua_xem" ${detail.trangThai == 'chua_xem' || empty detail.trangThai ? 'selected' : ''}>Chưa xem</option>
                        <option value="da_xem" ${detail.trangThai == 'da_xem' ? 'selected' : ''}>Đã xem</option>
                        <option value="da_ap_dung" ${detail.trangThai == 'da_ap_dung' ? 'selected' : ''}>Đã áp dụng</option>
                        <option value="bo_qua" ${detail.trangThai == 'bo_qua' ? 'selected' : ''}>Bỏ qua</option>
                    </select>
                </div>
                <div class="field" style="margin-bottom:12px;">
                    <label for="ghiChu">Ghi chú bác sĩ</label>
                    <textarea id="ghiChu" name="ghiChu" rows="3" placeholder="Ghi chú thêm (tuỳ chọn)">${fn:escapeXml(detail.ghiChuBs)}</textarea>
                </div>
                <div class="actions">
                    <button class="btn btn-primary" type="submit">Lưu</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doctor/ai-recommendations">Quay lại</a>
                </div>
            </form>
        </div>
    </main>
</div>
</body>
</html>
