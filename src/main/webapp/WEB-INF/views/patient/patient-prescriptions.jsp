<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn thuốc - DiabCare</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
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
        }

        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }
        body { background-color: var(--bg-body); color: var(--text-dark); }

        /* Navigation */
        .top-nav { display: flex; align-items: center; justify-content: space-between; background-color: var(--bg-white); border-bottom: 1px solid var(--border); padding: 0 2rem; height: 64px; position: fixed; top: 0; left: 0; right: 0; z-index: 100; }
        .nav-left { display: flex; align-items: center; gap: 2rem; }
        .logo { font-size: 1.25rem; font-weight: 700; color: var(--primary); }
        .nav-links { display: flex; gap: 1.5rem; }
        .nav-links a { text-decoration: none; color: var(--text-muted); font-weight: 500; font-size: 0.875rem; padding: 1.25rem 0; position: relative; }
        .nav-links a.active { color: var(--primary); }
        .nav-links a.active::after { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 2px; background-color: var(--primary); }
        .nav-right { display: flex; align-items: center; gap: 1.5rem; color: var(--text-muted); }
        .avatar-small { width: 32px; height: 32px; border-radius: 50%; background-color: #cbd5e1; background-image: url('https://ui-avatars.com/api/?name=${patientInfo.hoTen}&background=0D8ABC&color=fff'); background-size: cover; }

        /* Layout */
        .app-container { display: flex; margin-top: 64px; min-height: calc(100vh - 64px); }
        
        /* Sidebar */
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

        /* Content */
        .content { margin-left: 280px; padding: 2rem; flex-grow: 1; width: calc(100% - 280px); }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
        .page-title { font-size: 1.5rem; font-weight: 700; }
        .section-title { font-size: 1.125rem; font-weight: 600; margin-bottom: 1rem; color: var(--primary); display: flex; align-items: center; gap: 0.5rem; }
        
        .card { background-color: var(--bg-white); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; margin-bottom: 1.5rem; }
        
        /* Checklist styles */
        .checklist-item { display: flex; align-items: center; padding: 1rem; border: 1px solid var(--border); border-radius: 8px; margin-bottom: 0.75rem; transition: all 0.2s; }
        .checklist-item.done { background-color: var(--success-light); border-color: var(--success); }
        .check-circle { width: 24px; height: 24px; border-radius: 50%; border: 2px solid var(--text-muted); margin-right: 1rem; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; }
        .check-circle i { color: transparent; font-size: 0.875rem; }
        .checklist-item.done .check-circle { background-color: var(--success); border-color: var(--success); }
        .checklist-item.done .check-circle i { color: white; }
        
        .med-info { flex-grow: 1; }
        .med-name { font-weight: 600; font-size: 1rem; color: var(--text-dark); margin-bottom: 0.25rem; }
        .med-desc { font-size: 0.875rem; color: var(--text-muted); }
        .checklist-item.done .med-name { text-decoration: line-through; color: var(--success); }
        
        .med-time { padding: 0.25rem 0.75rem; border-radius: 16px; background-color: var(--bg-body); font-size: 0.75rem; font-weight: 600; color: var(--text-dark); }
        
        /* Tables */
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid var(--border); }
        th { font-size: 0.75rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; }
        td { font-size: 0.875rem; }
    </style>
</head>
<body>
    <nav class="top-nav">
        <div class="nav-left">
            <div class="logo">HealthAlert</div>
            <div class="nav-links">
                <a href="patient-dashboard">Tổng quan</a>
                <a href="patient-medical-profile">Hồ sơ sức khỏe</a>
                <a href="#">Lịch hẹn</a>
                <a href="#">Báo cáo</a>
            </div>
        </div>
        <div class="nav-right">
            <i class="far fa-bell"></i>
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
                <a href="patient-medical-profile" class="menu-btn"><i class="fas fa-file-medical"></i> Xem bệnh án cá nhân</a>
                <a href="#" class="menu-btn"><i class="far fa-calendar-alt"></i> Xem lịch khám</a>
                <a href="patient-prescriptions" class="menu-btn active"><i class="fas fa-pills"></i> Đơn thuốc</a>
                <a href="#" class="menu-btn"><i class="fas fa-chart-line"></i> Biểu đồ tiến triển</a>
                <a href="#" class="menu-btn"><i class="fas fa-history"></i> Lịch sử cảnh báo</a>
            </nav>
        </aside>

        <!-- Content -->
        <main class="content">
            <div class="page-header">
                <h1 class="page-title">Đơn thuốc & Theo dõi uống thuốc</h1>
            </div>

            <!-- To-do List Hôm nay -->
            <div class="card">
                <h2 class="section-title"><i class="fas fa-check-square"></i> Việc cần làm hôm nay</h2>
                <p style="font-size: 0.875rem; color: var(--text-muted); margin-bottom: 1.5rem;">Đánh dấu vào ô sau khi bạn đã uống thuốc để theo dõi tiến độ.</p>
                
                <div id="checklist-container">
                    <c:choose>
                        <c:when test="${not empty todayChecklist}">
                            <c:forEach var="log" items="${todayChecklist}">
                                <div class="checklist-item ${log.trangThai == 'da_uong' ? 'done' : ''}" data-id="${log.medicationId}">
                                    <div class="check-circle" onclick="toggleMed('${log.medicationId}')">
                                        <i class="fas fa-check"></i>
                                    </div>
                                    <div class="med-info">
                                        <div class="med-name">${log.tenThuoc} - ${log.lieuLuong} ${log.donVi}</div>
                                        <div class="med-desc">${log.tanSuat} ${log.ghiChu != null ? '(' += log.ghiChu += ')' : ''}</div>
                                    </div>
                                    <div class="med-time">
                                        <i class="far fa-clock"></i> ${log.thoiDiemUong != null ? log.thoiDiemUong : 'Trong ngày'}
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="color: var(--text-muted);">Không có danh sách thuốc nào cho hôm nay.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Chi tiết đơn thuốc hiện tại -->
            <div class="card">
                <h2 class="section-title"><i class="fas fa-file-prescription"></i> Chi tiết đơn thuốc hiện tại</h2>
                <c:choose>
                    <c:when test="${not empty latestPrescription}">
                        <div style="margin-bottom: 1rem; font-size: 0.875rem;">
                            <strong>Bác sĩ điều trị:</strong> ${latestPrescription.bacSiName} <br>
                            <strong>Ngày kê đơn:</strong> ${latestPrescription.ngayKeDon} <br>
                            <strong>Chẩn đoán:</strong> ${latestPrescription.chanDoan}
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th>Tên thuốc</th>
                                    <th>Liều lượng</th>
                                    <th>Tần suất</th>
                                    <th>Thời điểm</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="med" items="${latestPrescription.medications}">
                                    <tr>
                                        <td style="font-weight: 600; color: var(--primary);">${med.tenThuoc}</td>
                                        <td>${med.lieuLuong} ${med.donVi}</td>
                                        <td>${med.tanSuat}</td>
                                        <td>${med.thoiDiemUong != null ? med.thoiDiemUong : '--'}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        <p style="color: var(--text-muted);">Chưa có đơn thuốc nào.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>

    <script>
        function toggleMed(medicationId) {
            // Find the item element
            const item = document.querySelector('.checklist-item[data-id="' + medicationId + '"]');
            
            // Toggle visually immediately for better UX
            item.classList.toggle('done');
            
            // Send request to server
            fetch('${pageContext.request.contextPath}/patient-prescriptions/toggle', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'medicationId=' + medicationId
            })
            .then(response => response.json())
            .then(data => {
                if(data.status !== 'success') {
                    // Revert visual if failed
                    item.classList.toggle('done');
                    alert('Có lỗi xảy ra: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                item.classList.toggle('done');
                alert('Không thể kết nối đến máy chủ.');
            });
        }
    </script>
</body>
</html>
