<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử thông báo - DiabCare</title>
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
        .page-header-container { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
        .page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.5rem; }
        .page-subtitle { color: var(--text-muted); font-size: 0.9rem; }
        
        .filter-container { display: flex; align-items: center; gap: 1rem; background: var(--bg-white); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--border); }
        .search-input { border: none; outline: none; font-size: 0.875rem; color: var(--text-dark); width: 200px; font-family: 'Inter', sans-serif; }
        .search-icon { color: var(--text-muted); }
        
        .card { background: var(--bg-white); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; }
        
        .alert-item { display: flex; gap: 1rem; padding: 1.25rem; margin-bottom: 1rem; background: var(--bg-body); border-radius: 8px; transition: all 0.2s; }
        .alert-item:hover { background: #f1f5f9; transform: translateY(-1px); box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); }
        .alert-icon { font-size: 1.5rem; margin-top: 0.25rem; }
        .alert-icon.danger { color: var(--danger); }
        .alert-icon.warning { color: var(--warning); }
        .alert-icon.primary { color: var(--primary); }
        .alert-icon.success { color: var(--success); }
        .alert-content { flex: 1; }
        .alert-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
        .alert-title { font-size: 1.05rem; font-weight: 600; color: var(--text-dark); margin: 0; }
        .alert-time { font-size: 0.85rem; color: var(--text-muted); display: flex; align-items: center; gap: 0.25rem; }
        .alert-body { font-size: 0.95rem; color: var(--text-dark); line-height: 1.6; margin: 0; }
        
        .empty-state { color: var(--text-muted); text-align: center; padding: 4rem 1rem; }
        .empty-icon { font-size: 3rem; color: var(--border); margin-bottom: 1rem; }
        
    </style>
</head>
<body>
    <nav class="top-nav">
        <div class="nav-left">
            <div class="logo">DiabCare</div>
            <div class="nav-links">
                <a href="patient-dashboard">Tổng quan</a>
                <a href="patient-medical-profile">Hồ sơ sức khỏe</a>
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
            <jsp:param name="activeMenu" value="notifications"/>
        </jsp:include>

        <main class="content">
            <div class="page-header-container">
                <div>
                    <h1 class="page-title">Lịch sử thông báo</h1>
                    <p class="page-subtitle">Xem lại toàn bộ thông báo và cảnh báo sức khỏe của bạn.</p>
                </div>
                <div class="filter-container">
                    <select id="typeFilter" class="search-input" style="width: auto; padding-right: 0.5rem; border-right: 1px solid var(--border); margin-right: 0.5rem; cursor: pointer;">
                        <option value="all">Tất cả thông báo</option>
                        <option value="alert">Cảnh báo sức khỏe</option>
                        <option value="medication">Nhắc nhở uống thuốc</option>
                        <option value="appointment">Lịch tái khám</option>
                    </select>
                    <i class="fas fa-search search-icon"></i>
                    <input type="text" id="searchInput" class="search-input" placeholder="Tìm kiếm thông báo...">
                </div>
            </div>
            
            <div class="card">
                <c:choose>
                    <c:when test="${not empty allNotifs}">
                        <div style="display: flex; flex-direction: column;">
                            <c:forEach var="notif" items="${allNotifs}">
                                <div class="alert-item" data-type="${notif.type}" style="border-left: 4px solid var(--${notif.colorClass});">
                                    <div class="alert-icon ${notif.colorClass}">
                                        <i class="${notif.icon}"></i>
                                    </div>
                                    <div class="alert-content">
                                        <div class="alert-header">
                                            <h4 class="alert-title">${notif.title}</h4>
                                            <span class="alert-time"><i class="far fa-clock"></i> ${notif.time}</span>
                                        </div>
                                        <p class="alert-body">${notif.message}</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <i class="far fa-bell-slash empty-icon"></i>
                            <p>Không có thông báo hoặc cảnh báo nào.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>

    <jsp:include page="profile-modal.jsp">
        <jsp:param name="profileReturnUrl" value="patient-notifications" />
    </jsp:include>
    <jsp:include page="chatbot.jsp" />

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const searchInput = document.getElementById('searchInput');
            const typeFilter = document.getElementById('typeFilter');
            
            function filterNotifications() {
                const query = searchInput ? searchInput.value.toLowerCase().trim() : '';
                const selectedType = typeFilter ? typeFilter.value : 'all';
                const notifItems = document.querySelectorAll('.alert-item');
                let visibleCount = 0;
                
                notifItems.forEach(function(item) {
                    const title = item.querySelector('.alert-title')?.textContent?.toLowerCase() || '';
                    const body = item.querySelector('.alert-body')?.textContent?.toLowerCase() || '';
                    const itemType = item.getAttribute('data-type');
                    
                    const matchesQuery = title.includes(query) || body.includes(query);
                    const matchesType = selectedType === 'all' || itemType === selectedType;
                    
                    if (matchesQuery && matchesType) {
                        item.style.display = 'flex';
                        visibleCount++;
                    } else {
                        item.style.display = 'none';
                    }
                });

                // Manage empty state
                let emptyState = document.getElementById('searchEmptyState');
                const card = document.querySelector('.card');
                
                if (visibleCount === 0 && notifItems.length > 0) {
                    if (!emptyState) {
                        emptyState = document.createElement('div');
                        emptyState.id = 'searchEmptyState';
                        emptyState.className = 'empty-state';
                        emptyState.innerHTML = '<i class="far fa-frown empty-icon"></i><p>Không tìm thấy thông báo nào phù hợp với bộ lọc của bạn.</p>';
                        card.appendChild(emptyState);
                    }
                    emptyState.style.display = 'block';
                } else if (emptyState) {
                    emptyState.style.display = 'none';
                }
            }

            if (searchInput) searchInput.addEventListener('input', filterNotifications);
            if (typeFilter) typeFilter.addEventListener('change', filterNotifications);
        });
    </script>
</body>
</html>
