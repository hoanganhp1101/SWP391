<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu hình ngưỡng giám sát</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        :root {
            --primary: #1557d5;
            --danger: #dc2626;
            --ink: #1f2937;
            --muted: #6b7280;
            --line: #e5e7eb;
            --bg: #f5f7fb;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: "Segoe UI", Inter, Arial, sans-serif; background: var(--bg); color: var(--ink);
            height: 100vh; overflow: hidden; display: flex; flex-direction: column;
        }
        .topbar {
            height: 72px; flex-shrink: 0; background: #fff; display: flex; align-items: center;
            padding: 0 32px; border-bottom: 1px solid var(--line); z-index: 50;
        }
        .logo { font-size: 20px; font-weight: 700; color: #0d4bb5; }
        .top-nav { display: flex; gap: 32px; margin-left: 40px; }
        .top-nav a { color: #555; font-size: 15px; text-decoration: none; }
        .top-nav a.active { color: var(--primary); font-weight: 600; }
        .top-actions { display: flex; align-items: center; gap: 20px; margin-left: auto; }
        .top-icon { font-size: 20px; color: #4b5563; }
        .avatar { width: 38px; height: 38px; border-radius: 50%; object-fit: cover; }
        .layout { display: flex; flex: 1; min-height: 0; overflow: hidden; }
        .sidebar {
            width: 240px; background: #fff; border-right: 1px solid var(--line);
            display: flex; flex-direction: column; flex-shrink: 0; overflow-y: auto;
        }
        .doctor-profile { padding: 24px 20px; display: flex; align-items: center; gap: 12px; }
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
        .sidebar-bottom { margin-top: auto; padding: 18px 16px; }
        .bottom-link { display: flex; align-items: center; gap: 12px; padding: 12px; color: #374151; text-decoration: none; font-size: 14px; }
        .main-content { flex: 1; padding: 26px 30px; overflow-y: auto; min-width: 0; }
        .page-head { margin-bottom: 20px; }
        .page-head h1 { font-size: 26px; margin-bottom: 4px; }
        .page-head p { color: var(--muted); font-size: 14px; }
        .flash { padding: 12px 16px; border-radius: 10px; margin-bottom: 16px; font-size: 14px; font-weight: 600; }
        .flash.ok { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .flash.err { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
        .err-list { margin-bottom: 16px; padding: 12px 16px; background: #fef2f2; border: 1px solid #fecaca; border-radius: 10px; color: #991b1b; font-size: 14px; }
        .err-list li { margin-left: 18px; }
        .card { background: #fff; border: 1px solid var(--line); border-radius: 16px; padding: 28px 32px; width: 100%; }
        .card h2 { font-size: 17px; margin-bottom: 16px; }
        .field { margin-bottom: 16px; }
        .field label { display: block; font-size: 13px; font-weight: 700; margin-bottom: 6px; color: #374151; }
        .field .hint { font-size: 12px; color: var(--muted); margin-top: 4px; }
        .field input {
            width: 100%; height: 42px; border: 1px solid #cfd8e3; border-radius: 10px;
            padding: 0 12px; font-size: 14px;
        }
        .field input:focus { outline: 2px solid rgba(21, 87, 213, 0.25); border-color: var(--primary); }
        .form-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
        .actions { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 8px; }
        .btn {
            height: 42px; padding: 0 18px; border-radius: 10px; border: none; font-size: 14px;
            font-weight: 700; cursor: pointer; display: inline-flex; align-items: center; gap: 8px;
        }
        .btn-primary { background: var(--primary); color: #fff; }
        .btn-danger { background: #fff; color: var(--danger); border: 1px solid #fecaca; }
        .meta { font-size: 12px; color: var(--muted); margin-top: 12px; }
        @media (max-width: 1100px) {
            .form-row { grid-template-columns: 1fr 1fr; }
        }
        @media (max-width: 960px) {
            .sidebar, .top-nav { display: none; }
            .form-row { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<header class="topbar">
    <div class="logo">HealthAlert</div>
    <nav class="top-nav">
        <a href="${pageContext.request.contextPath}/doctor-dashboard">Dashboard</a>
        <a href="${pageContext.request.contextPath}/doctor/alerts">Alerts</a>
        <a href="${pageContext.request.contextPath}/doctor/analytics">Analytics</a>
        <a class="active">Threshold Settings</a>
    </nav>
    <div class="top-actions">
        <a href="${pageContext.request.contextPath}/doctor/threshold-settings" title="Cấu hình ngưỡng"><i class="fa-solid fa-gear top-icon"></i></a>
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
            <a href="${pageContext.request.contextPath}/doctor/threshold-settings" class="menu-item active"><i class="fa-solid fa-sliders"></i><span>Threshold Settings</span></a>
            <a href="${pageContext.request.contextPath}/doctor/ai-recommendations" class="menu-item"><i class="fa-solid fa-robot"></i><span>AI Recommendations</span></a>
        </nav>
        <div class="sidebar-bottom">
            <a class="bottom-link"><i class="fa-regular fa-circle-question"></i> Support</a>
        </div>
    </aside>

    <main class="main-content">
        <div class="page-head">
            <h1>Cấu hình ngưỡng giám sát</h1>
            <p>UC 17 — Thiết lập ngưỡng dùng cho Analytics (áp dụng cho bệnh nhân bạn phụ trách)</p>
        </div>

        <c:if test="${param.saved == '1'}">
            <div class="flash ok">
                <c:choose>
                    <c:when test="${param.reset == '1'}">Đã khôi phục ngưỡng mặc định.</c:when>
                    <c:otherwise>Đã lưu cấu hình ngưỡng thành công.</c:otherwise>
                </c:choose>
            </div>
        </c:if>
        <c:if test="${param.error == '1'}">
            <div class="flash err">Không thể lưu cấu hình. Vui lòng kiểm tra lại.</div>
        </c:if>
        <c:if test="${not empty errors}">
            <ul class="err-list">
                <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
            </ul>
        </c:if>

        <div class="card">
            <h2><i class="fa-solid fa-droplet"></i> Ngưỡng đường huyết</h2>
            <form method="post" action="${pageContext.request.contextPath}/doctor/threshold-settings">
                <div class="form-row">
                    <div class="field">
                        <label for="glucoseLow">Hạ đường huyết (&lt; mg/dL)</label>
                        <input type="number" id="glucoseLow" name="glucoseLow" min="40" max="100"
                               value="${form.glucoseLow}" required>
                        <div class="hint">Dùng cho TIR và đếm hạ đường huyết trên Analytics</div>
                    </div>
                    <div class="field">
                        <label for="glucoseHigh">Cao — giới hạn TIR (mg/dL)</label>
                        <input type="number" id="glucoseHigh" name="glucoseHigh" min="100" max="400"
                               value="${form.glucoseHigh}" required>
                        <div class="hint">Time in Range = trong khoảng hạ ↔ cao</div>
                    </div>
                    <div class="field">
                        <label for="glucoseDanger">Nguy hiểm — đỏ (≥ mg/dL)</label>
                        <input type="number" id="glucoseDanger" name="glucoseDanger" min="150" max="600"
                               value="${form.glucoseDanger}" required>
                        <div class="hint">Ngưỡng đường huyết rất cao</div>
                    </div>
                </div>

                <h2 style="margin-top:24px;"><i class="fa-solid fa-vial"></i> Ngưỡng HbA1c &amp; tuân thủ</h2>
                <div class="form-row">
                    <div class="field">
                        <label for="hba1cTarget">HbA1c mục tiêu (&lt; %)</label>
                        <input type="number" id="hba1cTarget" name="hba1cTarget" min="4" max="10" step="0.1"
                               value="${form.hba1cTarget}" required>
                    </div>
                    <div class="field">
                        <label for="hba1cPoor">HbA1c kiểm soát kém (≥ %)</label>
                        <input type="number" id="hba1cPoor" name="hba1cPoor" min="5" max="15" step="0.1"
                               value="${form.hba1cPoor}" required>
                    </div>
                    <div class="field">
                        <label for="daysNoMeasure">Không đo chỉ số (số ngày)</label>
                        <input type="number" id="daysNoMeasure" name="daysNoMeasure" min="1" max="90"
                               value="${form.daysNoMeasure}" required>
                        <div class="hint">Bệnh nhân không có health_record trong X ngày</div>
                    </div>
                </div>

                <div class="actions">
                    <button type="submit" class="btn btn-primary"><i class="fa-solid fa-floppy-disk"></i> Lưu cấu hình</button>
                    <button type="submit" class="btn btn-danger" formaction="${pageContext.request.contextPath}/doctor/threshold-settings"
                            name="reset" value="1"
                            onclick="return confirm('Khôi phục ngưỡng mặc định?');"
                            formnovalidate>
                        <i class="fa-solid fa-rotate-left"></i> Khôi phục mặc định
                    </button>
                </div>
            </form>
            <c:if test="${not empty form.ngayCapNhat}">
                <p class="meta">Cập nhật lần cuối: <c:out value="${form.ngayCapNhat}"/></p>
            </c:if>
        </div>
    </main>
</div>
</body>
</html>
