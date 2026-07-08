<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm hồ sơ bệnh án - HealthAlert</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .page-header { margin-bottom: 28px; }
        .step .num {
            width: 22px; height: 22px; border-radius: 50%; background: #4338ca; color: #fff;
            display: flex; align-items: center; justify-content: center; font-size: 12px;
        }
        .step.muted .num { background: #cbd5e1; }
        .card {
            background: white; border: 1px solid #e5e7eb; border-radius: 24px;
            margin-bottom: 24px;
        }
        .card-top {
            padding: 22px 26px; display: flex; justify-content: space-between; align-items: center;
            gap: 20px; border-bottom: 1px solid #e5e7eb; font-weight: 600; font-size: 18px;
        }
        .card-body { padding: 26px; }
        .form-container { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
        .form-group { display: flex; flex-direction: column; }
        .form-group label { margin-bottom: 10px; font-weight: 600; color: #374151; }
        .form-group label .req { color: #dc2626; }
        .form-group input,
        .form-group select,
        .form-group textarea {
            border: 1px solid #d1d5db; border-radius: 14px; padding: 14px 18px;
            font-size: 15px; outline: none;
        }
        .form-group input:focus,
        .form-group select:focus,
        .form-group textarea:focus { border-color: #1557d5; }
        .form-group input[readonly] { background: #f8fafc; color: #374151; }
        .full-width { grid-column: span 2; }
        .record-search-box { position: relative; }
        .record-search-box i {
            position: absolute; top: 50%; left: 16px; transform: translateY(-50%); color: #94a3b8;
        }
        .record-search-box input {
            width: 100%; padding: 16px 18px 16px 48px; border: 1px solid #dbe2ea;
            border-radius: 14px; outline: none; font-size: 15px;
        }
        .patient-results {
            position: absolute;
            top: calc(100% + 6px);
            left: 0;
            right: 0;

            max-height: 220px;

            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 14px;

            display: none;
            z-index: 9999;

            box-shadow: 0 10px 25px rgba(0,0,0,.12);
        }

        .patient-results.show {
            display: block;
        }
        .patient-item { padding: 14px 18px; cursor: pointer; border-bottom: 1px solid #f1f5f9; }
        .patient-item:hover { background: #f8fafc; }
        .patient-item.selected { background: #eff6ff; }
        .patient-item strong { color: #1557d5; margin-right: 8px; }
        .patient-info-panel {
            margin-top: 24px; padding: 20px; background: #f8fafc;
            border-radius: 16px; border: 1px solid #e5e7eb; display: none;
        }
        .patient-info-panel.show { display: block; }
        .patient-info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 12px; }
        .info-field label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }
        .info-field span { font-weight: 600; color: #1e293b; }
        .alert-error {
            background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b;
            padding: 14px 20px; border-radius: 12px; margin-bottom: 20px;
        }
        .alert-error ul { margin: 8px 0 0 18px; }
        .btn {
            border: none; padding: 14px 22px; border-radius: 14px; font-size: 15px; font-weight: 600;
            cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 8px;
        }
        .btn:disabled { opacity: .55; cursor: not-allowed; }
        .btn-outline { background: white; border: 1px solid #dbe2ea; color: #374151; }
        .btn-primary { background: #2563eb; color: white; }
        .btn-ai { background: #4338ca; color: white; }
        .form-actions {
            display: flex; justify-content: flex-end; gap: 16px; margin-top: 8px; margin-bottom: 32px;
        }
        .ai-card { display: none; }
        .ai-card.show { display: block; }
        .ai-badge {
            display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 700;
            padding: 4px 10px; border-radius: 999px;
        }
        .ai-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
        .ai-block h4 {
            font-size: 13px; color: #64748b; text-transform: uppercase;
            letter-spacing: .04em; margin-bottom: 8px;
        }
        .ai-block ul { margin: 0 0 0 18px; color: #334155; line-height: 1.7; }
        .ai-block p { color: #334155; line-height: 1.7; }
        .ai-note { margin-top: 16px; font-size: 12px; color: #94a3b8; }
        .ai-status { font-size: 13px; color: #64748b; }
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

            <div class="page-header">
                <h1>Thêm hồ sơ bệnh án</h1>
            </div>

            <c:if test="${not empty errors}">
                <div class="alert-error">
                    <strong>Vui lòng kiểm tra lại:</strong>
                    <ul>
                        <c:forEach var="err" items="${errors}">
                            <li>${err}</li>
                        </c:forEach>
                    </ul>
                </div>
            </c:if>

            <div class="alert-error" id="ajaxErrors" style="display:none;"></div>

            <form method="post" action="${pageContext.request.contextPath}/medical-encounters/add" id="encounterForm">
                <input type="hidden" name="patientId" id="patientId" value="${form.patientId}">
                <input type="hidden" name="thoiDiemDoDuong" value="luc_doi">
                <input type="hidden" name="aiSummary" id="aiSummary" value="">
                <input type="hidden" name="aiRiskLevel" id="aiRiskLevel" value="">
                <input type="hidden" name="aiRiskScore" id="aiRiskScore" value="">

                <!-- A. Thông tin chung -->
                <div class="card">
                    <div class="card-top"><i class="fa-solid fa-clipboard-list"></i> A. Thông tin chung</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group full-width">
                                <label>Tìm và chọn bệnh nhân <span class="req">*</span></label>
                                <div class="record-search-box">
                                    <i class="fa-solid fa-magnifying-glass"></i>

                                    <input
                                            type="text"
                                            id="patientSearch"
                                            placeholder="Tìm theo mã bệnh nhân hoặc họ tên..."
                                            autocomplete="off">

                                    <div class="patient-results" id="patientResults"></div>
                                </div>

                                <div id="selectedPatientLabel"
                                     style="margin-top:12px;font-size:14px;color:#64748b;">
                                </div>
                            </div>
                            <div class="form-group">
                                <label>Ngày khám <span class="req">*</span></label>
                                <input type="date" name="ngayKham" value="${form.ngayKham}" required>
                            </div>
                            <div class="form-group">
                                <label>Loại hồ sơ <span class="req">*</span></label>
                                <select name="encounterType" id="encounterType" required>
                                    <option value="tai_kham_noi_tiet" ${form.encounterType == 'tai_kham_noi_tiet' || empty form.encounterType ? 'selected' : ''}>Bệnh án tái khám Nội tiết</option>
                                    <option value="mau_tong_quat" ${form.encounterType == 'mau_tong_quat' ? 'selected' : ''}>Kết quả xét nghiệm máu tổng quát</option>
                                    <option value="sinh_hoa_mau" ${form.encounterType == 'sinh_hoa_mau' ? 'selected' : ''}>Kết quả sinh hóa máu</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Khoa khám</label>
                                <select name="khoaKham">
                                    <option value="Khoa Nội tiết" ${form.khoaKham eq 'Khoa Nội tiết' ? 'selected' : ''}>Khoa Nội tiết</option>
                                    <option value="Khoa Nội tổng quát" ${form.khoaKham eq 'Khoa Nội tổng quát' ? 'selected' : ''}>Khoa Nội tổng quát</option>
                                    <option value="Khoa Tim mạch" ${form.khoaKham eq 'Khoa Tim mạch' ? 'selected' : ''}>Khoa Tim mạch</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Bác sĩ khám</label>
                                <input type="text" readonly value="${not empty doctor.hoTen ? doctor.hoTen : 'Bác sĩ'}">
                            </div>
                        </div>

                        <div class="patient-info-panel" id="patientInfoPanel">
                            <strong style="font-size:16px;">Thông tin bệnh nhân đã chọn</strong>
                            <div class="patient-info-grid">
                                <div class="info-field"><label>Mã bệnh nhân</label><span id="piCode">—</span></div>
                                <div class="info-field"><label>Họ và tên</label><span id="piName">—</span></div>
                                <div class="info-field"><label>Giới tính</label><span id="piGender">—</span></div>
                                <div class="info-field"><label>Ngày sinh</label><span id="piDob">—</span></div>
                                <div class="info-field"><label>Tuổi</label><span id="piAge">—</span></div>
                                <div class="info-field"><label>Loại tiểu đường</label><span id="piDiabetes">—</span></div>
                                <div class="info-field"><label>Địa chỉ</label><span id="piAddress">—</span></div>
                                <div class="info-field"><label>Bảo hiểm y tế</label><span id="piInsurance">—</span></div>
                                <div class="info-field"><label>Chiều cao (cm)</label><span id="piHeight">—</span></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="patientStore" hidden>
                    <c:forEach var="p" items="${patients}">
                        <div class="patient-option"
                             data-id="${p.id}"
                             data-code="<c:out value='${p.patientCode}'/>"
                             data-name="<c:out value='${p.user.hoTen}'/>"
                             data-gender="<c:out value='${p.gioiTinh}'/>"
                             data-dob="${p.ngaySinh}"
                             data-age="${p.tuoi}"
                             data-diabetes="<c:out value='${p.loaiTieuDuong}'/>"
                             data-address="<c:out value='${p.diaChi}'/>"
                             data-insurance="<c:out value='${p.baoHiemYTe}'/>"
                             data-height="${p.chieuCaoCm}"></div>
                    </c:forEach>
                </div>

                <!-- B. Thông tin lâm sàng -->
                <div class="card" data-encounter-section="tai_kham_noi_tiet">
                    <div class="card-top"><i class="fa-solid fa-stethoscope"></i> B. Thông tin lâm sàng</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group full-width">
                                <label>Triệu chứng</label>
                                <textarea name="trieuChung" rows="2">${not empty form.trieuChung ? form.trieuChung : form.lyDoKham}</textarea>
                            </div>
                            <div class="form-group full-width">
                                <label>Tiền sử bệnh</label>
                                <textarea name="tienSuBenh" rows="2">${not empty form.tienSuBenh ? form.tienSuBenh : form.quaTrinhBenhLy}</textarea>
                            </div>
                            <div class="form-group full-width">
                                <label>Khám lâm sàng</label>
                                <textarea name="khamLamSang" rows="2">${form.khamLamSang}</textarea>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- C. Chỉ số sức khỏe -->
                <div class="card" data-encounter-section="tai_kham_noi_tiet">
                    <div class="card-top"><i class="fa-solid fa-heart-pulse"></i> C. Chỉ số sức khỏe</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group">
                                <label>Đường huyết (mg/dL) <span class="req tai-kham-required">*</span></label>
                                <input type="number" step="0.1" min="0" name="duongHuyetMgdl" value="${form.duongHuyetMgdl}" data-tai-kham-required="true">
                            </div>
                            <div class="form-group">
                                <label>HbA1c (%)</label>
                                <input type="number" step="0.1" min="0" name="hba1cPercent" value="${form.hba1cPercent}">
                            </div>
                            <div class="form-group">
                                <label>Chiều cao (cm)</label>
                                <input type="number" step="0.1" min="0" name="chieuCaoCm" id="chieuCaoCm" value="${form.chieuCaoCm}">
                            </div>
                            <div class="form-group">
                                <label>Cân nặng (kg)</label>
                                <input type="number" step="0.1" min="0" name="canNangKg" id="canNangKg" value="${form.canNangKg}">
                            </div>
                            <div class="form-group">
                                <label>BMI</label>
                                <input type="number" step="0.01" min="0" name="bmi" id="bmi" value="${form.bmi}" readonly>
                            </div>
                            <div class="form-group">
                                <label>Huyết áp tâm thu</label>
                                <input type="number" min="0" name="huyetApTamThu" value="${form.huyetApTamThu}">
                            </div>
                            <div class="form-group">
                                <label>Huyết áp tâm trương</label>
                                <input type="number" min="0" name="huyetApTamTruong" value="${form.huyetApTamTruong}">
                            </div>
                            <div class="form-group">
                                <label>Nhịp tim (bpm)</label>
                                <input type="number" min="0" name="nhipTim" value="${form.nhipTim}">
                            </div>
                            <div class="form-group">
                                <label>Nhiệt độ (°C)</label>
                                <input type="number" step="0.1" min="0" name="nhietDoC" value="${form.nhietDoC}">
                            </div>
                            <div class="form-group">
                                <label>Nhịp thở</label>
                                <input type="number" min="0" name="nhipTho" value="${form.nhipTho}">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- D. Kết quả sinh hóa -->
                <div class="card" data-encounter-section="sinh_hoa_mau">
                    <div class="card-top"><i class="fa-solid fa-flask"></i> D. Kết quả sinh hóa máu</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group"><label>Glucose (mmol/L)</label><input type="number" step="0.01" min="0" name="labGlucoseMau" value="${form.labGlucoseMau}"></div>
                            <div class="form-group"><label>HbA1c (%)</label><input type="number" step="0.1" min="0" name="labHba1c" value="${form.labHba1c}"></div>
                            <div class="form-group"><label>Cholesterol (mmol/L)</label><input type="number" step="0.01" min="0" name="labCholesterol" value="${form.labCholesterol}"></div>
                            <div class="form-group"><label>Triglyceride (mmol/L)</label><input type="number" step="0.01" min="0" name="labTriglyceride" value="${form.labTriglyceride}"></div>
                            <div class="form-group"><label>HDL (mmol/L)</label><input type="number" step="0.01" min="0" name="labHdl" value="${form.labHdl}"></div>
                            <div class="form-group"><label>LDL (mmol/L)</label><input type="number" step="0.01" min="0" name="labLdl" value="${form.labLdl}"></div>
                            <div class="form-group"><label>AST (U/L)</label><input type="number" step="0.01" min="0" name="labAst" value="${form.labAst}"></div>
                            <div class="form-group"><label>ALT (U/L)</label><input type="number" step="0.01" min="0" name="labAlt" value="${form.labAlt}"></div>
                            <div class="form-group"><label>Creatinine (µmol/L)</label><input type="number" step="0.01" min="0" name="labCreatinine" value="${form.labCreatinine}"></div>
                            <div class="form-group"><label>Ure (mmol/L)</label><input type="number" step="0.01" min="0" name="labUre" value="${form.labUre}"></div>
                        </div>
                    </div>
                </div>

                <!-- E. Xét nghiệm máu tổng quát -->
                <div class="card" data-encounter-section="mau_tong_quat">
                    <div class="card-top"><i class="fa-solid fa-vial"></i> E. Xét nghiệm máu tổng quát</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group"><label>WBC (G/L)</label><input type="number" step="0.01" min="0" name="labWbc" value="${form.labWbc}"></div>
                            <div class="form-group"><label>RBC (T/L)</label><input type="number" step="0.01" min="0" name="labRbc" value="${form.labRbc}"></div>
                            <div class="form-group"><label>HGB (g/dL)</label><input type="number" step="0.01" min="0" name="labHgb" value="${form.labHgb}"></div>
                            <div class="form-group"><label>HCT (%)</label><input type="number" step="0.01" min="0" name="labHct" value="${form.labHct}"></div>
                            <div class="form-group"><label>PLT (G/L)</label><input type="number" step="0.01" min="0" name="labPlt" value="${form.labPlt}"></div>
                        </div>
                    </div>
                </div>

                <!-- Kết quả phân tích AI -->
                <div class="card ai-card" id="aiCard">
                    <div class="card-top">
                        <span><i class="fa-solid fa-robot"></i> Kết quả phân tích AI</span>
                        <span class="ai-badge" id="aiRiskBadge">—</span>
                    </div>
                    <div class="card-body">
                        <div class="ai-status" id="aiStatus"></div>
                        <div class="ai-grid" style="margin-top:16px;">
                            <div class="ai-block">
                                <h4>Bệnh khả năng</h4>
                                <p id="aiDisease">—</p>
                            </div>
                            <div class="ai-block">
                                <h4>Điểm rủi ro</h4>
                                <p id="aiScore">—</p>
                            </div>
                            <div class="ai-block">
                                <h4>Yếu tố nguy cơ</h4>
                                <ul id="aiFactors"></ul>
                            </div>
                            <div class="ai-block">
                                <h4>Xét nghiệm đề xuất</h4>
                                <ul id="aiTests"></ul>
                            </div>
                            <div class="ai-block full-width" style="grid-column:span 2;">
                                <h4>Khuyến nghị</h4>
                                <ul id="aiRecs"></ul>
                            </div>
                            <div class="ai-block full-width" style="grid-column:span 2;">
                                <h4>Giải thích ngắn</h4>
                                <p id="aiExplain">—</p>
                            </div>
                        </div>
                        <p class="ai-note">AI chỉ hỗ trợ, không kê đơn và không đưa quyết định cuối cùng. Bác sĩ chịu trách nhiệm chẩn đoán và điều trị ở Bước 2.</p>
                    </div>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/doctor/patient-records" class="btn btn-outline">Hủy</a>
                    <button type="button" class="btn btn-ai" id="btnAnalyze"><i class="fa-solid fa-wand-magic-sparkles"></i> Phân tích AI</button>
                    <button type="submit" class="btn btn-primary" id="btnContinue" disabled><i class="fa-solid fa-arrow-right"></i> Tiếp tục kê đơn</button>
                    <button type="submit" class="btn btn-primary" id="btnSave" disabled style="display:none;"><i class="fa-solid fa-save"></i> Lưu hồ sơ</button>
                </div>
            </form>
    </main>
</div>

<script>
(function () {
    const ctx = '${pageContext.request.contextPath}';
    const store = document.getElementById('patientStore');
    const patients = Array.from(store.querySelectorAll('.patient-option')).map(function (el) {
        return {
            id: el.dataset.id, code: el.dataset.code || '', name: el.dataset.name || '',
            gender: el.dataset.gender || '—', dob: el.dataset.dob || '—', age: el.dataset.age || '—',
            diabetes: el.dataset.diabetes || '—', address: el.dataset.address || '—',
            insurance: el.dataset.insurance || '—', height: el.dataset.height || ''
        };
    });

    const searchInput = document.getElementById('patientSearch');
    const resultsBox = document.getElementById('patientResults');
    const patientIdInput = document.getElementById('patientId');
    const infoPanel = document.getElementById('patientInfoPanel');
    const selectedLabel = document.getElementById('selectedPatientLabel');

    function renderResults(keyword) {
        const q = (keyword || '').trim().toLowerCase();
        const filtered = patients.filter(function (p) {
            if (!q) return true;
            return p.code.toLowerCase().includes(q) || p.name.toLowerCase().includes(q);
        });
        resultsBox.innerHTML = '';
        if (filtered.length === 0) {
            resultsBox.innerHTML = '<div class="patient-item">Không tìm thấy bệnh nhân</div>';
        } else {
            filtered.forEach(function (p) {
                const item = document.createElement('div');
                item.className = 'patient-item' + (patientIdInput.value === p.id ? ' selected' : '');
                item.innerHTML = '<strong>' + escapeHtml(p.code) + '</strong>' + escapeHtml(p.name);
                item.addEventListener('click', function () { selectPatient(p); });
                resultsBox.appendChild(item);
            });
        }
        resultsBox.classList.add('show');
    }

    function escapeHtml(text) {
        const d = document.createElement('div');
        d.textContent = text == null ? '' : text;
        return d.innerHTML;
    }

    function selectPatient(p) {
        patientIdInput.value = p.id;
        searchInput.value = p.code + ' — ' + p.name;
        selectedLabel.textContent = 'Đã chọn: ' + p.code + ' — ' + p.name;
        document.getElementById('piCode').textContent = p.code || '—';
        document.getElementById('piName').textContent = p.name || '—';
        document.getElementById('piGender').textContent = p.gender || '—';
        document.getElementById('piDob').textContent = p.dob || '—';
        document.getElementById('piAge').textContent = p.age || '—';
        document.getElementById('piDiabetes').textContent = p.diabetes || '—';
        document.getElementById('piAddress').textContent = p.address || '—';
        document.getElementById('piInsurance').textContent = p.insurance || '—';
        document.getElementById('piHeight').textContent = p.height || '—';
        infoPanel.classList.add('show');
        if (p.height && !document.getElementById('chieuCaoCm').value) {
            document.getElementById('chieuCaoCm').value = p.height;
            calcBmi();
        }
        resultsBox.classList.remove('show');
    }

    searchInput.addEventListener('focus', function () { renderResults(searchInput.value); });
    searchInput.addEventListener('input', function () { renderResults(searchInput.value); });
    document.addEventListener('click', function (e) {
        if (!searchInput.contains(e.target) && !resultsBox.contains(e.target)) {
            resultsBox.classList.remove('show');
        }
    });

    const initialId = patientIdInput.value;
    if (initialId) {
        const found = patients.find(function (p) { return p.id === initialId; });
        if (found) selectPatient(found);
    }

    const heightInput = document.getElementById('chieuCaoCm');
    const weightInput = document.getElementById('canNangKg');
    const bmiInput = document.getElementById('bmi');
    function calcBmi() {
        const h = parseFloat(heightInput.value);
        const w = parseFloat(weightInput.value);
        if (!h || !w || h <= 0) return;
        const m = h / 100;
        bmiInput.value = (w / (m * m)).toFixed(2);
    }
    heightInput.addEventListener('input', calcBmi);
    weightInput.addEventListener('input', calcBmi);
    calcBmi();

    const encounterTypeSelect = document.getElementById('encounterType');
    const btnContinue = document.getElementById('btnContinue');
    const btnSave = document.getElementById('btnSave');
    let analyzed = false; // đã phân tích AI thành công chưa

    // Nút submit đang hiển thị theo loại hồ sơ (Nội tiết → kê đơn, còn lại → lưu hồ sơ).
    function activeSubmitButton() {
        return encounterTypeSelect.value === 'tai_kham_noi_tiet' ? btnContinue : btnSave;
    }

    function toggleActionButtons() {
        const isTaiKham = encounterTypeSelect.value === 'tai_kham_noi_tiet';
        btnContinue.style.display = isTaiKham ? '' : 'none';
        btnSave.style.display = isTaiKham ? 'none' : '';
        // Chỉ nút đang hiển thị mới có thể bấm, và phải sau khi phân tích AI.
        btnContinue.disabled = !isTaiKham || !analyzed;
        btnSave.disabled = isTaiKham || !analyzed;
    }

    function toggleEncounterSections() {
        const type = encounterTypeSelect.value;
        const isTaiKham = type === 'tai_kham_noi_tiet';
        document.querySelectorAll('[data-encounter-section]').forEach(function (card) {
            card.style.display = card.getAttribute('data-encounter-section') === type ? '' : 'none';
        });
        document.querySelectorAll('[data-tai-kham-required]').forEach(function (field) {
            if (isTaiKham) { field.setAttribute('required', 'required'); }
            else { field.removeAttribute('required'); }
        });
        document.querySelectorAll('.tai-kham-required').forEach(function (mark) {
            mark.style.display = isTaiKham ? '' : 'none';
        });
        toggleActionButtons();
    }
    encounterTypeSelect.addEventListener('change', function () {
        resetAI();
        toggleEncounterSections();
    });
    function resetAI() {
        analyzed = false;

        aiCard.classList.remove("show");

        document.getElementById("aiSummary").value = "";
        document.getElementById("aiRiskLevel").value = "";
        document.getElementById("aiRiskScore").value = "";

        ajaxErrors.style.display = "none";

        btnAnalyze.disabled = false;
        btnAnalyze.innerHTML =
            '<i class="fa-solid fa-wand-magic-sparkles"></i> Phân tích AI';

        toggleActionButtons();
    }
    toggleEncounterSections();

    // ---- Phân tích AI (AJAX, không lưu DB) ----
    const form = document.getElementById('encounterForm');
    const btnAnalyze = document.getElementById('btnAnalyze');
    const ajaxErrors = document.getElementById('ajaxErrors');
    const aiCard = document.getElementById('aiCard');

    function showErrors(list) {
        ajaxErrors.innerHTML = '<strong>Vui lòng kiểm tra lại:</strong><ul>' +
            list.map(function (e) { return '<li>' + escapeHtml(e) + '</li>'; }).join('') + '</ul>';
        ajaxErrors.style.display = 'block';
        ajaxErrors.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function fillList(id, items) {
        const ul = document.getElementById(id);
        ul.innerHTML = '';
        (items || []).forEach(function (it) {
            const li = document.createElement('li');
            li.textContent = it;
            ul.appendChild(li);
        });
    }

    function riskClass(level) {
        switch ((level || '').toLowerCase()) {
            case 'critical': return 'risk-critical';
            case 'high': return 'risk-high';
            case 'medium': return 'risk-medium';
            default: return 'risk-low';
        }
    }

    btnAnalyze.addEventListener('click', function () {
        ajaxErrors.style.display = 'none';
        if (!patientIdInput.value) {
            showErrors(['Vui lòng chọn bệnh nhân trước khi phân tích AI.']);
            searchInput.focus();
            return;
        }
        btnAnalyze.disabled = true;
        btnAnalyze.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang phân tích...';

        fetch(ctx + '/doctor/medical-encounter/analyze', {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: new URLSearchParams(new FormData(form))
        }).then(function (r) { return r.json(); }).then(function (data) {
            btnAnalyze.disabled = false;
            btnAnalyze.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> Phân tích lại';
            if (!data.ok) {
                showErrors(data.errors || ['Không thể phân tích AI.']);
                return;
            }
            const ai = data.ai || {};
            const badge = document.getElementById('aiRiskBadge');
            badge.className = 'ai-badge ' + riskClass(ai.riskLevel);
            badge.textContent = 'Mức độ: ' + ((ai.riskLevel || '—').toUpperCase());
            document.getElementById('aiDisease').textContent = ai.possibleDisease || '—';
            document.getElementById('aiScore').textContent = (ai.riskScore != null ? ai.riskScore : '—') + ' / 100';
            fillList('aiFactors', ai.riskFactors);
            fillList('aiTests', ai.recommendedTests);
            fillList('aiRecs', ai.recommendations);
            document.getElementById('aiExplain').textContent = ai.shortExplanation || '—';
            document.getElementById('aiStatus').textContent = data.used
                ? 'Nguồn: Gemini AI'
                : (data.error || 'Nguồn: phân tích theo quy tắc y khoa');

            document.getElementById('aiSummary').value = data.summaryText || '';
            document.getElementById('aiRiskLevel').value = ai.riskLevel || '';
            document.getElementById('aiRiskScore').value = (ai.riskScore != null ? ai.riskScore : '');

            aiCard.classList.add('show');
            aiCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
            analyzed = true;
            toggleActionButtons();
        }).catch(function () {
            btnAnalyze.disabled = false;
            btnAnalyze.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> Phân tích AI';
            showErrors(['Lỗi kết nối khi gọi phân tích AI. Vui lòng thử lại.']);
        });
    });

    form.addEventListener('submit', function (e) {
        if (!patientIdInput.value) {
            e.preventDefault();
            showErrors(['Vui lòng chọn bệnh nhân.']);
            searchInput.focus();
            return;
        }
        if (!analyzed) {
            e.preventDefault();
            showErrors(['Vui lòng nhấn "Phân tích AI" trước khi lưu hồ sơ.']);
            return;
        }
        const submitBtn = activeSubmitButton();
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
    });
})();
const encounterType = document.getElementById("encounterType");
const btnContinue = document.getElementById("btnContinue");

function updatePrescriptionButton() {
    if (encounterType.value === "tai_kham_noi_tiet") {
        btnContinue.style.display = "inline-flex";
    } else {
        btnContinue.style.display = "none";
    }
}

// chạy khi load trang
updatePrescriptionButton();

// chạy khi đổi loại hồ sơ
encounterType.addEventListener("change", updatePrescriptionButton);
</script>
</body>
</html>
