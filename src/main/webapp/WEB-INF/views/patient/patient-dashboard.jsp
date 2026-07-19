<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển bệnh nhân - DiabetesCare</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <style>
        :root {
            --primary-color: #1a56db;
            --primary-light: #e1effe;
            --text-main: #111827;
            --text-muted: #6b7280;
            --bg-body: #f9fafb;
            --bg-card: #ffffff;
            --border-color: #e5e7eb;
            --danger: #e02424;
            --warning: #faca15;
            --success: #31c48d;
            --purple: #9061f9;
            --orange: #ff8a4c;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', sans-serif;
        }

        body {
            background-color: var(--bg-body);
            color: var(--text-main);
            display: flex;
            min-height: 100vh;
        }

        /* Sidebar */
        .sidebar {
            width: 260px;
            background-color: var(--bg-card);
            border-right: 1px solid var(--border-color);
            display: flex;
            flex-direction: column;
            padding: 1.5rem 1rem;
            position: fixed;
            height: 100vh;
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding-bottom: 2rem;
            padding-left: 0.5rem;
        }

        .brand-icon {
            background-color: var(--primary-color);
            color: white;
            padding: 0.5rem;
            border-radius: 8px;
            font-size: 1.25rem;
        }

        .brand-text {
            display: flex;
            flex-direction: column;
        }

        .brand-name {
            font-weight: 700;
            font-size: 1.25rem;
        }

        .brand-subtitle {
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        .nav-menu {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
            flex-grow: 1;
        }

        .nav-item {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 0.75rem 1rem;
            border-radius: 8px;
            color: var(--text-muted);
            text-decoration: none;
            font-weight: 500;
            transition: all 0.2s;
        }

        .nav-item:hover {
            background-color: var(--bg-body);
        }

        .nav-item.active {
            background-color: var(--primary-light);
            color: var(--primary-color);
        }

        .nav-item i {
            width: 20px;
            text-align: center;
        }

        .user-profile {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 1rem 0.5rem 0;
            border-top: 1px solid var(--border-color);
            margin-top: auto;
        }

        .avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background-color: var(--primary-color);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
        }

        .user-info {
            display: flex;
            flex-direction: column;
        }

        .user-name {
            font-weight: 600;
            font-size: 0.875rem;
        }

        .user-email {
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        /* Main Content */
        .main-content {
            margin-left: 260px;
            padding: 2rem;
            flex-grow: 1;
            width: calc(100% - 260px);
        }

        /* Top Cards */
        .top-cards {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 1.5rem;
            margin-bottom: 1.5rem;
        }

        .card {
            background-color: var(--bg-card);
            border-radius: 12px;
            border: 1px solid var(--border-color);
            padding: 1.5rem;
            display: flex;
            flex-direction: column;
            gap: 1rem;
            box-shadow: 0 1px 2px rgba(0,0,0,0.05);
        }

        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
        }

        .icon-box {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.25rem;
        }

        .icon-box.red { background-color: #fde8e8; color: var(--danger); }
        .icon-box.blue { background-color: #e1effe; color: var(--primary-color); }
        .icon-box.orange { background-color: #fdf6b2; color: var(--orange); }
        .icon-box.purple { background-color: #edebfe; color: var(--purple); }

        .card-title {
            font-size: 0.875rem;
            color: var(--text-muted);
            font-weight: 500;
        }

        .card-value {
            font-size: 2rem;
            font-weight: 700;
            display: flex;
            align-items: baseline;
            gap: 0.5rem;
        }

        .card-unit {
            font-size: 1rem;
            color: var(--text-muted);
            font-weight: 400;
        }

        .card-subtitle {
            font-size: 0.875rem;
            color: var(--text-muted);
        }

        /* Middle Section */
        .content-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 1.5rem;
        }

        /* Chart Section */
        .chart-section {
            background-color: var(--bg-card);
            border-radius: 12px;
            border: 1px solid var(--border-color);
            padding: 1.5rem;
            display: flex;
            flex-direction: column;
        }

        .chart-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 1.5rem;
        }

        .chart-title h3 {
            font-size: 1.125rem;
            font-weight: 600;
        }

        .chart-title p {
            font-size: 0.875rem;
            color: var(--text-muted);
            margin-top: 0.25rem;
        }

        .chart-filters {
            display: flex;
            gap: 0.5rem;
        }

        .filter-btn {
            padding: 0.5rem 1rem;
            border-radius: 20px;
            border: 1px solid var(--border-color);
            background-color: transparent;
            font-size: 0.875rem;
            font-weight: 500;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .filter-btn.insulin { border-color: var(--purple); color: var(--purple); }
        .filter-btn.meals { border-color: var(--orange); color: var(--orange); }

        .chart-container {
            flex-grow: 1;
            position: relative;
            height: 300px;
            width: 100%;
        }

        /* Right Panel */
        .right-panel {
            display: flex;
            flex-direction: column;
            gap: 1.5rem;
        }

        /* Quick Log */
        .quick-log {
            background-color: var(--bg-card);
            border-radius: 12px;
            border: 1px solid var(--border-color);
            padding: 1.5rem;
        }

        .quick-log h3 {
            font-size: 1.125rem;
            font-weight: 600;
        }
        
        .quick-log p {
            font-size: 0.875rem;
            color: var(--text-muted);
            margin-bottom: 1.5rem;
        }

        .form-group {
            margin-bottom: 1rem;
        }

        .form-group label {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.875rem;
            font-weight: 500;
            margin-bottom: 0.5rem;
        }
        
        .form-group label.red { color: var(--danger); }
        .form-group label.orange { color: var(--orange); }
        .form-group label.purple { color: var(--purple); }

        .form-control {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            font-size: 0.875rem;
            outline: none;
            transition: border-color 0.2s;
        }

        .form-control:focus {
            border-color: var(--primary-color);
        }

        .btn {
            width: 100%;
            padding: 0.75rem 1rem;
            border: none;
            border-radius: 8px;
            font-size: 0.875rem;
            font-weight: 500;
            cursor: pointer;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 0.5rem;
            transition: opacity 0.2s;
        }

        .btn-primary {
            background-color: var(--primary-color);
            color: white;
        }

        .btn-primary:hover {
            opacity: 0.9;
        }

        /* Daily Foot Check */
        .foot-check {
            background-color: var(--primary-light);
            border-radius: 12px;
            border: 1px solid #c3ddfd;
            padding: 1.5rem;
        }

        .foot-header {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-bottom: 1rem;
        }

        .foot-icon {
            width: 40px;
            height: 40px;
            background-color: white;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--primary-color);
            font-size: 1.25rem;
        }

        .foot-title h3 {
            font-size: 1rem;
            font-weight: 600;
            color: var(--text-main);
        }

        .foot-title p {
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        .foot-checklist {
            background-color: white;
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
        }

        .foot-checklist ul {
            list-style-type: disc;
            padding-left: 1.5rem;
            font-size: 0.875rem;
            color: var(--text-main);
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }

        .trend-icon {
            font-size: 0.875rem;
            width: 24px;
            height: 24px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: var(--bg-body);
        }
    </style>
</head>
<body>

    <!-- Sidebar -->
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-icon">
                <i class="fas fa-heartbeat"></i>
            </div>
            <div class="brand-text">
                <span class="brand-name">DiabetesCare</span>
                <span class="brand-subtitle">Cổng thông tin bệnh nhân</span>
            </div>
        </div>

        <ul class="nav-menu">
            <li><a href="#" class="nav-item active"><i class="fas fa-home"></i> Tổng quan</a></li>
            <li><a href="#" class="nav-item"><i class="fas fa-chart-line"></i> Nhật ký đường huyết</a></li>
            <li><a href="#" class="nav-item"><i class="fas fa-chart-bar"></i> Xu hướng</a></li>
            <li><a href="#" class="nav-item"><i class="fas fa-calendar-alt"></i> Lịch hẹn</a></li>
            <li><a href="#" class="nav-item"><i class="fas fa-file-alt"></i> Báo cáo</a></li>
            <li><a href="#" class="nav-item"><i class="fas fa-user"></i> Hồ sơ cá nhân</a></li>
            <li><a href="#" class="nav-item"><i class="fas fa-cog"></i> Cài đặt</a></li>
        </ul>

        <div class="user-profile">
            <div class="avatar">JD</div>
            <div class="user-info">
                <span class="user-name">John Doe</span>
                <span class="user-email">john.doe@email.com</span>
            </div>
        </div>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
        
        <!-- Top Cards -->
        <div class="top-cards">
            <!-- Card 1 -->
            <div class="card">
                <div class="card-header">
                    <div class="icon-box red"><i class="fas fa-tint"></i></div>
                    <div class="trend-icon" style="color: var(--text-muted);"><i class="fas fa-minus"></i></div>
                </div>
                <div>
                    <div class="card-title">Đường huyết gần nhất</div>
                    <div class="card-value">${latestGlucose != null ? latestGlucose : 113} <span class="card-unit">mg/dL</span></div>
                    <div class="card-subtitle">${glucoseTime != null ? glucoseTime : '2 giờ trước'}</div>
                </div>
            </div>

            <!-- Card 2 -->
            <div class="card">
                <div class="card-header">
                    <div class="icon-box blue"><i class="fas fa-wave-square"></i></div>
                </div>
                <div>
                    <div class="card-title">HbA1c gần nhất</div>
                    <div class="card-value">${hba1c != null ? hba1c : 6.8} <span class="card-unit">%</span></div>
                    <div class="card-subtitle">Mục tiêu: &lt; 7,0%</div>
                </div>
            </div>

            <!-- Card 3 -->
            <div class="card">
                <div class="card-header">
                    <div class="icon-box orange"><i class="fas fa-utensils"></i></div>
                    <div class="trend-icon" style="color: var(--success);"><i class="fas fa-arrow-down"></i></div>
                </div>
                <div>
                    <div class="card-title">Lượng tinh bột trong ngày</div>
                    <div class="card-value">${dailyCarb != null ? dailyCarb : 180} <span class="card-unit">g</span></div>
                    <div class="card-subtitle">Tổng hôm nay</div>
                </div>
            </div>

            <!-- Card 4 -->
            <div class="card">
                <div class="card-header">
                    <div class="icon-box purple"><i class="fas fa-calendar-day"></i></div>
                </div>
                <div>
                    <div class="card-title">Lịch hẹn tiếp theo</div>
                    <div class="card-value" style="font-size: 1.75rem;">${nextAppointmentDate != null ? nextAppointmentDate : '20 tháng 5'}</div>
                    <div class="card-subtitle">${nextAppointmentDoctor != null ? nextAppointmentDoctor : 'Bác sĩ chuyên khoa Nội tiết'}</div>
                </div>
            </div>
        </div>

        <!-- Middle Section -->
        <div class="content-grid">
            
            <!-- Chart Section -->
            <div class="chart-section">
                <div class="chart-header">
                    <div class="chart-title">
                        <h3>Xu hướng đường huyết</h3>
                        <p>Trung bình hằng ngày trong 7 ngày</p>
                    </div>
                    <div class="chart-filters">
                        <button class="filter-btn insulin"><i class="fas fa-wave-square"></i> Insulin</button>
                        <button class="filter-btn meals"><i class="fas fa-utensils"></i> Bữa ăn</button>
                    </div>
                </div>
                <div class="chart-container">
                    <canvas id="glucoseChart"></canvas>
                </div>
            </div>

            <!-- Right Panel -->
            <div class="right-panel">
                
                <!-- Quick Log -->
                <div class="quick-log">
                    <h3>Ghi nhanh</h3>
                    <p>Nhập dữ liệu nhanh</p>
                    <form action="logData" method="POST">
                        <div class="form-group">
                            <label class="red"><i class="fas fa-tint"></i> Đường huyết (mg/dL)</label>
                            <input type="number" class="form-control" placeholder="120" name="glucose">
                        </div>
                        <div class="form-group">
                            <label class="orange"><i class="fas fa-utensils"></i> Tinh bột (g)</label>
                            <input type="number" class="form-control" placeholder="45" name="carbs">
                        </div>
                        <div class="form-group">
                            <label class="purple"><i class="fas fa-wave-square"></i> Insulin (đơn vị)</label>
                            <input type="number" class="form-control" placeholder="8" name="insulin">
                        </div>
                        <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Lưu dữ liệu</button>
                    </form>
                </div>

                <!-- Daily Foot Check -->
                <div class="foot-check">
                    <div class="foot-header">
                        <div class="foot-icon"><i class="fas fa-shoe-prints"></i></div>
                        <div class="foot-title">
                            <h3>Kiểm tra bàn chân hằng ngày</h3>
                            <p>Hôm nay chưa hoàn thành</p>
                        </div>
                    </div>
                    <div class="foot-checklist">
                        <ul>
                            <li>Kiểm tra vết cắt, phồng rộp hoặc vùng da đỏ</li>
                            <li>Kiểm tra kẽ giữa các ngón chân</li>
                            <li>Kiểm tra dấu hiệu sưng hoặc thay đổi màu da</li>
                        </ul>
                    </div>
                    <button class="btn btn-primary"><i class="fas fa-check"></i> Đánh dấu đã hoàn thành</button>
                </div>

            </div>
        </div>

    </main>

    <script>
        // Chart.js implementation for Glucose Trends
        const ctx = document.getElementById('glucoseChart').getContext('2d');
        
        // Mock data similar to the image
        const days = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'];
        const glucoseData = [107, 120, 106, 118, 115, 122, 110];
        const insulinData = [10, 12, 11, 14, 12, 13, 11]; // Bottom line data

        const glucoseChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: days,
                datasets: [
                    {
                        label: 'Đường huyết',
                        data: glucoseData,
                        borderColor: '#1a56db',
                        backgroundColor: '#1a56db',
                        tension: 0.4,
                        borderWidth: 2,
                        pointBackgroundColor: '#1a56db',
                        pointRadius: 4,
                        pointHoverRadius: 6,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Insulin',
                        data: insulinData,
                        borderColor: '#9061f9',
                        backgroundColor: '#9061f9',
                        tension: 0.4,
                        borderWidth: 2,
                        pointBackgroundColor: '#9061f9',
                        pointRadius: 4,
                        pointHoverRadius: 6,
                        yAxisID: 'y'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        }
                    },
                    y: {
                        min: 0,
                        max: 200,
                        ticks: {
                            stepSize: 50,
                            callback: function(value) {
                                if (value === 0) return '7';
                                if (value === 50) return '57';
                                if (value === 100) return '107';
                                if (value === 200) return '200';
                                return '';
                            }
                        },
                        grid: {
                            color: function(context) {
                                // Add target lines (dashed red) at specific values
                                if (context.tick.value === 50 || context.tick.value === 150) {
                                    return 'rgba(224, 36, 36, 0.3)';
                                }
                                return '#e5e7eb';
                            },
                            borderDash: function(context) {
                                if (context.tick.value === 50 || context.tick.value === 150) {
                                    return [5, 5];
                                }
                                return [];
                            }
                        }
                    }
                }
            }
        });
    </script>
</body>
</html>
