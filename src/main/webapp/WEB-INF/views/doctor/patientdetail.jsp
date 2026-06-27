<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HealthAlert Dashboard</title>
    <style>
        *{
            margin:0;
            padding:0;
            box-sizing:border-box;
            font-family:Inter, sans-serif;
        }

        body{
            background:#f5f6fa;
        }

        .layout{
            display:flex;
            height:calc(100vh - 80px);
        }

        .topbar{
            height:80px;
            background:white;

            display:flex;
            align-items:center;

            padding:0 48px;

            border-bottom:1px solid #e5e7eb;
        }

        .sidebar{
            width:240px;
            background:#fff;
            border-right:1px solid #e5e7eb;

            display:flex;
            flex-direction:column;
        }

        .doctor-profile{
            padding:28px 20px;
            display:flex;
            align-items:center;
            gap:12px;
        }

        .doctor-profile img{
            width:42px;
            height:42px;
            border-radius:10px;
            object-fit:cover;
        }

        .doctor-profile h4{
            font-size:16px;
            color:#1554c7;
        }

        .doctor-profile p{
            font-size:12px;
            color:#666;
        }

        .menu{
            padding:0 16px;
        }

        .menu-item{
            display:flex;
            align-items:center;
            gap:14px;

            height:52px;

            margin-bottom:8px;
            padding:0 16px;

            border-radius:12px;

            color:#374151;
            text-decoration:none;

            cursor:pointer;
        }

        .menu-item i{
            font-size:18px;
        }

        .menu-item.active{
            background:#1557d5;
            color:white;
            font-weight:600;
        }

        .sidebar-bottom{
            margin-top:auto;
            padding:20px 16px;
        }

        .new-record{
            width:100%;
            height:48px;

            border:none;
            border-radius:10px;

            background:#0d4bb5;
            color:white;

            font-size:15px;
            font-weight:600;

            cursor:pointer;
        }

        .new-record i{
            margin-right:8px;
        }

        .bottom-link{
            display:flex;
            align-items:center;
            gap:12px;

            padding:14px 12px;

            text-decoration:none;
            color:#374151;

            cursor:pointer;
        }

        /* ==========================
           MAIN
        ========================== */

        .main-content{
            flex:1;
        }

        /* ==========================
           TOPBAR
        ========================== */


        .logo{
            font-size:20px;
            font-weight:700;
            color:#0d4bb5;
        }

        .top-nav{
            display:flex;
            gap:36px;

            margin-left:40px; /* chỉnh số này */
        }

        .top-actions{
            display:flex;
            align-items:center;
            gap:22px;

            margin-left:auto;
        }

        .top-nav a{
            color:#555;
            cursor:pointer;
            font-size:16px;
            text-decoration:none;
        }

        .top-nav .active{
            color:#1557d5;
            font-weight:600;
            position:relative;
        }

        .top-nav .active::after{
            content:"";
            position:absolute;
            left:0;
            bottom:-28px;

            width:100%;
            height:3px;

            background:#1557d5;
        }

        .search-box{
            width:290px;
            height:42px;

            display:flex;
            align-items:center;

            padding:0 16px;

            border:1px solid #d1d5db;
            border-radius:10px;

            background:#fff;
        }

        .search-box i{
            color:#777;
        }

        .search-box input{
            border:none;
            outline:none;
            width:100%;
            margin-left:10px;
            font-size:14px;
        }

        .icon-btn{
            font-size:22px;
            color:#4b5563;
            cursor:pointer;
        }

        .avatar{
            width:38px;
            height:38px;
            border-radius:50%;
            object-fit:cover;
        }
        .page-content{
            padding:32px;
        }

        .page-header{
            margin-bottom:24px;
        }

        .page-header h1{
            font-size:32px;
            font-weight:700;
            color:#111827;
            margin-bottom:8px;
        }

        .page-header p{
            color:#6b7280;
        }

        .patient-card{
            background:white;
            border-radius:20px;
            border:1px solid #e5e7eb;
            overflow:hidden;
        }

        .card-header{
            padding:24px 32px;
            border-bottom:1px solid #e5e7eb;
        }

        .card-header h2{
            font-size:22px;
            color:#111827;
        }

        .form-container{
            padding:32px;
            display:grid;
            grid-template-columns:1fr 1fr;
            gap:24px;
        }

        .form-group{
            display:flex;
            flex-direction:column;
        }

        .form-group label{
            margin-bottom:10px;
            font-weight:600;
            color:#374151;
        }

        .form-group input,
        .form-group select{
            height:56px;
            border:1px solid #d1d5db;
            border-radius:14px;
            padding:0 18px;
            font-size:15px;
            outline:none;
        }

        .form-group input:focus,
        .form-group select:focus{
            border-color:#1557d5;
        }

        .full-width{
            grid-column:span 2;
        }

        .button-group{
            grid-column:span 2;
            display:flex;
            justify-content:flex-end;
            gap:16px;
            margin-top:8px;
        }

        .cancel-btn{
            height:52px;
            padding:0 24px;
            border:1px solid #d1d5db;
            background:white;
            border-radius:12px;
            cursor:pointer;
            font-weight:600;
        }

        .submit-btn{
            height:52px;
            padding:0 24px;
            border:none;
            background:#1557d5;
            color:white;
            border-radius:12px;
            cursor:pointer;
            font-weight:600;
        }

        .submit-btn:hover{
            background:#0f4cc7;
        }

        .encounter-card{
            background:white;
            border-radius:20px;
            border:1px solid #e5e7eb;
            overflow:hidden;
            margin-top:24px;
        }

        .health-records-section{
            margin-top:24px;
        }

        .health-records-section > .section-title{
            display:flex;
            align-items:center;
            gap:12px;
            margin-bottom:20px;
        }

        .health-records-section > .section-title h2{
            font-size:22px;
            font-weight:700;
            color:#111827;
        }

        .health-records-section > .section-title span{
            font-size:14px;
            color:#6b7280;
            font-weight:500;
        }

        .hr-record-card{
            background:#fff;
            border:1px solid #e5e7eb;
            border-radius:18px;
            padding:24px 28px;
            margin-bottom:20px;
            box-shadow:0 1px 3px rgba(0,0,0,.04);
        }

        .hr-record-head{
            display:flex;
            justify-content:space-between;
            align-items:center;
            gap:16px;
            padding-bottom:18px;
            margin-bottom:20px;
            border-bottom:1px solid #f1f5f9;
            flex-wrap:wrap;
        }

        .hr-record-head h3{
            font-size:17px;
            font-weight:700;
            color:#1557d5;
        }

        .hr-record-head .meta{
            font-size:13px;
            color:#64748b;
        }

        .hr-group{
            margin-bottom:22px;
        }

        .hr-group:last-child{
            margin-bottom:0;
        }

        .hr-group h4{
            font-size:13px;
            font-weight:700;
            text-transform:uppercase;
            letter-spacing:.4px;
            color:#64748b;
            margin-bottom:14px;
            padding-bottom:8px;
            border-bottom:1px dashed #e5e7eb;
        }

        .hr-grid{
            display:grid;
            grid-template-columns:repeat(3, 1fr);
            gap:16px 24px;
        }

        .hr-field{
            display:flex;
            flex-direction:column;
            gap:6px;
        }

        .hr-field .label{
            font-size:12px;
            color:#94a3b8;
            font-weight:600;
        }

        .hr-field .value{
            font-size:15px;
            color:#1e293b;
            font-weight:600;
            word-break:break-word;
        }

        .hr-field .value.empty{
            color:#9ca3af;
            font-weight:500;
            font-style:italic;
        }

        .hr-empty-state{
            padding:40px 24px;
            text-align:center;
            color:#6b7280;
            background:#fff;
            border:1px dashed #d1d5db;
            border-radius:18px;
        }

        .hr-section-actions{
            display:flex;
            align-items:center;
            gap:12px;
            margin-left:auto;
        }

        .hr-edit-btn,
        .hr-cancel-btn,
        .hr-save-btn{
            border:none;
            padding:10px 18px;
            border-radius:10px;
            font-size:14px;
            font-weight:600;
            cursor:pointer;
            text-decoration:none;
            display:inline-flex;
            align-items:center;
            gap:8px;
        }

        .hr-edit-btn{ background:#eff6ff; color:#2563eb; }
        .hr-save-btn{ background:#2563eb; color:#fff; }
        .hr-cancel-btn{ background:#fff; color:#374151; border:1px solid #d1d5db; }

        .hr-edit-input,
        .hr-edit-select,
        .hr-edit-textarea{
            width:100%;
            border:1px solid #d1d5db;
            border-radius:10px;
            padding:10px 12px;
            font-size:14px;
            outline:none;
            background:#fff;
        }

        .hr-edit-input:focus,
        .hr-edit-select:focus,
        .hr-edit-textarea:focus{ border-color:#2563eb; }

        .hr-form-actions{
            display:flex;
            justify-content:flex-end;
            gap:12px;
            margin-top:8px;
            padding-top:20px;
            border-top:1px solid #f1f5f9;
        }

        .hr-success-banner{
            background:#d1fae5;
            border:1px solid #6ee7b7;
            color:#065f46;
            padding:12px 16px;
            border-radius:12px;
            margin-bottom:16px;
            font-size:14px;
        }

        .hr-error-banner{
            background:#fee2e2;
            border:1px solid #fca5a5;
            color:#991b1b;
            padding:12px 16px;
            border-radius:12px;
            margin-bottom:16px;
            font-size:14px;
        }

        .section-title{
            display:flex;
            align-items:center;
            flex-wrap:wrap;
            gap:12px;
        }

        @media(max-width:992px){
            .hr-grid{ grid-template-columns:repeat(2, 1fr); }
        }

        @media(max-width:640px){
            .hr-grid{ grid-template-columns:1fr; }
        }

        @media(max-width:768px){

            .form-container{
                grid-template-columns:1fr;
            }

            .full-width,
            .button-group{
                grid-column:span 1;
            }
        }
    </style>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<!-- TOPBAR -->
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
                    placeholder="Tìm kiếm hồ sơ sức khỏe..."
            >
        </div>

        <i class="fa-regular fa-bell icon-btn"></i>
        <i class="fa-solid fa-gear icon-btn"></i>

        <img
                class="avatar"
                src="https://i.pravatar.cc/40"
                alt=""
        >

    </div>

</header>

<div class="layout">

    <!-- SIDEBAR -->
    <aside class="sidebar">

        <div class="doctor-profile">
            <img src="https://i.pravatar.cc/60" alt="">
            <div>
                <h4>BS. Smith</h4>
            </div>
        </div>

        <nav class="menu">

            <a class="menu-item active">
                <i class="fa-solid fa-table-cells"></i>
                <span>Tổng quan</span>
            </a>

            <a href="patientmanagement.html" class="menu-item">
                <i class="fa-solid fa-users"></i>
                <span>Danh sách bệnh nhân</span>
            </a>

            <a class="menu-item">
                <i class="fa-regular fa-bell"></i>
                <span>Cảnh báo khẩn cấp</span>
            </a>

            <a href="medicalrecordmanagement.html" class="menu-item">
                <i class="fa-regular fa-clipboard"></i>
                <span>Hồ sơ sức khỏe</span>
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

    <!-- MAIN -->
    <main class="main-content">

        <div class="page-content">

            <div class="page-header">
                <h1>Chi tiết bệnh nhân</h1>
                <p>${patient.user.hoTen} · ${patient.patientCode}</p>
            </div>

            <div class="patient-card">

                <div class="card-header">
                    <h2>Thông tin bệnh nhân</h2>
                </div>

                <form class="form-container">

                    <div class="form-group">
                        <label>Mã bệnh nhân</label>
                        <input type="text" value="${patient.patientCode}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Họ và tên</label>
                        <input type="text" value="${patient.user.hoTen}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Ngày sinh</label>
                        <input type="date" value="${patient.ngaySinh}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Tuổi</label>
                        <input type="number" value="${patient.tuoi}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Giới tính</label>
                        <input type="text" value="${patient.gioiTinh}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="text" value="${patient.user.soDienThoai}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" value="${patient.user.email}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Địa chỉ</label>
                        <input type="text" value="${patient.diaChi}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Loại tiểu đường</label>
                        <input type="text" value="${patient.loaiTieuDuong}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Tiền sử bệnh</label>
                        <input type="text" value="${patient.tienSuBenh}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Nhóm máu</label>
                        <input type="text" value="${patient.nhomMau}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Bảo hiểm y tế</label>
                        <input type="text" value="${patient.baoHiemYTe}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Dị ứng</label>
                        <input type="text" value="${patient.diUng}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Ngày chẩn đoán tiểu đường</label>
                        <input type="date" value="${patient.ngayChanDoanTieuDuong}" readonly>
                    </div>

                    <div class="form-group">
                        <label>Cập nhật lần cuối</label>
                        <input type="text" value="${patient.ngayCapNhat}" readonly>
                    </div>

                </form>

            </div>

            <div class="health-records-section">
                <div class="section-title">
                    <h2><i class="fa-solid fa-file-waveform"></i> Hồ sơ sức khỏe</h2>
                    <span>Hồ sơ mới nhất</span>
                    <c:if test="${not empty latestHealthRecord && !editMode}">
                        <div class="hr-section-actions">
                            <a href="${pageContext.request.contextPath}/doctor/patient-detail?id=${patient.id}&edit=1"
                               class="hr-edit-btn">
                                <i class="fa-solid fa-pen"></i> Chỉnh sửa
                            </a>
                        </div>
                    </c:if>
                </div>

                <c:if test="${param.hrUpdated eq '1'}">
                    <div class="hr-success-banner">
                        <i class="fa-solid fa-circle-check"></i> Đã cập nhật hồ sơ sức khỏe thành công.
                    </div>
                </c:if>
                <c:if test="${param.hrError eq '1'}">
                    <div class="hr-error-banner">
                        Không thể cập nhật hồ sơ. Vui lòng kiểm tra lại dữ liệu.
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty latestHealthRecord}">
                        <div class="hr-empty-state">
                            <i class="fa-regular fa-folder-open" style="font-size:32px;margin-bottom:12px;display:block;color:#cbd5e1;"></i>
                            Chưa có hồ sơ sức khỏe nào cho bệnh nhân này.
                        </div>
                    </c:when>
                    <c:when test="${editMode}">
                        <c:set var="hr" value="${latestHealthRecord}" />
                        <c:set var="f" value="${hrForm}" />
                        <article class="hr-record-card">
                            <form method="post" action="${pageContext.request.contextPath}/doctor/health-record/update">
                                <input type="hidden" name="recordId" value="${f.recordId}">
                                <input type="hidden" name="patientId" value="${f.patientId}">

                                <div class="hr-group">
                                    <h4>Thông tin chung</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field">
                                            <span class="label">Người nhập</span>
                                            <span class="value">${not empty hr.nhapBoi.hoTen ? hr.nhapBoi.hoTen : '—'}</span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Thời gian đo</span>
                                            <input class="hr-edit-input" type="datetime-local" name="thoiGianDo" value="${f.thoiGianDoLocal}">
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Ngày tạo</span>
                                            <span class="value empty" style="font-style:italic;color:#9ca3af;font-weight:500;">
                                                <c:choose>
                                                    <c:when test="${hr.ngayTao != null}">
                                                        ${hr.ngayTao.dayOfMonth}/${hr.ngayTao.monthValue}/${hr.ngayTao.year}
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Chỉ số sức khỏe</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field"><span class="label">Đường huyết (mg/dL)</span><input class="hr-edit-input" type="number" step="0.1" name="duongHuyetMgdl" value="${f.duongHuyetMgdl}"></div>
                                        <div class="hr-field">
                                            <span class="label">Thời điểm đo đường</span>
                                            <select class="hr-edit-select" name="thoiDiemDoDuong">
                                                <option value="">-- Chọn --</option>
                                                <option value="luc_doi" ${f.thoiDiemDoDuong eq 'luc_doi' ? 'selected' : ''}>Lúc đói</option>
                                                <option value="sau_an_1h" ${f.thoiDiemDoDuong eq 'sau_an_1h' ? 'selected' : ''}>Sau ăn 1h</option>
                                                <option value="sau_an_2h" ${f.thoiDiemDoDuong eq 'sau_an_2h' ? 'selected' : ''}>Sau ăn 2h</option>
                                                <option value="truoc_ngu" ${f.thoiDiemDoDuong eq 'truoc_ngu' ? 'selected' : ''}>Trước ngủ</option>
                                            </select>
                                        </div>
                                        <div class="hr-field"><span class="label">Huyết áp tâm thu</span><input class="hr-edit-input" type="number" name="huyetApTamThu" value="${f.huyetApTamThu}"></div>
                                        <div class="hr-field"><span class="label">Huyết áp tâm trương</span><input class="hr-edit-input" type="number" name="huyetApTamTruong" value="${f.huyetApTamTruong}"></div>
                                        <div class="hr-field"><span class="label">Nhịp tim (bpm)</span><input class="hr-edit-input" type="number" name="nhipTim" value="${f.nhipTim}"></div>
                                        <div class="hr-field"><span class="label">Nhiệt độ (°C)</span><input class="hr-edit-input" type="number" step="0.1" name="nhietDoC" value="${f.nhietDoC}"></div>
                                        <div class="hr-field"><span class="label">Nhịp thở</span><input class="hr-edit-input" type="number" name="nhipTho" value="${f.nhipTho}"></div>
                                        <div class="hr-field"><span class="label">Cân nặng (kg)</span><input class="hr-edit-input" type="number" step="0.1" name="canNangKg" value="${f.canNangKg}"></div>
                                        <div class="hr-field"><span class="label">BMI</span><input class="hr-edit-input" type="number" step="0.01" name="bmi" value="${f.bmi}"></div>
                                        <div class="hr-field"><span class="label">HbA1c (%)</span><input class="hr-edit-input" type="number" step="0.1" name="hba1cPercent" value="${f.hba1cPercent}"></div>
                                        <div class="hr-field"><span class="label">Cholesterol (mmol/L)</span><input class="hr-edit-input" type="number" step="0.01" name="cholesterolMmol" value="${f.cholesterolMmol}"></div>
                                        <div class="hr-field"><span class="label">Triglyceride (mmol/L)</span><input class="hr-edit-input" type="number" step="0.01" name="triglycerideMmol" value="${f.triglycerideMmol}"></div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Chế độ sinh hoạt</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field"><span class="label">Số bước chân</span><input class="hr-edit-input" type="number" name="soBuocChan" value="${f.soBuocChan}"></div>
                                        <div class="hr-field"><span class="label">Carbohydrate (g)</span><input class="hr-edit-input" type="number" step="0.1" name="carbsG" value="${f.carbsG}"></div>
                                        <div class="hr-field"><span class="label">Số giờ ngủ</span><input class="hr-edit-input" type="number" step="0.1" name="soGioNgu" value="${f.soGioNgu}"></div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Điều trị</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field"><span class="label">Liều insulin (UI)</span><input class="hr-edit-input" type="number" name="lieuLuongInsulinUi" value="${f.lieuLuongInsulinUi}"></div>
                                        <div class="hr-field"><span class="label">Loại insulin</span><input class="hr-edit-input" type="text" name="loaiInsulinTiem" value="${f.loaiInsulinTiem}"></div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Triệu chứng</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field">
                                            <span class="label">Đau ngực</span>
                                            <select class="hr-edit-select" name="chestPain">
                                                <option value="" ${f.chestPain == null ? 'selected' : ''}>Chưa có dữ liệu</option>
                                                <option value="true" ${f.chestPain == true ? 'selected' : ''}>Có</option>
                                                <option value="false" ${f.chestPain == false ? 'selected' : ''}>Không</option>
                                            </select>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Chóng mặt</span>
                                            <select class="hr-edit-select" name="dizziness">
                                                <option value="" ${f.dizziness == null ? 'selected' : ''}>Chưa có dữ liệu</option>
                                                <option value="true" ${f.dizziness == true ? 'selected' : ''}>Có</option>
                                                <option value="false" ${f.dizziness == false ? 'selected' : ''}>Không</option>
                                            </select>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Mệt mỏi</span>
                                            <select class="hr-edit-select" name="fatigue">
                                                <option value="" ${f.fatigue == null ? 'selected' : ''}>Chưa có dữ liệu</option>
                                                <option value="true" ${f.fatigue == true ? 'selected' : ''}>Có</option>
                                                <option value="false" ${f.fatigue == false ? 'selected' : ''}>Không</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Ghi chú</h4>
                                    <textarea class="hr-edit-textarea" name="ghiChu" rows="3">${f.ghiChu}</textarea>
                                </div>

                                <div class="hr-form-actions">
                                    <a href="${pageContext.request.contextPath}/doctor/patient-detail?id=${patient.id}" class="hr-cancel-btn">Hủy</a>
                                    <button type="submit" class="hr-save-btn"><i class="fa-solid fa-save"></i> Lưu thay đổi</button>
                                </div>
                            </form>
                        </article>
                    </c:when>
                    <c:otherwise>
                        <c:set var="hr" value="${latestHealthRecord}" />
                        <article class="hr-record-card">
                                <div class="hr-group">
                                    <h4>Thông tin chung</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field">
                                            <span class="label">Người nhập</span>
                                            <span class="value ${empty hr.nhapBoi.hoTen ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${not empty hr.nhapBoi.hoTen}">${hr.nhapBoi.hoTen}</c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Thời gian đo</span>
                                            <span class="value ${hr.thoiGianDo == null ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${hr.thoiGianDo != null}">
                                                        ${hr.thoiGianDo.dayOfMonth}/${hr.thoiGianDo.monthValue}/${hr.thoiGianDo.year}
                                                        ${hr.thoiGianDo.hour}:${hr.thoiGianDo.minute < 10 ? '0' : ''}${hr.thoiGianDo.minute}
                                                    </c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Ngày tạo</span>
                                            <span class="value ${hr.ngayTao == null ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${hr.ngayTao != null}">
                                                        ${hr.ngayTao.dayOfMonth}/${hr.ngayTao.monthValue}/${hr.ngayTao.year}
                                                        ${hr.ngayTao.hour}:${hr.ngayTao.minute < 10 ? '0' : ''}${hr.ngayTao.minute}
                                                    </c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Chỉ số sức khỏe</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field">
                                            <span class="label">Đường huyết (mg/dL)</span>
                                            <span class="value ${hr.duongHuyetMgdl == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.duongHuyetMgdl != null}">${hr.duongHuyetMgdl}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Thời điểm đo đường</span>
                                            <span class="value ${empty hr.thoiDiemDoDuong ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${hr.thoiDiemDoDuong eq 'luc_doi'}">Lúc đói</c:when>
                                                    <c:when test="${hr.thoiDiemDoDuong eq 'sau_an_1h'}">Sau ăn 1h</c:when>
                                                    <c:when test="${hr.thoiDiemDoDuong eq 'sau_an_2h'}">Sau ăn 2h</c:when>
                                                    <c:when test="${hr.thoiDiemDoDuong eq 'truoc_ngu'}">Trước ngủ</c:when>
                                                    <c:when test="${not empty hr.thoiDiemDoDuong}">${hr.thoiDiemDoDuong}</c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Huyết áp (mmHg)</span>
                                            <span class="value ${hr.huyetApTamThu == null && hr.huyetApTamTruong == null ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${hr.huyetApTamThu != null && hr.huyetApTamTruong != null}">${hr.huyetApTamThu} / ${hr.huyetApTamTruong}</c:when>
                                                    <c:when test="${hr.huyetApTamThu != null}">${hr.huyetApTamThu} / —</c:when>
                                                    <c:when test="${hr.huyetApTamTruong != null}">— / ${hr.huyetApTamTruong}</c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Nhịp tim (bpm)</span>
                                            <span class="value ${hr.nhipTim == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.nhipTim != null}">${hr.nhipTim}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Nhiệt độ (°C)</span>
                                            <span class="value ${hr.nhietDoC == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.nhietDoC != null}">${hr.nhietDoC}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Nhịp thở</span>
                                            <span class="value ${hr.nhipTho == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.nhipTho != null}">${hr.nhipTho}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Cân nặng (kg)</span>
                                            <span class="value ${hr.canNangKg == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.canNangKg != null}">${hr.canNangKg}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">BMI</span>
                                            <span class="value ${hr.bmi == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.bmi != null}">${hr.bmi}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">HbA1c (%)</span>
                                            <span class="value ${hr.hba1cPercent == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.hba1cPercent != null}">${hr.hba1cPercent}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Cholesterol (mmol/L)</span>
                                            <span class="value ${hr.cholesterolMmol == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.cholesterolMmol != null}">${hr.cholesterolMmol}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Triglyceride (mmol/L)</span>
                                            <span class="value ${hr.triglycerideMmol == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.triglycerideMmol != null}">${hr.triglycerideMmol}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Chế độ sinh hoạt</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field">
                                            <span class="label">Số bước chân</span>
                                            <span class="value ${hr.soBuocChan == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.soBuocChan != null}">${hr.soBuocChan}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Lượng Carbohydrate (g)</span>
                                            <span class="value ${hr.carbsG == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.carbsG != null}">${hr.carbsG}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Số giờ ngủ</span>
                                            <span class="value ${hr.soGioNgu == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.soGioNgu != null}">${hr.soGioNgu}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Điều trị</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field">
                                            <span class="label">Liều insulin (UI)</span>
                                            <span class="value ${hr.lieuLuongInsulinUi == null ? 'empty' : ''}">
                                                <c:choose><c:when test="${hr.lieuLuongInsulinUi != null}">${hr.lieuLuongInsulinUi}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Loại insulin</span>
                                            <span class="value ${empty hr.loaiInsulinTiem ? 'empty' : ''}">
                                                <c:choose><c:when test="${not empty hr.loaiInsulinTiem}">${hr.loaiInsulinTiem}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Triệu chứng</h4>
                                    <div class="hr-grid">
                                        <div class="hr-field">
                                            <span class="label">Đau ngực</span>
                                            <span class="value ${hr.chestPain == null ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${hr.chestPain == true}">Có</c:when>
                                                    <c:when test="${hr.chestPain == false}">Không</c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Chóng mặt</span>
                                            <span class="value ${hr.dizziness == null ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${hr.dizziness == true}">Có</c:when>
                                                    <c:when test="${hr.dizziness == false}">Không</c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <div class="hr-field">
                                            <span class="label">Mệt mỏi</span>
                                            <span class="value ${hr.fatigue == null ? 'empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${hr.fatigue == true}">Có</c:when>
                                                    <c:when test="${hr.fatigue == false}">Không</c:when>
                                                    <c:otherwise>Chưa có dữ liệu</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="hr-group">
                                    <h4>Ghi chú</h4>
                                    <div class="hr-field">
                                        <span class="value ${empty hr.ghiChu ? 'empty' : ''}" style="font-weight:500;line-height:1.6;">
                                            <c:choose><c:when test="${not empty hr.ghiChu}">${hr.ghiChu}</c:when><c:otherwise>Chưa có dữ liệu</c:otherwise></c:choose>
                                        </span>
                                    </div>
                                </div>
                        </article>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>

    </main>

</div>

</body>
</html>