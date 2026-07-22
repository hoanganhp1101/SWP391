<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch khám - DiabCare</title>
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
            --warning-light: #fef3c7;
        }

        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }
        body { background-color: var(--bg-body); color: var(--text-dark); }

        .top-nav { display: flex; align-items: center; justify-content: space-between; background-color: var(--bg-white); border-bottom: 1px solid var(--border); padding: 0 2rem; height: 64px; position: fixed; top: 0; left: 0; right: 0; z-index: 100; }
        .nav-left { display: flex; align-items: center; gap: 2rem; }
        .logo { font-size: 1.25rem; font-weight: 700; color: var(--primary); }
        .nav-links { display: flex; gap: 1.5rem; }
        .nav-links a { text-decoration: none; color: var(--text-muted); font-weight: 500; font-size: 0.875rem; padding: 1.25rem 0; position: relative; }
        .nav-links a.active { color: var(--primary); }
        .nav-links a.active::after { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 2px; background-color: var(--primary); }
        .nav-right { display: flex; align-items: center; gap: 1.5rem; color: var(--text-muted); }
        .avatar-small { width: 32px; height: 32px; border-radius: 50%; background-color: #cbd5e1; background-image: url('${not empty patientInfo.anhDaiDien ? patientInfo.anhDaiDien : "https://ui-avatars.com/api/?name=" += patientInfo.hoTen += "&background=0D8ABC&color=fff"}'); background-size: cover; background-position: center; }
        .avatar-link { text-decoration: none; color: inherit; display: inline-block; }

        .app-container { display: flex; margin-top: 64px; min-height: calc(100vh - 64px); }
        .sidebar { width: 280px; background-color: var(--bg-white); border-right: 1px solid var(--border); padding: 2rem 1.5rem; display: flex; flex-direction: column; position: fixed; top: 64px; bottom: 0; overflow-y: auto; }
        .profile-card { display: flex; flex-direction: column; align-items: center; text-align: center; margin-bottom: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border); }
        .profile-avatar { width: 80px; height: 80px; border-radius: 50%; margin-bottom: 1rem; background-color: #cbd5e1; background-image: url('${not empty patientInfo.anhDaiDien ? patientInfo.anhDaiDien : "https://ui-avatars.com/api/?name=" += patientInfo.hoTen += "&background=0D8ABC&color=fff"}'); background-size: cover; background-position: center; border: 2px solid transparent; transition: border-color 0.2s ease, transform 0.2s ease; }
        .profile-avatar:hover { border-color: var(--primary); transform: translateY(-1px); }
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

        .content { margin-left: 280px; padding: 2rem; flex-grow: 1; width: calc(100% - 280px); }
        .page-header { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
        .page-title { font-size: 1.5rem; font-weight: 700; }
        .section-title { font-size: 1.125rem; font-weight: 600; margin-bottom: 1rem; color: var(--primary); display: flex; align-items: center; gap: 0.5rem; }
        .grid-2 { display: grid; grid-template-columns: 0.9fr 1.4fr; gap: 1.5rem; align-items: start; }
        .card { background-color: var(--bg-white); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; margin-bottom: 1.5rem; }

        .form-group { margin-bottom: 1rem; }
        .form-group label { display: block; font-size: 0.875rem; font-weight: 600; color: var(--text-dark); margin-bottom: 0.5rem; }
        .form-control { width: 100%; padding: 0.75rem 1rem; border: 1px solid var(--border); border-radius: 8px; font-size: 0.875rem; outline: none; background: white; }
        .form-control:focus { border-color: var(--primary); }
        .btn-primary { background: var(--primary); color: white; border: none; border-radius: 8px; padding: 0.8rem 1.25rem; font-weight: 700; cursor: pointer; width: 100%; }
        .btn-primary:hover { background: #083c8a; }

        .profile-message { padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.875rem; font-weight: 500; }
        .profile-message.success { background: var(--success-light); color: var(--success); border: 1px solid #86efac; }
        .profile-message.error { background: var(--danger-light); color: var(--danger); border: 1px solid #fca5a5; }

        .appointment-item { display: flex; gap: 1rem; padding: 1rem 0; border-bottom: 1px solid var(--border); }
        .appointment-item:last-child { border-bottom: none; padding-bottom: 0; }
        .apt-date { width: 64px; height: 64px; border-radius: 12px; background: var(--primary-light); color: var(--primary); display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; font-weight: 700; }
        .apt-date .day { font-size: 1.35rem; line-height: 1; }
        .apt-date .month { font-size: 0.75rem; margin-top: 0.25rem; }
        .apt-info { flex: 1; }
        .apt-info h3 { font-size: 1rem; margin-bottom: 0.35rem; }
        .apt-info p { color: var(--text-muted); font-size: 0.875rem; margin-bottom: 0.3rem; }
        .status-badge { display: inline-flex; align-items: center; padding: 0.25rem 0.55rem; border-radius: 999px; font-size: 0.75rem; font-weight: 700; }
        .status-badge.waiting { background: var(--warning-light); color: var(--warning); }
        .status-badge.done { background: var(--success-light); color: var(--success); }
        .status-badge.cancelled { background: var(--danger-light); color: var(--danger); }
        .appointment-actions { margin-top: 0.75rem; display: flex; gap: 0.5rem; flex-wrap: wrap; }
        .btn-edit { border: 1px solid var(--primary); background: var(--primary-light); color: var(--primary); border-radius: 8px; padding: 0.45rem 0.75rem; font-weight: 700; cursor: pointer; font-size: 0.8125rem; }
        .btn-secondary { border: 1px solid var(--border); background: var(--bg-white); color: var(--text-muted); border-radius: 8px; padding: 0.8rem 1.25rem; font-weight: 700; cursor: pointer; width: 100%; margin-top: 0.75rem; display: none; }
        .empty-state { color: var(--text-muted); text-align: center; padding: 2rem 1rem; }
    </style>
</head>
<body>
    <nav class="top-nav">
        <div class="nav-left">
            <div class="logo">DiabCare</div>
            <div class="nav-links">
                <a href="patient-dashboard">Tổng quan</a>
                <a href="patient-medical-profile">Hồ sơ sức khỏe</a>
                <a href="patient-appointments" class="active">Lịch hẹn</a>
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
            <jsp:param name="activeMenu" value="appointments"/>
        </jsp:include>

        <main class="content">
            <div class="page-header">
                <h1 class="page-title">Lịch khám của tôi</h1>
            </div>

            <c:if test="${param.appointmentCreated == '1'}">
                <div class="profile-message success">
                    <i class="fas fa-check-circle"></i> Đặt lịch khám thành công.
                </div>
            </c:if>
            <c:if test="${param.appointmentCreated == '0'}">
                <div class="profile-message error">
                    <i class="fas fa-exclamation-circle"></i> ${not empty param.error ? param.error : 'Đặt lịch khám thất bại.'}
                </div>
            </c:if>
            <c:if test="${param.appointmentUpdated == '1'}">
                <div class="profile-message success">
                    <i class="fas fa-check-circle"></i> Thay đổi lịch khám thành công.
                </div>
            </c:if>
            <c:if test="${param.profileUpdated == '1'}">
                <div class="profile-message success">
                    <i class="fas fa-check-circle"></i> Hồ sơ bệnh nhân đã được cập nhật thành công.
                </div>
            </c:if>

            <div class="grid-2">
                <div class="card">
                    <h2 class="section-title" id="appointmentFormTitle"><i class="fas fa-calendar-plus"></i> Đặt lịch khám</h2>
                    <form action="patient-appointments" method="POST" id="appointmentForm">
                        <input type="hidden" name="action" id="appointmentAction" value="create">
                        <input type="hidden" name="appointmentId" id="appointmentId">
                        <div class="form-group">
                            <label>Nội dung khám</label>
                            <input type="text" class="form-control" name="tieuDe" id="tieuDe" required
                                placeholder="VD: Tái khám Nội tiết">
                        </div>
                        <div class="form-group">
                            <label>Bác sĩ</label>
                            <select class="form-control" name="bacSiId" id="bacSiId">
                                <option value="">-- Chưa chọn bác sĩ --</option>
                                <c:forEach var="doctor" items="${doctors}">
                                    <option value="${doctor.id}">${doctor.hoTen}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Thời gian hẹn</label>
                            <input type="datetime-local" class="form-control" name="thoiGianHen" id="thoiGianHen" required>
                        </div>
                        <div class="form-group">
                            <label>Địa điểm</label>
                            <input type="text" class="form-control" name="diaDiem" id="diaDiem" required
                                placeholder="VD: Phòng khám Đa khoa">
                        </div>
                        <button type="submit" class="btn-primary" id="appointmentSubmitBtn">
                            <i class="fas fa-calendar-check"></i> Đặt lịch khám
                        </button>
                        <button type="button" class="btn-secondary" id="cancelEditBtn">Hủy thay đổi</button>
                    </form>
                </div>

                <div class="card">
                    <h2 class="section-title"><i class="far fa-calendar-alt"></i> Danh sách lịch khám</h2>
                    <c:choose>
                        <c:when test="${not empty appointments}">
                            <c:forEach var="appt" items="${appointments}">
                                <div class="appointment-item">
                                    <div class="apt-date">
                                        <div class="day">${appt.thoiGianHen.toString().substring(8,10)}</div>
                                        <div class="month">THG ${appt.thoiGianHen.toString().substring(5,7)}</div>
                                    </div>
                                    <div class="apt-info">
                                        <h3>${appt.tieuDe}</h3>
                                        <p><i class="far fa-clock"></i> ${appt.thoiGianHen.toString().substring(0,16)}</p>
                                        <p><i class="fas fa-user-md"></i> ${not empty appt.bacSiName ? appt.bacSiName : 'Chưa phân công bác sĩ'}</p>
                                        <p><i class="fas fa-map-marker-alt"></i> ${appt.diaDiem}</p>
                                        <c:choose>
                                            <c:when test="${appt.trangThai == 'cho_kham'}">
                                                <span class="status-badge waiting">Chờ khám</span>
                                            </c:when>
                                            <c:when test="${appt.trangThai == 'da_kham'}">
                                                <span class="status-badge done">Đã khám</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge cancelled">Đã hủy</span>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:if test="${appt.trangThai == 'cho_kham'}">
                                            <div class="appointment-actions">
                                                <button type="button" class="btn-edit"
                                                    data-id="${appt.id}"
                                                    data-title="${appt.tieuDe}"
                                                    data-doctor="${appt.bacSiId}"
                                                    data-time="${appt.thoiGianHen.toString().substring(0,16).replace(' ', 'T')}"
                                                    data-location="${appt.diaDiem}">
                                                    <i class="fas fa-pen"></i> Thay đổi
                                                </button>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state">Bạn chưa có lịch khám nào.</div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>

    <jsp:include page="profile-modal.jsp">
        <jsp:param name="profileReturnUrl" value="patient-appointments" />
    </jsp:include>
    <jsp:include page="chatbot.jsp" />
    <script>
        const appointmentForm = document.getElementById('appointmentForm');
        const appointmentFormTitle = document.getElementById('appointmentFormTitle');
        const appointmentAction = document.getElementById('appointmentAction');
        const appointmentId = document.getElementById('appointmentId');
        const tieuDe = document.getElementById('tieuDe');
        const bacSiId = document.getElementById('bacSiId');
        const thoiGianHen = document.getElementById('thoiGianHen');
        const diaDiem = document.getElementById('diaDiem');
        const appointmentSubmitBtn = document.getElementById('appointmentSubmitBtn');
        const cancelEditBtn = document.getElementById('cancelEditBtn');

        document.querySelectorAll('.btn-edit').forEach(button => {
            button.addEventListener('click', () => {
                appointmentAction.value = 'update';
                appointmentId.value = button.dataset.id || '';
                tieuDe.value = button.dataset.title || '';
                bacSiId.value = button.dataset.doctor || '';
                thoiGianHen.value = button.dataset.time || '';
                diaDiem.value = button.dataset.location || '';
                appointmentFormTitle.innerHTML = '<i class="fas fa-pen"></i> Thay đổi lịch khám';
                appointmentSubmitBtn.innerHTML = '<i class="fas fa-save"></i> Lưu thay đổi';
                cancelEditBtn.style.display = 'block';
                appointmentForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
            });
        });

        if (cancelEditBtn) {
            cancelEditBtn.addEventListener('click', () => {
                appointmentForm.reset();
                appointmentAction.value = 'create';
                appointmentId.value = '';
                appointmentFormTitle.innerHTML = '<i class="fas fa-calendar-plus"></i> Đặt lịch khám';
                appointmentSubmitBtn.innerHTML = '<i class="fas fa-calendar-check"></i> Đặt lịch khám';
                cancelEditBtn.style.display = 'none';
            });
        }
    </script>
</body>
</html>
