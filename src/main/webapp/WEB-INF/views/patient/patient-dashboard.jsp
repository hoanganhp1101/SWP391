<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib uri="jakarta.tags.core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Tổng quan sức khỏe - DiabCare</title>
            <!-- Google Fonts -->
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                rel="stylesheet">
            <!-- Font Awesome -->
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
            <!-- Chart.js -->
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

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

                .nav-left {
                    display: flex;
                    align-items: center;
                    gap: 2rem;
                }

                .logo {
                    font-size: 1.25rem;
                    font-weight: 700;
                    color: var(--primary);
                }

                .nav-links {
                    display: flex;
                    gap: 1.5rem;
                }

                .nav-links a {
                    text-decoration: none;
                    color: var(--text-muted);
                    font-weight: 500;
                    font-size: 0.875rem;
                    padding: 1.25rem 0;
                    position: relative;
                }

                .nav-links a.active {
                    color: var(--primary);
                }

                .nav-links a.active::after {
                    content: '';
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    height: 2px;
                    background-color: var(--primary);
                }

                .nav-right {
                    display: flex;
                    align-items: center;
                    gap: 1.5rem;
                    color: var(--text-muted);
                }

                .nav-right i {
                    cursor: pointer;
                    font-size: 1.125rem;
                }

                .avatar-small {
                    width: 32px;
                    height: 32px;
                    border-radius: 50%;
                    background-color: #cbd5e1;
                    background-image: url('https://ui-avatars.com/api/?name=${patientInfo.hoTen}&background=0D8ABC&color=fff');
                    background-size: cover;
                }

                /* Main Layout */
                .app-container {
                    display: flex;
                    margin-top: 64px;
                    min-height: calc(100vh - 64px);
                }

                /* Left Sidebar */
                .sidebar {
                    width: 280px;
                    background-color: var(--bg-white);
                    border-right: 1px solid var(--border);
                    padding: 2rem 1.5rem;
                    display: flex;
                    flex-direction: column;
                    position: fixed;
                    top: 64px;
                    bottom: 0;
                    overflow-y: auto;
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
                    color: white;
                    border: none;
                    border-radius: 8px;
                    padding: 0.75rem;
                    font-weight: 600;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 0.5rem;
                    transition: background-color 0.2s;
                }

                .btn-new:hover {
                    background-color: #083c8a;
                }

                /* Main Content */
                .content {
                    margin-left: 280px;
                    padding: 2rem;
                    flex-grow: 1;
                    width: calc(100% - 280px);
                }

                .page-title {
                    font-size: 1.5rem;
                    font-weight: 700;
                    margin-bottom: 1.5rem;
                }

                /* Cards Row 1 */
                .row-top {
                    display: grid;
                    grid-template-columns: 1fr 1fr 1fr;
                    gap: 1.5rem;
                    margin-bottom: 1.5rem;
                    align-items: start;
                }

                .metric-card {
                    background: linear-gradient(145deg, #ffffff, #f8fafc);
                    border: 1px solid rgba(0, 0, 0, 0.05);
                    border-radius: 16px;
                    padding: 1.75rem 1.5rem;
                    position: relative;
                    box-shadow: 0 4px 15px -5px rgba(0, 0, 0, 0.05);
                    transition: transform 0.2s, box-shadow 0.2s;
                }

                .metric-card:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 8px 25px -5px rgba(0, 0, 0, 0.1);
                }

                .metric-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 1rem;
                }

                .metric-title {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    font-size: 0.875rem;
                    font-weight: 600;
                    color: var(--text-muted);
                    text-transform: uppercase;
                }

                .metric-title.red i {
                    color: var(--danger);
                }

                .metric-title.brown i {
                    color: #8b5a2b;
                }

                .badge {
                    padding: 0.25rem 0.5rem;
                    border-radius: 4px;
                    font-size: 0.75rem;
                    font-weight: 600;
                }

                .badge.success {
                    background-color: var(--success-light);
                    color: var(--success);
                }

                .badge.warning {
                    background-color: var(--warning-light);
                    color: var(--warning);
                }

                .badge.danger {
                    background-color: var(--danger-light);
                    color: var(--danger);
                }

                .metric-value {
                    font-size: 2.5rem;
                    font-weight: 700;
                    display: flex;
                    align-items: baseline;
                    gap: 0.25rem;
                }

                .metric-unit {
                    font-size: 1rem;
                    font-weight: 500;
                    color: var(--text-muted);
                }

                .metric-desc {
                    font-size: 0.875rem;
                    color: var(--danger);
                    font-weight: 500;
                    margin-top: 0.5rem;
                }

                .metric-card.red-border {
                    border-left: 4px solid var(--danger);
                }

                .progress-bar-bg {
                    height: 16px;
                    background: #e2e8f0;
                    border-radius: 8px;
                    margin-top: 1rem;
                    overflow: hidden;
                    display: flex;
                }

                .progress-bar-fill {
                    height: 100%;
                    width: 70%;
                    background-color: #93c5fd;
                }

                .alerts-card {
                    background-color: var(--bg-white);
                    border: 1px solid var(--border);
                    border-radius: 12px;
                    padding: 1.5rem;
                }

                .alerts-title {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    font-size: 1rem;
                    font-weight: 600;
                    margin-bottom: 1rem;
                }

                .alert-item {
                    display: flex;
                    gap: 1rem;
                    margin-bottom: 1rem;
                    padding-bottom: 1rem;
                    border-bottom: 1px solid var(--border);
                }

                .alert-item:last-child {
                    margin-bottom: 0;
                    padding-bottom: 0;
                    border-bottom: none;
                }

                .alert-icon {
                    font-size: 1.25rem;
                }

                .alert-icon.danger {
                    color: var(--danger);
                }

                .alert-icon.muted {
                    color: var(--text-muted);
                }

                .alert-content p {
                    font-size: 0.875rem;
                    font-weight: 600;
                    margin-bottom: 0.25rem;
                }

                .alert-content span {
                    font-size: 0.75rem;
                    color: var(--text-muted);
                }

                /* Middle Row */
                .row-middle {
                    display: grid;
                    grid-template-columns: 2fr 1fr;
                    gap: 1.5rem;
                    margin-bottom: 1.5rem;
                }

                .card {
                    background-color: var(--bg-white);
                    border: 1px solid var(--border);
                    border-radius: 12px;
                    padding: 1.5rem;
                }

                .card-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 1.5rem;
                }

                .card-title h3 {
                    font-size: 1.125rem;
                    font-weight: 600;
                }

                .card-title p {
                    font-size: 0.875rem;
                    color: var(--text-muted);
                    margin-top: 0.25rem;
                }

                .chart-controls {
                    display: flex;
                    gap: 0.5rem;
                    align-items: center;
                }

                .chart-controls button {
                    padding: 0.25rem 0.75rem;
                    border: 1px solid var(--border);
                    background: var(--bg-white);
                    border-radius: 16px;
                    font-size: 0.75rem;
                    font-weight: 500;
                    cursor: pointer;
                }

                .chart-controls button.active {
                    background-color: var(--primary);
                    color: white;
                    border-color: var(--primary);
                }

                .date-picker {
                    padding: 0.25rem 0.5rem;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    font-size: 0.75rem;
                    color: var(--text-muted);
                    outline: none;
                    cursor: pointer;
                    background: var(--bg-white);
                }

                .date-picker:focus {
                    border-color: var(--primary);
                }

                .chart-container {
                    height: 300px;
                    position: relative;
                }

                .apt-item {
                    display: flex;
                    gap: 1rem;
                    margin-bottom: 1rem;
                    background-color: var(--bg-body);
                    padding: 0.75rem;
                    border-radius: 8px;
                }

                .apt-date {
                    background-color: #e2e8f0;
                    border-radius: 8px;
                    padding: 0.5rem;
                    text-align: center;
                    min-width: 60px;
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                }

                .apt-date span:first-child {
                    font-size: 1.25rem;
                    font-weight: 700;
                    color: var(--text-dark);
                }

                .apt-date span:last-child {
                    font-size: 0.75rem;
                    font-weight: 600;
                    color: var(--text-muted);
                    text-transform: uppercase;
                }

                .apt-info h4 {
                    font-size: 0.875rem;
                    font-weight: 600;
                    margin-bottom: 0.25rem;
                }

                .apt-info p {
                    font-size: 0.75rem;
                    color: var(--text-muted);
                    display: flex;
                    align-items: center;
                    gap: 0.25rem;
                    margin-bottom: 0.125rem;
                }

                .btn-outline {
                    display: block;
                    width: 100%;
                    padding: 0.75rem;
                    border: 1px solid var(--primary);
                    background: transparent;
                    color: var(--primary);
                    border-radius: 8px;
                    font-weight: 600;
                    text-align: center;
                    text-decoration: none;
                    margin-top: 1rem;
                    cursor: pointer;
                    transition: all 0.2s;
                }

                .btn-outline:hover {
                    background-color: var(--primary-light);
                }

                /* Table Section */
                .table-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 1.5rem;
                }

                .search-box {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    padding: 0.5rem 1rem;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    background-color: var(--bg-white);
                    width: 250px;
                }

                .search-box input {
                    border: none;
                    outline: none;
                    font-size: 0.875rem;
                    width: 100%;
                }

                .filter-icon {
                    padding: 0.5rem 0.75rem;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    cursor: pointer;
                    color: var(--text-muted);
                }

                table {
                    width: 100%;
                    border-collapse: collapse;
                }

                th,
                td {
                    padding: 1rem;
                    text-align: left;
                    border-bottom: 1px solid var(--border);
                }

                th {
                    font-size: 0.75rem;
                    font-weight: 600;
                    color: var(--text-muted);
                    text-transform: uppercase;
                }

                td {
                    font-size: 0.875rem;
                }

                .record-type {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    color: var(--primary);
                    font-weight: 500;
                }

                .status-badge {
                    padding: 0.25rem 0.5rem;
                    border-radius: 4px;
                    font-size: 0.75rem;
                    font-weight: 600;
                    background-color: #e2e8f0;
                    color: var(--text-muted);
                }

                .status-badge.action {
                    background-color: var(--primary-light);
                    color: var(--primary);
                }

                .action-link {
                    color: var(--primary);
                    text-decoration: none;
                    font-weight: 600;
                    font-size: 0.875rem;
                }

                /* Footer */
                .footer {
                    margin-top: 2rem;
                    padding-top: 1rem;
                    border-top: 1px solid var(--border);
                    display: flex;
                    justify-content: space-between;
                    font-size: 0.75rem;
                    color: var(--text-muted);
                }

                .footer-links {
                    display: flex;
                    gap: 1rem;
                }

                .footer-links a {
                    color: var(--text-muted);
                    text-decoration: none;
                }

                /* Modal Styles */
                .modal-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: rgba(0, 0, 0, 0.5);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 1000;
                    opacity: 0;
                    pointer-events: none;
                    transition: opacity 0.3s;
                }

                .modal-overlay.active {
                    opacity: 1;
                    pointer-events: auto;
                }

                .modal {
                    background: var(--bg-white);
                    border-radius: 12px;
                    width: 100%;
                    max-width: 500px;
                    padding: 2rem;
                    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
                    transform: translateY(-20px);
                    transition: transform 0.3s;
                }

                .modal-overlay.active .modal {
                    transform: translateY(0);
                }

                .modal-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 1.5rem;
                }

                .modal-title {
                    font-size: 1.25rem;
                    font-weight: 600;
                }

                .close-btn {
                    background: none;
                    border: none;
                    font-size: 1.25rem;
                    cursor: pointer;
                    color: var(--text-muted);
                }

                .form-group {
                    margin-bottom: 1rem;
                }

                .form-group label {
                    display: block;
                    font-size: 0.875rem;
                    font-weight: 500;
                    margin-bottom: 0.5rem;
                    color: var(--text-dark);
                }

                .form-control {
                    width: 100%;
                    padding: 0.75rem 1rem;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    font-size: 0.875rem;
                    outline: none;
                }

                .form-control:focus {
                    border-color: var(--primary);
                }

                .form-row {
                    display: flex;
                    gap: 1rem;
                }

                .form-row .form-group {
                    flex: 1;
                }

                .modal-footer {
                    margin-top: 2rem;
                    display: flex;
                    justify-content: flex-end;
                    gap: 1rem;
                }

                .btn {
                    padding: 0.75rem 1.5rem;
                    border-radius: 8px;
                    font-weight: 600;
                    cursor: pointer;
                    border: none;
                    font-size: 0.875rem;
                }

                .btn-cancel {
                    background: var(--bg-body);
                    color: var(--text-dark);
                }

                .btn-save {
                    background: var(--primary);
                    color: white;
                }

                /* === AI Analysis Card === */
                .ai-card {
                    background: linear-gradient(145deg, #0f172a, #1e293b);
                    border: 1px solid rgba(139, 92, 246, 0.3);
                    border-radius: 20px;
                    padding: 1.5rem;
                    color: #f8fafc;
                    grid-column: 1 / -1;
                    position: relative;
                    overflow: hidden;
                    box-shadow: 0 10px 30px -5px rgba(99, 102, 241, 0.2);
                    transition: transform 0.3s ease, box-shadow 0.3s ease;
                }

                .ai-card:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 15px 35px -5px rgba(99, 102, 241, 0.3);
                }

                .ai-card::before {
                    content: '';
                    position: absolute;
                    top: -20%;
                    left: -10%;
                    width: 50%;
                    height: 150%;
                    background: radial-gradient(ellipse at center, rgba(139, 92, 246, 0.15) 0%, transparent 70%);
                    transform: rotate(-15deg);
                    pointer-events: none;
                }

                .ai-card-header {
                    display: flex;
                    align-items: center;
                    gap: 1rem;
                    margin-bottom: 1.25rem;
                }

                .ai-icon {
                    width: 44px;
                    height: 44px;
                    border-radius: 12px;
                    background: linear-gradient(135deg, #8b5cf6, #3b82f6);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 1.25rem;
                    color: white;
                    box-shadow: 0 8px 16px rgba(139, 92, 246, 0.3);
                }

                .ai-card-header h3 {
                    font-size: 1.15rem;
                    font-weight: 700;
                    background: linear-gradient(to right, #ffffff, #cbd5e1);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    margin-bottom: 0.25rem;
                }

                .ai-card-header span {
                    font-size: 0.8rem;
                    color: #94a3b8;
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                }

                .ai-risk-section {
                    display: flex;
                    align-items: center;
                    gap: 1.75rem;
                    margin-bottom: 1.5rem;
                    background: rgba(255, 255, 255, 0.03);
                    padding: 1.25rem;
                    border-radius: 16px;
                    border: 1px solid rgba(255, 255, 255, 0.05);
                }

                .ai-risk-score {
                    font-size: 2.75rem;
                    font-weight: 800;
                    line-height: 1;
                    text-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
                }

                .ai-risk-score.safe {
                    color: #34d399;
                }

                .ai-risk-score.medium {
                    color: #fbbf24;
                }

                .ai-risk-score.high {
                    color: #fb923c;
                }

                .ai-risk-score.danger {
                    color: #f87171;
                }

                .ai-risk-bar {
                    flex: 1;
                    height: 12px;
                    background: rgba(255, 255, 255, 0.1);
                    border-radius: 6px;
                    overflow: hidden;
                    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2);
                }

                .ai-risk-fill {
                    height: 100%;
                    border-radius: 6px;
                    transition: width 1.5s cubic-bezier(0.4, 0, 0.2, 1);
                    position: relative;
                }

                .ai-risk-fill::after {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.4) 50%, rgba(255, 255, 255, 0) 100%);
                    animation: shimmer 2s infinite;
                }

                @keyframes shimmer {
                    0% {
                        transform: translateX(-100%);
                    }

                    100% {
                        transform: translateX(100%);
                    }
                }

                .ai-risk-fill.safe {
                    background: linear-gradient(90deg, #10b981, #34d399);
                    box-shadow: 0 0 10px rgba(52, 211, 153, 0.5);
                }

                .ai-risk-fill.medium {
                    background: linear-gradient(90deg, #d97706, #fbbf24);
                    box-shadow: 0 0 10px rgba(251, 191, 36, 0.5);
                }

                .ai-risk-fill.high {
                    background: linear-gradient(90deg, #ea580c, #fb923c);
                    box-shadow: 0 0 10px rgba(251, 146, 60, 0.5);
                }

                .ai-risk-fill.danger {
                    background: linear-gradient(90deg, #dc2626, #f87171);
                    box-shadow: 0 0 10px rgba(248, 113, 113, 0.5);
                }

                .ai-risk-label {
                    font-size: 0.85rem;
                    padding: 0.4rem 1.25rem;
                    border-radius: 20px;
                    font-weight: 700;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }

                .ai-risk-label.safe {
                    background: rgba(52, 211, 153, 0.15);
                    color: #34d399;
                    border: 1px solid rgba(52, 211, 153, 0.3);
                }

                .ai-risk-label.medium {
                    background: rgba(251, 191, 36, 0.15);
                    color: #fbbf24;
                    border: 1px solid rgba(251, 191, 36, 0.3);
                }

                .ai-risk-label.high {
                    background: rgba(251, 146, 60, 0.15);
                    color: #fb923c;
                    border: 1px solid rgba(251, 146, 60, 0.3);
                }

                .ai-risk-label.danger {
                    background: rgba(248, 113, 113, 0.15);
                    color: #f87171;
                    border: 1px solid rgba(248, 113, 113, 0.3);
                }

                .ai-detail {
                    font-size: 0.95rem;
                    color: #cbd5e1;
                    line-height: 1.6;
                    margin-bottom: 1.25rem;
                    padding: 1rem 1.25rem;
                    background: rgba(139, 92, 246, 0.05);
                    border-radius: 12px;
                    border-left: 4px solid #8b5cf6;
                }

                .ai-recommendations {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 0.75rem;
                    margin-top: 0.75rem;
                }

                .ai-rec-tag {
                    font-size: 0.85rem;
                    padding: 0.6rem 1.2rem;
                    background: rgba(30, 41, 59, 0.6);
                    border: 1px solid rgba(139, 92, 246, 0.3);
                    border-radius: 12px;
                    color: #e2e8f0;
                    display: flex;
                    align-items: flex-start;
                    gap: 0.75rem;
                    transition: all 0.2s;
                }

                .ai-rec-tag:hover {
                    background: rgba(139, 92, 246, 0.15);
                    border-color: rgba(139, 92, 246, 0.6);
                    transform: translateY(-2px);
                }

                .ai-disclaimer {
                    margin-top: 1.5rem;
                    padding-top: 1rem;
                    border-top: 1px dashed rgba(255, 255, 255, 0.1);
                    font-size: 0.75rem;
                    color: #64748b;
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                }



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
        </head>

        <body>

            <!-- Top Navigation -->
            <nav class="top-nav">
                <div class="nav-left">
                    <div class="logo">DiabCare</div>
                    <div class="nav-links">
                        <a href="patient-dashboard" class="active">Tổng quan</a>
                        <a href="patient-medical-profile">Hồ sơ sức khỏe</a>
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
                        <div class="profile-role">Bệnh nhân - ĐTĐ ${patientInfo.loaiTieuDuong != null ?
                            patientInfo.loaiTieuDuong : 'Type 2'}</div>
                    </div>

                    <nav class="sidebar-menu">
                        <a href="patient-dashboard" class="menu-btn active"><i class="fas fa-chart-pie"></i> Tổng
                            quan</a>
                        <a href="patient-medical-profile" class="menu-btn"><i class="fas fa-file-medical"></i> Xem bệnh
                            án cá nhân</a>
                        <a href="#" class="menu-btn"><i class="far fa-calendar-alt"></i> Xem lịch khám</a>
                        <a href="patient-prescriptions" class="menu-btn"><i class="fas fa-pills"></i> Đơn thuốc</a>
                        <a href="patient-diet" class="menu-btn"><i class="fas fa-utensils"></i> Thực đơn AI</a>
                        <a href="#" class="menu-btn"><i class="fas fa-chart-line"></i> Biểu đồ tiến triển</a>
                        <a href="#" class="menu-btn"><i class="fas fa-history"></i> Lịch sử cảnh báo</a>
                    </nav>

                    <div class="sidebar-bottom">
                        <button class="btn-new"><i class="fas fa-plus"></i> Thêm bản ghi mới</button>
                        <a href="#" class="menu-btn"><i class="far fa-question-circle"></i> Hỗ trợ</a>
                        <a href="#" class="menu-btn"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
                    </div>
                </aside>

                <!-- Main Content -->
                <main class="content" style="box-shadow: 0px 4px 30px rgba(0, 0, 0, 0.1); background-color: var(--bg-body); border-radius: 12px; margin-top: 1rem; margin-right: 1rem; margin-bottom: 1rem; width: calc(100% - 280px - 2rem);">
                    <h1 class="page-title">Tổng quan sức khỏe</h1>

                    <!-- Top Cards -->
                    <div class="row-top">
                        <!-- Heart Rate -->
                        <div
                            class="metric-card ${latestHeartRate != null && (latestHeartRate < 60 || latestHeartRate > 100) ? 'red-border' : ''}">
                            <div class="metric-header">
                                <div class="metric-title red"><i class="far fa-heart"></i> NHỊP TIM</div>
                                <c:choose>
                                    <c:when test="${latestHeartRate == null}">
                                        <span class="badge" style="background: #e2e8f0; color: #64748b;">--</span>
                                    </c:when>
                                    <c:when test="${latestHeartRate < 60}">
                                        <span class="badge warning" style="background: #fef3c7; color: #d97706;">NHỊP
                                            CHẬM</span>
                                    </c:when>
                                    <c:when test="${latestHeartRate <= 100}">
                                        <span class="badge success">BÌNH THƯỜNG</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge danger">NHỊP NHANH</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="metric-value">
                                ${latestHeartRate != null ? latestHeartRate : '--'} <span class="metric-unit">BPM</span>
                            </div>
                            <c:if test="${latestHeartRate != null}">
                                <div class="metric-desc">
                                    <c:choose>
                                        <c:when test="${latestHeartRate < 60}">Dưới mức tiêu chuẩn (60-100 BPM).
                                        </c:when>
                                        <c:when test="${latestHeartRate <= 100}">Trong khoảng an toàn.</c:when>
                                        <c:otherwise>Cao hơn mức tiêu chuẩn.</c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>
                        </div>

                        <!-- Blood Pressure -->
                        <div
                            class="metric-card ${latestSystolic != null && (latestSystolic > 120 || latestDiastolic > 80 || latestSystolic < 90) ? 'red-border' : ''}">
                            <div class="metric-header">
                                <div class="metric-title brown"><i class="fas fa-stethoscope"></i> HUYẾT ÁP</div>
                                <c:choose>
                                    <c:when test="${latestSystolic == null || latestDiastolic == null}">
                                        <span class="badge" style="background: #e2e8f0; color: #64748b;">--</span>
                                    </c:when>
                                    <c:when test="${latestSystolic < 90 || latestDiastolic < 60}">
                                        <span class="badge warning" style="background: #fef3c7; color: #d97706;">HUYẾT
                                            ÁP THẤP</span>
                                    </c:when>
                                    <c:when test="${latestSystolic <= 120 && latestDiastolic <= 80}">
                                        <span class="badge success">BÌNH THƯỜNG</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge danger">CẢNH BÁO CAO</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="metric-value">
                                ${latestSystolic != null ? latestSystolic : '--'}/${latestDiastolic != null ?
                                latestDiastolic : '--'} <span class="metric-unit">mmHg</span>
                            </div>
                            <c:if test="${latestSystolic != null && latestDiastolic != null}">
                                <div class="metric-desc">
                                    <c:choose>
                                        <c:when test="${latestSystolic < 90 || latestDiastolic < 60}">Cần theo dõi nguy
                                            cơ tụt huyết áp.</c:when>
                                        <c:when test="${latestSystolic <= 120 && latestDiastolic <= 80}">Chỉ số lý
                                            tưởng.</c:when>
                                        <c:otherwise>Cần theo dõi, có nguy cơ tăng huyết áp.</c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>
                        </div>

                        <!-- Recent Alerts -->
                        <div class="alerts-card">
                            <div class="alerts-title">
                                <i class="far fa-bell" style="color: var(--danger);"></i> Cảnh báo gần đây
                            </div>
                            <c:choose>
                                <c:when test="${not empty alerts}">
                                    <c:forEach var="alert" items="${alerts}">
                                        <div class="alert-item">
                                            <div
                                                class="alert-icon ${alert.mucDo == 'nguy_hiem' || alert.mucDo == 'cao' ? 'danger' : 'muted'}">
                                                <i
                                                    class="fas ${alert.mucDo == 'nguy_hiem' || alert.mucDo == 'cao' ? 'fa-exclamation-triangle' : 'fa-info-circle'}"></i>
                                            </div>
                                            <div class="alert-content">
                                                <p>${alert.tieuDe}</p>
                                                <span>${alert.noiDung}</span>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div class="alert-item">
                                        <div class="alert-content">
                                            <p>Không có cảnh báo nào gần đây.</p>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- AI Analysis Card -->
                    <c:if test="${not empty aiAnalysis}">
                        <div class="ai-card" style="margin-bottom: 2rem;">
                            <div class="ai-card-header">
                                <div class="ai-icon"><i class="fas fa-sparkles"></i></div>
                                <div>
                                    <h3>AI Phân tích sức khỏe</h3>
                                    <span><i class="far fa-clock"></i> Cập nhật: ${aiAnalysis.thoiGianPhanTich != null ?
                                        aiAnalysis.thoiGianPhanTich.toString().substring(0,16) : 'N/A'} • <i
                                            class="fas fa-cube" style="margin-left: 0.25rem;"></i> Model:
                                        ${aiAnalysis.modelVersion}</span>
                                </div>
                            </div>
                            <div class="ai-risk-section">
                                <c:set var="riskClass"
                                    value="${aiAnalysis.mucCanhBao == 'an_toan' ? 'safe' : (aiAnalysis.mucCanhBao == 'trung_binh' ? 'medium' : (aiAnalysis.mucCanhBao == 'cao' ? 'high' : 'danger'))}" />
                                <div class="ai-risk-score ${riskClass}">${aiAnalysis.diemNguyCo}<span
                                        style="font-size:1.25rem;color:#64748b;font-weight:600;">/100</span></div>
                                <div style="flex:1">
                                    <div class="ai-risk-bar">
                                        <div class="ai-risk-fill ${riskClass}"
                                            style="width: ${aiAnalysis.diemNguyCo}%;">
                                        </div>
                                    </div>
                                    <div style="display:flex;justify-content:space-between;margin-top:0.75rem">
                                        <span class="ai-risk-label ${riskClass}">
                                            ${aiAnalysis.mucCanhBao == 'an_toan' ? 'AN TOÀN' : (aiAnalysis.mucCanhBao ==
                                            'trung_binh' ? 'TRUNG BÌNH' : (aiAnalysis.mucCanhBao == 'cao' ? 'NGUY CƠ CAO' : 'NGUY HIỂM'))}
                                        </span>
                                        <!-- <c:if test="${aiAnalysis.doTinCay != null}">
                                            <span style="font-size:0.8rem;color:#94a3b8;font-weight:500;">Độ tin cậy:
                                                ${aiAnalysis.doTinCay}</span>
                                        </c:if> -->
                                    </div>
                                </div>
                            </div>
                            <div class="ai-detail">${aiAnalysis.phanTichChiTiet}</div>
                            <c:if test="${not empty aiAnalysis.khuyenNghi}">
                                <h4
                                    style="font-size:1.05rem;font-weight:600;color:#f8fafc;margin-bottom:1rem;display:flex;align-items:center;gap:0.5rem;">
                                    <i class="fas fa-magic" style="color:#a78bfa;"></i> Gợi ý từ AI
                                </h4>
                                <div class="ai-recommendations" id="aiRecommendations"></div>
                            </c:if>
                            <div class="ai-disclaimer"><i class="fas fa-exclamation-triangle"
                                    style="color: #fbbf24;"></i> Kết quả phân tích chỉ mang tính tham khảo. Mọi quyết
                                định y tế cần được bác sĩ xác nhận.</div>
                        </div>
                    </c:if>

                    <!-- Middle Cards -->
                    <div class="row-middle">
                        <!-- Health Trends Chart -->
                        <div class="card">
                            <div class="card-header">
                                <div class="card-title">
                                    <h3>Biểu đồ theo dõi chỉ số</h3>
                                    <p>Chi tiết các lần đo: Đường huyết, Nhịp tim, Huyết áp</p>
                                </div>
                                <div class="chart-controls" style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                                    <button class="active" id="btn-7days">7 Ngày</button>
                                    <button id="btn-30days">30 Ngày</button>
                                    <div style="display: flex; align-items: center; gap: 0.5rem; background: var(--bg-white); padding: 0.25rem 0.5rem; border-radius: 6px; border: 1px solid var(--border);">
                                        <input type="date" class="date-picker" id="startDatePicker" title="Từ ngày" style="border: none; background: transparent; padding: 0; outline: none; font-family: inherit; color: var(--text-dark);">
                                        <span style="color: var(--text-muted); font-weight: 500;">-</span>
                                        <input type="date" class="date-picker" id="endDatePicker" title="Đến ngày" style="border: none; background: transparent; padding: 0; outline: none; font-family: inherit; color: var(--text-dark);">
                                    </div>
                                </div>
                            </div>
                            <div class="chart-container" style="height: 350px;">
                                <canvas id="trendsChart"></canvas>
                            </div>
                        </div>

                        <!-- Appointments -->
                        <div class="card">
                            <div class="card-header" style="margin-bottom: 1rem;">
                                <div class="card-title">
                                    <h3><i class="far fa-calendar-alt" style="color: var(--primary);"></i> Lịch hẹn</h3>
                                </div>
                            </div>
                            <c:choose>
                                <c:when test="${not empty appointments}">
                                    <c:forEach var="appt" items="${appointments}">
                                        <div class="apt-item">
                                            <div class="apt-date">
                                                <span>${appt.thoiGianHen.toString().substring(8,10)}</span>
                                                <span>THG ${appt.thoiGianHen.toString().substring(5,7)}</span>
                                            </div>
                                            <div class="apt-info">
                                                <h4>${appt.tieuDe}</h4>
                                                <p><i class="far fa-clock"></i>
                                                    ${appt.thoiGianHen.toString().substring(11, 16)}</p>
                                                <p><i class="fas fa-map-marker-alt"></i> ${appt.diaDiem}</p>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <p style="padding: 1rem; color: var(--text-muted);">Không có lịch hẹn nào sắp tới.
                                    </p>
                                </c:otherwise>
                            </c:choose>

                            <button class="btn-outline">Xem toàn bộ lịch</button>
                        </div>
                    </div>

                    <!-- Bottom Table -->
                    <div class="card">
                        <div class="table-header">
                            <h3>Lịch sử khám bệnh</h3>
                            <div style="display: flex; gap: 0.5rem;">
                                <div class="search-box">
                                    <i class="fas fa-search" style="color: var(--text-muted);"></i>
                                    <input type="text" placeholder="Tìm kiếm hồ sơ...">
                                </div>
                                <div class="filter-icon">
                                    <i class="fas fa-filter"></i>
                                </div>
                            </div>
                        </div>

                        <table>
                            <thead>
                                <tr>
                                    <th>NGÀY</th>
                                    <th>LOẠI HỒ SƠ</th>
                                    <th>BÁC SĨ</th>
                                    <th>TRẠNG THÁI</th>
                                    <th>THAO TÁC</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty medicalDocuments}">
                                        <c:forEach var="doc" items="${medicalDocuments}">
                                            <tr>
                                                <td>${doc.ngayThucHien}</td>
                                                <td>
                                                    <div class="record-type">
                                                        <i class="far fa-file-pdf"></i> ${doc.loaiTaiLieu}
                                                    </div>
                                                </td>
                                                <td>Bác sĩ</td>
                                                <td>
                                                    <span
                                                        class="status-badge ${doc.trangThai == 'hoan_thanh' ? '' : 'action'}">
                                                        ${doc.trangThai == 'hoan_thanh' ? 'HOÀN THÀNH' : (doc.trangThai
                                                        == 'can_xu_ly' ? 'CẦN XỬ LÝ' : 'ĐÃ HỦY')}
                                                    </span>
                                                </td>
                                                <td><a href="#" class="action-link">Chi tiết</a></td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" style="text-align: center;">Không có tài liệu nào.</td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Footer -->
                    <div class="footer">
                        <div>© 2024 DiabCare. All rights reserved. Confidential Medical Data.</div>
                        <div class="footer-links">
                            <a href="#">Chính sách bảo mật</a>
                            <a href="#">Điều khoản dịch vụ</a>
                            <a href="#">Tuân thủ HIPAA</a>
                            <a href="#">Liên hệ hỗ trợ</a>
                        </div>
                    </div>

                </main>
            </div>

            <!-- Modal Thêm bản ghi mới -->
            <div class="modal-overlay" id="recordModal">
                <div class="modal">
                    <div class="modal-header">
                        <h3 class="modal-title">Ghi chỉ số sức khỏe</h3>
                        <button class="close-btn" id="closeModalBtn"><i class="fas fa-times"></i></button>
                    </div>
                    <form action="logData" method="POST" id="newRecordForm">
                        <div id="formErrorMsg" style="color: var(--danger); font-size: 0.875rem; font-weight: 500; margin-bottom: 1rem; display: none; background: var(--danger-light); padding: 0.75rem; border-radius: 8px; border: 1px solid #fca5a5;"></div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>Đường huyết</label>
                                <div style="display: flex; gap: 0.5rem;">
                                    <input type="number" step="0.1" name="duong_huyet" id="duong_huyet" class="form-control" placeholder="VD: 110">
                                    <select name="don_vi_duong_huyet" id="don_vi_duong_huyet" class="form-control" style="width: 100px;">
                                        <option value="mg/dL">mg/dL</option>
                                        <option value="mmol/L">mmol/L</option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label>Nhịp tim (BPM)</label>
                                <input type="number" name="nhip_tim" id="nhip_tim" class="form-control" placeholder="VD: 75">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>H/áp Tâm thu (mmHg)</label>
                                <input type="number" name="huyet_ap_thu" id="huyet_ap_thu" class="form-control" placeholder="VD: 120">
                            </div>
                            <div class="form-group">
                                <label>H/áp Tâm trương (mmHg)</label>
                                <input type="number" name="huyet_ap_truong" id="huyet_ap_truong" class="form-control" placeholder="VD: 80">
                            </div>
                        </div>
                        <div class="form-group">
                            <label>Thời điểm đo</label>
                            <select class="form-control" name="thoi_diem">
                                <option value="luc_doi">Lúc đói (Sáng sớm)</option>
                                <option value="sau_an_1h">Sau ăn 1 giờ</option>
                                <option value="sau_an_2h">Sau ăn 2 giờ</option>
                                <option value="truoc_ngu">Trước khi ngủ</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Triệu chứng</label>
                            <div style="display: flex; flex-direction: column; gap: 0.5rem; margin-top: 0.5rem;">
                                <label
                                    style="display: flex; align-items: center; gap: 0.5rem; font-weight: normal; cursor: pointer;">
                                    <input type="checkbox" name="chest_pain" value="1"
                                        style="width: 16px; height: 16px;"> Đau tức ngực
                                </label>
                                <label
                                    style="display: flex; align-items: center; gap: 0.5rem; font-weight: normal; cursor: pointer;">
                                    <input type="checkbox" name="dizziness" value="1"
                                        style="width: 16px; height: 16px;"> Hoa mắt, chóng mặt
                                </label>
                                <label
                                    style="display: flex; align-items: center; gap: 0.5rem; font-weight: normal; cursor: pointer;">
                                    <input type="checkbox" name="fatigue" value="1" style="width: 16px; height: 16px;">
                                    Mệt mỏi kéo dài
                                </label>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-cancel" id="cancelModalBtn">Hủy</button>
                            <button type="submit" class="btn btn-save">Lưu bản ghi</button>
                        </div>
                    </form>
                </div>
            </div>

            <jsp:include page="chatbot.jsp" />

            <script>
                // Chart.js configuration for Health Trends
                const ctx = document.getElementById('trendsChart').getContext('2d');

                let currentUnit = 'mg/dL'; // default

                function processData(dataList) {
                    const result = {
                        labels: [],
                        glucose: [],
                        heartRate: [],
                        systolic: [],
                        diastolic: []
                    };
                    dataList.forEach(item => {
                        if (item.time) {
                            const d = new Date(item.time);
                            result.labels.push(d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }) + ' ' + d.getHours() + 'h');
                        } else {
                            result.labels.push('');
                        }

                        // Convert glucose if needed
                        let glucoseVal = item.glucose;
                        if (glucoseVal !== null && currentUnit === 'mmol/L') {
                            glucoseVal = (glucoseVal / 18.0).toFixed(1);
                        }

                        // Nếu null sẽ vẽ đứt quãng hoặc không vẽ
                        result.glucose.push(glucoseVal);
                        result.heartRate.push(item.hr);
                        result.systolic.push(item.sys);
                        result.diastolic.push(item.dia);
                    });
                    return result;
                }

                const dbData = ${ chartDataJson != null ? chartDataJson : '[]'};
                const realChartData = processData(dbData);

                let trendsChart = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: realChartData.labels,
                        datasets: [
                            {
                                label: 'Đường huyết (mg/dL)',
                                data: realChartData.glucose,
                                borderColor: '#0a4aa8', // Primary blue
                                backgroundColor: '#0a4aa8',
                                spanGaps: true, // Nối liền các điểm null
                                tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                            },
                            {
                                label: 'Nhịp tim (BPM)',
                                data: realChartData.heartRate,
                                borderColor: '#ef4444', // Danger red
                                backgroundColor: '#ef4444',
                                spanGaps: true,
                                tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                            },
                            {
                                label: 'Huyết áp tâm thu (mmHg)',
                                data: realChartData.systolic,
                                borderColor: '#f59e0b', // Warning orange
                                backgroundColor: '#f59e0b',
                                spanGaps: true,
                                borderDash: [5, 5],
                                tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                            },
                            {
                                label: 'Huyết áp tâm trương (mmHg)',
                                data: realChartData.diastolic,
                                borderColor: '#10b981', // Success green
                                backgroundColor: '#10b981',
                                spanGaps: true,
                                borderDash: [5, 5],
                                tension: 0.4, borderWidth: 2, pointRadius: 4, pointHoverRadius: 6
                            }
                        ]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                display: true,
                                position: 'top',
                                labels: {
                                    usePointStyle: true,
                                    boxWidth: 8
                                }
                            },
                            tooltip: {
                                enabled: true,
                                mode: 'index',
                                intersect: false
                            }
                        },
                        scales: {
                            x: {
                                grid: {
                                    display: false,
                                    drawBorder: false
                                },
                                ticks: {
                                    display: true,
                                    font: { size: 11 }
                                }
                            },
                            y: {
                                grid: {
                                    color: '#e2e8f0',
                                    borderDash: [3, 3]
                                },
                                ticks: {
                                    display: true,
                                    stepSize: 20
                                },
                                min: 50,
                                max: 350
                            }
                        }
                    }
                });

                // Interactivity for controls
                const btn7 = document.getElementById('btn-7days');
                const btn30 = document.getElementById('btn-30days');
                const startDatePicker = document.getElementById('startDatePicker');
                const endDatePicker = document.getElementById('endDatePicker');

                function updateChart(dataset) {
                    trendsChart.data.labels = dataset.labels;
                    trendsChart.data.datasets[0].data = dataset.glucose;
                    trendsChart.data.datasets[0].label = 'Đường huyết (' + currentUnit + ')';
                    trendsChart.data.datasets[1].data = dataset.heartRate;
                    trendsChart.data.datasets[2].data = dataset.systolic;
                    trendsChart.data.datasets[3].data = dataset.diastolic;
                    trendsChart.update();
                }

                function filterByDays(data, days) {
                    if (!data || data.length === 0) return [];

                    // Tìm ngày mới nhất trong data làm mốc
                    let maxDate = new Date(0);
                    data.forEach(item => {
                        if (item.time) {
                            const tDate = new Date(item.time.replace(' ', 'T'));
                            if (tDate > maxDate) maxDate = tDate;
                        }
                    });

                    if (maxDate.getTime() === 0) maxDate = new Date();

                    const pastDate = new Date(maxDate);
                    pastDate.setDate(maxDate.getDate() - days + 1);
                    pastDate.setHours(0, 0, 0, 0);

                    return data.filter(item => {
                        if (!item.time) return false;
                        const tDate = new Date(item.time.replace(' ', 'T'));
                        return tDate >= pastDate;
                    });
                }

                function filterByDateRange() {
                    const startVal = startDatePicker.value;
                    const endVal = endDatePicker.value;
                    
                    if (startVal || endVal) {
                        btn7.classList.remove('active');
                        btn30.classList.remove('active');
                        
                        let filteredData = dbData.filter(item => {
                            if (!item.time) return false;
                            const itemDate = item.time.substring(0, 10); // YYYY-MM-DD
                            
                            let isValid = true;
                            if (startVal && itemDate < startVal) isValid = false;
                            if (endVal && itemDate > endVal) isValid = false;
                            
                            return isValid;
                        });
                        updateChart(processData(filteredData));
                    } else {
                        btn7.click();
                    }
                }

                btn7.addEventListener('click', () => {
                    btn7.classList.add('active');
                    btn30.classList.remove('active');
                    startDatePicker.value = '';
                    endDatePicker.value = '';
                    const filteredData = filterByDays(dbData, 7);
                    updateChart(processData(filteredData.length > 0 ? filteredData : dbData));
                });

                btn30.addEventListener('click', () => {
                    btn30.classList.add('active');
                    btn7.classList.remove('active');
                    startDatePicker.value = '';
                    endDatePicker.value = '';
                    const filteredData = filterByDays(dbData, 30);
                    updateChart(processData(filteredData.length > 0 ? filteredData : dbData));
                });

                startDatePicker.addEventListener('change', filterByDateRange);
                endDatePicker.addEventListener('change', filterByDateRange);

                // Kích hoạt mặc định xem 7 ngày gần nhất
                setTimeout(() => {
                    btn7.click();
                }, 100);

                // Existing chatbot logic
                const chatbotFab = document.getElementById('chatbotFab');
                const btnNewRecord = document.querySelector('.btn-new');
                const closeModalBtn = document.getElementById('closeModalBtn');
                const cancelModalBtn = document.getElementById('cancelModalBtn');

                function openModal() {
                    recordModal.classList.add('active');
                }

                function closeModal() {
                    recordModal.classList.remove('active');
                    const formErrorMsg = document.getElementById('formErrorMsg');
                    if (formErrorMsg) formErrorMsg.style.display = 'none';
                }

                if (btnNewRecord) btnNewRecord.addEventListener('click', openModal);
                if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
                if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeModal);

                // Đóng modal khi bấm ra ngoài
                recordModal.addEventListener('click', (e) => {
                    if (e.target === recordModal) {
                        closeModal();
                    }
                });

                // Form validation
                const newRecordForm = document.getElementById('newRecordForm');
                const formErrorMsg = document.getElementById('formErrorMsg');

                if (newRecordForm) {
                    newRecordForm.addEventListener('submit', function(e) {
                        const dh = document.getElementById('duong_huyet').value;
                        const unit = document.getElementById('don_vi_duong_huyet').value;
                        const hr = document.getElementById('nhip_tim').value;
                        const sys = document.getElementById('huyet_ap_thu').value;
                        const dia = document.getElementById('huyet_ap_truong').value;

                        let error = '';

                        // Rule 1: At least one metric
                        if (!dh && !hr && !sys && !dia) {
                            error = 'Vui lòng nhập ít nhất một chỉ số sức khỏe.';
                        } 
                        // Rule 2: Blood Pressure logic
                        else if ((sys && !dia) || (!sys && dia)) {
                            error = 'Vui lòng nhập đầy đủ cả Huyết áp Tâm thu và Tâm trương.';
                        }
                        // Rule 3: Valid BP range
                        else if (sys && dia) {
                            if (parseFloat(sys) <= parseFloat(dia)) {
                                error = 'Huyết áp Tâm thu phải lớn hơn Huyết áp Tâm trương.';
                            } else if (sys < 60 || sys > 250 || dia < 30 || dia > 150) {
                                error = 'Chỉ số Huyết áp không hợp lệ. Vui lòng kiểm tra lại.';
                            }
                        }
                        // Rule 4: Valid Heart rate
                        if (!error && hr && (hr < 30 || hr > 250)) {
                            error = 'Chỉ số Nhịp tim không hợp lệ.';
                        }
                        // Rule 5: Valid Glucose
                        if (!error && dh) {
                            let dhVal = parseFloat(dh);
                            if (dhVal <= 0) {
                                error = 'Đường huyết phải lớn hơn 0.';
                            } else if (unit === 'mg/dL' && (dhVal < 10 || dhVal > 1000)) {
                                error = 'Chỉ số Đường huyết (mg/dL) có vẻ không hợp lệ.';
                            } else if (unit === 'mmol/L' && (dhVal < 0.5 || dhVal > 55)) {
                                error = 'Chỉ số Đường huyết (mmol/L) có vẻ không hợp lệ.';
                            }
                        }

                        
                        if (error) {
                            e.preventDefault(); // Dừng submit
                            formErrorMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + error;
                            formErrorMsg.style.display = 'block';

                            
                            // Lắc nhẹ modal để báo lỗi
                            const modal = document.querySelector('.modal');
                            modal.animate([
                                { transform: 'translateX(0)' },
                                { transform: 'translateX(-5px)' },
                                { transform: 'translateX(5px)' },
                                { transform: 'translateX(0)' }
                            ], { duration: 300 });
                        } else {
                            formErrorMsg.style.display = 'none';
                        }
                    });
                }

                // ==================== AI RECOMMENDATIONS PARSING ====================
                (function () {
                    const recContainer = document.getElementById('aiRecommendations');
                    if (recContainer) {
                        try {
                            const rawRec = `${aiAnalysis.khuyenNghi}`;
                            if (rawRec && rawRec !== 'null' && rawRec.trim().startsWith('[')) {
                                const recs = JSON.parse(rawRec);
                                recs.forEach(function (rec) {
                                    const tag = document.createElement('span');
                                    tag.className = 'ai-rec-tag';
                                    tag.innerHTML = '<i class="fas fa-check-circle" style="color:#a78bfa;"></i> <span>' + rec + '</span>';
                                    recContainer.appendChild(tag);
                                });
                            }
                        } catch (e) { console.log('AI rec parse error:', e); }
                    }
                })();

            </script>
        </body>

        </html>