<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử khám bệnh - DiabCare</title>
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
        .page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.5rem; }
        .page-subtitle { color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem; }
        .card { background: var(--bg-white); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid var(--border); vertical-align: middle; }
        th { font-size: 0.75rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; }
        td { font-size: 0.875rem; }
        .doc-type { display: flex; align-items: center; gap: 0.6rem; font-weight: 700; color: var(--text-dark); }
        .doc-type i { color: var(--danger); font-size: 1.1rem; }
        .status-badge { display: inline-flex; padding: 0.25rem 0.55rem; border-radius: 999px; font-size: 0.75rem; font-weight: 700; }
        .status-badge.done { background: var(--success-light); color: var(--success); }
        .status-badge.pending { background: var(--warning-light); color: var(--warning); }
        .status-badge.cancelled { background: var(--danger-light); color: var(--danger); }
        .btn-pdf { display: inline-flex; align-items: center; gap: 0.4rem; background: var(--primary); color: white; text-decoration: none; padding: 0.55rem 0.85rem; border-radius: 8px; font-weight: 700; font-size: 0.8125rem; }
        .btn-disabled { display: inline-flex; align-items: center; gap: 0.4rem; background: var(--bg-body); color: var(--text-muted); padding: 0.55rem 0.85rem; border-radius: 8px; font-weight: 700; font-size: 0.8125rem; }
        .empty-state { color: var(--text-muted); text-align: center; padding: 2rem 1rem; }
        .pagination-bar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-top: 1.25rem; flex-wrap: wrap; }
        .pagination-info { color: var(--text-muted); font-size: 0.875rem; }
        .pagination { display: flex; align-items: center; gap: 0.35rem; }
        .page-link { min-width: 36px; height: 36px; display: inline-flex; align-items: center; justify-content: center; padding: 0 0.75rem; border: 1px solid var(--border); border-radius: 8px; color: var(--text-muted); text-decoration: none; font-weight: 700; font-size: 0.875rem; background: var(--bg-white); }
        .page-link:hover { border-color: var(--primary); color: var(--primary); }
        .page-link.active { background: var(--primary); color: white; border-color: var(--primary); }
        .page-link.disabled { pointer-events: none; opacity: 0.45; background: var(--bg-body); }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/patient-layout.css">
</head>
<body class="patient-app">
    <jsp:include page="layout/topnav.jsp">
        <jsp:param name="activeTop" value="dashboard"/>
    </jsp:include>

    <div class="app-container">
        <jsp:include page="layout/sidebar.jsp">
            <jsp:param name="activeMenu" value="history"/>
        </jsp:include>

        <main class="content">
            <h1 class="page-title">Lịch sử khám bệnh</h1>
            <p class="page-subtitle">Danh sách hồ sơ và PDF bác sĩ đã tải lên, sắp xếp theo thời gian mới nhất.</p>
            <div class="card">
                <c:choose>
                    <c:when test="${not empty medicalDocuments}">
                        <table>
                            <thead>
                                <tr>
                                    <th>Ngày khám</th>
                                    <th>Hồ sơ / PDF</th>
                                    <th>Bác sĩ</th>
                                    <th>Thời gian tải lên</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="doc" items="${medicalDocuments}">
                                    <tr>
                                        <td>${doc.ngayThucHien}</td>
                                        <td>
                                            <div class="doc-type">
                                                <i class="far fa-file-pdf"></i>
                                                ${doc.loaiTaiLieu}
                                            </div>
                                        </td>
                                        <td>${not empty doc.bacSiName ? doc.bacSiName : 'Chưa rõ'}</td>
                                        <td>${doc.ngayTao != null ? doc.ngayTao.toString().substring(0,16) : '--'}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${doc.trangThai == 'hoan_thanh'}">
                                                    <span class="status-badge done">Hoàn thành</span>
                                                </c:when>
                                                <c:when test="${doc.trangThai == 'can_xu_ly'}">
                                                    <span class="status-badge pending">Cần xử lý</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-badge cancelled">Hủy bỏ</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty doc.fileUrl && doc.fileUrl != '#'}">
                                                    <a href="${pageContext.request.contextPath}/${doc.fileUrl}" class="btn-pdf" target="_blank" rel="noopener">
                                                        <i class="fas fa-eye"></i> Xem PDF
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="btn-disabled"><i class="fas fa-ban"></i> Chưa có PDF</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <div class="pagination-bar">
                            <div class="pagination-info">
                                Tổng ${totalRecords} hồ sơ - Trang ${currentPage}/${totalPages}
                            </div>
                            <div class="pagination">
                                <a class="page-link ${currentPage <= 1 ? 'disabled' : ''}"
                                   href="patient-medical-history?page=${currentPage - 1}">
                                    <i class="fas fa-chevron-left"></i>
                                </a>
                                <c:forEach var="pageNum" begin="1" end="${totalPages}">
                                    <a class="page-link ${pageNum == currentPage ? 'active' : ''}"
                                       href="patient-medical-history?page=${pageNum}">
                                        ${pageNum}// bắt đầu page từ 1 kết thúc khi nhiều nhất nếu database có 
                                    </a>
                                </c:forEach>
                                <a class="page-link ${currentPage >= totalPages ? 'disabled' : ''}"
                                   href="patient-medical-history?page=${currentPage + 1}">
                                    <i class="fas fa-chevron-right"></i>
                                </a>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">Chưa có hồ sơ khám bệnh hoặc PDF nào được bác sĩ tải lên.</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>

    <jsp:include page="profile-modal.jsp">
        <jsp:param name="profileReturnUrl" value="patient-medical-history" />
    </jsp:include>
    <jsp:include page="chatbot.jsp" />
</body>
</html>
