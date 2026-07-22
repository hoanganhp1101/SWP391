<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ sức khỏe - DiabCare</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <style>
        :root {
            --primary: #0a4aa8;
            --primary-light: #e6effc;
            --text-dark: #1e293b;
            --text-muted: #64748b;
            --bg-body: #f8fafc;
            --bg-white: #ffffff;
            --border: #e2e8f0;
            --danger: #ef4444;
            --danger-light: #fee2e2;
            --success: #10b981;
            --success-light: #d1fae5;
            --warning: #f59e0b;
            --warning-light: #fef3c7;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', sans-serif;
        }

        body {
            background-color: var(--bg-body);
            color: var(--text-dark);
        }

        /* Top Navigation */
        .top-nav {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background-color: var(--bg-white);
            border-bottom: 1px solid var(--border);
            padding: 0 2rem;
            height: 64px;
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 100;
        }

        .nav-left { display: flex; align-items: center; gap: 2rem; }
        .logo { font-size: 1.25rem; font-weight: 700; color: var(--primary); }
        .nav-links { display: flex; gap: 1.5rem; }
        .nav-links a { text-decoration: none; color: var(--text-muted); font-weight: 500; font-size: 0.875rem; padding: 1.25rem 0; position: relative; }
        .nav-links a.active { color: var(--primary); }
        .nav-links a.active::after { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 2px; background-color: var(--primary); }
        .nav-right { display: flex; align-items: center; gap: 1.5rem; color: var(--text-muted); }
        .avatar-small { width: 32px; height: 32px; border-radius: 50%; background-color: #cbd5e1; background-image: url('${not empty patientInfo.anhDaiDien ? patientInfo.anhDaiDien : "https://ui-avatars.com/api/?name=" += patientInfo.hoTen += "&background=0D8ABC&color=fff"}'); background-size: cover; background-position: center; }

        /* Main Layout */
        .app-container { display: flex; margin-top: 64px; min-height: calc(100vh - 64px); }

        /* Left Sidebar */
        .sidebar { width: 280px; background-color: var(--bg-white); border-right: 1px solid var(--border); padding: 2rem 1.5rem; display: flex; flex-direction: column; position: fixed; top: 64px; bottom: 0; overflow-y: auto; }
        .profile-card { display: flex; flex-direction: column; align-items: center; text-align: center; margin-bottom: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border); }
        .profile-avatar { width: 80px; height: 80px; border-radius: 50%; margin-bottom: 1rem; background-color: #cbd5e1; background-image: url('${not empty patientInfo.anhDaiDien ? patientInfo.anhDaiDien : "https://ui-avatars.com/api/?name=" += patientInfo.hoTen += "&background=0D8ABC&color=fff"}'); background-size: cover; background-position: center; border: 2px solid transparent; transition: border-color 0.2s ease, transform 0.2s ease; }
        .profile-avatar:hover { border-color: var(--primary); transform: translateY(-1px); }
        .avatar-link { text-decoration: none; color: inherit; display: inline-block; }
        .avatar-hint { margin-top: 0.25rem; font-size: 0.75rem; color: var(--text-muted); }
        .profile-name { font-weight: 600; font-size: 1.125rem; color: var(--text-dark); }
        .profile-role { font-size: 0.875rem; color: var(--text-muted); }
        .sidebar-menu { display: flex; flex-direction: column; gap: 0.5rem; flex-grow: 1; }
        .menu-btn { display: flex; align-items: center; gap: 1rem; padding: 0.75rem 1rem; border-radius: 8px; color: var(--text-muted); text-decoration: none; font-weight: 500; font-size: 0.875rem; transition: all 0.2s; border: none; background: none; width: 100%; cursor: pointer; text-align: left; }
        .menu-btn i { width: 20px; text-align: center; font-size: 1rem; }
        .menu-btn:hover { background-color: var(--bg-body); }
        .menu-btn.active { background-color: var(--primary); color: var(--bg-white); }
        .sidebar-bottom { margin-top: auto; display: flex; flex-direction: column; gap: 1rem; }
        .profile-help-text { margin-top: 0.25rem; font-size: 0.75rem; color: var(--text-muted); }
        .btn-new { background-color: var(--primary); color: white; border: none; border-radius: 8px; padding: 0.75rem; font-weight: 600; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 0.5rem; text-decoration: none; transition: background-color 0.2s; }
        .btn-new:hover { background-color: #083c8a; color: white; }

        /* Content */
        .content { margin-left: 280px; padding: 2rem; flex-grow: 1; width: calc(100% - 280px); }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
        .page-title { font-size: 1.5rem; font-weight: 700; }
        
        .section-title { font-size: 1.125rem; font-weight: 600; margin-bottom: 1rem; color: var(--primary); display: flex; align-items: center; gap: 0.5rem; }
        .section-title i { font-size: 1.25rem; }

        .card { background-color: var(--bg-white); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; margin-bottom: 1.5rem; }
        
        /* Grid Layouts */
        .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
        .grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
        .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem; }

        /* Info Items */
        .info-group { margin-bottom: 1rem; }
        .info-label { font-size: 0.75rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.25rem; }
        .info-value { font-size: 0.95rem; font-weight: 500; color: var(--text-dark); }
        
        /* Badges */
        .badge { padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; display: inline-block; }
        .badge.success { background-color: var(--success-light); color: var(--success); }
        .badge.warning { background-color: var(--warning-light); color: var(--warning); }
        .badge.danger { background-color: var(--danger-light); color: var(--danger); }
        .badge.info { background-color: var(--primary-light); color: var(--primary); }

        /* Tables */
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid var(--border); }
        th { font-size: 0.75rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; }
        td { font-size: 0.875rem; }
        
        /* Alerts List */
        .alert-item { display: flex; gap: 1rem; margin-bottom: 1rem; padding-bottom: 1rem; border-bottom: 1px solid var(--border); }
        .alert-item:last-child { margin-bottom: 0; padding-bottom: 0; border-bottom: none; }
        .alert-icon { font-size: 1.25rem; }
        .alert-icon.danger { color: var(--danger); }
        .alert-icon.warning { color: var(--warning); }
        .alert-content h4 { font-size: 0.875rem; font-weight: 600; margin-bottom: 0.25rem; }
        .alert-content p { font-size: 0.875rem; color: var(--text-muted); margin-bottom: 0.25rem; }
        .alert-time { font-size: 0.75rem; color: var(--text-muted); }

        .btn-print { background-color: var(--primary-light); color: var(--primary); border: 1px solid var(--primary); border-radius: 8px; padding: 0.5rem 1rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
        .btn-print:hover { background-color: var(--primary); color: white; }

        /* Modal Styles */
        .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; opacity: 0; pointer-events: none; transition: opacity 0.3s; }
        .modal-overlay.active { opacity: 1; pointer-events: auto; }
        .modal { background: var(--bg-white); border-radius: 12px; width: 100%; max-width: 600px; max-height: 90vh; overflow-y: auto; padding: 2rem; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); transform: translateY(-20px); transition: transform 0.3s; }
        .modal-overlay.active .modal { transform: translateY(0); }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
        .modal-title { font-size: 1.25rem; font-weight: 600; }
        .close-btn { background: none; border: none; font-size: 1.25rem; cursor: pointer; color: var(--text-muted); }
        .form-group { margin-bottom: 1rem; }
        .form-group label { display: block; font-size: 0.875rem; font-weight: 500; margin-bottom: 0.5rem; color: var(--text-dark); }
        .form-control { width: 100%; padding: 0.75rem 1rem; border: 1px solid var(--border); border-radius: 8px; font-size: 0.875rem; outline: none; }
        .form-control:focus { border-color: var(--primary); }
        .form-row { display: flex; gap: 1rem; }
        .form-row .form-group { flex: 1; }
        .modal-footer { margin-top: 2rem; display: flex; justify-content: flex-end; gap: 1rem; }
        .btn { padding: 0.75rem 1.5rem; border-radius: 8px; font-weight: 600; cursor: pointer; border: none; font-size: 0.875rem; }
        .btn-cancel { background: var(--bg-body); color: var(--text-dark); }
        .btn-save { background: var(--primary); color: white; }
        
        /* Spinner for AI extraction */
        .spinner-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(255,255,255,0.8); z-index: 10; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: 12px; opacity: 0; pointer-events: none; transition: opacity 0.3s; }
        .spinner-overlay.active { opacity: 1; pointer-events: auto; }
        .spinner { width: 40px; height: 40px; border: 4px solid var(--primary-light); border-top-color: var(--primary); border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 1rem; }
        @keyframes spin { to { transform: rotate(360deg); } }

        /* --- Print Styles (Mock Medical Record) --- */
        @media print {
            body { background-color: white !important; color: black !important; font-family: 'Times New Roman', serif; }
            .top-nav, .sidebar, .page-header, .modal-overlay, .spinner-overlay, #chatbot, .chatbot-container { display: none !important; }
            .app-container { margin: 0; min-height: auto; }
            .content { margin: 0; padding: 0; width: 100%; border: none; }
            .card { border: none; box-shadow: none; margin-bottom: 1.5rem; padding: 0; background: transparent; break-inside: avoid; }
            .section-title { font-size: 1.25rem; font-weight: bold; color: black; border-bottom: 2px solid black; padding-bottom: 0.5rem; margin-bottom: 1rem; text-transform: uppercase; }
            .section-title i { display: none; }
            .info-label { color: black; font-size: 0.85rem; font-weight: bold; }
            .info-value, .info-value span { color: black !important; font-size: 1rem !important; font-weight: normal !important; }
            .badge { border: 1px solid black; background: transparent !important; color: black !important; padding: 2px 5px; font-weight: normal; }
            table th, table td { border: 1px solid black !important; padding: 0.5rem !important; }
            
            /* Custom Header for Print */
            .content::before {
                content: "BỘ Y TẾ\000A BỆNH VIỆN ĐA KHOA DIABCARE\000A \000A BỆNH ÁN ĐIỆN TỬ (MẪU)";
                display: block;
                text-align: center;
                white-space: pre;
                font-weight: bold;
                font-size: 1.3rem;
                margin-bottom: 2rem;
                line-height: 1.5;
            }
            .grid-2, .grid-3, .grid-4 { display: flex; flex-wrap: wrap; gap: 1rem; }
            .info-group { flex: 1 1 45%; border-bottom: 1px dotted #ccc; padding-bottom: 0.25rem; }
            .info-group .info-label { display: inline-block; width: 160px; text-transform: none; }
            .info-group .info-value { display: inline; }
            
            /* Hide non-printable messages */
            .alert-item { border-left: 3px solid black; padding-left: 1rem; border-bottom: none; margin-bottom: 0.5rem; }
            .alert-icon { display: none; }
            a.btn-print, i.fa-external-link-alt { display: none !important; }
            .info-value i { display: none !important; }
        }
    </style>
</head>
<body>

    <!-- Top Navigation -->
    <nav class="top-nav">
        <div class="nav-left">
            <div class="logo">DiabCare</div>
            <div class="nav-links">
                <a href="patient-dashboard">Tổng quan</a>
                <a href="patient-medical-profile" class="active">Hồ sơ sức khỏe</a>
                <a href="patient-appointments">Lịch hẹn</a>
                <a href="#">Báo cáo</a>
            </div>
        </div>
        <div class="nav-right">
            <jsp:include page="notifications.jsp" />
            <a class="avatar-link" href="#" title="Chỉnh sửa hồ sơ" data-open-profile-modal>
                <div class="avatar-small"></div>
            </a>
        </div>
    </nav>

    <div class="app-container">
        <jsp:include page="layout/sidebar.jsp">
            <jsp:param name="activeMenu" value="profile"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="content">
            <div class="page-header">
                <h1 class="page-title">Hồ sơ Bệnh án Điện tử (EHR)</h1>
                <div>
                    <button class="btn-print" onclick="openUpdateModal()" style="margin-right: 10px;"><i class="fas fa-edit"></i> Cập nhật Bệnh án</button>
                    <button class="btn-print" onclick="window.print()"><i class="fas fa-print"></i> In Hồ Sơ</button>
                </div>
            </div>

            <c:if test="${param.success == 'true'}">
                <div style="background-color: var(--success-light); color: var(--success); padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem; font-weight: 500;">
                    Cập nhật bệnh án thành công!
                </div>
            </c:if>
            <c:if test="${param.error == 'true'}">
                <div style="background-color: var(--danger-light); color: var(--danger); padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem; font-weight: 500;">
                    Đã có lỗi xảy ra. Vui lòng thử lại.
                </div>
            </c:if>

            <!-- 1. Thông tin Hành chính & Tiền sử -->
            <div class="card">
                <h2 class="section-title"><i class="fas fa-id-card"></i> 1. Thông tin Hành chính & Tiền sử (Demographics & History)</h2>
                
                <div class="grid-3" style="margin-bottom: 1.5rem; border-bottom: 1px solid var(--border); padding-bottom: 1.5rem;">
                    <div class="info-group">
                        <div class="info-label">Mã bệnh nhân (ID)</div>
                        <div class="info-value">${patientInfo.id.substring(0,8)}...</div>
                    </div>
                    <div class="info-group">
                        <div class="info-label">Họ và tên</div>
                        <div class="info-value">${patientInfo.hoTen}</div>
                    </div>
                    <div class="info-group">
                        <div class="info-label">Ngày sinh / Tuổi</div>
                        <div class="info-value">${patientInfo.ngaySinh}</div>
                    </div>
                    <div class="info-group">
                        <div class="info-label">Giới tính</div>
                        <div class="info-value">${patientInfo.gioiTinh == 'nu' ? 'Nữ' : (patientInfo.gioiTinh == 'nam' ? 'Nam' : 'Khác')}</div>
                    </div>
                    <div class="info-group">
                        <div class="info-label">Địa chỉ</div>
                        <div class="info-value">${patientInfo.diaChi != null ? patientInfo.diaChi : 'Chưa cập nhật'}</div>
                    </div>
                    <div class="info-group">
                        <div class="info-label">Bảo hiểm y tế</div>
                        <div class="info-value">${patientInfo.baoHiemYTe != null ? patientInfo.baoHiemYTe : 'Chưa cập nhật'}</div>
                    </div>
                </div>

                <div class="grid-2">
                    <div>
                        <div class="info-group">
                            <div class="info-label">Chẩn đoán chính</div>
                            <div class="info-value" style="color: var(--danger); font-weight: 600;">
                                Tiểu đường ${patientInfo.loaiTieuDuong} 
                                <span class="badge danger" style="margin-left: 0.5rem;">Cấp tính</span>
                            </div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Năm phát hiện bệnh</div>
                            <div class="info-value">${patientInfo.ngayChanDoanTieuDuong != null ? patientInfo.ngayChanDoanTieuDuong : 'Chưa rõ'}</div>
                        </div>
                    </div>
                    <div>
                        <div class="info-group">
                            <div class="info-label">Tiền sử bệnh lý</div>
                            <div class="info-value">${patientInfo.tienSuBenh != null ? patientInfo.tienSuBenh : 'Không ghi nhận'}</div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Tiền sử gia đình</div>
                            <div class="info-value">${patientInfo.tienSuGiaDinh != null ? patientInfo.tienSuGiaDinh : 'Không ghi nhận'}</div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Dị ứng thuốc / Thức ăn</div>
                            <div class="info-value">${patientInfo.diUng != null ? patientInfo.diUng : 'Không có'}</div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Nhóm máu</div>
                            <div class="info-value">${patientInfo.nhomMau != null ? patientInfo.nhomMau : 'Chưa rõ'}</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Vitals and Labs Grid -->
            <div class="grid-2">
                <!-- 2. Chỉ số sinh tồn (Vitals) -->
                <div class="card">
                    <h2 class="section-title"><i class="fas fa-heartbeat"></i> 2. Chỉ số sinh tồn (Vitals)</h2>
                    <p style="font-size: 0.875rem; color: var(--text-muted); margin-bottom: 1rem;">Cập nhật lần cuối: ${latestRecord.thoiGianDo}</p>
                    
                    <div class="grid-2">
                        <div class="info-group">
                            <div class="info-label">Đường huyết (Glucose)</div>
                            <div class="info-value">
                                <span style="font-size: 1.5rem; font-weight: 700; color: var(--primary);">${latestRecord.duongHuyetMgdl != null ? latestRecord.duongHuyetMgdl : '--'}</span> mg/dL
                            </div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Huyết áp</div>
                            <div class="info-value">
                                <span style="font-size: 1.5rem; font-weight: 700; color: var(--danger);">${latestRecord.huyetApTamThu != null ? latestRecord.huyetApTamThu : '--'}/${latestRecord.huyetApTamTruong != null ? latestRecord.huyetApTamTruong : '--'}</span> mmHg
                            </div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Nhịp tim</div>
                            <div class="info-value">
                                <span style="font-size: 1.5rem; font-weight: 700;">${latestRecord.nhipTim != null ? latestRecord.nhipTim : '--'}</span> BPM
                            </div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Cân nặng / BMI</div>
                            <div class="info-value">
                                <span style="font-size: 1.5rem; font-weight: 700;">${latestRecord.canNangKg != null ? latestRecord.canNangKg : '--'}</span> kg / 
                                <span style="font-size: 1.5rem; font-weight: 700; color: var(--warning);">${latestRecord.bmi != null ? latestRecord.bmi : '--'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 3. Kết quả Cận lâm sàng (Lab Results) -->
                <div class="card">
                    <h2 class="section-title"><i class="fas fa-flask"></i> 3. Cận lâm sàng (Lab Results)</h2>
                    <p style="font-size: 0.875rem; color: var(--text-muted); margin-bottom: 1rem;">Kết quả xét nghiệm máu gần nhất</p>
                    
                    <div class="info-group">
                        <div class="info-label">HbA1c (Trung bình đường huyết 3 tháng)</div>
                        <div class="info-value">
                            <span style="font-size: 1.5rem; font-weight: 700; color: var(--danger);">${latestRecord.hba1cPercent != null ? latestRecord.hba1cPercent : '--'}</span> %
                            <span class="badge danger" style="margin-left: 0.5rem;">Cần kiểm soát</span>
                        </div>
                    </div>
                    
                    <div class="grid-2">
                        <div class="info-group">
                            <div class="info-label">Cholesterol</div>
                            <div class="info-value">
                                <span style="font-size: 1.5rem; font-weight: 700;">${latestRecord.cholesterolMmol != null ? latestRecord.cholesterolMmol : '--'}</span> mmol/L
                            </div>
                        </div>
                        <div class="info-group">
                            <div class="info-label">Triglyceride</div>
                            <div class="info-value">
                                <span style="font-size: 1.5rem; font-weight: 700;">${latestRecord.triglycerideMmol != null ? latestRecord.triglycerideMmol : '--'}</span> mmol/L
                            </div>
                        </div>
                    </div>
                    
                    <div class="info-group" style="margin-top: 0.5rem; padding-top: 1rem; border-top: 1px dashed var(--border);">
                        <div class="info-label">Tầm soát biến chứng (Mắt, Thần kinh, Thận)</div>
                        <div class="info-value" style="font-size: 0.875rem; color: var(--primary); cursor: pointer;">
                            <i class="fas fa-external-link-alt"></i> Xem kết quả chẩn đoán hình ảnh (AI)
                        </div>
                    </div>
                </div>
            </div>

            <!-- 4. Kế hoạch điều trị & Đơn thuốc (Treatment Plan) -->
            <div class="card">
                <h2 class="section-title"><i class="fas fa-notes-medical"></i> 4. Kế hoạch điều trị (Treatment Plan)</h2>
                <c:choose>
                    <c:when test="${not empty latestPrescription}">
                        <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
                            <div>
                                <span class="info-label">Bác sĩ điều trị:</span> <span class="info-value">${latestPrescription.bacSiName}</span>
                            </div>
                            <div>
                                <span class="info-label">Ngày kê đơn:</span> <span class="info-value">${latestPrescription.ngayKeDon}</span>
                            </div>
                        </div>
                        
                        <div class="info-group">
                            <div class="info-label">Hướng điều trị</div>
                            <div class="info-value">${latestPrescription.huongDieuTri != null ? latestPrescription.huongDieuTri : 'Đang cập nhật'}</div>
                        </div>
                        
                        <div class="grid-2" style="margin-bottom: 1.5rem;">
                            <div class="info-group">
                                <div class="info-label"><i class="fas fa-utensils"></i> Chế độ dinh dưỡng</div>
                                <div class="info-value" style="font-size: 0.875rem;">${latestPrescription.cheDoAn != null ? latestPrescription.cheDoAn : 'Ăn kiêng tiểu đường, giảm tinh bột.'}</div>
                            </div>
                            <div class="info-group">
                                <div class="info-label"><i class="fas fa-running"></i> Luyện tập</div>
                                <div class="info-value" style="font-size: 0.875rem;">${latestPrescription.luyenTap != null ? latestPrescription.luyenTap : 'Tập thể dục nhẹ nhàng 30p mỗi ngày.'}</div>
                            </div>
                        </div>

                        <div class="info-label" style="margin-bottom: 0.5rem;">ĐƠN THUỐC (PRESCRIPTIONS)</div>
                        <table>
                            <thead>
                                <tr>
                                    <th>Tên thuốc</th>
                                    <th>Hoạt chất</th>
                                    <th>Liều lượng</th>
                                    <th>Tần suất</th>
                                    <th>Ghi chú</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty latestPrescription.medications}">
                                        <c:forEach var="med" items="${latestPrescription.medications}">
                                            <tr>
                                                <td style="font-weight: 600; color: var(--primary);">${med.tenThuoc}</td>
                                                <td>${med.hoatChat != null ? med.hoatChat : '--'}</td>
                                                <td>${med.lieuLuong} ${med.donVi}</td>
                                                <td>${med.tanSuat}</td>
                                                <td>${med.ghiChu != null ? med.ghiChu : ''}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr><td colspan="5" style="text-align: center;">Không có dữ liệu đơn thuốc.</td></tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        <p style="color: var(--text-muted); font-style: italic;">Chưa có kế hoạch điều trị hoặc đơn thuốc nào được ghi nhận.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- 5. Tài liệu y khoa (Medical Documents) -->
            <div class="card">
                <h2 class="section-title"><i class="fas fa-file-pdf"></i> 5. Tài liệu y khoa đính kèm</h2>
                <c:choose>
                    <c:when test="${not empty medicalDocuments}">
                        <div class="grid-2">
                            <c:forEach var="doc" items="${medicalDocuments}">
                                <div style="display: flex; align-items: center; gap: 1rem; padding: 1rem; border: 1px solid var(--border); border-radius: 8px;">
                                    <i class="fas fa-file-pdf" style="font-size: 2rem; color: var(--danger);"></i>
                                    <div style="flex-grow: 1;">
                                        <div style="font-weight: 600; font-size: 0.875rem;">${doc.loaiTaiLieu}</div>
                                        <div style="font-size: 0.75rem; color: var(--text-muted);">${doc.ngayThucHien}</div>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/${doc.fileUrl}" target="_blank" class="btn-print" style="text-decoration: none; padding: 0.25rem 0.5rem; font-size: 0.75rem;">Xem</a>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p style="color: var(--text-muted); font-style: italic;">Chưa có tài liệu y khoa nào được tải lên.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- 6. Nhật ký y khoa & Tiến triển (Progress Notes) -->
            <div class="card">
                <h2 class="section-title"><i class="fas fa-clipboard-list"></i> 6. Nhật ký y khoa & Tiến triển (Progress Notes)</h2>
                <div class="info-label" style="margin-bottom: 1rem;">CÁC CẢNH BÁO / LƯU Ý LÂM SÀNG GẦN ĐÂY</div>
                
                <c:choose>
                    <c:when test="${not empty alerts}">
                        <c:forEach var="alert" items="${alerts}">
                            <div class="alert-item">
                                <div class="alert-icon ${alert.mucDo == 'nguy_hiem' || alert.mucDo == 'cao' ? 'danger' : 'warning'}">
                                    <i class="fas ${alert.mucDo == 'nguy_hiem' || alert.mucDo == 'cao' ? 'fa-exclamation-triangle' : 'fa-exclamation-circle'}"></i>
                                </div>
                                <div class="alert-content">
                                    <h4>${alert.tieuDe} <span class="badge ${alert.mucDo == 'nguy_hiem' || alert.mucDo == 'cao' ? 'danger' : 'warning'}" style="margin-left: 0.5rem; font-size: 0.65rem;">${alert.mucDo == 'nguy_hiem' ? 'NGUY HIỂM' : 'LƯU Ý'}</span></h4>
                                    <p>${alert.noiDung}</p>
                                    <div class="alert-time"><i class="far fa-clock"></i> ${alert.thoiGianTao}</div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p style="color: var(--text-muted); font-style: italic;">Bệnh nhân hiện đang ổn định, không có cảnh báo hay lưu ý đặc biệt nào.</p>
                    </c:otherwise>
                </c:choose>
            </div>

        </main>
    </div>

    <!-- Update Medical Profile Modal -->
    <div class="modal-overlay" id="updateProfileModal">
        <div class="modal" style="position: relative;">
            <!-- Loading Spinner -->
            <div class="spinner-overlay" id="aiLoadingSpinner">
                <div class="spinner"></div>
                <div style="font-weight: 600; color: var(--primary);">AI đang đọc bệnh án...</div>
            </div>

            <div class="modal-header">
                <h2 class="modal-title">Cập nhật Bệnh án</h2>
                <button class="close-btn" onclick="closeUpdateModal()"><i class="fas fa-times"></i></button>
            </div>
            <form action="${pageContext.request.contextPath}/patient-medical-profile" method="POST" enctype="multipart/form-data">
                <h3 style="font-size: 1rem; margin: 0 0 1rem; color: var(--primary);">Tải lên tài liệu y khoa mới (PDF)</h3>
                <div class="form-group" style="padding: 1rem; background-color: var(--primary-light); border-radius: 8px; margin-bottom: 1.5rem; border: 1px dashed var(--primary);">
                    <label style="color: var(--primary);"><i class="fas fa-magic"></i> AI Tự động trích xuất thông tin từ Bệnh án (.pdf)</label>
                    <input type="file" name="pdfFile" id="pdfFile" class="form-control" accept="application/pdf" style="padding: 0.5rem 1rem; background: white;" onchange="handlePDFUpload(this)">
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Giới tính</label>
                        <select name="gioiTinh" class="form-control">
                            <option value="nam" ${patientInfo.gioiTinh == 'nam' ? 'selected' : ''}>Nam</option>
                            <option value="nu" ${patientInfo.gioiTinh == 'nu' ? 'selected' : ''}>Nữ</option>
                            <option value="khac" ${patientInfo.gioiTinh == 'khac' ? 'selected' : ''}>Khác</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Chiều cao (cm)</label>
                        <input type="number" step="0.1" name="chieuCaoCm" class="form-control" value="${patientInfo.chieuCaoCm}">
                    </div>
                    <div class="form-group">
                        <label>Cân nặng (kg)</label>
                        <input type="number" step="0.1" name="canNangKg" id="canNangKg" class="form-control" value="${latestRecord.canNangKg}">
                    </div>
                </div>
                
                <h3 style="font-size: 1rem; margin: 1.5rem 0 1rem; color: var(--primary);">Các chỉ số sức khỏe (Vitals)</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label>Huyết áp tâm thu</label>
                        <input type="number" name="huyetApTamThu" id="huyetApTamThu" class="form-control" placeholder="VD: 120" value="${latestRecord.huyetApTamThu}">
                    </div>
                    <div class="form-group">
                        <label>Huyết áp tâm trương</label>
                        <input type="number" name="huyetApTamTruong" id="huyetApTamTruong" class="form-control" placeholder="VD: 80" value="${latestRecord.huyetApTamTruong}">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Nhịp tim (BPM)</label>
                        <input type="number" name="nhipTim" id="nhipTim" class="form-control" placeholder="VD: 75" value="${latestRecord.nhipTim}">
                    </div>
                    <div class="form-group">
                        <label>Đường huyết (mg/dL)</label>
                        <input type="number" step="0.1" name="duongHuyetMgdl" id="duongHuyetMgdl" class="form-control" placeholder="VD: 90.0" value="${latestRecord.duongHuyetMgdl}">
                    </div>
                </div>

                <h3 style="font-size: 1rem; margin: 1.5rem 0 1rem; color: var(--primary);">Chỉ số sinh hóa (Lab Results)</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label>HbA1c (%)</label>
                        <input type="number" step="0.1" name="hba1c" id="hba1c" class="form-control" placeholder="VD: 6.5" value="${latestRecord.hba1cPercent}">
                    </div>
                    <div class="form-group">
                        <label>Cholesterol (mmol/L)</label>
                        <input type="number" step="0.1" name="cholesterol" id="cholesterol" class="form-control" placeholder="VD: 5.2" value="${latestRecord.cholesterolMmol}">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Triglyceride (mmol/L)</label>
                        <input type="number" step="0.1" name="triglyceride" id="triglyceride" class="form-control" placeholder="VD: 1.7" value="${latestRecord.triglycerideMmol}">
                    </div>
                    <div class="form-group">
                    </div>
                </div>

                <h3 style="font-size: 1rem; margin: 1.5rem 0 1rem; color: var(--primary);">Thông tin chung</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label>Nhóm máu</label>
                        <input type="text" name="nhomMau" id="nhomMau" class="form-control" value="${patientInfo.nhomMau}">
                    </div>
                    <div class="form-group">
                        <label>Ngày chẩn đoán tiểu đường</label>
                        <input type="date" name="ngayChanDoanTieuDuong" id="ngayChanDoanTieuDuong" class="form-control" value="${patientInfo.ngayChanDoanTieuDuong}">
                    </div>
                </div>
                <div class="form-group">
                    <label>Địa chỉ</label>
                    <input type="text" name="diaChi" class="form-control" value="${patientInfo.diaChi}">
                </div>
                <div class="form-group">
                    <label>Bảo hiểm y tế</label>
                    <input type="text" name="baoHiemYTe" class="form-control" value="${patientInfo.baoHiemYTe}">
                </div>
                <div class="form-group">
                    <label>Tiền sử bệnh</label>
                    <textarea name="tienSuBenh" id="tienSuBenh" class="form-control" rows="2">${patientInfo.tienSuBenh}</textarea>
                </div>
                <div class="form-group">
                    <label>Tiền sử gia đình</label>
                    <textarea name="tienSuGiaDinh" class="form-control" rows="2">${patientInfo.tienSuGiaDinh}</textarea>
                </div>
                <div class="form-group">
                    <label>Dị ứng</label>
                    <input type="text" name="diUng" class="form-control" value="${patientInfo.diUng}">
                </div>
                


                <div class="modal-footer">
                    <button type="button" class="btn btn-cancel" onclick="closeUpdateModal()">Hủy</button>
                    <button type="submit" class="btn btn-save">Lưu Cập Nhật</button>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="profile-modal.jsp">
        <jsp:param name="profileReturnUrl" value="patient-medical-profile" />
    </jsp:include>
    <jsp:include page="chatbot.jsp" />

    <script>
        function openUpdateModal() {
            document.getElementById('updateProfileModal').classList.add('active');
        }
        function closeUpdateModal() {
            document.getElementById('updateProfileModal').classList.remove('active');
        }

        async function handlePDFUpload(inputElement) {
            const file = inputElement.files[0];
            if (!file) return;

            // Hiển thị vòng xoay loading
            document.getElementById('aiLoadingSpinner').classList.add('active');

            const formData = new FormData();
            formData.append('pdfFile', file);

            try {
                const response = await fetch('${pageContext.request.contextPath}/api/extract-pdf', {
                    method: 'POST',
                    body: formData
                });
                
                const data = await response.json();
                
                if (data.error) {
                    alert('Lỗi khi đọc bệnh án: ' + data.error);
                } else {
                    // Autofill các trường
                    const parseNum = (val) => {
                        if (val === null || val === undefined) return null;
                        let str = String(val).replace(',', '.').replace(/[^0-9.-]/g, '');
                        let num = parseFloat(str);
                        return isNaN(num) ? null : num;
                    };

                    if (data.canNangKg) document.getElementById('canNangKg').value = parseNum(data.canNangKg);
                    if (data.chieuCaoCm) document.getElementsByName('chieuCaoCm')[0].value = parseNum(data.chieuCaoCm);
                    if (data.nhomMau) document.getElementById('nhomMau').value = data.nhomMau;
                    if (data.ngayChanDoanTieuDuong) document.getElementById('ngayChanDoanTieuDuong').value = data.ngayChanDoanTieuDuong;
                    if (data.huyetApTamThu) document.getElementById('huyetApTamThu').value = parseNum(data.huyetApTamThu);
                    if (data.huyetApTamTruong) document.getElementById('huyetApTamTruong').value = parseNum(data.huyetApTamTruong);
                    if (data.nhipTim) document.getElementById('nhipTim').value = parseNum(data.nhipTim);
                    if (data.duongHuyetMgdl) document.getElementById('duongHuyetMgdl').value = parseNum(data.duongHuyetMgdl);
                    
                    if (data.hba1c !== undefined && data.hba1c !== null) {
                        let val = parseNum(data.hba1c);
                        if (val !== null) document.getElementById('hba1c').value = val;
                    }
                    if (data.cholesterol !== undefined && data.cholesterol !== null) {
                        let val = parseNum(data.cholesterol);
                        if (val !== null) document.getElementById('cholesterol').value = val;
                    }
                    if (data.triglyceride !== undefined && data.triglyceride !== null) {
                        let val = parseNum(data.triglyceride);
                        if (val !== null) document.getElementById('triglyceride').value = val;
                    }
                    
                    if (data.ghiChu) {
                        let currentHistory = document.getElementById('tienSuBenh').value;
                        document.getElementById('tienSuBenh').value = currentHistory ? currentHistory + '\n- ' + data.ghiChu : '- ' + data.ghiChu;
                    }
                    
                    alert('AI đã phân tích và điền các chỉ số vào form. Hãy kiểm tra lại trước khi Lưu!');
                }
            } catch (error) {
                console.error('Error extracting PDF:', error);
                alert('Không thể kết nối đến máy chủ AI.');
            } finally {
                // Tắt vòng xoay loading
                document.getElementById('aiLoadingSpinner').classList.remove('active');
            }
        }
    </script>
</body>
</html>
