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
        *{ margin:0; padding:0; box-sizing:border-box; font-family:Inter, sans-serif; }
        body{ background:#f5f6fa; }
        .layout{ display:flex; height:calc(100vh - 80px); }
        .topbar{
            height:80px; background:white; display:flex; align-items:center;
            padding:0 48px; border-bottom:1px solid #e5e7eb;
        }
        .sidebar{
            width:240px; background:#fff; border-right:1px solid #e5e7eb;
            display:flex; flex-direction:column;
        }
        .doctor-profile{ padding:28px 20px; display:flex; align-items:center; gap:12px; }
        .doctor-profile img{ width:42px; height:42px; border-radius:10px; object-fit:cover; }
        .doctor-profile h4{ font-size:16px; color:#1554c7; }
        .doctor-profile p{ font-size:12px; color:#666; }
        .menu{ padding:0 16px; }
        .menu-item{
            display:flex; align-items:center; gap:14px; height:52px; margin-bottom:8px;
            padding:0 16px; border-radius:12px; color:#374151; text-decoration:none; cursor:pointer;
        }
        .menu-item i{ font-size:18px; }
        .menu-item.active{ background:#1557d5; color:white; font-weight:600; }
        .sidebar-bottom{ margin-top:auto; padding:20px 16px; }
        .new-record{
            width:100%; height:48px; border:none; border-radius:10px;
            background:#0d4bb5; color:white; font-size:15px; font-weight:600; cursor:pointer;
        }
        .new-record i{ margin-right:8px; }
        .bottom-link{
            display:flex; align-items:center; gap:12px; padding:14px 12px;
            text-decoration:none; color:#374151; cursor:pointer;
        }
        .main-content{ flex:1; overflow-y:auto; }
        .logo{ font-size:20px; font-weight:700; color:#0d4bb5; }
        .top-nav{ display:flex; gap:36px; margin-left:40px; }
        .top-actions{ display:flex; align-items:center; gap:22px; margin-left:auto; }
        .top-nav a{ color:#555; cursor:pointer; font-size:16px; text-decoration:none; }
        .top-nav .active{ color:#1557d5; font-weight:600; position:relative; }
        .top-nav .active::after{
            content:""; position:absolute; left:0; bottom:-28px;
            width:100%; height:3px; background:#1557d5;
        }
        .search-box{
            width:290px; height:42px; display:flex; align-items:center;
            padding:0 16px; border:1px solid #d1d5db; border-radius:10px; background:#fff;
        }
        .search-box i{ color:#777; }
        .search-box input{ border:none; outline:none; width:100%; margin-left:10px; font-size:14px; }
        .icon-btn{ font-size:22px; color:#4b5563; cursor:pointer; }
        .avatar{ width:38px; height:38px; border-radius:50%; object-fit:cover; }
        .page-content{ padding:32px; }
        .page-header{ margin-bottom:28px; }
        .page-header h1{ font-size:38px; font-weight:700; margin-bottom:10px; }
        .page-header p{ color:#64748b; }
        .breadcrumb{ margin-bottom:20px; font-size:14px; color:#64748b; }
        .breadcrumb a{ color:#1557d5; text-decoration:none; }
        .breadcrumb span{ margin:0 8px; }
        .card{
            background:white; border:1px solid #e5e7eb; border-radius:24px;
            overflow:visible; margin-bottom:24px;
        }
        .card-top{
            padding:26px; display:flex; justify-content:space-between; align-items:center;
            gap:20px; border-bottom:1px solid #e5e7eb; font-weight:600; font-size:18px;
        }
        .card-body{ padding:26px; }
        .form-container{
            display:grid; grid-template-columns:repeat(2, 1fr); gap:24px;
        }
        .form-group{ display:flex; flex-direction:column; }
        .form-group label{ margin-bottom:10px; font-weight:600; color:#374151; }
        .form-group label .req{ color:#dc2626; }
        .form-group input,
        .form-group select,
        .form-group textarea{
            border:1px solid #d1d5db; border-radius:14px; padding:14px 18px;
            font-size:15px; outline:none;
        }
        .form-group input:focus,
        .form-group select:focus,
        .form-group textarea:focus{ border-color:#1557d5; }
        .form-group input[readonly]{ background:#f8fafc; color:#374151; }
        .full-width{ grid-column:span 2; }
        .record-search-box{ position:relative; }
        .record-search-box i{
            position:absolute; top:50%; left:16px; transform:translateY(-50%); color:#94a3b8;
        }
        .record-search-box input{
            width:100%; padding:16px 18px 16px 48px; border:1px solid #dbe2ea;
            border-radius:14px; outline:none; font-size:15px;
        }
        .patient-results{
            max-height:220px; overflow-y:auto; border:1px solid #e5e7eb;
            border-radius:14px; margin-top:12px; display:none;
        }
        .patient-results.show{ display:block; }
        .patient-item{
            padding:14px 18px; cursor:pointer; border-bottom:1px solid #f1f5f9;
        }
        .patient-item:hover{ background:#f8fafc; }
        .patient-item.selected{ background:#eff6ff; }
        .patient-item strong{ color:#1557d5; margin-right:8px; }
        .patient-info-panel{
            margin-top:24px; padding:20px; background:#f8fafc;
            border-radius:16px; border:1px solid #e5e7eb; display:none;
        }
        .patient-info-panel.show{ display:block; }
        .patient-info-grid{
            display:grid; grid-template-columns:repeat(3, 1fr); gap:16px; margin-top:12px;
        }
        .info-field label{ display:block; font-size:12px; color:#64748b; margin-bottom:4px; }
        .info-field span{ font-weight:600; color:#1e293b; }
        .alert-error{
            background:#fee2e2; border:1px solid #fca5a5; color:#991b1b;
            padding:14px 20px; border-radius:12px; margin-bottom:20px;
        }
        .alert-error ul{ margin:8px 0 0 18px; }
        .med-row{
            border:1px dashed #d1d5db; border-radius:12px; padding:16px;
            margin-bottom:12px; background:#fafbfc;
        }
        .med-row-header{ display:flex; justify-content:space-between; margin-bottom:12px; }
        .btn{
            border:none; padding:14px 22px; border-radius:14px;
            font-size:15px; font-weight:600; cursor:pointer; text-decoration:none;
            display:inline-flex; align-items:center; gap:8px;
        }
        .btn-outline{ background:white; border:1px solid #dbe2ea; color:#374151; }
        .btn-primary{ background:#2563eb; color:white; }
        .btn-sm{ padding:8px 14px; font-size:13px; border-radius:10px; }
        .btn-danger-outline{ background:#fff; border:1px solid #fca5a5; color:#dc2626; }
        .btn-add-outline{ background:#fff; border:1px solid #93c5fd; color:#2563eb; }
        .form-actions{
            display:flex; justify-content:flex-end; gap:16px; margin-top:8px; margin-bottom:32px;
        }
        .section-toggle{
            background:none; border:none; font-size:18px; font-weight:600;
            cursor:pointer; color:#1e293b; display:flex; align-items:center; gap:8px;
        }
        .collapsible{ display:block; }
        .collapsible.hidden{ display:none; }
    </style>
</head>
<body>

<header class="topbar">

    <div class="logo">
        HealthAlert
    </div>

    <div class="top-nav">
        <a class="active">Bảng điều khiển</a>
        <a>Bệnh nhân</a>
        <a>Hồ sơ</a>
        <a>Báo cáo</a>
    </div>

    <div class="top-actions">

        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input
                    type="text"
                    placeholder="Tìm kiếm hồ sơ y tế..."
            >
        </div>

        <i class="fa-regular fa-bell icon-btn"></i>
        <i class="fa-solid fa-gear icon-btn"></i>

        <img
                class="topbar-avatar"
                src="${not empty doctor.anhDaiDien ? doctor.anhDaiDien : 'https://i.pravatar.cc/40'}"
                alt=""
        >

    </div>

</header>

<div class="layout">
    <aside class="sidebar">

        <div class="doctor-profile">
            <img src="${not empty doctor.anhDaiDien ? doctor.anhDaiDien : 'https://i.pravatar.cc/60'}" alt="">
            <div>
                <h4>${not empty doctor.hoTen ? doctor.hoTen : 'Bác sĩ'}</h4>
                <p>${not empty doctor.vaiTro ? doctor.vaiTro : 'Bác sĩ điều trị'}</p>
            </div>
        </div>

        <nav class="menu">

            <a class="menu-item active">
                <i class="fa-solid fa-table-cells"></i>
                <span>Tổng quan</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/patient-list" class="menu-item">
                <i class="fa-solid fa-users"></i>
                <span>Danh sách bệnh nhân</span>
            </a>

            <a class="menu-item">
                <i class="fa-regular fa-bell"></i>
                <span>Cảnh báo khẩn cấp</span>
            </a>

            <a href="${pageContext.request.contextPath}/doctor/patient-records" class="menu-item">
                <i class="fa-regular fa-clipboard"></i>
                <span>Tiền sử bệnh án</span>
            </a>

            <a class="menu-item">
                <i class="fa-solid fa-chart-column"></i>
                <span>Phân tích dữ liệu</span>
            </a>

        </nav>

        <div class="sidebar-bottom">

            <button class="new-record">
                <i class="fa-solid fa-plus"></i>
                Tạo hồ sơ mới
            </button>

            <a class="bottom-link">
                <i class="fa-regular fa-circle-question"></i>
                Hỗ trợ
            </a>

            <a class="bottom-link">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
                Đăng xuất
            </a>

        </div>

    </aside>

    <main class="main-content">
        <div class="page-content">

            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/doctor-dashboard">Dashboard</a>
                <span>/</span>
                <a href="${pageContext.request.contextPath}/doctor/patient-records">Quản lý hồ sơ bệnh án</a>
                <span>/</span>
                <span>Thêm hồ sơ bệnh án</span>
            </nav>

            <div class="page-header">
                <h1>Thêm hồ sơ bệnh án mới</h1>
                <p>Chọn bệnh nhân và nhập thông tin khám, chỉ số sức khỏe, xét nghiệm và đơn thuốc</p>
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

            <form method="post" action="${pageContext.request.contextPath}/medical-encounters/add" id="encounterForm">
                <input type="hidden" name="patientId" id="patientId" value="${form.patientId}">
                <input type="hidden" name="thoiDiemDoDuong" value="luc_doi">

                <div class="card">
                    <div class="card-top"><i class="fa-solid fa-clipboard-list"></i> A. Thông tin chung</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group full-width">
                                <label>Tìm và chọn bệnh nhân <span class="req">*</span></label>
                                <div class="record-search-box">
                                    <i class="fa-solid fa-magnifying-glass"></i>
                                    <input type="text" id="patientSearch" placeholder="Tìm theo mã bệnh nhân hoặc họ tên..." autocomplete="off">
                                </div>
                                <div class="patient-results" id="patientResults"></div>
                                <div id="selectedPatientLabel" style="margin-top:12px;font-size:14px;color:#64748b;"></div>
                            </div>
                            <div class="form-group">
                                <label>Mã hồ sơ</label>
                                <input type="text" readonly value="Tự sinh sau khi lưu">
                            </div>
                            <div class="form-group">
                                <label>Ngày khám <span class="req">*</span></label>
                                <input type="date" name="ngayKham" value="${form.ngayKham}" required>
                            </div>
                            <div class="form-group">
                                <label>Khoa khám</label>
                                <input type="text" readonly value="Khoa nội tiết">
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
                <div class="card">
                    <div class="card-top"><i class="fa-solid fa-stethoscope"></i> B. Thông tin lâm sàng</div>
                    <div class="card-body">
                        <div class="form-container">
                            <div class="form-group full-width">
                                <label>Triệu chứng <span class="req">*</span></label>
                                <textarea name="trieuChung" rows="2" required>${not empty form.trieuChung ? form.trieuChung : form.lyDoKham}</textarea>
                            </div>
                            <div class="form-group full-width">
                                <label>Tiền sử bệnh</label>
                                <textarea name="tienSuBenh" rows="2">${not empty form.tienSuBenh ? form.tienSuBenh : form.quaTrinhBenhLy}</textarea>
                            </div>
                            <div class="form-group full-width">
                                <label>Khám lâm sàng</label>
                                <textarea name="khamLamSang" rows="2">${form.khamLamSang}</textarea>
                            </div>
                            <div class="form-group">
                                <label>Chẩn đoán chính <span class="req">*</span></label>
                                <input type="text" name="chanDoanChinh" value="${form.chanDoanChinh}" required>
                            </div>
                            <div class="form-group">
                                <label>Chẩn đoán phụ</label>
                                <input type="text" name="chanDoanPhu" value="${form.chanDoanPhu}">
                            </div>
                            <div class="form-group">
                                <label>Phân loại tiểu đường</label>
                                <select name="phanLoaiTieuDuong">
                                    <option value="">-- Chọn --</option>
                                    <option value="Type 1" ${form.phanLoaiTieuDuong eq 'Type 1' ? 'selected' : ''}>Type 1</option>
                                    <option value="Type 2" ${form.phanLoaiTieuDuong eq 'Type 2' ? 'selected' : ''}>Type 2</option>
                                    <option value="Tiền đái tháo đường" ${form.phanLoaiTieuDuong eq 'Tiền đái tháo đường' ? 'selected' : ''}>Tiền đái tháo đường</option>
                                    <option value="Khác" ${form.phanLoaiTieuDuong eq 'Khác' ? 'selected' : ''}>Khác</option>
                                </select>
                            </div>
                            <div class="form-group full-width">
                                <label>Hướng xử trí</label>
                                <textarea name="huongXuTri" rows="2">${form.huongXuTri}</textarea>
                            </div>
                            <div class="form-group full-width">
                                <label>Khuyến nghị điều trị</label>
                                <textarea name="khuyenNghiDieuTri" rows="2">${form.khuyenNghiDieuTri}</textarea>
                            </div>
                            <div class="form-group">
                                <label>Chế độ ăn</label>
                                <input type="text" name="cheDoAn" value="${form.cheDoAn}" placeholder="VD: Hạn chế tinh bột, ăn nhiều rau xanh">
                            </div>
                            <div class="form-group">
                                <label>Luyện tập</label>
                                <input type="text" name="luyenTap" value="${form.luyenTap}" placeholder="VD: Đi bộ 30 phút/ngày">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- C. Chỉ số sức khỏe -->
                <div class="card">
                    <div class="card-top"><i class="fa-solid fa-heart-pulse"></i> C. Chỉ số sức khỏe</div>
                    <div class="card-body">
                        <div class="form-container">
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
                <div class="card">
                    <div class="card-top"><i class="fa-solid fa-flask"></i> D. Kết quả sinh hóa máu (tùy chọn)</div>
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
                <div class="card">
                    <div class="card-top"><i class="fa-solid fa-vial"></i> E. Xét nghiệm máu tổng quát (tùy chọn)</div>
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

                <!-- F. Đơn thuốc -->
                <div class="card">
                    <div class="card-top" style="justify-content:space-between;">
                        <span><i class="fa-solid fa-pills"></i> F. Đơn thuốc (tùy chọn)</span>
                        <button type="button" class="btn btn-sm btn-add-outline" id="btnAddMed">
                            <i class="fa-solid fa-plus"></i> Thêm thuốc
                        </button>
                    </div>
                    <div class="card-body" id="medicationList">
                        <c:choose>
                            <c:when test="${not empty form.medications}">
                                <c:forEach var="med" items="${form.medications}" varStatus="st">
                                    <div class="med-row" data-med-row>
                                        <div class="med-row-header">
                                            <strong>Thuốc #<span class="med-index">${st.index + 1}</span></strong>
                                            <button type="button" class="btn btn-sm btn-danger-outline btn-remove-med"><i class="fa-solid fa-xmark"></i></button>
                                        </div>
                                        <div class="form-container">
                                            <div class="form-group"><input name="medTenThuoc" placeholder="Tên thuốc *" value="${med.tenThuoc}"></div>
                                            <div class="form-group"><input name="medHoatChat" placeholder="Hoạt chất" value="${med.hoatChat}"></div>
                                            <div class="form-group"><input name="medLieuLuong" placeholder="Liều lượng *" value="${med.lieuLuong}"></div>
                                            <div class="form-group"><input name="medDonVi" placeholder="Đơn vị" value="${med.donVi}"></div>
                                            <div class="form-group"><input name="medDuongDung" placeholder="Đường dùng" value="${med.duongDung}"></div>
                                            <div class="form-group"><input name="medTanSuat" placeholder="Tần suất *" value="${med.tanSuat}"></div>
                                            <div class="form-group"><input type="number" min="0" name="medThoiGianDungNgay" placeholder="Số ngày dùng" value="${med.thoiGianDungNgay}"></div>
                                            <div class="form-group full-width"><input name="medGhiChu" placeholder="Ghi chú" value="${med.ghiChu}"></div>
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
                    <button type="submit" class="btn btn-primary"><i class="fa-solid fa-save"></i> Lưu bệnh án</button>
                </div>
            </form>
        </div>
    </main>
</div>

<script>
(function () {
    const store = document.getElementById('patientStore');
    const patients = Array.from(store.querySelectorAll('.patient-option')).map(function (el) {
        return {
            id: el.dataset.id,
            code: el.dataset.code || '',
            name: el.dataset.name || '',
            gender: el.dataset.gender || '—',
            dob: el.dataset.dob || '—',
            age: el.dataset.age || '—',
            diabetes: el.dataset.diabetes || '—',
            address: el.dataset.address || '—',
            insurance: el.dataset.insurance || '—',
            height: el.dataset.height || ''
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
        d.textContent = text;
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

    document.getElementById('encounterForm').addEventListener('submit', function (e) {
        if (!patientIdInput.value) {
            e.preventDefault();
            alert('Vui lòng chọn bệnh nhân trước khi lưu.');
            searchInput.focus();
            return;
        }
        const trieuChung = document.querySelector('[name="trieuChung"]');
        if (trieuChung && !trieuChung.value.trim()) {
            e.preventDefault();
            alert('Vui lòng nhập triệu chứng.');
            trieuChung.focus();
        }
    });
})();
</script>
</body>
</html>
