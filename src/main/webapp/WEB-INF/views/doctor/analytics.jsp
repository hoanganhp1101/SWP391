<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân tích & Theo dõi</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
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
            font-family: "Segoe UI", Inter, Arial, sans-serif; background: var(--bg); color: var(--ink);
            height: 100vh; overflow: hidden; display: flex; flex-direction: column;
        }

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
        .top-nav a { color: #555; font-size: 15px; text-decoration: none; cursor: pointer; }
        .top-nav a.active { color: var(--primary); font-weight: 600; }
        .top-actions { display: flex; align-items: center; gap: 20px; margin-left: auto; }
        .search-box {
            width: 260px; height: 40px; display: flex; align-items: center; padding: 0 14px;
            border: 1px solid #d1d5db; border-radius: 10px; background: #fff;
        }
        .search-box i { color: #777; }
        .search-box input { border: none; outline: none; width: 100%; margin-left: 10px; font-size: 14px; }
        .top-icon { font-size: 20px; color: #4b5563; cursor: pointer; }
        .avatar { width: 38px; height: 38px; border-radius: 50%; object-fit: cover; }

        .layout { display: flex; flex: 1; min-height: 0; overflow: hidden; }

        .sidebar {
            width: 240px; background: #fff; border-right: 1px solid var(--line);
            display: flex; flex-direction: column; flex-shrink: 0; overflow-y: auto;
        }
        .doctor-profile { padding: 24px 20px; display: flex; align-items: center; gap: 12px; }
        .doctor-profile img { width: 42px; height: 42px; border-radius: 10px; object-fit: cover; }
        .doctor-profile h4 { font-size: 15px; color: #1554c7; }
        .doctor-profile p { font-size: 12px; color: #666; }
        .menu { padding: 0 14px; }
        .menu-item {
            display: flex; align-items: center; gap: 14px; height: 48px; margin-bottom: 6px;
            padding: 0 16px; border-radius: 12px; color: #374151; text-decoration: none; cursor: pointer; font-size: 14px;
        }
        .menu-item i { font-size: 17px; width: 20px; text-align: center; }
        .menu-item.active { background: var(--primary); color: #fff; font-weight: 600; }
        .menu-item:not(.active):hover { background: #f1f5ff; }
        .sidebar-bottom { margin-top: auto; padding: 18px 16px; }
        .new-record {
            width: 100%; height: 46px; border: none; border-radius: 10px; background: #0d4bb5;
            color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; margin-bottom: 8px;
        }
        .bottom-link { display: flex; align-items: center; gap: 12px; padding: 12px; text-decoration: none; color: #374151; cursor: pointer; font-size: 14px; }

        .main-content { flex: 1; padding: 26px 30px; overflow-y: auto; min-width: 0; }

        .page-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; margin-bottom: 20px; flex-wrap: wrap; }
        .page-head h1 { font-size: 26px; margin-bottom: 4px; }
        .page-head p { color: var(--muted); }

        .range-tabs { display: flex; gap: 6px; }
        .range-tabs a {
            padding: 8px 14px; border-radius: 8px; border: 1px solid #cfd8e3; background: #fff;
            color: #374151; text-decoration: none; font-weight: 700; font-size: 13px;
        }
        .range-tabs a.active { background: var(--primary); border-color: var(--primary); color: #fff; }

        .kpi-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 22px; }
        .kpi-card { background: #fff; border: 1px solid var(--line); border-radius: 16px; padding: 18px; }
        .kpi-card .label { color: var(--muted); font-size: 13px; }
        .kpi-card .value { font-size: 30px; font-weight: 800; margin-top: 6px; }
        .kpi-card .unit { font-size: 14px; color: var(--muted); font-weight: 600; }
        .kpi-card.good .value { color: var(--success); }
        .kpi-card.warn .value { color: var(--warning); }
        .kpi-card.bad .value { color: var(--danger); }

        .section-title { font-size: 16px; font-weight: 800; margin: 6px 0 12px; }
        .section-note { font-size: 12px; font-weight: 600; color: var(--muted); }

        .action-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 26px; }
        .action-card {
            background: #fff; border: 1px solid var(--line); border-left: 5px solid #9ca3af;
            border-radius: 14px; padding: 16px 18px; text-decoration: none; color: inherit; display: block;
        }
        .action-card:hover { box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08); }
        .action-card.info { cursor: default; }
        .action-card.info:hover { box-shadow: none; }
        .action-card.red { border-left-color: var(--danger); }
        .action-card.orange { border-left-color: #f97316; }
        .action-card.yellow { border-left-color: var(--warning); }
        .action-card.blue { border-left-color: var(--primary); }
        .action-card .num { font-size: 28px; font-weight: 800; }
        .action-card .desc { color: var(--muted); font-size: 13px; margin-top: 4px; }

        .chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
        .chart-card { background: #fff; border: 1px solid var(--line); border-radius: 16px; padding: 20px; }
        .chart-card.full { grid-column: 1 / -1; }
        .chart-card h3 { font-size: 15px; margin-bottom: 14px; }
        .chart-wrap { position: relative; height: 280px; }
        .chart-empty { color: var(--muted); text-align: center; padding: 40px 0; font-size: 14px; }

        @media (max-width: 1100px) {
            .kpi-grid { grid-template-columns: repeat(2, 1fr); }
            .action-grid { grid-template-columns: repeat(2, 1fr); }
            .chart-grid { grid-template-columns: 1fr; }
        }
        @media (max-width: 760px) {
            .sidebar, .top-nav, .search-box { display: none; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/doctor/layout/topbar.jsp"/>
<div class="layout">
    <jsp:include page="/WEB-INF/views/doctor/layout/sidebar.jsp"/>

    <main class="main-content">
        <div class="page-head">
            <div>
                <h1>Phân tích & Theo dõi</h1>
                <p>Tổng hợp trên ${totalPatients} bệnh nhân — dữ liệu ${days} ngày gần nhất</p>
            </div>
            <div class="range-tabs">
                <a href="?days=7" class="${days == 7 ? 'active' : ''}">7 ngày</a>
                <a href="?days=30" class="${days == 30 ? 'active' : ''}">30 ngày</a>
                <a href="?days=90" class="${days == 90 ? 'active' : ''}">90 ngày</a>
            </div>
        </div>

        <!-- KPI -->
        <div class="kpi-grid">
            <div class="kpi-card">
                <div class="label">Tổng bệnh nhân</div>
                <div class="value">${totalPatients}</div>
            </div>
            <div class="kpi-card">
                <div class="label">Đường huyết TB</div>
                <div class="value">${avgGlucose}<span class="unit"> mg/dL</span></div>
            </div>
            <div class="kpi-card ${timeInRange >= 70 ? 'good' : 'warn'}">
                <div class="label">Time in Range (70–180)</div>
                <div class="value">${timeInRange}<span class="unit"> %</span></div>
            </div>
            <div class="kpi-card ${pctHba1c >= 50 ? 'good' : 'warn'}">
                <div class="label">Đạt HbA1c &lt; 7%</div>
                <div class="value">${pctHba1c}<span class="unit"> %</span></div>
            </div>
            <div class="kpi-card ${unresolvedAlerts > 0 ? 'bad' : 'good'}">
                <div class="label">Cảnh báo tồn đọng</div>
                <div class="value">${unresolvedAlerts}</div>
            </div>
        </div>

        <!-- ACTION LIST: đếm từ bảng alerts, bấm vào ra đúng filter tương ứng -->
        <div class="section-title">Cần hành động (theo cảnh báo)</div>
        <div class="action-grid">
            <a class="action-card red" href="${pageContext.request.contextPath}/doctor/alerts?severity=danger">
                <div class="num">${alertDanger}</div>
                <div class="desc">Cảnh báo mức Nguy hiểm (Đỏ)</div>
            </a>
            <a class="action-card orange" href="${pageContext.request.contextPath}/doctor/alerts?severity=high">
                <div class="num">${alertHigh}</div>
                <div class="desc">Cảnh báo mức Cao (Vàng)</div>
            </a>
            <a class="action-card yellow" href="${pageContext.request.contextPath}/doctor/alerts?status=unread">
                <div class="num">${alertUnread}</div>
                <div class="desc">Cảnh báo chưa xem</div>
            </a>
            <a class="action-card blue" href="${pageContext.request.contextPath}/doctor/alerts?status=processing">
                <div class="num">${alertProcessing}</div>
                <div class="desc">Cảnh báo đang xử lý</div>
            </a>
        </div>

        <!-- THEO DÕI LÂM SÀNG: chỉ số tổng hợp (thông tin, không gắn filter alert) -->
        <div class="section-title">Theo dõi lâm sàng <span class="section-note">(chỉ số tổng hợp ${days} ngày)</span></div>
        <div class="action-grid">
            <div class="action-card info red">
                <div class="num">${hypoPatients}</div>
                <div class="desc">Bệnh nhân có hạ đường huyết (&lt;70 mg/dL)</div>
            </div>
            <div class="action-card info orange">
                <div class="num">${highHba1c}</div>
                <div class="desc">Bệnh nhân HbA1c ≥ 8% (kiểm soát kém)</div>
            </div>
            <div class="action-card info yellow">
                <div class="num">${notMeasured}</div>
                <div class="desc">Bệnh nhân không đo chỉ số &gt; 7 ngày</div>
            </div>
            <div class="action-card info blue">
                <div class="num">${overdueFollowups}</div>
                <div class="desc">Bệnh nhân quá hạn tái khám</div>
            </div>
        </div>

        <!-- CHARTS -->
        <div class="chart-grid">
            <div class="chart-card full">
                <h3>Đường huyết trung bình theo ngày</h3>
                <div class="chart-wrap"><canvas id="glucoseChart"></canvas></div>
                <div class="chart-empty" id="glucoseEmpty" hidden>Chưa có dữ liệu đường huyết trong khoảng thời gian này.</div>
            </div>
            <div class="chart-card">
                <h3>Phân nhóm HbA1c</h3>
                <div class="chart-wrap"><canvas id="hba1cChart"></canvas></div>
                <div class="chart-empty" id="hba1cEmpty" hidden>Chưa có dữ liệu HbA1c.</div>
            </div>
            <div class="chart-card">
                <h3>Cảnh báo theo mức độ</h3>
                <div class="chart-wrap"><canvas id="severityChart"></canvas></div>
                <div class="chart-empty" id="severityEmpty" hidden>Chưa có cảnh báo.</div>
            </div>
            <div class="chart-card full">
                <h3>Cảnh báo theo loại</h3>
                <div class="chart-wrap"><canvas id="typeChart"></canvas></div>
                <div class="chart-empty" id="typeEmpty" hidden>Chưa có cảnh báo.</div>
            </div>
        </div>
    </main>
</div>

<script>
    var DATA = {
        glucose: { labels: ${glucoseLabels}, data: ${glucoseData} },
        hba1c: { labels: ${hba1cLabels}, data: ${hba1cData} },
        severity: { labels: ${alertSeverityLabels}, data: ${alertSeverityData} },
        type: { labels: ${alertTypeLabels}, data: ${alertTypeData} }
    };

    var PALETTE = ['#1557d5', '#16a34a', '#f59e0b', '#dc2626', '#f97316', '#6366f1', '#0ea5e9'];

    function hasData(d) { return d && d.data && d.data.length > 0; }

    function toggleEmpty(canvasId, emptyId, dataset) {
        if (!hasData(dataset)) {
            document.getElementById(canvasId).style.display = 'none';
            document.getElementById(emptyId).hidden = false;
            return false;
        }
        return true;
    }

    if (toggleEmpty('glucoseChart', 'glucoseEmpty', DATA.glucose)) {
        new Chart(document.getElementById('glucoseChart'), {
            type: 'line',
            data: {
                labels: DATA.glucose.labels,
                datasets: [{
                    label: 'mg/dL', data: DATA.glucose.data,
                    borderColor: '#1557d5', backgroundColor: 'rgba(21,87,213,0.12)',
                    fill: true, tension: 0.3, pointRadius: 3
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }
        });
    }

    if (toggleEmpty('hba1cChart', 'hba1cEmpty', DATA.hba1c)) {
        new Chart(document.getElementById('hba1cChart'), {
            type: 'doughnut',
            data: { labels: DATA.hba1c.labels, datasets: [{ data: DATA.hba1c.data, backgroundColor: ['#16a34a', '#f59e0b', '#dc2626'] }] },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
        });
    }

    if (toggleEmpty('severityChart', 'severityEmpty', DATA.severity)) {
        new Chart(document.getElementById('severityChart'), {
            type: 'doughnut',
            data: { labels: DATA.severity.labels, datasets: [{ data: DATA.severity.data, backgroundColor: PALETTE }] },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
        });
    }

    if (toggleEmpty('typeChart', 'typeEmpty', DATA.type)) {
        new Chart(document.getElementById('typeChart'), {
            type: 'bar',
            data: { labels: DATA.type.labels, datasets: [{ label: 'Số cảnh báo', data: DATA.type.data, backgroundColor: '#1557d5' }] },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
        });
    }
</script>
</body>
</html>
