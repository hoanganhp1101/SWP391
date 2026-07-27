<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mô phỏng IoT - DiabCare</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/patient-layout.css">
    <style>
        .page-header { margin-bottom: 1.5rem; }
        .page-sub { color: var(--text-muted); margin-top: 0.35rem; font-size: 0.95rem; }
        .badge-sim {
            display: inline-flex; align-items: center; gap: 0.4rem; margin-top: 0.75rem;
            background: #ecfeff; color: #0e7490; border: 1px solid #a5f3fc;
            padding: 0.35rem 0.75rem; border-radius: 999px; font-size: 0.8rem; font-weight: 600;
        }
        .panel {
            background: var(--bg-white); border: 1px solid var(--border);
            border-radius: 16px; padding: 1.5rem; margin-bottom: 1.25rem;
        }
        .panel h2 { font-size: 1.05rem; margin-bottom: 1rem; }
        .controls {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 1rem; margin-bottom: 1rem;
        }
        .field label {
            display: block; font-size: 0.8rem; color: var(--text-muted);
            font-weight: 600; margin-bottom: 0.35rem;
        }
        .field select {
            width: 100%; padding: 0.65rem 0.75rem; border: 1px solid var(--border);
            border-radius: 10px; font-size: 0.9rem; background: #fff;
        }
        .device-grid {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 1rem; margin-top: 0.5rem;
        }
        .device-card {
            border: 1px solid var(--border); border-radius: 14px; padding: 1.15rem;
            background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
            position: relative; overflow: hidden;
        }
        .device-card.online::before,
        .device-card.offline::before {
            content: ''; position: absolute; top: 12px; right: 12px;
            width: 10px; height: 10px; border-radius: 50%;
        }
        .device-card.online::before {
            background: var(--success); box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2);
        }
        .device-card.offline::before { background: #94a3b8; }
        .device-icon {
            width: 44px; height: 44px; border-radius: 12px; display: flex;
            align-items: center; justify-content: center; color: #fff; margin-bottom: 0.75rem;
        }
        .device-icon.glu { background: var(--primary); }
        .device-icon.bp { background: #0d9488; }
        .device-icon.hr { background: #db2777; }
        .device-name { font-weight: 700; font-size: 0.95rem; }
        .device-id { color: var(--text-muted); font-size: 0.75rem; margin: 0.2rem 0 0.85rem; }
        .metric-value { font-size: 1.75rem; font-weight: 700; letter-spacing: -0.02em; }
        .metric-unit { font-size: 0.85rem; color: var(--text-muted); font-weight: 500; }
        .metric-sub { margin-top: 0.25rem; color: var(--text-muted); font-size: 0.85rem; }
        .actions { display: flex; flex-wrap: wrap; gap: 0.75rem; margin-top: 1.25rem; }
        .btn {
            border: none; border-radius: 10px; padding: 0.7rem 1.15rem; font-weight: 600;
            font-size: 0.9rem; cursor: pointer; display: inline-flex; align-items: center; gap: 0.5rem;
            text-decoration: none;
        }
        .btn:disabled { opacity: 0.55; cursor: not-allowed; }
        .btn-primary { background: var(--primary); color: #fff; }
        .btn-success { background: var(--success); color: #fff; }
        .btn-ghost { background: var(--primary-light); color: var(--primary); }
        .status-bar {
            margin-top: 1rem; padding: 0.85rem 1rem; border-radius: 10px;
            background: #f1f5f9; color: var(--text-muted); font-size: 0.9rem; font-weight: 500;
        }
        .status-bar.warn { background: #fef3c7; color: #92400e; }
        .status-bar.ok { background: #ecfdf5; color: #065f46; }
        .status-bar.err { background: #fef2f2; color: #991b1b; }
        .progress-wrap {
            display: none; margin-top: 1rem; height: 8px; border-radius: 999px;
            background: #e2e8f0; overflow: hidden;
        }
        .progress-wrap.active { display: block; }
        .progress-bar {
            height: 100%; width: 0%; background: linear-gradient(90deg, var(--primary), #0d9488);
            transition: width 0.2s linear;
        }
        .hint { color: var(--text-muted); font-size: 0.85rem; margin-top: 0.75rem; line-height: 1.5; }
        .history-toolbar {
            display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: center;
            justify-content: space-between; margin-bottom: 1rem;
        }
        .history-toolbar h2 { margin: 0; font-size: 1.05rem; }
        .filter-group { display: flex; gap: 0.5rem; }
        .filter-btn {
            border: 1px solid var(--border); background: #fff; color: var(--text-muted);
            border-radius: 999px; padding: 0.4rem 0.85rem; font-size: 0.8rem;
            font-weight: 600; cursor: pointer;
        }
        .filter-btn.active { background: var(--primary-light); color: var(--primary); border-color: #bfdbfe; }
        .history-table-wrap { overflow-x: auto; }
        table.history { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
        table.history th, table.history td {
            padding: 0.75rem 0.65rem; text-align: left; border-bottom: 1px solid var(--border);
            white-space: nowrap;
        }
        table.history th {
            color: var(--text-muted); font-size: 0.75rem; text-transform: uppercase;
            letter-spacing: 0.03em; font-weight: 700;
        }
        table.history tbody tr:hover { background: #f8fafc; }
        .tag {
            display: inline-flex; align-items: center; padding: 0.15rem 0.55rem;
            border-radius: 999px; font-size: 0.75rem; font-weight: 700;
        }
        .tag.iot { background: #ecfeff; color: #0e7490; }
        .tag.manual { background: #f1f5f9; color: #475569; }
        .empty-history {
            text-align: center; color: var(--text-muted); padding: 1.5rem 0.5rem; font-size: 0.9rem;
        }
        .val-warn { color: var(--danger); font-weight: 700; }
    </style>
</head>
<body class="patient-app">
<jsp:include page="layout/topnav.jsp">
    <jsp:param name="activeTop" value="iot"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="layout/sidebar.jsp">
        <jsp:param name="activeMenu" value="iot"/>
    </jsp:include>

    <main class="main-content">
        <div class="page-header">
            <h1 class="page-title">Mô phỏng thiết bị IoT</h1>
            <p class="page-sub">Mô phỏng máy đo đường huyết, huyết áp và nhịp tim — lưu thẳng vào hồ sơ sức khỏe.</p>
            <div class="badge-sim"><i class="fas fa-microchip"></i> Simulation mode — không kết nối thiết bị thật</div>
        </div>

        <section class="panel">
            <h2>Cấu hình đo</h2>
            <div class="controls">
                <div class="field">
                    <label for="scenario">Kịch bản mô phỏng</label>
                    <select id="scenario">
                        <option value="RANDOM">Ngẫu nhiên</option>
                        <option value="NORMAL">Bình thường</option>
                        <option value="HIGH_GLUCOSE">Đường huyết cao</option>
                        <option value="LOW_GLUCOSE">Đường huyết thấp</option>
                        <option value="HIGH_BP">Huyết áp cao</option>
                    </select>
                </div>
                <div class="field">
                    <label for="timing">Thời điểm đo đường</label>
                    <select id="timing">
                        <option value="luc_doi">Lúc đói</option>
                        <option value="sau_an_1h">Sau ăn 1 giờ</option>
                        <option value="sau_an_2h">Sau ăn 2 giờ</option>
                        <option value="truoc_ngu">Trước ngủ</option>
                    </select>
                </div>
            </div>

            <div class="device-grid">
                <div class="device-card offline" id="cardGlu">
                    <div class="device-icon glu"><i class="fas fa-tint"></i></div>
                    <div class="device-name">Glucometer IoT</div>
                    <div class="device-id" id="idGlu">Chưa kết nối</div>
                    <div class="metric-value"><span id="valGlu">--</span> <span class="metric-unit">mg/dL</span></div>
                    <div class="metric-sub">Đường huyết mao mạch</div>
                </div>
                <div class="device-card offline" id="cardBp">
                    <div class="device-icon bp"><i class="fas fa-heart-pulse"></i></div>
                    <div class="device-name">Blood Pressure Cuff</div>
                    <div class="device-id" id="idBp">Chưa kết nối</div>
                    <div class="metric-value"><span id="valSys">--</span>/<span id="valDia">--</span> <span class="metric-unit">mmHg</span></div>
                    <div class="metric-sub">Tâm thu / Tâm trương</div>
                </div>
                <div class="device-card offline" id="cardHr">
                    <div class="device-icon hr"><i class="fas fa-wave-square"></i></div>
                    <div class="device-name">Heart Rate Sensor</div>
                    <div class="device-id" id="idHr">Chưa kết nối</div>
                    <div class="metric-value"><span id="valHr">--</span> <span class="metric-unit">bpm</span></div>
                    <div class="metric-sub">Nhịp tim tức thời</div>
                </div>
            </div>

            <div class="progress-wrap" id="progressWrap"><div class="progress-bar" id="progressBar"></div></div>
            <div class="status-bar" id="statusBar">Sẵn sàng. Nhấn Bắt đầu đo để mô phỏng tín hiệu thiết bị.</div>

            <div class="actions">
                <button type="button" class="btn btn-primary" id="btnMeasure">
                    <i class="fas fa-broadcast-tower"></i> Bắt đầu đo
                </button>
                <button type="button" class="btn btn-success" id="btnSave" disabled>
                    <i class="fas fa-save"></i> Lưu vào hồ sơ
                </button>
                <a class="btn btn-ghost" href="${ctx}/patient-dashboard#charts">
                    <i class="fas fa-chart-line"></i> Xem biểu đồ
                </a>
            </div>
            <p class="hint">
                Chỉ số sau khi lưu xuất hiện trong lịch sử bên dưới và trên Tổng quan / biểu đồ tiến triển.
            </p>
        </section>

        <section class="panel">
            <div class="history-toolbar">
                <h2><i class="fas fa-history"></i> Lịch sử đo</h2>
                <div class="filter-group">
                    <button type="button" class="filter-btn active" data-source="iot">Chỉ IoT</button>
                    <button type="button" class="filter-btn" data-source="all">Tất cả</button>
                </div>
            </div>
            <div class="history-table-wrap">
                <table class="history">
                    <thead>
                    <tr>
                        <th>Thời gian</th>
                        <th>Nguồn</th>
                        <th>Đường huyết</th>
                        <th>Thời điểm</th>
                        <th>Huyết áp</th>
                        <th>Nhịp tim</th>
                    </tr>
                    </thead>
                    <tbody id="historyBody">
                        <tr class="empty-row"><td colspan="6"><div class="empty-history">Đang tải lịch sử đo...</div></td></tr>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</div>

<script>
(function () {
    const ctx = '${ctx}';
    let latestReading = null;
    let measuring = false;
    let historySource = 'iot';

    const statusBar = document.getElementById('statusBar');
    const progressWrap = document.getElementById('progressWrap');
    const progressBar = document.getElementById('progressBar');
    const btnMeasure = document.getElementById('btnMeasure');
    const btnSave = document.getElementById('btnSave');
    const historyBody = document.getElementById('historyBody');

    function setStatus(text, type) {
        statusBar.textContent = text;
        statusBar.className = 'status-bar' + (type ? ' ' + type : '');
    }

    function alertStatusType(mucCanhBao) {
        if (mucCanhBao === 'nguy_hiem' || mucCanhBao === 'cao') return 'err';
        if (mucCanhBao === 'trung_binh') return 'warn';
        return 'ok';
    }

    function setOnline(online) {
        ['cardGlu', 'cardBp', 'cardHr'].forEach(function (id) {
            const el = document.getElementById(id);
            el.classList.toggle('online', online);
            el.classList.toggle('offline', !online);
        });
    }

    function dash(v) {
        return v == null || v === '' ? '--' : v;
    }

    function warnClass(cond) {
        return cond ? ' class="val-warn"' : '';
    }

    function renderHistory(rows) {
        if (!rows || !rows.length) {
            historyBody.innerHTML = '<tr class="empty-row"><td colspan="6"><div class="empty-history">'
                + (historySource === 'iot'
                    ? 'Chưa có lần đo IoT nào. Hãy đo và lưu để xem lịch sử.'
                    : 'Chưa có bản ghi đo nào.')
                + '</div></td></tr>';
            return;
        }
        historyBody.innerHTML = rows.map(function (row) {
            var glu = row.duongHuyet;
            var sys = row.huyetApTamThu;
            var dia = row.huyetApTamTruong;
            var hr = row.nhipTim;
            var tagClass = row.source === 'IoT' ? 'iot' : 'manual';
            var gluHtml = glu == null ? '--'
                : '<span' + warnClass(glu < 70 || glu > 180) + '>' + glu + '</span> mg/dL';
            var bpHtml = sys == null ? '--'
                : '<span' + warnClass(sys >= 140 || (dia != null && dia >= 90)) + '>'
                    + sys + '/' + dash(dia) + '</span> mmHg';
            var hrHtml = hr == null ? '--'
                : '<span' + warnClass(hr > 100) + '>' + hr + '</span> bpm';
            return '<tr>'
                + '<td>' + dash(row.measuredAt) + '</td>'
                + '<td><span class="tag ' + tagClass + '">' + dash(row.source) + '</span></td>'
                + '<td>' + gluHtml + '</td>'
                + '<td>' + dash(row.thoiDiemDoDuong) + '</td>'
                + '<td>' + bpHtml + '</td>'
                + '<td>' + hrHtml + '</td>'
                + '</tr>';
        }).join('');
    }

    async function loadHistory() {
        try {
            var q = historySource === 'all' ? '?source=all' : '';
            var res = await fetch(ctx + '/api/iot/history' + q, { method: 'GET', credentials: 'same-origin' });
            var parsed = await parseJsonResponse(res);
            if (parsed.ok && parsed.data && parsed.data.status === 'success') {
                renderHistory(parsed.data.history || []);
            } else {
                renderHistory([]);
            }
        } catch (e) {
            renderHistory([]);
        }
    }

    document.querySelectorAll('.filter-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            document.querySelectorAll('.filter-btn').forEach(function (b) { b.classList.remove('active'); });
            btn.classList.add('active');
            historySource = btn.getAttribute('data-source') || 'iot';
            loadHistory();
        });
    });

    function renderReading(r) {
        document.getElementById('valGlu').textContent = r.duongHuyet;
        document.getElementById('valSys').textContent = r.huyetApTamThu;
        document.getElementById('valDia').textContent = r.huyetApTamTruong;
        document.getElementById('valHr').textContent = r.nhipTim;
        document.getElementById('idGlu').textContent = r.deviceId || 'SIM';
        document.getElementById('idBp').textContent = r.bpDeviceId || 'SIM';
        document.getElementById('idHr').textContent = r.hrDeviceId || 'SIM';
        setOnline(true);
        const warn = (r.statusLabel || '').toLowerCase().indexOf('cảnh báo') >= 0
            || (r.statusLabel || '').toLowerCase().indexOf('nhanh') >= 0;
        setStatus(r.statusLabel || 'Đo xong', warn ? 'warn' : 'ok');
    }

    function animateProgress(durationMs) {
        return new Promise(function (resolve) {
            progressWrap.classList.add('active');
            progressBar.style.width = '0%';
            const start = Date.now();
            const timer = setInterval(function () {
                const pct = Math.min(100, ((Date.now() - start) / durationMs) * 100);
                progressBar.style.width = pct + '%';
                if (pct >= 100) {
                    clearInterval(timer);
                    resolve();
                }
            }, 40);
        });
    }

    function parseJsonResponse(res) {
        return res.text().then(function (text) {
            var trimmed = (text || '').trim();
            if (trimmed.charAt(0) === '{' || trimmed.charAt(0) === '[') {
                try {
                    return { ok: res.ok, status: res.status, data: JSON.parse(trimmed) };
                } catch (e) { /* fall through */ }
            }
            var lower = trimmed.toLowerCase();
            if (res.status === 401 || res.status === 403) {
                throw new Error('Phiên đăng nhập hết hạn. Hãy đăng xuất rồi đăng nhập lại bằng tài khoản bệnh nhân.');
            }
            if (lower.indexOf('<!doctype') >= 0 || lower.indexOf('<html') >= 0) {
                throw new Error('Server trả về HTML thay vì JSON (HTTP ' + res.status + '). Hãy đăng xuất → đăng nhập lại, hoặc rebuild/redeploy ứng dụng.');
            }
            throw new Error('Phản hồi không hợp lệ từ server (HTTP ' + res.status + ').');
        });
    }

    async function saveReading(reading) {
        const res = await fetch(ctx + '/api/iot/save', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                duongHuyet: reading.duongHuyet,
                huyetApTamThu: reading.huyetApTamThu,
                huyetApTamTruong: reading.huyetApTamTruong,
                nhipTim: reading.nhipTim,
                thoiDiemDoDuong: reading.thoiDiemDoDuong,
                deviceId: reading.deviceId
            })
        });
        const parsed = await parseJsonResponse(res);
        if (!parsed.ok || !parsed.data || parsed.data.status !== 'success') {
            throw new Error((parsed.data && parsed.data.message) || 'Lưu thất bại');
        }
        return parsed.data;
    }

    btnMeasure.addEventListener('click', async function () {
        if (measuring) return;
        measuring = true;
        btnMeasure.disabled = true;
        btnSave.disabled = true;
        setStatus('Đang kết nối thiết bị IoT mô phỏng...', '');
        setOnline(false);

        try {
            await animateProgress(1600);
            const res = await fetch(ctx + '/api/iot/measure', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    scenario: document.getElementById('scenario').value,
                    thoiDiemDoDuong: document.getElementById('timing').value
                })
            });
            const parsed = await parseJsonResponse(res);
            if (!parsed.ok || !parsed.data || parsed.data.status !== 'success') {
                throw new Error((parsed.data && parsed.data.message) || 'Không đo được chỉ số');
            }
            latestReading = parsed.data.reading;
            renderReading(latestReading);
            setStatus('Đo xong. Đang lưu vào hồ sơ...', '');

            // Tự động lưu vào database sau khi đo
            const saved = await saveReading(latestReading);
            var msg = saved.message || 'Đã lưu chỉ số IoT vào hồ sơ';
            if (saved.analysis && saved.analysis.alertCreated) {
                setStatus(msg + ' ' + (saved.analysis.alertTitle || 'Đã tạo cảnh báo') + '.',
                    alertStatusType(saved.analysis.mucCanhBao));
            } else {
                setStatus(msg, 'ok');
            }
            loadHistory();
            btnSave.disabled = true;
        } catch (err) {
            setStatus(err.message || 'Lỗi mô phỏng / lưu hồ sơ', 'err');
            if (latestReading) {
                btnSave.disabled = false;
            }
        } finally {
            progressWrap.classList.remove('active');
            progressBar.style.width = '0%';
            btnMeasure.disabled = false;
            measuring = false;
        }
    });

    btnSave.addEventListener('click', async function () {
        if (!latestReading) return;
        btnSave.disabled = true;
        setStatus('Đang lưu chỉ số vào hồ sơ...', '');
        try {
            const saved = await saveReading(latestReading);
            var msg = saved.message || 'Đã lưu';
            if (saved.analysis && saved.analysis.alertCreated) {
                setStatus(msg + ' ' + (saved.analysis.alertTitle || 'Đã tạo cảnh báo') + '.',
                    alertStatusType(saved.analysis.mucCanhBao));
            } else {
                setStatus(msg, 'ok');
            }
            loadHistory();
        } catch (err) {
            setStatus(err.message || 'Lỗi lưu hồ sơ', 'err');
            btnSave.disabled = false;
        }
    });

    loadHistory();
})();
</script>
</body>
</html>
