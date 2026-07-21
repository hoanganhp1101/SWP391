<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết hồ sơ khám bệnh - ${detailView.patientName}</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/doctor-layout.css">
    <style>
        *{margin:0;padding:0;box-sizing:border-box;font-family:Inter,sans-serif;}
        body{background:#f0f4f8;color:#111827;}
        .page{max-width:1200px;margin:0 auto;padding:24px 20px 48px;}
        .top-bar{display:flex;justify-content:space-between;align-items:center;gap:16px;margin-bottom:24px;flex-wrap:wrap;}
        .back-link{color:#2563eb;text-decoration:none;font-weight:600;display:inline-flex;align-items:center;gap:8px;}
        .export-actions{display:flex;gap:10px;flex-wrap:wrap;}
        .btn-export{display:inline-flex;align-items:center;gap:8px;padding:10px 16px;border-radius:10px;border:1px solid #d1d5db;background:#fff;color:#1d4ed8;font-weight:600;font-size:13px;text-decoration:none;cursor:pointer;}
        .btn-export.primary{background:#1d4ed8;color:#fff;border-color:#1d4ed8;}
        .btn-export:hover{opacity:.92;}
        .btn-delete{display:inline-flex;align-items:center;gap:8px;padding:10px 16px;border-radius:10px;border:1px solid #fca5a5;background:#fff;color:#dc2626;font-weight:600;font-size:13px;cursor:pointer;}
        .success-banner{background:#d1fae5;border:1px solid #6ee7b7;color:#065f46;padding:12px 16px;border-radius:12px;margin-bottom:16px;}
        .patient-header{background:#fff;border:1px solid #e5e7eb;border-radius:16px;padding:24px 28px;margin-bottom:24px;display:flex;justify-content:space-between;gap:20px;flex-wrap:wrap;}
        .patient-header h1{font-size:28px;margin-bottom:8px;}
        .meta{color:#6b7280;font-size:14px;line-height:1.8;}
        .meta strong{color:#374151;}
        .core-panel{display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:24px;}
        .core-metric{background:linear-gradient(135deg,#eff6ff,#dbeafe);border:2px solid #93c5fd;border-radius:16px;padding:24px;text-align:center;}
        .core-metric.critical{background:linear-gradient(135deg,#fef2f2,#fee2e2);border-color:#fca5a5;}
        .core-metric.warning{background:linear-gradient(135deg,#fffbeb,#fef3c7);border-color:#fcd34d;}
        .core-metric .label{font-size:14px;color:#1e40af;font-weight:600;text-transform:uppercase;letter-spacing:.5px;}
        .core-metric.critical .label{color:#b91c1c;}
        .core-metric .value{font-size:42px;font-weight:800;margin:8px 0;color:#1d4ed8;}
        .core-metric.critical .value{color:#dc2626;}
        .core-metric .ref{font-size:12px;color:#6b7280;}
        .alert-banner{background:#fef2f2;border:1px solid #fecaca;color:#b91c1c;padding:12px 16px;border-radius:12px;margin-bottom:24px;font-size:14px;}
        .section{margin-bottom:24px;}
        .section-title{position:sticky;top:0;z-index:10;background:#f0f4f8;padding:12px 0;margin-bottom:12px;display:flex;justify-content:space-between;align-items:center;}
        .section-title h2{font-size:20px;display:flex;align-items:center;gap:10px;}
        .section-title .dot{width:10px;height:10px;border-radius:50%;}
        .dot.blue{background:#3b82f6;}
        .dot.green{background:#10b981;}
        .dot.red{background:#ef4444;}
        .dot.purple{background:#8b5cf6;}
        .dot.orange{background:#f97316;}
        .med-card{background:#fff;border:1px solid #e5e7eb;border-radius:16px;padding:16px;margin-bottom:16px;}
        .med-card h3{font-size:15px;color:#6b7280;margin-bottom:12px;font-weight:600;}
        .field-row{display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #f3f4f6;gap:16px;}
        .field-row:last-child{border-bottom:none;}
        .field-label{color:#6b7280;font-size:14px;flex:1;}
        .field-value{font-weight:600;font-size:15px;text-align:right;}
        .field-value.abnormal{color:#dc2626;}
        .field-value.warning{color:#d97706;}
        .field-value.core{color:#1d4ed8;font-size:16px;}
        .field-ref{font-size:11px;color:#9ca3af;display:block;margin-top:2px;}
        .rec-list{padding-left:20px;line-height:1.8;color:#4b5563;font-size:14px;}
        .lab-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:12px;}
        .lab-item{background:#f9fafb;border-radius:12px;padding:14px 16px;}
        .lab-item.abnormal{background:#fef2f2;border:1px solid #fecaca;}
        .lab-item .name{font-size:13px;color:#6b7280;}
        .lab-item .val{font-size:20px;font-weight:700;margin-top:4px;}
        .lab-item.abnormal .val{color:#dc2626;}
        .lab-item .range{font-size:11px;color:#9ca3af;margin-top:4px;}
        .bio-group{margin-bottom:16px;}
        .bio-group h4{font-size:13px;color:#9ca3af;text-transform:uppercase;letter-spacing:.5px;margin-bottom:10px;}
        .empty-note{color:#9ca3af;font-size:14px;padding:12px 0;}
        .rx-table{width:100%;border-collapse:collapse;}
        .rx-table th,.rx-table td{padding:10px 12px;border-bottom:1px solid #f3f4f6;text-align:left;font-size:14px;}
        .rx-table th{color:#6b7280;font-weight:600;}
        @media(max-width:768px){
            .core-panel,.lab-grid{grid-template-columns:1fr;}
            .export-actions{width:100%;}
        }
    </style>
</head>
<body>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty doctor}">
    <c:set var="doctor" value="${sessionScope.user}"/>
</c:if>
<jsp:include page="/WEB-INF/views/doctor/layout/topbar.jsp"/>
<div class="layout">
    <jsp:include page="/WEB-INF/views/doctor/layout/sidebar.jsp"/>
    <main class="main-content">
<div class="page">

    <div class="top-bar">
        <a href="${pageContext.request.contextPath}/doctor/patient-records" class="back-link">
            <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách hồ sơ khám bệnh
        </a>
        <div class="export-actions">
            <a class="btn-export primary"
               href="${pageContext.request.contextPath}/doctor/record-export-pdf?id=${detailView.recordId}&type=full">
                <i class="fa-solid fa-file-pdf"></i> Xuất PDF
            </a>
        </div>
    </div>

    <c:if test="${param.success eq '1'}">
        <div class="success-banner">
            <i class="fa-solid fa-circle-check"></i> Tạo hồ sơ khám bệnh thành công.
        </div>
    </c:if>
    <c:if test="${param.error eq 'delete'}">
        <div class="alert-banner">Không thể xóa hồ sơ khám bệnh. Vui lòng thử lại sau.</div>
    </c:if>

    <div class="patient-header">
        <div>
            <h1>${detailView.patientName}</h1>
            <div class="meta">
                <div><strong>Bệnh nhân:</strong> ${detailView.patientName}</div>
                <div><strong>Mã bệnh nhân:</strong> ${detailView.patientCode}</div>
                <div><strong>Mã hồ sơ:</strong> ${detailView.recordCode}</div>
                <div><strong>Ngày khám:</strong> ${detailView.examDate}</div>
                <div><strong>Loại hồ sơ:</strong> ${detailView.encounterTypeLabel}</div>
                <div><strong>Khoa khám:</strong> ${detailView.department}</div>
                <div><strong>Bác sĩ khám:</strong> ${detailView.doctorName}</div>
            </div>
        </div>
    </div>

    <c:if test="${detailView.biochemistry.hasData()}">
    <c:forEach items="${detailView.biochemistry.alerts}" var="alert">
        <div class="alert-banner">
            <i class="fa-solid fa-triangle-exclamation"></i> ${alert}
        </div>
    </c:forEach>

    <div class="core-panel">
        <div class="core-metric ${detailView.biochemistry.glucose.abnormal ? 'critical' : ''}">
            <div class="label"><i class="fa-solid fa-droplet"></i> Glucose</div>
            <div class="value">${detailView.biochemistry.glucose.displayValue}</div>
            <div class="ref">Tham chiếu: ${detailView.biochemistry.glucose.referenceRange}</div>
        </div>
        <div class="core-metric ${detailView.biochemistry.hba1c.abnormal ? 'critical' : ''}">
            <div class="label"><i class="fa-solid fa-chart-line"></i> HbA1c</div>
            <div class="value">${detailView.biochemistry.hba1c.displayValue}</div>
            <div class="ref">Tham chiếu: ${detailView.biochemistry.hba1c.referenceRange}</div>
        </div>
    </div>
    </c:if>

    <c:if test="${detailView.encounterType == 'tai_kham_noi_tiet'}">
    <!-- A. BỆNH ÁN TÁI KHÁM NỘI TIẾT -->
    <div class="section" id="section-internal">
        <div class="section-title">
            <h2><span class="dot blue"></span> A. Bệnh án tái khám Nội tiết</h2>
            <a class="btn-export"
               href="${pageContext.request.contextPath}/doctor/record-export-pdf?id=${detailView.recordId}&type=internal">
                <i class="fa-solid fa-file-pdf"></i> Export PDF
            </a>
        </div>

        <c:choose>
            <c:when test="${detailView.internalMedicine.hasData()}">
                <div class="med-card">
                    <h3><i class="fa-solid fa-stethoscope"></i> B. Thông tin lâm sàng</h3>
                    <c:forEach items="${detailView.internalMedicine.clinicalInfo}" var="field">
                        <div class="field-row">
                            <span class="field-label">${field.label}</span>
                            <span class="field-value ${field.abnormal ? 'abnormal' : ''}">
                                ${field.displayValue}
                            </span>
                        </div>
                    </c:forEach>
                </div>

                <div class="med-card">
                    <h3><i class="fa-solid fa-diagnoses"></i> Chẩn đoán</h3>
                    <c:forEach items="${detailView.internalMedicine.diagnosisInfo}" var="field">
                        <div class="field-row">
                            <span class="field-label">${field.label}</span>
                            <span class="field-value">${field.displayValue}</span>
                        </div>
                    </c:forEach>
                </div>

                <div class="med-card">
                    <h3><i class="fa-solid fa-clipboard-list"></i> Khuyến nghị &amp; sinh hoạt</h3>
                    <c:forEach items="${detailView.internalMedicine.recommendationFields}" var="field">
                        <div class="field-row">
                            <span class="field-label">${field.label}</span>
                            <span class="field-value">${field.displayValue}</span>
                        </div>
                    </c:forEach>
                </div>

                <div class="med-card">
                    <h3><i class="fa-solid fa-heart-pulse"></i> C. Chỉ số sức khỏe</h3>
                    <c:forEach items="${detailView.internalMedicine.healthMetrics}" var="field">
                        <div class="field-row">
                            <span class="field-label">${field.label}</span>
                            <span class="field-value ${field.abnormal ? 'abnormal' : ''}">
                                ${field.displayValue}
                                <c:if test="${not empty field.referenceRange}">
                                    <span class="field-ref">${field.referenceRange}</span>
                                </c:if>
                            </span>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="med-card">
                    <p class="empty-note">Chưa có dữ liệu bệnh án.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- F. ĐƠN THUỐC -->
    <div class="section" id="section-prescription">
        <div class="section-title">
            <h2><span class="dot purple"></span> F. Đơn thuốc</h2>
            <a class="btn-export"
               href="${pageContext.request.contextPath}/doctor/record-export-pdf?id=${detailView.recordId}&type=prescription">
                <i class="fa-solid fa-file-pdf"></i> Export PDF
            </a>
        </div>

        <div class="med-card">
            <c:choose>
                <c:when test="${detailView.prescriptionDetail.hasData()}">
                    <table class="rx-table">
                        <thead>
                        <tr>
                            <th>Tên thuốc</th>
                            <th>Hoạt chất</th>
                            <th>Liều lượng</th>
                            <th>Đơn vị</th>
                            <th>Đường dùng</th>
                            <th>Tần suất</th>
                            <th>Thời điểm uống</th>
                            <th>Số ngày</th>
                            <th>Ghi chú</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${detailView.prescriptionDetail.items}" var="med">
                            <tr>
                                <td>${med.name}</td>
                                <td>${med.ingredient}</td>
                                <td>${med.dose}</td>
                                <td>${med.unit}</td>
                                <td>${med.route}</td>
                                <td>${med.frequency}</td>
                                <td>${med.usage}</td>
                                <td>${med.days}</td>
                                <td>${med.note}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p class="empty-note">Chưa có đơn thuốc.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    </c:if>

    <c:if test="${detailView.bloodCount.hasData()}">
    <!-- C. XÉT NGHIỆM MÁU TỔNG QUÁT -->
    <div class="section" id="section-blood">
        <div class="section-title">
            <h2><span class="dot green"></span> C. Kết quả xét nghiệm máu tổng quát</h2>
            <a class="btn-export"
               href="${pageContext.request.contextPath}/doctor/record-export-pdf?id=${detailView.recordId}&type=blood">
                <i class="fa-solid fa-file-pdf"></i> Export PDF
            </a>
        </div>

        <div class="med-card">
            <c:choose>
                <c:when test="${detailView.bloodCount.hasData()}">
                    <div class="lab-grid">
                        <c:forEach items="${detailView.bloodCount.items}" var="lab">
                            <div class="lab-item ${lab.abnormal ? 'abnormal' : ''}">
                                <div class="name">${lab.label}</div>
                                <div class="val">${lab.displayValue}</div>
                                <div class="range">${lab.referenceRange}</div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="empty-note">Chưa có dữ liệu xét nghiệm máu tổng quát cho hồ sơ này.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    </c:if>

    <c:if test="${detailView.biochemistry.hasData()}">
    <!-- D. SINH HÓA MÁU -->
    <div class="section" id="section-bio">
        <div class="section-title">
            <h2><span class="dot red"></span> D. Kết quả sinh hóa máu</h2>
            <a class="btn-export"
               href="${pageContext.request.contextPath}/doctor/record-export-pdf?id=${detailView.recordId}&type=biochemistry">
                <i class="fa-solid fa-file-pdf"></i> Export PDF
            </a>
        </div>

        <div class="med-card">
            <div class="bio-group">
                <h4>Đường huyết</h4>
                <div class="field-row">
                    <span class="field-label">${detailView.biochemistry.glucose.label}</span>
                    <span class="field-value core ${detailView.biochemistry.glucose.abnormal ? 'abnormal' : ''}">
                        ${detailView.biochemistry.glucose.displayValue}
                    </span>
                </div>
                <div class="field-row">
                    <span class="field-label">${detailView.biochemistry.hba1c.label}</span>
                    <span class="field-value core ${detailView.biochemistry.hba1c.abnormal ? 'abnormal' : ''}">
                        ${detailView.biochemistry.hba1c.displayValue}
                    </span>
                </div>
            </div>

            <div class="bio-group">
                <h4>Mỡ máu</h4>
                <c:forEach items="${detailView.biochemistry.lipidProfile}" var="field">
                    <div class="field-row">
                        <span class="field-label">${field.label}</span>
                        <span class="field-value ${field.abnormal ? 'abnormal' : ''}">${field.displayValue}
                            <span class="field-ref">${field.referenceRange}</span>
                        </span>
                    </div>
                </c:forEach>
            </div>

            <div class="bio-group">
                <h4>Chức năng gan</h4>
                <c:forEach items="${detailView.biochemistry.liverEnzymes}" var="field">
                    <div class="field-row">
                        <span class="field-label">${field.label}</span>
                        <span class="field-value ${field.abnormal ? 'abnormal' : ''}">${field.displayValue}
                            <span class="field-ref">${field.referenceRange}</span>
                        </span>
                    </div>
                </c:forEach>
            </div>

            <div class="bio-group">
                <h4>Chức năng thận</h4>
                <c:forEach items="${detailView.biochemistry.kidneyFunction}" var="field">
                    <div class="field-row">
                        <span class="field-label">${field.label}</span>
                        <span class="field-value ${field.abnormal ? 'abnormal' : ''}">${field.displayValue}
                            <span class="field-ref">${field.referenceRange}</span>
                        </span>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
    </c:if>

</div>
</main>
</div>
</body>
</html>
