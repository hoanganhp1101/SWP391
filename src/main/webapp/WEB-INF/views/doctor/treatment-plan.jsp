<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Treatment Plan - HealthAlert</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
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
        .full-width { grid-column: span 2; }
        .patient-info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
        .info-field label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }
        .info-field span { font-weight: 600; color: #1e293b; }
        .ai-summary {
            background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 16px; padding: 20px;
            white-space: pre-wrap; font-size: 14px; color: #334155; line-height: 1.7; min-height: 80px;
        }
        .ai-badge {
            display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 700;
            padding: 4px 10px; border-radius: 999px; background: #eef2ff; color: #4338ca;
        }
        .ai-note { margin-top: 12px; font-size: 12px; color: #94a3b8; }
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

            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/doctor-dashboard">Dashboard</a>
                <span>/</span>
                <a href="${pageContext.request.contextPath}/doctor/patient-records">Quản lý hồ sơ khám bệnh</a>
                <span>/</span>
                <span>Treatment Plan</span>
            </nav>

            <div class="page-header">
                <h1>Bước 2 · Treatment Plan</h1>
                <p>Xem lại phân tích AI và hoàn tất chẩn đoán, đơn thuốc, hướng xử trí cho lần khám
                    <strong>${encounter.displayCode}</strong>.</p>
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
                        <div class="info-field"><label>Loại tiểu đường</label><span><c:out value="${not empty patient.loaiTieuDuong ? patient.loaiTieuDuong : '—'}"/></span></div>
                        <div class="info-field"><label>Triệu chứng</label><span><c:out value="${not empty encounter.lyDoKham ? encounter.lyDoKham : '—'}"/></span></div>
                    </div>
                </div>
            </div>

            <!-- AI Summary (readonly) -->
            <div class="card">
                <div class="card-top">
                    <span><i class="fa-solid fa-robot"></i> AI Summary</span>
                    <span class="ai-badge"><i class="fa-solid fa-circle-info"></i> Chỉ tham khảo</span>
                </div>
                <div class="card-body">
                    <div class="ai-summary"><c:choose>
                        <c:when test="${not empty aiSummary}"><c:out value="${aiSummary}"/></c:when>
                        <c:otherwise>Không có dữ liệu phân tích AI cho lần khám này (phiên có thể đã hết hạn). Bác sĩ tự đánh giá lâm sàng.</c:otherwise>
                    </c:choose></div>
                    <p class="ai-note">AI chỉ hỗ trợ, không kê đơn và không đưa quyết định cuối cùng. Bác sĩ chịu trách nhiệm chẩn đoán và điều trị.</p>
                </div>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/doctor/treatment-plan" id="treatmentForm">
                <input type="hidden" name="encounterId" value="${encounter.id}">

                <!-- Chẩn đoán -->
                <div class="card">
                    <div class="card-top"><i class="fa-solid fa-notes-medical"></i> Chẩn đoán</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group">
                                <label>Chẩn đoán chính <span class="req">*</span></label>
                                <input type="text" name="chanDoanChinh" value="${currentDiagnosis}" required>
                            </div>
                            <div class="form-group">
                                <label>Chẩn đoán phụ</label>
                                <input type="text" name="chanDoanPhu" value="${encounter.chanDoanPhu}">
                            </div>
                            <div class="form-group">
                                <label>Phân loại tiểu đường</label>
                                <select name="phanLoaiTieuDuong">
                                    <option value="">-- Chọn --</option>
                                    <option value="Type 1" ${patient.loaiTieuDuong eq 'Type 1' ? 'selected' : ''}>Type 1</option>
                                    <option value="Type 2" ${patient.loaiTieuDuong eq 'Type 2' ? 'selected' : ''}>Type 2</option>
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
    const medList = document.getElementById('medicationList');
    document.getElementById('btnAddMed').addEventListener('click', function () {
        const tpl = medList.querySelector('[data-med-row]');
        if (!tpl) return;
        const clone = tpl.cloneNode(true);
        clone.querySelectorAll('input').forEach(function (i) { i.value = ''; });
        medList.appendChild(clone);
        reindexMeds();
    });
    medList.addEventListener('click', function (e) {
        const btn = e.target.closest('.btn-remove-med');
        if (!btn) return;
        const rows = medList.querySelectorAll('[data-med-row]');
        if (rows.length <= 1) {
            rows[0].querySelectorAll('input').forEach(function (i) { i.value = ''; });
            return;
        }
        btn.closest('[data-med-row]').remove();
        reindexMeds();
    });
    function reindexMeds() {
        medList.querySelectorAll('[data-med-row]').forEach(function (row, idx) {
            row.querySelector('.med-index').textContent = idx + 1;
        });
    }

    document.getElementById('treatmentForm').addEventListener('submit', function (e) {
        const diagnosis = document.querySelector('[name="chanDoanChinh"]');
        if (diagnosis && !diagnosis.value.trim()) {
            e.preventDefault();
            alert('Vui lòng nhập chẩn đoán chính.');
            diagnosis.focus();
            return;
        }
        document.getElementById('btnSave').setAttribute('disabled', 'disabled');
    });
})();
</script>
</body>
</html>
