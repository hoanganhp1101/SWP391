<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết khuyến nghị AI</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        :root {
            --primary: #1557d5; --danger: #dc2626; --ink: #1f2937; --muted: #6b7280;
            --line: #e5e7eb; --bg: #f5f7fb;
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
        .layout { display: flex; flex: 1; min-height: 0; overflow: hidden; }
        .sidebar {
            width: 240px; background: #fff; border-right: 1px solid var(--line);
            display: flex; flex-direction: column; flex-shrink: 0; overflow-y: auto;
        }
        .doctor-profile { padding: 24px 20px; display: flex; gap: 12px; align-items: center; }
        .doctor-profile img { width: 42px; height: 42px; border-radius: 10px; }
        .doctor-profile h4 { font-size: 15px; color: #1554c7; }
        .menu { padding: 0 14px; }
        .menu-item {
            display: flex; align-items: center; gap: 14px; height: 48px; margin-bottom: 6px;
            padding: 0 16px; border-radius: 12px; color: #374151; text-decoration: none; font-size: 14px;
        }
        .menu-item.active { background: var(--primary); color: #fff; font-weight: 600; }
        .menu-item:not(.active):hover { background: #f1f5ff; }
        .main-content { flex: 1; padding: 26px 30px; overflow-y: auto; }
        .back { display: inline-flex; gap: 8px; align-items: center; color: var(--primary); text-decoration: none; font-weight: 600; margin-bottom: 14px; }
        .flash { padding: 12px 16px; border-radius: 10px; margin-bottom: 16px; font-weight: 600; }
        .flash.ok { background: #dcfce7; color: #166534; }
        .flash.err { background: #fee2e2; color: #991b1b; }
        .grid { display: grid; grid-template-columns: 1.4fr 1fr; gap: 18px; }
        .card { background: #fff; border: 1px solid var(--line); border-radius: 16px; padding: 22px; }
        .card h1 { font-size: 24px; margin-bottom: 6px; }
        .meta { color: var(--muted); font-size: 14px; margin-bottom: 18px; }
        .kpi { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 18px; }
        .kpi-item { background: #f8fafc; border: 1px solid var(--line); border-radius: 12px; padding: 14px; }
        .kpi-item .label { font-size: 12px; color: var(--muted); }
        .kpi-item .value { font-size: 22px; font-weight: 800; margin-top: 4px; }
        .section { margin-top: 18px; }
        .section h3 { font-size: 15px; margin-bottom: 8px; }
        .section p, .section pre {
            white-space: pre-wrap; line-height: 1.55; font-size: 14px; color: #374151;
            font-family: inherit; background: #f8fafc; border-radius: 10px; padding: 12px; border: 1px solid var(--line);
        }
        .badge { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 700; }
        .level-high { background: #fee2e2; color: #991b1b; }
        .level-medium { background: #ffedd5; color: #9a3412; }
        .level-low { background: #dcfce7; color: #166534; }
        label { display: block; font-size: 13px; font-weight: 700; margin-bottom: 6px; }
        select, textarea {
            width: 100%; border: 1px solid #cfd8e3; border-radius: 10px; padding: 10px 12px; font-size: 14px;
        }
        textarea { min-height: 100px; resize: vertical; }
        .field { margin-bottom: 14px; }
        .btn {
            height: 42px; padding: 0 16px; border-radius: 10px; border: none; font-weight: 700;
            cursor: pointer; display: inline-flex; align-items: center; gap: 8px;
        }
        .btn-primary { background: var(--primary); color: #fff; }
        .btn-secondary { background: #fff; border: 1px solid #cfd8e3; color: #374151; text-decoration: none; }
        .actions { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 8px; }
        @media (max-width: 960px) {
            .sidebar { display: none; }
            .grid, .kpi { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<header class="topbar">
    <div class="logo">HealthAlert</div>
</header>
<div class="layout">
    <aside class="sidebar">
        <div class="doctor-profile">
            <img src="https://i.pravatar.cc/60" alt="">
            <div><h4>Dr. Smith</h4><p>Endocrinologist</p></div>
        </div>
        <nav class="menu">
            <a href="${pageContext.request.contextPath}/doctor/ai-recommendations" class="menu-item active"><i class="fa-solid fa-robot"></i><span>AI Recommendations</span></a>
            <a href="${pageContext.request.contextPath}/doctor/alerts" class="menu-item"><i class="fa-regular fa-bell"></i><span>Emergency Alerts</span></a>
            <a href="${pageContext.request.contextPath}/doctor/analytics" class="menu-item"><i class="fa-solid fa-chart-column"></i><span>Analytics</span></a>
            <a href="${pageContext.request.contextPath}/doctor/threshold-settings" class="menu-item"><i class="fa-solid fa-sliders"></i><span>Threshold Settings</span></a>
        </nav>
    </aside>

    <main class="main-content">
        <a class="back" href="${pageContext.request.contextPath}/doctor/ai-recommendations">
            <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách
        </a>

        <c:if test="${param.saved == '1'}"><div class="flash ok">Đã cập nhật trạng thái khuyến nghị.</div></c:if>
        <c:if test="${param.error == '1'}">
            <div class="flash err">
                Không thể cập nhật.
                <c:if test="${not empty param.errmsg}"><br/><small><c:out value="${param.errmsg}"/></small></c:if>
                <c:if test="${empty param.errmsg}"> Vui lòng thử lại.</c:if>
            </div>
        </c:if>

        <div class="grid">
            <div class="card">
                <h1><c:out value="${detail.hoTenBenhNhan}"/></h1>
                <div class="meta">
                    Phân tích lúc <c:out value="${detail.thoiGianPhanTich}"/> · Model <c:out value="${detail.modelVersion}"/>
                </div>

                <div class="kpi">
                    <div class="kpi-item">
                        <div class="label">Điểm nguy cơ</div>
                        <div class="value"><c:out value="${detail.diemNguyCo}"/></div>
                    </div>
                    <div class="kpi-item">
                        <div class="label">Mức cảnh báo</div>
                        <div class="value"><span class="badge ${detail.mucCanhBaoCss}"><c:out value="${detail.mucCanhBaoLabel}"/></span></div>
                    </div>
                    <div class="kpi-item">
                        <div class="label">Trạng thái</div>
                        <div class="value" style="font-size:16px;"><c:out value="${detail.trangThaiLabel}"/></div>
                    </div>
                </div>

                <div class="section">
                    <h3>Yếu tố nguy cơ</h3>
                    <p><c:out value="${detail.yeuToNguyCo}"/></p>
                </div>
                <div class="section">
                    <h3>Phân tích chi tiết</h3>
                    <p><c:out value="${detail.phanTichChiTiet}"/></p>
                </div>
                <div class="section">
                    <h3>Khuyến nghị</h3>
                    <pre><c:out value="${detail.khuyenNghi}"/></pre>
                </div>
                <div class="section">
                    <h3>Dữ liệu đầu vào (tóm tắt)</h3>
                    <p><c:out value="${detail.duLieuDauVaoDisplay}"/></p>
                </div>
            </div>

            <div class="card">
                <h3 style="margin-bottom:14px;">Quản lý khuyến nghị</h3>
                <form method="post" action="${pageContext.request.contextPath}/doctor/ai-recommendations">
                    <input type="hidden" name="id" value="${detail.id}">
                    <div class="field">
                        <label for="status">Cập nhật trạng thái</label>
                        <select id="status" name="status" required>
                            <option value="da_xem" ${detail.trangThai == 'da_xem' ? 'selected' : ''}>Đã xem</option>
                            <option value="da_ap_dung" ${detail.trangThai == 'da_ap_dung' ? 'selected' : ''}>Đã áp dụng</option>
                            <option value="bo_qua" ${detail.trangThai == 'bo_qua' ? 'selected' : ''}>Bỏ qua</option>
                            <option value="chua_xem" ${detail.trangThai == 'chua_xem' || empty detail.trangThai ? 'selected' : ''}>Chưa xem</option>
                        </select>
                    </div>
                    <div class="field">
                        <label for="ghiChu">Ghi chú bác sĩ (tuỳ chọn)</label>
                        <textarea id="ghiChu" name="ghiChu" placeholder="VD: Đã gọi nhắc bệnh nhân tái khám..."></textarea>
                    </div>
                    <c:if test="${not empty detail.ghiChuBs}">
                        <div class="section" style="margin-top:0;margin-bottom:14px;">
                            <h3>Ghi chú trước đó</h3>
                            <pre><c:out value="${detail.ghiChuBs}"/></pre>
                        </div>
                    </c:if>
                    <div class="actions">
                        <button type="submit" class="btn btn-primary"><i class="fa-solid fa-floppy-disk"></i> Lưu</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doctor/alerts">Xem Alerts</a>
                    </div>
                </form>
            </div>
        </div>
    </main>
</div>
</body>
</html>
