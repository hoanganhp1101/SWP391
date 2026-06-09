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

        /* Filter & Progress View */
        .filter-group { display: flex; align-items: center; gap: 0.5rem; }
        .filter-btn { padding: 0.5rem 1rem; border: 1px solid var(--border); background: var(--bg-white); color: var(--text-muted); border-radius: 20px; font-size: 0.875rem; cursor: pointer; transition: all 0.2s; font-weight: 500; text-decoration: none; display: inline-block; }
        .filter-btn.active { background: var(--primary); color: white; border-color: var(--primary); }
        .date-picker { padding: 0.4rem 0.5rem; border: 1px solid var(--border); border-radius: 8px; color: var(--text-muted); font-size: 0.875rem; outline: none; font-family: inherit; }
        .card-header-flex { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; flex-wrap: wrap; gap: 1rem; }
        .progress-view { display: none; padding: 2rem 0; text-align: center; animation: fadeIn 0.3s; }
        .progress-view.active { display: block; }
        .checklist-view { display: block; animation: fadeIn 0.3s; }
        .checklist-view.hidden { display: none; }
        .progress-circle { width: 120px; height: 120px; border-radius: 50%; border: 8px solid var(--success-light); border-top-color: var(--success); margin: 0 auto 1rem; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: 700; color: var(--success); }
        @keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }

        /* AI Reminder */
        .ai-reminder-box { background: linear-gradient(135deg, #f0f7ff, #e6f0fa); border: 1px solid #cce0ff; border-radius: 12px; padding: 1rem 1.5rem; margin-bottom: 1.5rem; display: flex; gap: 1rem; align-items: flex-start; }
        .ai-icon { width: 40px; height: 40px; background: var(--primary); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; flex-shrink: 0; font-size: 1.25rem; }
        .ai-content { flex-grow: 1; }
        .ai-content h4 { color: var(--primary); font-size: 0.875rem; font-weight: 700; margin-bottom: 0.35rem; display: flex; align-items: center; gap: 0.5rem; }
        .ai-text { font-size: 0.875rem; color: var(--text-dark); line-height: 1.5; }
        .shimmer { display: inline-block; animation: shimmer 1.5s infinite linear; background: linear-gradient(to right, #eff1f3 4%, #e2e8f0 25%, #eff1f3 36%); background-size: 1000px 100%; border-radius: 4px; }
        @keyframes shimmer { 0% { background-position: -1000px 0; } 100% { background-position: 1000px 0; } }

        /* Notifications */
        .notification-container { position: relative; cursor: pointer; display: flex; align-items: center; }
        .notif-badge { position: absolute; top: -5px; right: -8px; background: var(--danger, #ef4444); color: white; font-size: 0.65rem; font-weight: bold; border-radius: 50%; min-width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; border: 2px solid var(--bg-white, #ffffff); }
        .notification-dropdown { position: absolute; top: 150%; right: -10px; width: 340px; background: var(--bg-white, #ffffff); border: 1px solid var(--border, #e2e8f0); border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); display: none; flex-direction: column; z-index: 1000; overflow: hidden; animation: slideDown 0.2s ease-out; cursor: default; }
        .notification-dropdown.show { display: flex; }
        .notif-header { padding: 1rem 1.25rem; font-weight: 600; border-bottom: 1px solid var(--border, #e2e8f0); font-size: 0.875rem; background: var(--bg-body, #f8fafc); color: var(--text-dark, #1e293b); }
        .notif-body { max-height: 400px; overflow-y: auto; }
        .notif-item { display: flex; gap: 1rem; padding: 1rem 1.25rem; border-bottom: 1px solid var(--border, #e2e8f0); transition: background 0.2s, opacity 0.2s; align-items: flex-start; cursor: pointer; }
        .notif-item:hover { background: var(--bg-body, #f8fafc); }
        .notif-item.read { background: var(--bg-body, #f8fafc); opacity: 0.6; }
        .notif-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1rem; flex-shrink: 0; }
        .notif-content { flex-grow: 1; }
        .notif-title { font-size: 0.875rem; font-weight: 600; color: var(--text-dark, #1e293b); margin-bottom: 0.25rem; }
        .notif-message { font-size: 0.8125rem; color: var(--text-muted, #64748b); line-height: 1.4; margin-bottom: 0.25rem; }
        .notif-time { font-size: 0.75rem; color: #94a3b8; }
        .notif-empty { padding: 2rem; text-align: center; color: var(--text-muted, #64748b); font-size: 0.875rem; }
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
                <jsp:include page="notifications.jsp" />
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

            <!-- Nhật ký uống thuốc -->
            <div class="card">
                <div class="card-header-flex">
                    <h2 class="section-title" style="margin-bottom:0;"><i class="fas fa-calendar-check"></i> Nhật ký uống thuốc</h2>
                    <div class="filter-group">
                        <a href="?range=7" class="filter-btn ${range == 7 ? 'active' : ''}">7 Ngày</a>
                        <a href="?range=30" class="filter-btn ${range == 30 ? 'active' : ''}">30 Ngày</a>
                        <input type="date" class="date-picker" value="${selectedDate}" onchange="if(this.value) window.location.href='?date=' + this.value; else window.location.href='?date=';">
                    </div>
                </div>
                <p style="font-size: 0.875rem; color: var(--text-muted); margin-bottom: 1.5rem;">Đánh dấu vào ô sau khi bạn đã uống thuốc để theo dõi tiến độ.</p>
                
                <c:choose>
                    <c:when test="${viewMode == 'progress'}">
                        <div id="progress-view" class="progress-view active">
                            <div class="progress-circle">${adherenceRate}%</div>
                            <h3 style="margin-bottom: 0.5rem; color: var(--text-dark);">Tỉ lệ tuân thủ điều trị (${range} ngày qua)</h3>
                            <p style="color: var(--text-muted); font-size: 0.875rem;">
                                <c:choose>
                                    <c:when test="${adherenceRate >= 80}">Tuyệt vời! Bạn đang duy trì uống thuốc rất đều đặn.</c:when>
                                    <c:when test="${adherenceRate >= 50}">Khá tốt! Cố gắng đừng quên uống thuốc nhé.</c:when>
                                    <c:otherwise>Cần chú ý! Hãy cố gắng uống thuốc đúng giờ hơn để đảm bảo hiệu quả điều trị.</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div id="checklist-view" class="checklist-view">
                            <!-- AI Reminder Box -->
                            <div class="ai-reminder-box" id="aiReminderBox">
                                <div class="ai-icon"><i class="fas fa-robot"></i></div>
                                <div class="ai-content">
                                    <h4>DiabCare AI Nhắc nhở</h4>
                                    <div class="ai-text" id="aiReminderText">
                                        <span class="shimmer" style="width: 100%; height: 1.15rem; display: block; margin-bottom: 0.4rem;"></span>
                                        <span class="shimmer" style="width: 80%; height: 1.15rem; display: block;"></span>
                                    </div>
                                </div>
                            </div>

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
                            <p style="color: var(--text-muted);">Không có danh sách thuốc nào cho ngày này.</p>
                        </c:otherwise>
                            </c:choose>
                        </div>
                    </c:otherwise>
                </c:choose>
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

    <jsp:include page="chatbot.jsp" />

    <script>
        // ==================== FETCH AI REMINDER ====================
        function fetchAIReminder() {
            const aiTextElement = document.getElementById('aiReminderText');
            if (!aiTextElement) return;
            
            fetch('${pageContext.request.contextPath}/patient-prescriptions/ai-reminder?date=${selectedDate}')
                .then(response => response.json())
                .then(data => {
                    aiTextElement.innerHTML = data.reminder || 'Đã có lỗi xảy ra khi tải lời nhắc.';
                })
                .catch(err => {
                    aiTextElement.innerHTML = 'Hệ thống AI đang bận. Vui lòng thử lại sau.';
                });
        }
        
        <c:if test="${viewMode != 'progress'}">
            document.addEventListener("DOMContentLoaded", function() {
                fetchAIReminder();
            });
        </c:if>

        // ==================== TOGGLE MED LOGIC ====================
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
                body: 'medicationId=' + medicationId + '&date=${selectedDate}'
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
