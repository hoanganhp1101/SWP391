<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thực đơn dinh dưỡng - DiabCare</title>
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
            --success: #10b981;
            --warning: #f59e0b;
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
        .nav-links a.active::after {
            content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 2px; background-color: var(--primary);
        }

        .nav-right { display: flex; align-items: center; gap: 1.5rem; color: var(--text-muted); }
        
        .app-container {
            display: flex;
            margin-top: 64px;
            min-height: calc(100vh - 64px);
        }

        .sidebar {
            width: 280px;
            background-color: var(--bg-white);
            border-right: 1px solid var(--border);
            padding: 2rem 1.5rem;
            position: fixed;
            top: 64px;
            bottom: 0;
        }

        .profile-card {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            margin-bottom: 2rem;
            padding-bottom: 2rem;
            border-bottom: 1px solid var(--border);
        }

        .profile-avatar {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            margin-bottom: 1rem;
            background-color: #cbd5e1;
            background-image: url('https://ui-avatars.com/api/?name=${patientInfo.hoTen}&background=0D8ABC&color=fff');
            background-size: cover;
        }

        .profile-name {
            font-weight: 600;
            font-size: 1.125rem;
            color: var(--text-dark);
        }

        .profile-role {
            font-size: 0.875rem;
            color: var(--text-muted);
        }

        .sidebar-menu {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
            flex-grow: 1;
        }

        .menu-btn {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 0.75rem 1rem;
            border-radius: 8px;
            color: var(--text-muted);
            text-decoration: none;
            font-weight: 500;
            font-size: 0.875rem;
            transition: all 0.2s;
            border: none;
            background: none;
            width: 100%;
            cursor: pointer;
            text-align: left;
        }

        .menu-btn i {
            width: 20px;
            text-align: center;
            font-size: 1rem;
        }

        .menu-btn:hover {
            background-color: var(--bg-body);
        }

        .menu-btn.active {
            background-color: var(--primary);
            color: var(--bg-white);
        }

        .sidebar-bottom {
            margin-top: auto;
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }

        .btn-new {
            background-color: var(--primary);
            color: var(--bg-white);
            border: none;
            padding: 0.75rem;
            border-radius: 8px;
            font-weight: 600;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            cursor: pointer;
            transition: background-color 0.2s;
        }

        .btn-new:hover {
            background-color: #2563eb;
        }
        
        .main-content {
            flex: 1;
            margin-left: 280px;
            padding: 2rem;
        }

        .page-header {
            display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;
        }
        .page-title { font-size: 1.5rem; font-weight: 700; }
        .btn-generate {
            background-color: var(--primary); color: white; padding: 0.75rem 1.5rem;
            border-radius: 0.5rem; border: none; font-weight: 600; cursor: pointer;
            transition: 0.2s;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
        }
        .btn-generate:hover { background-color: #083a85; }

        .empty-state {
            text-align: center; padding: 4rem 2rem;
            background-color: var(--bg-white); border-radius: 1rem;
            border: 1px dashed var(--border);
            margin-top: 2rem;
        }
        .empty-icon { font-size: 4rem; color: var(--text-muted); margin-bottom: 1rem; }
        .empty-text { color: var(--text-muted); font-size: 1.1rem; margin-bottom: 1.5rem; }

        /* Diet Plan UI */
        .diet-container {
            display: grid;
            grid-template-columns: 1fr;
            gap: 1.5rem;
        }
        .meal-card {
            background: var(--bg-white);
            border-radius: 1rem;
            padding: 1.5rem;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
            border: 1px solid var(--border);
            position: relative;
            overflow: hidden;
        }
        
        .meal-card::before {
            content: '';
            position: absolute;
            left: 0; top: 0; bottom: 0; width: 4px;
        }
        .meal-card.sang::before { background-color: var(--warning); }
        .meal-card.trua::before { background-color: var(--primary); }
        .meal-card.toi::before { background-color: var(--success); }

        .meal-header {
            display: flex; justify-content: space-between; align-items: center;
            margin-bottom: 1rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--border);
        }
        .meal-title { font-size: 1.25rem; font-weight: 600; display: flex; align-items: center; gap: 0.5rem; }
        
        .food-item {
            display: flex; justify-content: space-between; align-items: center;
            padding: 1rem; background-color: var(--bg-body);
            border-radius: 0.5rem; margin-bottom: 0.75rem;
        }
        .food-info h4 { font-size: 1rem; font-weight: 600; margin-bottom: 0.25rem; }
        .food-meta { font-size: 0.875rem; color: var(--text-muted); }
        .food-stats { text-align: right; }
        .food-stats .calo { font-weight: 700; color: var(--danger); }
        .food-stats .carbs { font-size: 0.875rem; color: var(--primary); }

        /* Summary Stats */
        .stats-summary {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 1rem;
            margin-bottom: 2rem;
        }
        .stat-card {
            background: linear-gradient(135deg, var(--primary), #1e3a8a);
            color: white;
            padding: 1.5rem;
            border-radius: 1rem;
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }
        .stat-card.carbs-bg {
            background: linear-gradient(135deg, var(--success), #047857);
        }
        .stat-label { font-size: 0.875rem; opacity: 0.9; }
        .stat-value { font-size: 2rem; font-weight: 700; }
        
        /* Loading Overlay */
        .loading-overlay {
            position: fixed; top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(255, 255, 255, 0.9);
            z-index: 1000;
            display: none;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }
        .loading-overlay.active { display: flex; }
        .spinner {
            width: 50px; height: 50px; border: 4px solid var(--border);
            border-top-color: var(--primary); border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        @keyframes spin { 100% { transform: rotate(360deg); } }
        .loading-text { margin-top: 1rem; font-weight: 600; color: var(--text-dark); }
        
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/patient-layout.css">
</head>

<body class="patient-app">
    <jsp:include page="layout/topnav.jsp">
        <jsp:param name="activeTop" value="diet"/>
    </jsp:include>

    <div class="app-container">
        <jsp:include page="layout/sidebar.jsp">
            <jsp:param name="activeMenu" value="diet"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="main-content">
            <div class="page-header">
                <h1 class="page-title">Thực đơn Dinh dưỡng Hàng ngày</h1>
                <form method="post" action="${pageContext.request.contextPath}/patient-diet" onsubmit="showLoading()">
                    <input type="hidden" name="action" value="generate">
                    <button type="submit" class="btn-generate">
                        <i class="fa-solid fa-wand-magic-sparkles"></i> AI Gợi ý Thực Đơn
                    </button>
                </form>
            </div>
            
            <c:choose>
                <c:when test="${empty todayPlan}">
                    <div class="empty-state">
                        <i class="fa-solid fa-pizza-slice empty-icon"></i>
                        <h2 style="margin-bottom: 0.5rem">Bạn chưa có thực đơn hôm nay</h2>
                        <p class="empty-text">Hãy để AI của DiabCare gợi ý thực đơn phù hợp nhất với chỉ số đường huyết và BMI của bạn.</p>
                        <form method="post" action="${pageContext.request.contextPath}/patient-diet" onsubmit="showLoading()">
                            <input type="hidden" name="action" value="generate">
                            <button type="submit" class="btn-generate">
                                Bắt đầu khởi tạo ngay
                            </button>
                        </form>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Tóm tắt -->
                    <div class="stats-summary">
                        <div class="stat-card">
                            <span class="stat-label">Tổng lượng Calo dự kiến</span>
                            <span class="stat-value" id="totalCalo">0</span>
                        </div>
                        <div class="stat-card carbs-bg">
                            <span class="stat-label">Tổng lượng Carbs</span>
                            <span class="stat-value" id="totalCarbs">0g</span>
                        </div>
                    </div>

                    <!-- Chi tiết từng bữa -->
                    <div class="diet-container">
                        <!-- Sáng -->
                        <div class="meal-card sang">
                            <div class="meal-header">
                                <div class="meal-title"><i class="fa-solid fa-sun" style="color:var(--warning)"></i> Bữa Sáng</div>
                            </div>
                            <div class="meal-foods">
                                <c:forEach var="item" items="${todayPlan.chiTietThucPham}">
                                    <c:if test="${item.buaAn == 'Sáng' || item.buaAn == 'sang' || item.buaAn == 'Sang'}">
                                        <div class="food-item">
                                            <div class="food-info">
                                                <h4>${item.thucPhamGoc.tenThucPham}</h4>
                                                <div class="food-meta">${item.ghiChu != null ? item.ghiChu : item.thucPhamGoc.donViKhauPhan}</div>
                                            </div>
                                            <div class="food-stats">
                                                <div class="calo" data-val="${item.thucPhamGoc.caloKcal}">${item.thucPhamGoc.caloKcal} Kcal</div>
                                                <div class="carbs" data-val="${item.thucPhamGoc.carbsG}">${item.thucPhamGoc.carbsG}g Carbs</div>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>

                        <!-- Trưa -->
                        <div class="meal-card trua">
                            <div class="meal-header">
                                <div class="meal-title"><i class="fa-solid fa-cloud-sun" style="color:var(--primary)"></i> Bữa Trưa</div>
                            </div>
                            <div class="meal-foods">
                                <c:forEach var="item" items="${todayPlan.chiTietThucPham}">
                                    <c:if test="${item.buaAn == 'Trưa' || item.buaAn == 'trua' || item.buaAn == 'Trua'}">
                                        <div class="food-item">
                                            <div class="food-info">
                                                <h4>${item.thucPhamGoc.tenThucPham}</h4>
                                                <div class="food-meta">${item.ghiChu != null ? item.ghiChu : item.thucPhamGoc.donViKhauPhan}</div>
                                            </div>
                                            <div class="food-stats">
                                                <div class="calo" data-val="${item.thucPhamGoc.caloKcal}">${item.thucPhamGoc.caloKcal} Kcal</div>
                                                <div class="carbs" data-val="${item.thucPhamGoc.carbsG}">${item.thucPhamGoc.carbsG}g Carbs</div>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>

                        <!-- Tối -->
                        <div class="meal-card toi">
                            <div class="meal-header">
                                <div class="meal-title"><i class="fa-solid fa-moon" style="color:var(--text-dark)"></i> Bữa Tối</div>
                            </div>
                            <div class="meal-foods">
                                <c:forEach var="item" items="${todayPlan.chiTietThucPham}">
                                    <c:if test="${item.buaAn == 'Tối' || item.buaAn == 'toi' || item.buaAn == 'Toi'}">
                                        <div class="food-item">
                                            <div class="food-info">
                                                <h4>${item.thucPhamGoc.tenThucPham}</h4>
                                                <div class="food-meta">${item.ghiChu != null ? item.ghiChu : item.thucPhamGoc.donViKhauPhan}</div>
                                            </div>
                                            <div class="food-stats">
                                                <div class="calo" data-val="${item.thucPhamGoc.caloKcal}">${item.thucPhamGoc.caloKcal} Kcal</div>
                                                <div class="carbs" data-val="${item.thucPhamGoc.carbsG}">${item.thucPhamGoc.carbsG}g Carbs</div>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
            
        </main>
    </div>

    <!-- Loading Overlay -->
    <div class="loading-overlay" id="loadingOverlay">
        <div class="spinner"></div>
        <div class="loading-text">AI đang tính toán thực đơn tốt nhất cho bạn...</div>
    </div>

    <script>
        function showLoading() {
            document.getElementById('loadingOverlay').classList.add('active');
        }

        // Calculate totals
        document.addEventListener("DOMContentLoaded", function() {
            let totalCalo = 0;
            let totalCarbs = 0;
            
            document.querySelectorAll('.calo').forEach(el => {
                let val = parseFloat(el.getAttribute('data-val'));
                if (!isNaN(val)) totalCalo += val;
            });
            
            document.querySelectorAll('.carbs').forEach(el => {
                let val = parseFloat(el.getAttribute('data-val'));
                if (!isNaN(val)) totalCarbs += val;
            });

            const caloEl = document.getElementById('totalCalo');
            const carbsEl = document.getElementById('totalCarbs');
            
            if (caloEl) caloEl.innerText = Math.round(totalCalo) + ' Kcal';
            if (carbsEl) carbsEl.innerText = Math.round(totalCarbs * 10) / 10 + 'g';
        });
    </script>
</body>
</html>
