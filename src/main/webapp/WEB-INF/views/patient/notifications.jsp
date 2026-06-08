<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
/* Notifications */
.notification-container {
    position: relative;
    cursor: pointer;
    display: flex;
    align-items: center;
}

.notif-badge {
    position: absolute;
    top: -5px;
    right: -8px;
    background: var(--danger, #ef4444);
    color: white;
    font-size: 0.65rem;
    font-weight: bold;
    border-radius: 50%;
    min-width: 18px;
    height: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid var(--bg-white, #ffffff);
}

.notification-dropdown {
    position: absolute;
    top: 150%;
    right: -10px;
    width: 340px;
    background: var(--bg-white, #ffffff);
    border: 1px solid var(--border, #e2e8f0);
    border-radius: 12px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
    display: none;
    flex-direction: column;
    z-index: 1000;
    overflow: hidden;
    animation: slideDown 0.2s ease-out;
    cursor: default;
}

.notification-dropdown.show {
    display: flex;
}

.notif-header {
    padding: 1rem 1.25rem;
    font-weight: 600;
    border-bottom: 1px solid var(--border, #e2e8f0);
    font-size: 0.875rem;
    background: var(--bg-body, #f8fafc);
    color: var(--text-dark, #1e293b);
}

.notif-body {
    max-height: 400px;
    overflow-y: auto;
}

.notif-item {
    display: flex;
    gap: 1rem;
    padding: 1rem 1.25rem;
    border-bottom: 1px solid var(--border, #e2e8f0);
    transition: background 0.2s, opacity 0.2s;
    align-items: flex-start;
    cursor: pointer;
}

.notif-item:hover {
    background: var(--bg-body, #f8fafc);
}

.notif-item.read {
    background: var(--bg-body, #f8fafc);
    opacity: 0.6;
}

.notif-icon {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    flex-shrink: 0;
}

.notif-content {
    flex-grow: 1;
}

.notif-title {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--text-dark, #1e293b);
    margin-bottom: 0.25rem;
}

.notif-message {
    font-size: 0.8125rem;
    color: var(--text-muted, #64748b);
    line-height: 1.4;
    margin-bottom: 0.25rem;
}

.notif-time {
    font-size: 0.75rem;
    color: #94a3b8;
}

.notif-empty {
    padding: 2rem;
    text-align: center;
    color: var(--text-muted, #64748b);
    font-size: 0.875rem;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>

<div class="notification-container" id="notifContainer">
    <i class="far fa-bell" id="notifBell"></i>
    <div class="notif-badge" id="notifBadge" style="display: none;">0</div>
    <div class="notification-dropdown" id="notifDropdown">
        <div class="notif-header">Thông báo</div>
        <div class="notif-body" id="notifBody">
            <div class="notif-empty">Đang tải...</div>
        </div>
    </div>
</div>

<script>
    // ==================== NOTIFICATIONS LOGIC ====================
    const notifContainer = document.getElementById('notifContainer');
    const notifBell = document.getElementById('notifBell');
    const notifDropdown = document.getElementById('notifDropdown');
    const notifBadge = document.getElementById('notifBadge');
    const notifBody = document.getElementById('notifBody');

    if (notifBell) {
        notifBell.addEventListener('click', (e) => {
            e.stopPropagation();
            notifDropdown.classList.toggle('show');
        });
    }

    document.addEventListener('click', (e) => {
        if (notifContainer && !notifContainer.contains(e.target)) {
            if (notifDropdown && notifDropdown.classList.contains('show')) {
                notifDropdown.classList.remove('show');
            }
        }
    });

    function markAsRead(id, link) {
        if (id.startsWith('med_')) {
            // Nhắc thuốc: Lưu session (nhắc lại mỗi phiên)
            let sessionReadNotifs = JSON.parse(sessionStorage.getItem('sessionReadNotifs') || '[]');
            if (!sessionReadNotifs.includes(id)) {
                sessionReadNotifs.push(id);
                sessionStorage.setItem('sessionReadNotifs', JSON.stringify(sessionReadNotifs));
            }
        } else {
            // Các cảnh báo khác: Lưu local (ẩn vĩnh viễn)
            let localReadNotifs = JSON.parse(localStorage.getItem('localReadNotifs') || '[]');
            if (!localReadNotifs.includes(id)) {
                localReadNotifs.push(id);
                localStorage.setItem('localReadNotifs', JSON.stringify(localReadNotifs));
            }
        }

        if (link && link !== '#') {
            window.location.href = link;
        } else {
            fetchNotifications();
        }
    }

    function fetchNotifications() {
        let sessionReadNotifs = JSON.parse(sessionStorage.getItem('sessionReadNotifs') || '[]');
        let localReadNotifs = JSON.parse(localStorage.getItem('localReadNotifs') || '[]');
        let readNotifs = [...sessionReadNotifs, ...localReadNotifs];

        fetch('${pageContext.request.contextPath}/api/notifications')
            .then(response => response.json())
            .then(data => {
                if (data && data.length > 0) {
                    let unreadCount = data.filter(item => !readNotifs.includes(item.id)).length;

                    if (unreadCount > 0) {
                        notifBadge.style.display = 'flex';
                        notifBadge.textContent = unreadCount;
                    } else {
                        notifBadge.style.display = 'none';
                    }

                    let html = '';
                    data.forEach(item => {
                        const isRead = readNotifs.includes(item.id);
                        html += `
                            <div class="notif-item ` + (isRead ? 'read' : '') + `" onclick="markAsRead('` + item.id + `', '` + item.link + `')">
                                <div class="notif-icon" style="color: ` + item.color + `; background: ` + item.bgColor + `;">
                                    <i class="` + item.icon + `"></i>
                                </div>
                                <div class="notif-content">
                                    <div class="notif-title">` + item.title + `</div>
                                    <div class="notif-message">` + item.message + `</div>
                                    <div class="notif-time">` + item.time + `</div>
                                </div>
                            </div>
                        `;
                    });
                    notifBody.innerHTML = html;
                } else {
                    notifBadge.style.display = 'none';
                    notifBody.innerHTML = '<div class="notif-empty">Bạn đã đọc hết thông báo.</div>';
                }
            })
            .catch(err => console.error('Error fetching notifications:', err));
    }

    document.addEventListener("DOMContentLoaded", function () {
        fetchNotifications();
    });
</script>
