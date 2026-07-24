<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kế hoạch điều trị - HealthAlert</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
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
            .card { background: white; border: 1px solid #e5e7eb; border-radius: 24px; margin-bottom: 24px; }
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
                border: 1px solid #d1d5db; border-radius: 14px; padding: 14px 18px; font-size: 15px; outline: none;
            }
            .form-group input:focus,
            .form-group select:focus,
            .form-group textarea:focus { border-color: #1557d5; }
            .form-group .input-error { border-color: #dc2626; background: #fff7f7; }
            .field-error { min-height: 18px; margin-top: 6px; color: #dc2626; font-size: 13px; }
            .full-width { grid-column: span 2; }
            .patient-info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
            .info-field label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }
            .info-field span { font-weight: 600; color: #1e293b; }
            .ai-summary {
                background: #f8fafc;
                border: 1px solid #e5e7eb;
                border-radius: 16px;
                padding: 20px 22px;
                white-space: pre-wrap;
                word-break: break-word;
                font-size: 14px;
                color: #334155;
                line-height: 1.7;
                display: block;
                width: 100%;
                box-sizing: border-box;
                min-height: 160px;
            }
            .ai-summary-empty {
                background: #f8fafc;
                border: 1px solid #e5e7eb;
                border-radius: 16px;
                min-height: 160px;
                height: 160px;
                padding: 24px 28px;
                box-sizing: border-box;
                width: 100%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-align: center;
                gap: 8px;
            }
            .ai-summary-empty .ai-empty-icon {
                width: 48px;
                height: 48px;
                border-radius: 14px;
                background: #eef2ff;
                color: #4338ca;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 20px;
                margin-bottom: 4px;
            }
            .ai-summary-empty .ai-empty-title {
                font-size: 15px;
                font-weight: 700;
                color: #1e293b;
                line-height: 1.4;
            }
            .ai-summary-empty .ai-empty-desc {
                font-size: 13px;
                color: #64748b;
                line-height: 1.6;
                max-width: 420px;
            }
            .ai-badge {
                display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 700;
                padding: 4px 10px; border-radius: 999px; background: #eef2ff; color: #4338ca;
            }
            .ai-note {
                margin-top: 16px;
                font-size: 12px;
                color: #94a3b8;
                line-height: 1.5;
            }
            .alert-error {
                background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b;
                padding: 14px 20px; border-radius: 12px; margin-bottom: 20px;
            }
            .alert-error ul { margin: 8px 0 0 18px; }
            .med-row {
                border: 1px dashed #d1d5db; border-radius: 12px; padding: 16px; margin-bottom: 12px; background: #fafbfc;
            }
            .med-row-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
            .btn {
                border: none; padding: 14px 22px; border-radius: 14px; font-size: 15px; font-weight: 600;
                cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 8px;
            }
            .btn-outline { background: white; border: 1px solid #dbe2ea; color: #374151; }
            .btn-primary { background: #2563eb; color: white; }
            .btn-sm { padding: 8px 14px; font-size: 13px; border-radius: 10px; }
            .btn-danger-outline { background: #fff; border: 1px solid #fca5a5; color: #dc2626; }
            .btn-add-outline { background: #fff; border: 1px solid #93c5fd; color: #2563eb; }
            .form-actions { display: flex; justify-content: flex-end; gap: 16px; margin-top: 8px; margin-bottom: 32px; }
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
            <h1>Đưa ra chẩn đoán, đơn thuốc</h1>
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

        <!-- Thông tin bệnh nhân -->
        <div class="card">
            <div class="card-top"><i class="fa-solid fa-user"></i> Thông tin bệnh nhân</div>
            <div class="card-body">
                <div class="patient-info-grid">
                    <div class="info-field"><label>Mã bệnh nhân</label><span><c:out value="${not empty patient ? patient.patientCode : encounter.patientCode}"/></span></div>
                    <div class="info-field"><label>Họ và tên</label><span><c:out value="${not empty patient.user ? patient.user.hoTen : encounter.patientName}"/></span></div>
                    <div class="info-field"><label>Giới tính</label><span><c:out value="${not empty patient.gioiTinh ? patient.gioiTinh : '—'}"/></span></div>
                    <div class="info-field"><label>Tuổi</label><span><c:out value="${not empty patient.tuoi ? patient.tuoi : '—'}"/></span></div>
                    <div class="info-field"><label>Loại tiểu đường</label><span><c:out value="${patient.loaiTieuDuong eq 'Type 1' ? 'Tiểu đường týp 1' : (patient.loaiTieuDuong eq 'Type 2' ? 'Tiểu đường týp 2' : (not empty patient.loaiTieuDuong ? patient.loaiTieuDuong : '—'))}"/></span></div>
                    <div class="info-field"><label>Triệu chứng</label><span><c:out value="${not empty encounter.lyDoKham ? encounter.lyDoKham : '—'}"/></span></div>
                </div>
            </div>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/doctor/treatment-plan" id="treatmentForm" novalidate>
            <input type="hidden" name="encounterId" value="${encounter.id}">

            <!-- Chẩn đoán -->
            <div class="card">
                <div class="card-top"><i class="fa-solid fa-notes-medical"></i> Chẩn đoán</div>
                <div class="card-body">
                    <div class="form-container">
                        <div class="form-group">
                            <label>Chẩn đoán chính <span class="req">*</span></label>
                            <input type="text" name="chanDoanChinh" value="${currentDiagnosis}" required>
                            <div class="field-error" data-error-for="chanDoanChinh"><c:out value="${fieldErrors['chanDoanChinh']}"/></div>
                        </div>
                        <div class="form-group">
                            <label>Chẩn đoán phụ</label>
                            <input type="text" name="chanDoanPhu" value="${encounter.chanDoanPhu}">
                        </div>
                        <div class="form-group">
                            <label>Phân loại tiểu đường</label>
                            <select name="phanLoaiTieuDuong">
                                <option value="">-- Chọn --</option>
                                <option value="Type 1" ${patient.loaiTieuDuong eq 'Type 1' ? 'selected' : ''}>Tiểu đường týp 1</option>
                                <option value="Type 2" ${patient.loaiTieuDuong eq 'Type 2' ? 'selected' : ''}>Tiểu đường týp 2</option>
                                <option value="Tiền đái tháo đường" ${patient.loaiTieuDuong eq 'Tiền đái tháo đường' ? 'selected' : ''}>Tiền đái tháo đường</option>
                                <option value="Khác" ${patient.loaiTieuDuong eq 'Khác' ? 'selected' : ''}>Khác</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Hướng xử trí</label>
                            <input type="text" name="huongXuTri" value="${encounter.huongXuTri}">
                        </div>
                    </div>
                </div>
            </div>

            <!-- Hướng xử trí / Lifestyle -->
            <div class="card">
                <div class="card-top"><i class="fa-solid fa-heart"></i> Hướng xử trí &amp; Lối sống</div>
                <div class="card-body">
                    <div class="form-container">
                        <div class="form-group full-width">
                            <label>Khuyến nghị điều trị</label>
                            <textarea name="khuyenNghiDieuTri" rows="2">${advice['huong_dieu_tri']}</textarea>
                        </div>
                        <div class="form-group">
                            <label>Chế độ ăn</label>
                            <input type="text" name="cheDoAn" value="${advice['che_do_an']}" placeholder="VD: Hạn chế tinh bột, ăn nhiều rau xanh">
                        </div>
                        <div class="form-group">
                            <label>Luyện tập</label>
                            <input type="text" name="luyenTap" value="${advice['luyen_tap']}" placeholder="VD: Đi bộ 30 phút/ngày">
                        </div>
                    </div>
                </div>
            </div>

            <!-- Đơn thuốc -->
            <div class="card">
                <div class="card-top" style="justify-content:space-between;">
                    <span><i class="fa-solid fa-pills"></i> Đơn thuốc (tùy chọn)</span>
                    <button type="button" class="btn btn-sm btn-add-outline" id="btnAddMed">
                        <i class="fa-solid fa-plus"></i> Thêm thuốc
                    </button>
                </div>
                <div class="card-body" id="medicationList">
                    <c:choose>
                        <c:when test="${not empty meds}">
                            <c:forEach var="med" items="${meds}" varStatus="st">
                                <div class="med-row" data-med-row>
                                    <div class="med-row-header">
                                        <strong>Thuốc #<span class="med-index">${st.index + 1}</span></strong>
                                        <button type="button" class="btn btn-sm btn-danger-outline btn-remove-med"><i class="fa-solid fa-xmark"></i></button>
                                    </div>
                                    <div class="form-container">
                                        <div class="form-group"><input name="medTenThuoc" placeholder="Tên thuốc *" value="<c:out value='${med.name}'/>"></div>
                                        <div class="form-group"><input name="medHoatChat" placeholder="Hoạt chất" value="<c:out value='${med.ingredient}'/>"></div>
                                        <div class="form-group"><input name="medLieuLuong" placeholder="Liều lượng *" value="<c:out value='${med.dose}'/>"></div>
                                        <div class="form-group"><input name="medDonVi" placeholder="Đơn vị" value="<c:out value='${med.unit}'/>"></div>
                                        <div class="form-group"><input name="medDuongDung" placeholder="Đường dùng" value="<c:out value='${med.route}'/>"></div>
                                        <div class="form-group"><input name="medTanSuat" placeholder="Tần suất *" value="<c:out value='${med.frequency}'/>"></div>
                                        <div class="form-group"><input type="number" min="0" name="medThoiGianDungNgay" placeholder="Số ngày dùng" value="<c:out value='${med.days}'/>"></div>
                                        <div class="form-group full-width"><input name="medGhiChu" placeholder="Ghi chú" value="<c:out value='${med.note}'/>"></div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="med-row" data-med-row>
                                <div class="med-row-header">
                                    <strong>Thuốc #<span class="med-index">1</span></strong>
                                    <button type="button" class="btn btn-sm btn-danger-outline btn-remove-med"><i class="fa-solid fa-xmark"></i></button>
                                </div>
                                <div class="form-container">
                                    <div class="form-group"><input name="medTenThuoc" placeholder="Tên thuốc *"></div>
                                    <div class="form-group"><input name="medHoatChat" placeholder="Hoạt chất"></div>
                                    <div class="form-group"><input name="medLieuLuong" placeholder="Liều lượng *"></div>
                                    <div class="form-group"><input name="medDonVi" placeholder="Đơn vị"></div>
                                    <div class="form-group"><input name="medDuongDung" placeholder="Đường dùng"></div>
                                    <div class="form-group"><input name="medTanSuat" placeholder="Tần suất *"></div>
                                    <div class="form-group"><input type="number" min="0" name="medThoiGianDungNgay" placeholder="Số ngày dùng"></div>
                                    <div class="form-group full-width"><input name="medGhiChu" placeholder="Ghi chú"></div>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/doctor/patient-records" class="btn btn-outline">Hủy</a>
                <button type="submit" class="btn btn-primary" id="btnSave"><i class="fa-solid fa-save"></i> Lưu hồ sơ</button>
            </div>
        </form>
    </main>
</div>

<script>
    (function () {
        const form = document.getElementById('treatmentForm');
        const diagnosis = form.elements.chanDoanChinh;
        const saveButton = document.getElementById('btnSave');

        function setError(field, message) {
            field.classList.toggle('input-error', Boolean(message));
            field.setAttribute('aria-invalid', message ? 'true' : 'false');
            let error = field.parentElement.querySelector('.field-error');
            if (!error) {
                error = document.createElement('div');
                error.className = 'field-error';
                field.parentElement.appendChild(error);
            }
            error.textContent = message || '';
        }

        function validateDiagnosis() {
            const message = diagnosis.value.trim() ? '' : 'Vui lòng nhập Chẩn đoán chính.';
            setError(diagnosis, message);
            return message;
        }

        function validateMedicationRow(row, index) {
            const errors = [];
            const name = row.querySelector('[name="medTenThuoc"]');
            const ingredient = row.querySelector('[name="medHoatChat"]');
            const dose = row.querySelector('[name="medLieuLuong"]');
            const unit = row.querySelector('[name="medDonVi"]');
            const route = row.querySelector('[name="medDuongDung"]');
            const frequency = row.querySelector('[name="medTanSuat"]');
            const timing = row.querySelector('[name="medThoiDiemUong"]');
            const days = row.querySelector('[name="medThoiGianDungNgay"]');
            const note = row.querySelector('[name="medGhiChu"]');
            const medFields = [name, ingredient, dose, unit, route, frequency, timing, days, note];
            const hasMedication = medFields.some(function (field) {
                return field && field.value.trim();
            });

            function requireText(field, label) {
                if (!field) {
                    return;
                }
                if (!field.value.trim()) {
                    const message = 'Vui lòng nhập ' + label + ' cho thuốc dòng ' + index + '.';
                    setError(field, message);
                    errors.push(message);
                } else {
                    setError(field, '');
                }
            }

            function requirePositiveNumber(field, label, integerOnly) {
                if (!field) {
                    return;
                }
                const value = field.value.trim();
                const numberPattern = integerOnly ? /^\d+$/ : /^\d+(\.\d+)?$/;
                if (!value) {
                    const message = 'Vui lòng nhập ' + label + ' cho thuốc dòng ' + index + '.';
                    setError(field, message);
                    errors.push(message);
                    return;
                }
                if (!numberPattern.test(value) || Number(value) <= 0) {
                    const message = label + ' của thuốc dòng ' + index
                        + ' chỉ được nhập số lớn hơn 0, không chứa chữ, ký tự đặc biệt hoặc khoảng trắng.';
                    setError(field, message);
                    errors.push(message);
                } else {
                    setError(field, '');
                }
            }

            if (!hasMedication) {
                setError(name, '');
                setError(ingredient, '');
                setError(dose, '');
                setError(unit, '');
                setError(route, '');
                setError(frequency, '');
                if (timing) {
                    setError(timing, '');
                }
                setError(days, '');
                return errors;
            }

            requireText(name, 'Tên thuốc');
            requireText(ingredient, 'Hoạt chất');
            requirePositiveNumber(dose, 'Liều lượng', false);
            requireText(unit, 'Đơn vị');
            requireText(route, 'Đường dùng');
            requireText(frequency, 'Tần suất');
            requireText(timing, 'Thời điểm uống');
            requirePositiveNumber(days, 'Số ngày', true);
            return errors;
        }

        function validateForm() {
            const errors = [];
            const diagnosisError = validateDiagnosis();
            if (diagnosisError) errors.push(diagnosisError);
            form.querySelectorAll('[data-med-row]').forEach(function (row, index) {
                errors.push.apply(errors, validateMedicationRow(row, index + 1));
            });
            return errors;
        }

        diagnosis.addEventListener('input', validateDiagnosis);
        form.addEventListener('input', function (event) {
            const row = event.target.closest('[data-med-row]');
            if (!row) {
                return;
            }
            if (event.target.matches('[name^="med"]')) {
                setError(event.target, '');
            }
            const rows = Array.from(form.querySelectorAll('[data-med-row]'));
            validateMedicationRow(row, rows.indexOf(row) + 1);
        });
        form.addEventListener('submit', function (event) {
            const errors = validateForm();
            if (errors.length) {
                event.preventDefault();
                const first = form.querySelector('.input-error');
                if (first) first.focus();
                return;
            }
            saveButton.disabled = true;
            saveButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';
        });
    })();
</script>
</body>
</html>
