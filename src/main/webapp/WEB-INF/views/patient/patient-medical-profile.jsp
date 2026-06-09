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
        .avatar-small { width: 32px; height: 32px; border-radius: 50%; background-color: #cbd5e1; background-image: url('https://ui-avatars.com/api/?name=${patientInfo.hoTen}&background=0D8ABC&color=fff'); background-size: cover; }

        /* Main Layout */
        .app-container { display: flex; margin-top: 64px; min-height: calc(100vh - 64px); }

        /* Left Sidebar */
        .sidebar { width: 280px; background-color: var(--bg-white); border-right: 1px solid var(--border); padding: 2rem 1.5rem; display: flex; flex-direction: column; position: fixed; top: 64px; bottom: 0; overflow-y: auto; }
        .profile-card { display: flex; flex-direction: column; align-items: center; text-align: center; margin-bottom: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border); }
        .profile-avatar { width: 80px; height: 80px; border-radius: 50%; margin-bottom: 1rem; background-color: #cbd5e1; background-image: url('https://ui-avatars.com/api/?name=${patientInfo.hoTen}&background=0D8ABC&color=fff'); background-size: cover; }
        .profile-name { font-weight: 600; font-size: 1.125rem; color: var(--text-dark); }
        .profile-role { font-size: 0.875rem; color: var(--text-muted); }
        .sidebar-menu { display: flex; flex-direction: column; gap: 0.5rem; flex-grow: 1; }
        .menu-btn { display: flex; align-items: center; gap: 1rem; padding: 0.75rem 1rem; border-radius: 8px; color: var(--text-muted); text-decoration: none; font-weight: 500; font-size: 0.875rem; transition: all 0.2s; border: none; background: none; width: 100%; cursor: pointer; text-align: left; }
        .menu-btn i { width: 20px; text-align: center; font-size: 1rem; }
        .menu-btn:hover { background-color: var(--bg-body); }
        .menu-btn.active { background-color: var(--primary); color: var(--bg-white); }
        .sidebar-bottom { margin-top: auto; display: flex; flex-direction: column; gap: 1rem; }

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
    </style>
</head>
<body>

    <!-- Top Navigation -->
    <nav class="top-nav">
        <div class="nav-left">
            <div class="logo">HealthAlert</div>
            <div class="nav-links">
                <a href="patient-dashboard">Tổng quan</a>
                <a href="patient-medical-profile" class="active">Hồ sơ sức khỏe</a>
                <a href="#">Lịch hẹn</a>
                <a href="#">Báo cáo</a>
            </div>
        </div>
        <div class="nav-right">
            <jsp:include page="notifications.jsp" />
            <i class="fas fa-cog"></i>
            <div class="avatar-small"></div>
        </div>
    </nav>

    <div class="app-container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="profile-card">
                <div class="profile-avatar"></div>
                <div class="profile-name">${patientInfo.hoTen != null ? patientInfo.hoTen : 'Bệnh nhân'}</div>
                <div class="profile-role">Bệnh nhân - ĐTĐ ${patientInfo.loaiTieuDuong != null ? patientInfo.loaiTieuDuong : 'Type 2'}</div>
            </div>

            <nav class="sidebar-menu">
                <a href="patient-dashboard" class="menu-btn"><i class="fas fa-chart-pie"></i> Tổng quan</a>
                <a href="patient-medical-profile" class="menu-btn active"><i class="fas fa-file-medical"></i> Xem bệnh án cá nhân</a>
                <a href="#" class="menu-btn"><i class="far fa-calendar-alt"></i> Xem lịch khám</a>
                <a href="patient-prescriptions" class="menu-btn"><i class="fas fa-pills"></i> Đơn thuốc</a>
                <a href="#" class="menu-btn"><i class="fas fa-chart-line"></i> Biểu đồ tiến triển</a>
                <a href="#" class="menu-btn"><i class="fas fa-history"></i> Lịch sử cảnh báo</a>
            </nav>

            <div class="sidebar-bottom">
                <a href="#" class="menu-btn"><i class="far fa-question-circle"></i> Hỗ trợ</a>
                <a href="#" class="menu-btn"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
            </div>
        </aside>

        <!-- Main Content -->
        <main class="content">
            <div class="page-header">
                <h1 class="page-title">Hồ sơ Bệnh án Điện tử (EHR)</h1>
                <button class="btn-print" onclick="window.print()"><i class="fas fa-print"></i> In Hồ Sơ</button>
            </div>

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

            <!-- 5. Nhật ký y khoa & Tiến triển (Progress Notes) -->
            <div class="card">
                <h2 class="section-title"><i class="fas fa-clipboard-list"></i> 5. Nhật ký y khoa & Tiến triển (Progress Notes)</h2>
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
    <jsp:include page="chatbot.jsp" />
</body>
</html>
