<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết bệnh nhân - HealthAlert</title>
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
            display:flex; align-items:center; gap:14px; height:52px;
            margin-bottom:8px; padding:0 16px; border-radius:12px;
            color:#374151; text-decoration:none; cursor:pointer;
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
        .search-box input{
            border:none; outline:none; width:100%; margin-left:10px; font-size:14px;
        }
        .icon-btn{ font-size:22px; color:#4b5563; cursor:pointer; }
        .avatar{ width:38px; height:38px; border-radius:50%; object-fit:cover; }
        .page-content{ padding:32px; }
        .page-header{ margin-bottom:24px; }
        .page-header h1{
            font-size:32px; font-weight:700; color:#111827; margin-bottom:8px;
        }
        .page-header p{ color:#6b7280; }
        .patient-card{
            background:white; border-radius:20px;
            border:1px solid #e5e7eb; overflow:hidden; margin-top:24px;
        }
        .patient-card:first-of-type{ margin-top:0; }
        .card-header{
            padding:24px 32px; border-bottom:1px solid #e5e7eb;
        }
        .card-header h2{ font-size:22px; color:#111827; }
        .card-header .card-subtitle{
            margin-top:6px; font-size:14px; color:#6b7280; font-weight:500;
        }
        .card-header-sub{
            padding:18px 32px; background:#f9fafb;
        }
        .card-header-sub h3{
            font-size:16px; font-weight:700; color:#374151;
        }
        .form-container{
            padding:32px; display:grid;
            grid-template-columns:1fr 1fr; gap:24px;
        }
        .form-group{ display:flex; flex-direction:column; }
        .form-group label{
            margin-bottom:10px; font-weight:600; color:#374151;
        }
        .form-group input,
        .form-group select,
        .form-group textarea{
            min-height:56px; border:1px solid #d1d5db; border-radius:14px;
            padding:14px 18px; font-size:15px; outline:none;
            background:#f8fafc; color:#111827;
        }
        .form-group textarea{ min-height:96px; resize:vertical; line-height:1.6; }
        .form-group input[readonly],
        .form-group textarea[readonly]{ cursor:default; }
        .full-width{ grid-column:span 2; }
        .hr-empty-state{
            padding:40px 24px; margin:32px;
            text-align:center; color:#6b7280;
            border:1px dashed #d1d5db; border-radius:14px; background:#f9fafb;
        }
        .hr-error-banner{
            background:#fee2e2; border:1px solid #fca5a5; color:#991b1b;
            padding:12px 16px; border-radius:12px; margin:24px 32px 0; font-size:14px;
        }
        @media(max-width:768px){
            .form-container{ grid-template-columns:1fr; }
            .full-width{ grid-column:span 1; }
        }
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
        <div class="page-content">

            <div class="page-header">
                <h1>Chi tiết bệnh nhân</h1>
                <p>${patient.user.hoTen} · ${patient.patientCode}</p>
            </div>

            <c:if test="${param.success eq '1'}">
                <div style="background:#d1fae5;border:1px solid #6ee7b7;color:#065f46;padding:14px 20px;border-radius:12px;margin-bottom:20px;font-weight:500;">
                    <i class="fa-solid fa-circle-check"></i>
                    Đã lưu hồ sơ khám bệnh vào hệ thống.
                </div>
            </c:if>

            <div class="patient-card">
                <div class="card-header">
                    <h2>Thông tin bệnh nhân</h2>
                </div>
                <div class="form-container">
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
                        <input type="text" value="${patient.tuoi}" readonly>
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
                </div>
            </div>

            <div class="patient-card">
                <div class="card-header">
                    <h2><i class="fa-solid fa-file-waveform"></i> Hồ sơ sức khỏe</h2>
                    <p class="card-subtitle">Dữ liệu lưu trong hồ sơ sức khỏe (cập nhật khi tạo lần khám mới)</p>
                </div>

                <c:if test="${param.hrReadOnly eq '1'}">
                    <div class="hr-error-banner">
                        Hồ sơ sức khỏe chỉ đọc. Dữ liệu được tổng hợp tự động vào snapshot.
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty healthRecord}">
                        <div class="hr-empty-state">
                            <i class="fa-regular fa-folder-open" style="font-size:32px;margin-bottom:12px;display:block;color:#cbd5e1;"></i>
                            Chưa có dữ liệu
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:set var="hr" value="${healthRecord}"/>
                        <c:set var="hrEmpty" value="Chưa có dữ liệu"/>
                        <c:set var="hrThoiGianDo" value="${hrEmpty}"/>
                        <c:if test="${hr.thoiGianDo != null}">
                            <c:set var="hrThoiGianDo" value="${hr.thoiGianDo.dayOfMonth}/${hr.thoiGianDo.monthValue}/${hr.thoiGianDo.year} ${hr.thoiGianDo.hour}:${hr.thoiGianDo.minute < 10 ? '0' : ''}${hr.thoiGianDo.minute}"/>
                        </c:if>
                        <c:set var="hrNgayTao" value="${hrEmpty}"/>
                        <c:if test="${hr.ngayTao != null}">
                            <c:set var="hrNgayTao" value="${hr.ngayTao.dayOfMonth}/${hr.ngayTao.monthValue}/${hr.ngayTao.year} ${hr.ngayTao.hour}:${hr.ngayTao.minute < 10 ? '0' : ''}${hr.ngayTao.minute}"/>
                        </c:if>
                        <c:set var="hrThoiDiemDo" value="${hrEmpty}"/>
                        <c:if test="${hr.thoiDiemDoDuong eq 'luc_doi'}"><c:set var="hrThoiDiemDo" value="Lúc đói"/></c:if>
                        <c:if test="${hr.thoiDiemDoDuong eq 'sau_an_1h'}"><c:set var="hrThoiDiemDo" value="Sau ăn 1h"/></c:if>
                        <c:if test="${hr.thoiDiemDoDuong eq 'sau_an_2h'}"><c:set var="hrThoiDiemDo" value="Sau ăn 2h"/></c:if>
                        <c:if test="${hr.thoiDiemDoDuong eq 'truoc_ngu'}"><c:set var="hrThoiDiemDo" value="Trước ngủ"/></c:if>
                        <c:if test="${not empty hr.thoiDiemDoDuong && hrThoiDiemDo eq hrEmpty}"><c:set var="hrThoiDiemDo" value="${hr.thoiDiemDoDuong}"/></c:if>
                        <c:set var="hrHuyetAp" value="${hrEmpty}"/>
                        <c:if test="${hr.huyetApTamThu != null && hr.huyetApTamTruong != null}"><c:set var="hrHuyetAp" value="${hr.huyetApTamThu} / ${hr.huyetApTamTruong}"/></c:if>
                        <c:if test="${hr.huyetApTamThu != null && hr.huyetApTamTruong == null}"><c:set var="hrHuyetAp" value="${hr.huyetApTamThu} / —"/></c:if>
                        <c:if test="${hr.huyetApTamThu == null && hr.huyetApTamTruong != null}"><c:set var="hrHuyetAp" value="— / ${hr.huyetApTamTruong}"/></c:if>
                        <c:set var="hrChestPain" value="${hrEmpty}"/>
                        <c:if test="${hr.chestPain == true}"><c:set var="hrChestPain" value="Có"/></c:if>
                        <c:if test="${hr.chestPain == false}"><c:set var="hrChestPain" value="Không"/></c:if>
                        <c:set var="hrDizziness" value="${hrEmpty}"/>
                        <c:if test="${hr.dizziness == true}"><c:set var="hrDizziness" value="Có"/></c:if>
                        <c:if test="${hr.dizziness == false}"><c:set var="hrDizziness" value="Không"/></c:if>
                        <c:set var="hrFatigue" value="${hrEmpty}"/>
                        <c:if test="${hr.fatigue == true}"><c:set var="hrFatigue" value="Có"/></c:if>
                        <c:if test="${hr.fatigue == false}"><c:set var="hrFatigue" value="Không"/></c:if>

                        <div class="card-header card-header-sub">
                            <h3>Thông tin chung</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group">
                                <label>Người nhập</label>
                                <input type="text" readonly value="${not empty hr.nhapBoi.hoTen ? hr.nhapBoi.hoTen : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Thời gian đo</label>
                                <input type="text" readonly value="${hrThoiGianDo}">
                            </div>
                            <div class="form-group">
                                <label>Ngày tạo</label>
                                <input type="text" readonly value="${hrNgayTao}">
                            </div>
                        </div>

                        <div class="card-header card-header-sub">
                            <h3>Khám nội tiết &amp; điều trị</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group full-width">
                                <label>Triệu chứng</label>
                                <textarea readonly>${not empty hr.trieuChung ? hr.trieuChung : 'Chưa có dữ liệu'}</textarea>
                            </div>
                            <div class="form-group full-width">
                                <label>Tiền sử bệnh</label>
                                <textarea readonly>${not empty hr.tienSuBenh ? hr.tienSuBenh : 'Chưa có dữ liệu'}</textarea>
                            </div>
                            <div class="form-group full-width">
                                <label>Khám lâm sàng</label>
                                <textarea readonly>${not empty hr.khamLamSang ? hr.khamLamSang : 'Chưa có dữ liệu'}</textarea>
                            </div>
                            <div class="form-group">
                                <label>Chẩn đoán chính</label>
                                <input type="text" readonly value="${not empty hr.chanDoanChinh ? hr.chanDoanChinh : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Chẩn đoán phụ</label>
                                <input type="text" readonly value="${not empty hr.chanDoanPhu ? hr.chanDoanPhu : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Phân loại tiểu đường</label>
                                <input type="text" readonly value="${not empty hr.phanLoaiTieuDuong ? hr.phanLoaiTieuDuong : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Hướng xử trí</label>
                                <input type="text" readonly value="${not empty hr.huongXuTri ? hr.huongXuTri : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group full-width">
                                <label>Khuyến nghị điều trị</label>
                                <textarea readonly>${not empty hr.khuyenNghiDieuTri ? hr.khuyenNghiDieuTri : (not empty hr.khuyenNghi ? hr.khuyenNghi : 'Chưa có dữ liệu')}</textarea>
                            </div>
                            <div class="form-group">
                                <label>Chế độ ăn</label>
                                <input type="text" readonly value="${not empty hr.cheDoAn ? hr.cheDoAn : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Luyện tập</label>
                                <input type="text" readonly value="${not empty hr.luyenTap ? hr.luyenTap : 'Chưa có dữ liệu'}">
                            </div>
                        </div>

                        <div class="card-header card-header-sub">
                            <h3>Chỉ số sinh tồn</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group">
                                <label>Chiều cao (cm)</label>
                                <input type="text" readonly value="${hr.chieuCaoCm != null ? hr.chieuCaoCm : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Cân nặng (kg)</label>
                                <input type="text" readonly value="${hr.canNangKg != null ? hr.canNangKg : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>BMI</label>
                                <input type="text" readonly value="${hr.bmi != null ? hr.bmi : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Huyết áp (mmHg)</label>
                                <input type="text" readonly value="${hrHuyetAp}">
                            </div>
                            <div class="form-group">
                                <label>Nhịp tim (bpm)</label>
                                <input type="text" readonly value="${hr.nhipTim != null ? hr.nhipTim : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Nhiệt độ (°C)</label>
                                <input type="text" readonly value="${hr.nhietDoC != null ? hr.nhietDoC : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Nhịp thở</label>
                                <input type="text" readonly value="${hr.nhipTho != null ? hr.nhipTho : 'Chưa có dữ liệu'}">
                            </div>
                        </div>

                        <div class="card-header card-header-sub">
                            <h3>Xét nghiệm &amp; chỉ số máu</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group">
                                <label>Đường huyết (mg/dL)</label>
                                <input type="text" readonly value="${hr.duongHuyetMgdl != null ? hr.duongHuyetMgdl : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Thời điểm đo đường</label>
                                <input type="text" readonly value="${hrThoiDiemDo}">
                            </div>
                            <div class="form-group">
                                <label>HbA1c (%)</label>
                                <input type="text" readonly value="${hr.hba1cPercent != null ? hr.hba1cPercent : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Cholesterol (mmol/L)</label>
                                <input type="text" readonly value="${hr.cholesterolMmol != null ? hr.cholesterolMmol : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Triglyceride (mmol/L)</label>
                                <input type="text" readonly value="${hr.triglycerideMmol != null ? hr.triglycerideMmol : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>HDL (mmol/L)</label>
                                <input type="text" readonly value="${hr.hdlMmol != null ? hr.hdlMmol : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>LDL (mmol/L)</label>
                                <input type="text" readonly value="${hr.ldlMmol != null ? hr.ldlMmol : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>WBC (G/L)</label>
                                <input type="text" readonly value="${hr.wbc != null ? hr.wbc : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>RBC (T/L)</label>
                                <input type="text" readonly value="${hr.rbc != null ? hr.rbc : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>HGB (g/dL)</label>
                                <input type="text" readonly value="${hr.hgb != null ? hr.hgb : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>HCT (%)</label>
                                <input type="text" readonly value="${hr.hct != null ? hr.hct : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>PLT (G/L)</label>
                                <input type="text" readonly value="${hr.plt != null ? hr.plt : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>AST (U/L)</label>
                                <input type="text" readonly value="${hr.ast != null ? hr.ast : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>ALT (U/L)</label>
                                <input type="text" readonly value="${hr.alt != null ? hr.alt : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Ure (mmol/L)</label>
                                <input type="text" readonly value="${hr.ure != null ? hr.ure : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Creatinine (µmol/L)</label>
                                <input type="text" readonly value="${hr.creatinine != null ? hr.creatinine : 'Chưa có dữ liệu'}">
                            </div>
                        </div>

                        <div class="card-header card-header-sub">
                            <h3>Chế độ sinh hoạt</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group">
                                <label>Số bước chân</label>
                                <input type="text" readonly value="${hr.soBuocChan != null ? hr.soBuocChan : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Lượng Carbohydrate (g)</label>
                                <input type="text" readonly value="${hr.carbsG != null ? hr.carbsG : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Số giờ ngủ</label>
                                <input type="text" readonly value="${hr.soGioNgu != null ? hr.soGioNgu : 'Chưa có dữ liệu'}">
                            </div>
                        </div>

                        <div class="card-header card-header-sub">
                            <h3>Điều trị</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group">
                                <label>Liều insulin (UI)</label>
                                <input type="text" readonly value="${hr.lieuLuongInsulinUi != null ? hr.lieuLuongInsulinUi : 'Chưa có dữ liệu'}">
                            </div>
                            <div class="form-group">
                                <label>Loại insulin</label>
                                <input type="text" readonly value="${not empty hr.loaiInsulinTiem ? hr.loaiInsulinTiem : 'Chưa có dữ liệu'}">
                            </div>
                        </div>

                        <div class="card-header card-header-sub">
                            <h3>Triệu chứng</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group">
                                <label>Đau ngực</label>
                                <input type="text" readonly value="${hrChestPain}">
                            </div>
                            <div class="form-group">
                                <label>Chóng mặt</label>
                                <input type="text" readonly value="${hrDizziness}">
                            </div>
                            <div class="form-group">
                                <label>Mệt mỏi</label>
                                <input type="text" readonly value="${hrFatigue}">
                            </div>
                        </div>

                        <div class="card-header card-header-sub">
                            <h3>Ghi chú</h3>
                        </div>
                        <div class="form-container">
                            <div class="form-group full-width">
                                <label>Nội dung</label>
                                <textarea readonly>${not empty hr.ghiChu ? hr.ghiChu : 'Chưa có dữ liệu'}</textarea>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>
    </main>
</div>
</body>
</html>
