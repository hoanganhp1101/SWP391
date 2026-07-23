<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Thêm hồ sơ bệnh án - HealthAlert</title>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        *{margin:0;padding:0;box-sizing:border-box;font-family:Inter,sans-serif;}
        body{background:#f5f7fb;color:#111827;
            .layout{
                display:flex;
                height:calc(100vh - 80px);
            }

            .main-content{
                flex:1;
                background:#f5f7fb;
                padding:28px;
                overflow:auto;
            }
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
        .form-group .input-error { border-color: #dc2626; background: #fff7f7; }
        .field-error {
            min-height: 18px; margin-top: 6px; color: #dc2626;
            font-size: 13px; line-height: 1.35;
        }
        .form-group input[readonly] { background: #f8fafc; color: #374151; }
        .full-width { grid-column: span 2; }
        .record-search-box { position: relative;width: 100%; }
        .record-search-box i {
            position: absolute; top: 50%; left: 16px; transform: translateY(-50%); color: #94a3b8;
        }
        .record-search-box input {
            width: 100%; padding: 16px 18px 16px 48px; border: 1px solid #dbe2ea;
            border-radius: 14px; outline: none; font-size: 15px;
        }
        .patient-results{
            position: absolute;
            top: calc(100% + 4px);
            left: 0;
            right: 0;

            background: #fff;
            border: 1px solid #dbe2ea;
            border-radius: 14px;

            max-height: 260px;
            overflow-y: auto;

            display: none;

            z-index: 100;

            box-shadow: 0 12px 30px rgba(0,0,0,.15);
        }

        .patient-results.show{
            display:block;
        }
        .card{
            overflow: visible;
        }

        .patient-results.show {
            display: block;
        }
        .patient-item{
            padding:14px 18px;
            cursor:pointer;
            transition:.15s;
        }

        .patient-item:hover{
            background:#f1f5f9;
        }

        .patient-item.selected{
            background:#dbeafe;
        }
        .patient-item strong { color: #1557d5; margin-right: 8px; }
        .patient-info-panel{
            margin-top:20px;
            position:relative;
            z-index:1;
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

            <form method="post" action="${pageContext.request.contextPath}/doctor/patient-records" id="encounterForm" novalidate>
                <c:set var="activeEncounterType"
                       value="${empty form.encounterType ? 'tai_kham_noi_tiet' : form.encounterType}"/>
                <c:set var="showTaiKham" value="${activeEncounterType eq 'tai_kham_noi_tiet'}"/>
                <c:set var="showSinhHoa" value="${activeEncounterType eq 'sinh_hoa_mau'}"/>
                <c:set var="showMauTongQuat" value="${activeEncounterType eq 'mau_tong_quat'}"/>

                <input type="hidden" name="action" id="action" value="form">
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
                                <div class="field-error" data-error-for="patientId"><c:out value="${fieldErrors['patientId']}"/></div>

                                <div id="selectedPatientLabel"
                                     style="margin-top:12px;font-size:14px;color:#64748b;">
                                </div>
                            </div>
                            <div class="form-group">
                                <label>Ngày khám <span class="req">*</span></label>
                                <input type="date" name="ngayKham" value="${form.ngayKham}" required>
                                <div class="field-error" data-error-for="ngayKham"><c:out value="${fieldErrors['ngayKham']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>Loại hồ sơ <span class="req">*</span></label>
                                <select name="encounterType" id="encounterType" required>
                                    <option value="tai_kham_noi_tiet" ${form.encounterType == 'tai_kham_noi_tiet' || empty form.encounterType ? 'selected' : ''}>Bệnh án tái khám Nội tiết</option>
                                    <option value="mau_tong_quat" ${form.encounterType == 'mau_tong_quat' ? 'selected' : ''}>Kết quả xét nghiệm máu tổng quát</option>
                                    <option value="sinh_hoa_mau" ${form.encounterType == 'sinh_hoa_mau' ? 'selected' : ''}>Kết quả sinh hóa máu</option>
                                </select>
                                <div class="field-error" data-error-for="encounterType"><c:out value="${fieldErrors['encounterType']}"/></div>
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
                        <c:set var="diabetesDisplay" value="${p.loaiTieuDuong eq 'Type 1' ? 'Tiểu đường týp 1' : (p.loaiTieuDuong eq 'Type 2' ? 'Tiểu đường týp 2' : p.loaiTieuDuong)}"/>
                        <div class="patient-option"
                             data-id="${p.id}"
                             data-code="<c:out value='${p.patientCode}'/>"
                             data-name="<c:out value='${p.user.hoTen}'/>"
                             data-gender="<c:out value='${p.gioiTinh}'/>"
                             data-dob="${p.ngaySinh}"
                             data-age="${p.tuoi}"
                             data-diabetes="<c:out value='${diabetesDisplay}'/>"
                             data-address="<c:out value='${p.diaChi}'/>"
                             data-insurance="<c:out value='${p.baoHiemYTe}'/>"
                             data-height="${p.chieuCaoCm}"></div>
                    </c:forEach>
                </div>

                <!-- B. Thông tin lâm sàng -->
                <div class="card" data-encounter-section="tai_kham_noi_tiet"
                     style="display:${showTaiKham ? 'block' : 'none'};">
                    <div class="card-top"><i class="fa-solid fa-stethoscope"></i> B. Thông tin lâm sàng</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group full-width">
                                <label>Triệu chứng</label>
                                <textarea name="trieuChung" rows="2">${not empty form.trieuChung ? form.trieuChung : form.lyDoKham}</textarea>
                                <div class="field-error" data-error-for="trieuChung"><c:out value="${fieldErrors['trieuChung']}"/></div>
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
                <div class="card" data-encounter-section="tai_kham_noi_tiet"
                     style="display:${showTaiKham ? 'block' : 'none'};">
                    <div class="card-top"><i class="fa-solid fa-heart-pulse"></i> C. Chỉ số sức khỏe</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group">
                                <label>Đường huyết (mg/dL) <span class="req tai-kham-required" style="display:${showTaiKham ? 'inline' : 'none'};">*</span></label>
                                <input type="number" step="any" min="0" max="800" name="duongHuyetMgdl" value="${form.duongHuyetMgdl}" data-tai-kham-required="true" ${showTaiKham ? 'required' : ''} inputmode="decimal">
                                <div class="field-error" data-error-for="duongHuyetMgdl"><c:out value="${fieldErrors['duongHuyetMgdl']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>HbA1c (%)</label>
                                <input type="number" step="any" min="0" max="20" name="hba1cPercent" value="${form.hba1cPercent}" inputmode="decimal">
                                <div class="field-error" data-error-for="hba1cPercent"><c:out value="${fieldErrors['hba1cPercent']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>Chiều cao (cm)</label>
                                <input type="number" step="any" min="0" max="250" name="chieuCaoCm" id="chieuCaoCm" value="${form.chieuCaoCm}" inputmode="decimal">
                                <div class="field-error" data-error-for="chieuCaoCm"><c:out value="${fieldErrors['chieuCaoCm']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>Cân nặng (kg)</label>
                                <input type="number" step="any" min="0" max="500" name="canNangKg" id="canNangKg" value="${form.canNangKg}" inputmode="decimal">
                                <div class="field-error" data-error-for="canNangKg"><c:out value="${fieldErrors['canNangKg']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>BMI</label>
                                <input type="number" step="any" min="0" name="bmi" id="bmi" value="${form.bmi}" readonly>
                            </div>
                            <div class="form-group">
                                <label>Huyết áp tâm thu</label>
                                <input type="number" step="1" min="0" max="300" name="huyetApTamThu" value="${form.huyetApTamThu}" inputmode="numeric">
                                <div class="field-error" data-error-for="huyetApTamThu"><c:out value="${fieldErrors['huyetApTamThu']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>Huyết áp tâm trương</label>
                                <input type="number" step="1" min="0" max="200" name="huyetApTamTruong" value="${form.huyetApTamTruong}" inputmode="numeric">
                                <div class="field-error" data-error-for="huyetApTamTruong"><c:out value="${fieldErrors['huyetApTamTruong']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>Nhịp tim (bpm)</label>
                                <input type="number" step="1" min="0" max="250" name="nhipTim" value="${form.nhipTim}" inputmode="numeric">
                                <div class="field-error" data-error-for="nhipTim"><c:out value="${fieldErrors['nhipTim']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>Nhiệt độ (°C)</label>
                                <input type="number" step="any" min="0" max="45" name="nhietDoC" value="${form.nhietDoC}" inputmode="decimal">
                                <div class="field-error" data-error-for="nhietDoC"><c:out value="${fieldErrors['nhietDoC']}"/></div>
                            </div>
                            <div class="form-group">
                                <label>Nhịp thở</label>
                                <input type="number" step="1" min="0" max="80" name="nhipTho" value="${form.nhipTho}" inputmode="numeric">
                                <div class="field-error" data-error-for="nhipTho"><c:out value="${fieldErrors['nhipTho']}"/></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- D. Kết quả sinh hóa -->
                <div class="card" data-encounter-section="sinh_hoa_mau"
                     style="display:${showSinhHoa ? 'block' : 'none'};">
                    <div class="card-top"><i class="fa-solid fa-flask"></i> D. Kết quả sinh hóa máu</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group"><label>Đường huyết (mmol/L)</label><input type="number" step="any" min="0" name="labGlucoseMau" value="${form.labGlucoseMau}" inputmode="decimal"><div class="field-error" data-error-for="labGlucoseMau"><c:out value="${fieldErrors['labGlucoseMau']}"/></div></div>
                            <div class="form-group"><label>HbA1c (%)</label><input type="number" step="any" min="0" max="20" name="labHba1c" value="${form.labHba1c}" inputmode="decimal"><div class="field-error" data-error-for="labHba1c"><c:out value="${fieldErrors['labHba1c']}"/></div></div>
                            <div class="form-group"><label>Cholesterol (mmol/L)</label><input type="number" step="any" min="0" name="labCholesterol" value="${form.labCholesterol}" inputmode="decimal"><div class="field-error" data-error-for="labCholesterol"><c:out value="${fieldErrors['labCholesterol']}"/></div></div>
                            <div class="form-group"><label>Triglyceride (mmol/L)</label><input type="number" step="any" min="0" name="labTriglyceride" value="${form.labTriglyceride}" inputmode="decimal"><div class="field-error" data-error-for="labTriglyceride"><c:out value="${fieldErrors['labTriglyceride']}"/></div></div>
                            <div class="form-group"><label>HDL (mmol/L)</label><input type="number" step="any" min="0" name="labHdl" value="${form.labHdl}" inputmode="decimal"><div class="field-error" data-error-for="labHdl"><c:out value="${fieldErrors['labHdl']}"/></div></div>
                            <div class="form-group"><label>LDL (mmol/L)</label><input type="number" step="any" min="0" name="labLdl" value="${form.labLdl}" inputmode="decimal"><div class="field-error" data-error-for="labLdl"><c:out value="${fieldErrors['labLdl']}"/></div></div>
                            <div class="form-group"><label>AST (U/L)</label><input type="number" step="any" min="0" name="labAst" value="${form.labAst}" inputmode="decimal"><div class="field-error" data-error-for="labAst"><c:out value="${fieldErrors['labAst']}"/></div></div>
                            <div class="form-group"><label>ALT (U/L)</label><input type="number" step="any" min="0" name="labAlt" value="${form.labAlt}" inputmode="decimal"><div class="field-error" data-error-for="labAlt"><c:out value="${fieldErrors['labAlt']}"/></div></div>
                            <div class="form-group"><label>Creatinine (µmol/L)</label><input type="number" step="any" min="0" name="labCreatinine" value="${form.labCreatinine}" inputmode="decimal"><div class="field-error" data-error-for="labCreatinine"><c:out value="${fieldErrors['labCreatinine']}"/></div></div>
                            <div class="form-group"><label>Urê (mmol/L)</label><input type="number" step="any" min="0" name="labUre" value="${form.labUre}" inputmode="decimal"><div class="field-error" data-error-for="labUre"><c:out value="${fieldErrors['labUre']}"/></div></div>
                        </div>
                    </div>
                </div>

                <!-- E. Xét nghiệm máu tổng quát -->
                <div class="card" data-encounter-section="mau_tong_quat"
                     style="display:${showMauTongQuat ? 'block' : 'none'};">
                    <div class="card-top"><i class="fa-solid fa-vial"></i> E. Xét nghiệm máu tổng quát</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group"><label>Bạch cầu (WBC, G/L)</label><input type="number" step="any" min="0" name="labWbc" value="${form.labWbc}" inputmode="decimal"><div class="field-error" data-error-for="labWbc"><c:out value="${fieldErrors['labWbc']}"/></div></div>
                            <div class="form-group"><label>Hồng cầu (RBC, T/L)</label><input type="number" step="any" min="0" name="labRbc" value="${form.labRbc}" inputmode="decimal"><div class="field-error" data-error-for="labRbc"><c:out value="${fieldErrors['labRbc']}"/></div></div>
                            <div class="form-group"><label>Huyết sắc tố (HGB, g/dL)</label><input type="number" step="any" min="0" name="labHgb" value="${form.labHgb}" inputmode="decimal"><div class="field-error" data-error-for="labHgb"><c:out value="${fieldErrors['labHgb']}"/></div></div>
                            <div class="form-group"><label>Dung tích hồng cầu (HCT, %)</label><input type="number" step="any" min="0" name="labHct" value="${form.labHct}" inputmode="decimal"><div class="field-error" data-error-for="labHct"><c:out value="${fieldErrors['labHct']}"/></div></div>
                            <div class="form-group"><label>Tiểu cầu (PLT, G/L)</label><input type="number" step="any" min="0" name="labPlt" value="${form.labPlt}" inputmode="decimal"><div class="field-error" data-error-for="labPlt"><c:out value="${fieldErrors['labPlt']}"/></div></div>
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
                    <button type="submit" class="btn btn-primary" id="btnContinue"
                            style="display:${showTaiKham ? 'inline-flex' : 'none'};">
                        <i class="fa-solid fa-arrow-right"></i> Tiếp tục kê đơn
                    </button>
                    <button type="submit" class="btn btn-primary" id="btnSave"
                            style="display:${showTaiKham ? 'none' : 'inline-flex'};">
                        <i class="fa-solid fa-save"></i> Lưu hồ sơ
                    </button>
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

        let filtered = patients.filter(function (p) {
            if (!q) return true;
            return p.code.toLowerCase().includes(q) ||
                p.name.toLowerCase().includes(q);
        });

        // Nếu không tìm thấy thì hiển thị toàn bộ danh sách
        if (filtered.length === 0) {
            filtered = patients;
        }

        resultsBox.innerHTML = '';

        filtered.forEach(function (p) {
            const item = document.createElement('div');
            item.className = 'patient-item' +
                (patientIdInput.value === p.id ? ' selected' : '');

            item.innerHTML =
                '<strong>' + escapeHtml(p.code) + '</strong> ' +
                escapeHtml(p.name);

            item.addEventListener('click', function () {
                selectPatient(p);
            });

            resultsBox.appendChild(item);
        });

        resultsBox.classList.add('show');
    }

    function escapeHtml(text) {
        const d = document.createElement('div');
        d.textContent = text == null ? '' : text;
        return d.innerHTML;
    }

    const heightInput = document.getElementById('chieuCaoCm');
    const weightInput = document.getElementById('canNangKg');
    const bmiInput = document.getElementById('bmi');

    function calcBmi() {
        if (!heightInput || !weightInput || !bmiInput) {
            return;
        }
        const h = Number(heightInput.value);
        const w = Number(weightInput.value);

        if (!Number.isFinite(h) || !Number.isFinite(w) || h <= 0 || w <= 0) {
            bmiInput.value = '';
            return;
        }

        const bmi = w / Math.pow(h / 100, 2);
        bmiInput.value = bmi.toFixed(2);
    }

    ['input', 'change', 'blur'].forEach(function (evt) {
        if (heightInput) {
            heightInput.addEventListener(evt, calcBmi);
        }
        if (weightInput) {
            weightInput.addEventListener(evt, calcBmi);
        }
    });

    function selectPatient(p) {
        patientIdInput.value = p.id;
        setFieldError('patientId', '');
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
        if (heightInput && p.height && !heightInput.value) {
            heightInput.value = p.height;
        }
        calcBmi();
        resultsBox.classList.remove('show');
    }

    searchInput.addEventListener('focus', function () {
        renderResults('');
    });
    searchInput.addEventListener('input', function () { renderResults(searchInput.value); });
    document.addEventListener('click', function (e) {
        if (!searchInput.contains(e.target) && !resultsBox.contains(e.target)) {
            resultsBox.classList.remove('show');
        }
    });

    const encounterTypeSelect = document.getElementById('encounterType');
    const btnContinue = document.getElementById('btnContinue');
    const btnSave = document.getElementById('btnSave');
    const form = document.getElementById('encounterForm');
    const btnAnalyze = document.getElementById('btnAnalyze');
    const ajaxErrors = document.getElementById('ajaxErrors');
    const aiCard = document.getElementById('aiCard');
    let analyzed = false; // đã phân tích AI thành công chưa

    const initialId = patientIdInput.value;
    if (initialId) {
        const found = patients.find(function (p) { return p.id === initialId; });
        if (found) selectPatient(found);
    } else {
        calcBmi();
    }

    // Nút submit đang hiển thị theo loại hồ sơ (Nội tiết → kê đơn, còn lại → lưu hồ sơ).
    function activeSubmitButton() {
        return encounterTypeSelect.value === 'tai_kham_noi_tiet' ? btnContinue : btnSave;
    }

    /** Chỉ quyết định nút nào hiển thị — không enable/disable. */
    function toggleActionButtons() {
        const type = encounterTypeSelect.value;

        if (type === "tai_kham_noi_tiet") {
            btnContinue.style.display = "inline-flex";
            btnSave.style.display = "none";
        } else {
            btnContinue.style.display = "none";
            btnSave.style.display = "inline-flex";
        }
    }

    /** Khôi phục trạng thái nút sau POST fail / bfcache / client validation fail. */
    function resetSubmitButtons() {
        btnSave.disabled = false;
        btnContinue.disabled = false;

        btnSave.innerHTML =
            '<i class="fa-solid fa-save"></i> Lưu hồ sơ';

        btnContinue.innerHTML =
            '<i class="fa-solid fa-arrow-right"></i> Tiếp tục kê đơn';
    }

    /**
     * Synchronize encounter sections with #encounterType.
     * Initial visibility is rendered by JSP from ${form.encounterType}.
     * JS only re-syncs on load and when the user changes encounterType.
     */
    function updateEncounterUI() {
        const type = encounterTypeSelect ? encounterTypeSelect.value : '';
        const isTaiKham = type === 'tai_kham_noi_tiet';

        document.querySelectorAll('[data-encounter-section]').forEach(function (card) {
            const active = card.getAttribute('data-encounter-section') === type;
            card.style.display = active ? 'block' : 'none';
        });

        document.querySelectorAll('[data-tai-kham-required]').forEach(function (field) {
            if (isTaiKham) {
                field.setAttribute('required', 'required');
            } else {
                field.removeAttribute('required');
            }
        });
        document.querySelectorAll('.tai-kham-required').forEach(function (mark) {
            mark.style.display = isTaiKham ? 'inline' : 'none';
        });

        toggleActionButtons();
        calcBmi();
    }

    function resetAI() {
        analyzed = false;
        if (aiCard) {
            aiCard.classList.remove('show');
        }
        var aiSummary = document.getElementById('aiSummary');
        var aiRiskLevel = document.getElementById('aiRiskLevel');
        var aiRiskScore = document.getElementById('aiRiskScore');
        if (aiSummary) aiSummary.value = '';
        if (aiRiskLevel) aiRiskLevel.value = '';
        if (aiRiskScore) aiRiskScore.value = '';
        if (ajaxErrors) {
            ajaxErrors.style.display = 'none';
        }
        if (btnAnalyze) {
            btnAnalyze.disabled = false;
            btnAnalyze.innerHTML =
                '<i class="fa-solid fa-wand-magic-sparkles"></i> Phân tích AI';
        }
        // Do NOT change encounter section visibility.
        toggleActionButtons();
    }

    encounterTypeSelect.addEventListener('change', function () {
        resetAI();
        updateEncounterUI();
    });

    document.addEventListener('DOMContentLoaded', function () {
        resetSubmitButtons();
        updateEncounterUI();
        calcBmi();
    });

    window.addEventListener('pageshow', function () {
        resetSubmitButtons();
        calcBmi();
    });

    // Script ở cuối body: DOM đã sẵn sàng — chạy ngay nếu DOMContentLoaded đã qua.
    if (document.readyState !== 'loading') {
        resetSubmitButtons();
        updateEncounterUI();
        calcBmi();
    }

    // ---- Phân tích AI (AJAX, không lưu DB) ----
    const visitDateInput = form.elements.ngayKham;
    const now = new Date();
    const today = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0')
        + '-' + String(now.getDate()).padStart(2, '0');
    visitDateInput.max = today;

    const DECIMAL_PATTERN = /^\d+(\.\d+)?$/;
    const INTEGER_PATTERN = /^\d+$/;
    const INTEGER_FIELDS = {
        huyetApTamThu: true,
        huyetApTamTruong: true,
        nhipTim: true,
        nhipTho: true
    };

    const validationRules = {
        duongHuyetMgdl: {label: 'Đường huyết', min: 0, max: 800, range: 'Đường huyết phải nằm trong khoảng 0–800 mg/dL.'},
        hba1cPercent: {label: 'HbA1c', min: 0, max: 20, range: 'HbA1c chỉ được nhập từ 0% đến 20%.'},
        chieuCaoCm: {label: 'Chiều cao', min: 0, max: 250, range: 'Chiều cao phải nằm trong khoảng 0–250 cm.'},
        canNangKg: {label: 'Cân nặng', min: 0, max: 500, range: 'Cân nặng phải nằm trong khoảng 0–500 kg.'},
        huyetApTamThu: {label: 'Huyết áp tâm thu', min: 0, max: 300, range: 'Huyết áp tâm thu phải từ 0–300 mmHg.'},
        huyetApTamTruong: {label: 'Huyết áp tâm trương', min: 0, max: 200, range: 'Huyết áp tâm trương phải từ 0–200 mmHg.'},
        nhipTim: {label: 'Nhịp tim', min: 0, max: 250, range: 'Nhịp tim phải từ 0–250 bpm.'},
        nhietDoC: {label: 'Nhiệt độ', min: 0, max: 45, range: 'Nhiệt độ cơ thể phải từ 0°C đến 45°C.'},
        nhipTho: {label: 'Nhịp thở', min: 0, max: 80, range: 'Nhịp thở phải từ 0–80 lần/phút.'},
        labGlucoseMau: {label: 'Đường huyết', min: 0},
        labHba1c: {label: 'HbA1c', min: 0, max: 20, range: 'HbA1c chỉ được nhập từ 0% đến 20%.'},
        labCholesterol: {label: 'Cholesterol', min: 0},
        labTriglyceride: {label: 'Triglyceride', min: 0},
        labHdl: {label: 'HDL', min: 0},
        labLdl: {label: 'LDL', min: 0},
        labAst: {label: 'AST', min: 0},
        labAlt: {label: 'ALT', min: 0},
        labUre: {label: 'Urê', min: 0},
        labCreatinine: {label: 'Creatinine', min: 0},
        labWbc: {label: 'WBC', min: 0},
        labRbc: {label: 'RBC', min: 0},
        labHgb: {label: 'HGB', min: 0},
        labHct: {label: 'HCT', min: 0},
        labPlt: {label: 'PLT', min: 0}
    };

    function setFieldError(name, message) {
        const field = form.elements[name];
        const error = form.querySelector('[data-error-for="' + name + '"]');
        if (field && field.classList) {
            field.classList.toggle('input-error', Boolean(message));
            field.setAttribute('aria-invalid', message ? 'true' : 'false');
        }
        if (error) error.textContent = message || '';
    }

    function isNumericTextInvalid(field, raw) {
        if (field.validity && field.validity.badInput) {
            return true;
        }
        if (!raw) {
            return false;
        }
        const normalized = raw.replace(',', '.');
        if (INTEGER_FIELDS[field.name]) {
            return !INTEGER_PATTERN.test(normalized);
        }
        return !DECIMAL_PATTERN.test(normalized);
    }

    function validateNumberField(name) {
        const field = form.elements[name];
        const rule = validationRules[name];
        const section = field ? field.closest('[data-encounter-section]') : null;
        if (!field || !rule || (section && section.style.display === 'none')) {
            setFieldError(name, '');
            return null;
        }
        if (field.readOnly) {
            setFieldError(name, '');
            return null;
        }
        const raw = (field.value || '').trim();
        if (isNumericTextInvalid(field, raw)) {
            const message = 'Chỉ được nhập số.';
            setFieldError(name, message);
            return message;
        }
        if (!raw) {
            setFieldError(name, '');
            return null;
        }
        const value = Number(raw.replace(',', '.'));
        if (!Number.isFinite(value)) {
            const message = 'Chỉ được nhập số.';
            setFieldError(name, message);
            return message;
        }
        let message = null;
        if ((rule.min != null && value < rule.min) || (rule.max != null && value > rule.max)) {
            message = rule.range || (rule.label + ' không hợp lệ.');
        }
        setFieldError(name, message);
        return message;
    }

    function validateEncounterForm() {
        const errors = [];
        if (!patientIdInput.value) {
            const message = 'Vui lòng chọn bệnh nhân.';
            setFieldError('patientId', message);
            errors.push(message);
        } else setFieldError('patientId', '');

        const date = visitDateInput;
        if (!date.value) {
            const message = 'Vui lòng chọn Ngày khám.';
            setFieldError('ngayKham', message);
            errors.push(message);
        } else if (date.value > today) {
            const message = 'Ngày khám không được lớn hơn ngày hiện tại.';
            setFieldError('ngayKham', message);
            errors.push(message);
        } else setFieldError('ngayKham', '');

        if (!encounterTypeSelect.value) {
            const message = 'Vui lòng chọn Loại hồ sơ.';
            setFieldError('encounterType', message);
            errors.push(message);
        } else setFieldError('encounterType', '');

        if (encounterTypeSelect.value === 'tai_kham_noi_tiet') {
            const symptoms = form.elements.trieuChung;
            if (!symptoms.value.trim()) {
                const message = 'Vui lòng nhập Lý do khám.';
                setFieldError('trieuChung', message);
                errors.push(message);
            } else setFieldError('trieuChung', '');
            if (!form.elements.duongHuyetMgdl.value.trim()
                    || (form.elements.duongHuyetMgdl.validity
                        && form.elements.duongHuyetMgdl.validity.badInput)) {
                const message = form.elements.duongHuyetMgdl.validity
                        && form.elements.duongHuyetMgdl.validity.badInput
                    ? 'Chỉ được nhập số.'
                    : 'Vui lòng nhập Đường huyết.';
                setFieldError('duongHuyetMgdl', message);
                if (!errors.includes(message)) errors.push(message);
            }
        } else {
            setFieldError('trieuChung', '');
            setFieldError('duongHuyetMgdl', '');
        }

        Object.keys(validationRules).forEach(function (name) {
            const message = validateNumberField(name);
            if (message && !errors.includes(message)) errors.push(message);
        });

        if (encounterTypeSelect.value === 'mau_tong_quat') {
            const hasCbc = ['labWbc', 'labRbc', 'labHgb', 'labHct', 'labPlt']
                .some(function (name) { return form.elements[name].value.trim(); });
            if (!hasCbc) errors.push('Vui lòng nhập ít nhất một chỉ số xét nghiệm máu tổng quát.');
        }
        if (encounterTypeSelect.value === 'sinh_hoa_mau') {
            const hasLab = ['labGlucoseMau', 'labHba1c', 'labCholesterol', 'labTriglyceride',
                'labHdl', 'labLdl', 'labAst', 'labAlt', 'labUre', 'labCreatinine']
                .some(function (name) { return form.elements[name].value.trim(); });
            if (!hasLab) errors.push('Vui lòng nhập ít nhất một chỉ số sinh hóa máu.');
        }
        return errors;
    }

    Object.keys(validationRules).forEach(function (name) {
        const field = form.elements[name];
        if (!field) return;
        field.addEventListener('blur', function () { validateNumberField(name); });
        field.addEventListener('input', function () {
            if (field.classList.contains('input-error') || (field.value || '').trim()) {
                validateNumberField(name);
            }
        });
    });
    form.querySelectorAll('input, select, textarea').forEach(function (field) {
        if (validationRules[field.name]) return;
        const eventName = field.tagName === 'SELECT' ? 'change' : 'input';
        field.addEventListener(eventName, function () {
            if (field.name) setFieldError(field.name, '');
        });
    });

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

    function riskLabel(level) {
        switch ((level || '').toLowerCase()) {
            case 'critical': return 'Nguy kịch';
            case 'high': return 'Cao';
            case 'medium': return 'Trung bình';
            case 'low': return 'Thấp';
            default: return 'Chưa xác định';
        }
    }

    btnAnalyze.addEventListener('click', function () {
        ajaxErrors.style.display = 'none';
        const clientErrors = validateEncounterForm();
        if (clientErrors.length) {
            showErrors(clientErrors);
            const firstInvalid = form.querySelector('.input-error');
            if (firstInvalid) firstInvalid.focus();
            return;
        }
        document.getElementById('action').value = 'analyze';
        btnAnalyze.disabled = true;
        btnAnalyze.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang phân tích...';

        fetch(ctx + '/doctor/patient-records', {
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
            badge.textContent = 'Mức độ: ' + riskLabel(ai.riskLevel);
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
        document.getElementById('action').value = 'form';
        const clientErrors = validateEncounterForm();
        if (clientErrors.length) {
            e.preventDefault();
            resetSubmitButtons();
            showErrors(clientErrors);
            const firstInvalid = form.querySelector('.input-error');
            if (firstInvalid) firstInvalid.focus();
            return;
        }
        const submitBtn = activeSubmitButton();
        submitBtn.disabled = true;
        submitBtn.innerHTML =
            '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
    });

    // Hiển thị lại lỗi server dưới từng ô (nếu có).
    form.querySelectorAll('[data-error-for]').forEach(function (errorBox) {
        if ((errorBox.textContent || '').trim()) {
            const name = errorBox.getAttribute('data-error-for');
            const field = form.elements[name];
            if (field && field.classList) {
                field.classList.add('input-error');
                field.setAttribute('aria-invalid', 'true');
            }
        }
    });
})();
</script>
</body>
</html>
