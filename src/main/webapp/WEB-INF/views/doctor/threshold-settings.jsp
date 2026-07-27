<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu hình ngưỡng giám sát</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/doctor-layout.css">
    <style>
        :root { --primary:#1557d5; --danger:#dc2626; --muted:#6b7280; --line:#e5e7eb; --bg:#f5f7fb; }
        body { margin:0; font-family:"Segoe UI",Inter,Arial,sans-serif; background:var(--bg); color:#1f2937; }
        .page-head { margin-bottom:20px; }
        .page-head h1 { font-size:26px; margin:0 0 4px; }
        .page-head p { color:var(--muted); font-size:14px; margin:0; }
        .flash { padding:12px 16px; border-radius:10px; margin-bottom:16px; font-size:14px; font-weight:600; }
        .flash.ok { background:#dcfce7; color:#166534; border:1px solid #bbf7d0; }
        .flash.err { background:#fee2e2; color:#991b1b; border:1px solid #fecaca; }
        .err-list { margin-bottom:16px; padding:12px 16px; background:#fef2f2; border:1px solid #fecaca; border-radius:10px; color:#991b1b; }
        .err-list li { margin-left:18px; }
        .card { background:#fff; border:1px solid var(--line); border-radius:16px; padding:28px 32px; }
        .card h2 { font-size:17px; margin:0 0 16px; }
        .form-row { display:grid; grid-template-columns:repeat(3,1fr); gap:18px; }
        .field { margin-bottom:16px; }
        .field label { display:block; font-size:13px; font-weight:700; margin-bottom:6px; }
        .field .hint { font-size:12px; color:var(--muted); margin-top:4px; }
        .field input { width:100%; height:42px; border:1px solid #cfd8e3; border-radius:10px; padding:0 12px; font-size:14px; box-sizing:border-box; }
        .actions { display:flex; gap:10px; flex-wrap:wrap; margin-top:8px; }
        .btn { height:42px; padding:0 18px; border-radius:10px; border:none; font-size:14px; font-weight:700; cursor:pointer; display:inline-flex; align-items:center; gap:8px; text-decoration:none; }
        .btn-primary { background:var(--primary); color:#fff; }
        .btn-danger { background:#fff; color:var(--danger); border:1px solid #fecaca; }
        .btn-secondary { background:#fff; color:#374151; border:1px solid var(--line); }
        @media (max-width:900px){ .form-row{ grid-template-columns:1fr; } }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/doctor/layout/topbar.jsp"/>
<div class="layout">
    <jsp:include page="/WEB-INF/views/doctor/layout/sidebar.jsp"/>
    <main class="main-content">
        <div class="page-head">
            <h1>Cấu hình ngưỡng giám sát</h1>
            <p>Ngưỡng riêng của bạn dùng cho quét cảnh báo và khuyến nghị AI.</p>
        </div>

        <c:if test="${param.saved == '1'}">
            <div class="flash ok">
                <c:choose>
                    <c:when test="${param.reset == '1'}">Đã khôi phục ngưỡng mặc định.</c:when>
                    <c:otherwise>Đã lưu cấu hình ngưỡng.</c:otherwise>
                </c:choose>
            </div>
        </c:if>
        <c:if test="${param.error == '1'}"><div class="flash err">Không lưu được cấu hình. Vui lòng thử lại.</div></c:if>
        <c:if test="${not empty errors}">
            <ul class="err-list"><c:forEach var="e" items="${errors}"><li>${e}</li></c:forEach></ul>
        </c:if>

        <div class="card">
            <h2>Ngưỡng đường huyết &amp; HbA1c</h2>
            <form method="post" action="${pageContext.request.contextPath}/doctor/threshold-settings">
                <div class="form-row">
                    <div class="field">
                        <label for="glucoseLow">Hạ đường huyết (mg/dL)</label>
                        <input id="glucoseLow" name="glucoseLow" type="number" value="${form.glucoseLow}" required>
                        <div class="hint">Mặc định 70</div>
                    </div>
                    <div class="field">
                        <label for="glucoseHigh">Cao / TIR max (mg/dL)</label>
                        <input id="glucoseHigh" name="glucoseHigh" type="number" value="${form.glucoseHigh}" required>
                        <div class="hint">Mặc định 180</div>
                    </div>
                    <div class="field">
                        <label for="glucoseDanger">Nguy hiểm (mg/dL)</label>
                        <input id="glucoseDanger" name="glucoseDanger" type="number" value="${form.glucoseDanger}" required>
                        <div class="hint">Mặc định 250</div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="field">
                        <label for="hba1cTarget">HbA1c mục tiêu (%)</label>
                        <input id="hba1cTarget" name="hba1cTarget" type="number" step="0.01" value="${form.hba1cTarget}" required>
                    </div>
                    <div class="field">
                        <label for="hba1cPoor">HbA1c kém (%)</label>
                        <input id="hba1cPoor" name="hba1cPoor" type="number" step="0.01" value="${form.hba1cPoor}" required>
                    </div>
                    <div class="field">
                        <label for="daysNoMeasure">Ngày không đo</label>
                        <input id="daysNoMeasure" name="daysNoMeasure" type="number" value="${form.daysNoMeasure}" required>
                    </div>
                </div>
                <div class="actions">
                    <button class="btn btn-primary" type="submit"><i class="fa-solid fa-floppy-disk"></i> Lưu</button>
                    <button class="btn btn-danger" type="submit" name="reset" value="1"
                            onclick="return confirm('Khôi phục ngưỡng mặc định?');">
                        <i class="fa-solid fa-rotate-left"></i> Mặc định
                    </button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/doctor/alerts">Về cảnh báo</a>
                </div>
            </form>
        </div>
    </main>
</div>
</body>
</html>
