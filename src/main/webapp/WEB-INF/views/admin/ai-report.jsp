<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo AI - DiabCare</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        * { font-family: 'Inter', sans-serif; }
        body { background: #0f172a; color: #e2e8f0; min-height: 100vh; }

        .report-container { max-width: 900px; margin: 0 auto; padding: 2rem; }

        .report-header {
            text-align: center;
            margin-bottom: 2rem;
            padding: 2rem;
            background: linear-gradient(135deg, rgba(99,102,241,0.15), rgba(139,92,246,0.1));
            border: 1px solid rgba(99,102,241,0.2);
            border-radius: 16px;
        }
        .report-header h1 {
            font-size: 1.75rem;
            font-weight: 700;
            background: linear-gradient(135deg, #a5b4fc, #c4b5fd);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 0.5rem;
        }
        .report-header p { color: #94a3b8; font-size: 0.875rem; }

        .patient-summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }
        .summary-card {
            background: rgba(255,255,255,0.03);
            border: 1px solid rgba(255,255,255,0.06);
            border-radius: 12px;
            padding: 1.25rem;
        }
        .summary-card .label {
            font-size: 0.7rem;
            text-transform: uppercase;
            color: #64748b;
            letter-spacing: 0.05em;
            margin-bottom: 0.5rem;
        }
        .summary-card .value {
            font-size: 1.125rem;
            font-weight: 600;
            color: #f1f5f9;
        }

        .ai-report-content {
            background: rgba(255,255,255,0.03);
            border: 1px solid rgba(99,102,241,0.2);
            border-radius: 16px;
            padding: 2rem;
            margin-bottom: 2rem;
            line-height: 1.8;
            font-size: 0.9375rem;
        }
        .ai-report-content h2, .ai-report-content h3 {
            color: #a5b4fc;
            margin-top: 1.5rem;
            margin-bottom: 0.75rem;
        }
        .ai-report-content strong { color: #f1f5f9; }
        .ai-report-content ul { padding-left: 1.5rem; }
        .ai-report-content li { margin-bottom: 0.5rem; }

        .report-actions {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-bottom: 2rem;
        }
        .btn-ai {
            padding: 0.75rem 1.5rem;
            border-radius: 12px;
            font-weight: 600;
            font-size: 0.875rem;
            border: none;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            transition: all 0.3s;
        }
        .btn-ai.primary {
            background: linear-gradient(135deg, #6366f1, #8b5cf6);
            color: white;
        }
        .btn-ai.primary:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(99,102,241,0.3); }
        .btn-ai.secondary {
            background: rgba(255,255,255,0.06);
            color: #e2e8f0;
            border: 1px solid rgba(255,255,255,0.1);
        }
        .btn-ai.secondary:hover { background: rgba(255,255,255,0.1); }

        .disclaimer {
            text-align: center;
            padding: 1rem;
            font-size: 0.75rem;
            color: #64748b;
            border-top: 1px solid rgba(255,255,255,0.06);
        }

        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            color: #94a3b8;
            text-decoration: none;
            font-size: 0.875rem;
            margin-bottom: 1.5rem;
            transition: color 0.2s;
        }
        .back-link:hover { color: #e2e8f0; }

        .error-box {
            background: rgba(239,68,68,0.1);
            border: 1px solid rgba(239,68,68,0.3);
            border-radius: 12px;
            padding: 2rem;
            text-align: center;
            color: #fca5a5;
        }

        /* Simple markdown-like rendering */
        .ai-report-content p { margin-bottom: 0.75rem; }
    </style>
</head>
<body>

<div class="report-container">
    <a href="admin-dashboard" class="back-link"><i class="fas fa-arrow-left"></i> Quay lại Dashboard</a>

    <div class="report-header">
        <h1><i class="fas fa-robot"></i> Báo cáo phân tích AI</h1>
        <p>Được tạo tự động bởi DiabCare AI • ${patient.hoTen != null ? patient.hoTen : 'Bệnh nhân'}</p>
    </div>

    <c:if test="${not empty error}">
        <div class="error-box">
            <i class="fas fa-exclamation-triangle fa-2x mb-3"></i>
            <p>${error}</p>
        </div>
    </c:if>

    <c:if test="${not empty patient}">
        <div class="patient-summary">
            <div class="summary-card">
                <div class="label">Họ tên bệnh nhân</div>
                <div class="value">${patient.hoTen != null ? patient.hoTen : 'N/A'}</div>
            </div>
            <div class="summary-card">
                <div class="label">Loại tiểu đường</div>
                <div class="value">${patient.loaiTieuDuong != null ? patient.loaiTieuDuong : 'Type 2'}</div>
            </div>
            <div class="summary-card">
                <div class="label">Giới tính</div>
                <div class="value">${patient.gioiTinh != null ? patient.gioiTinh : 'N/A'}</div>
            </div>
            <div class="summary-card">
                <div class="label">Nhóm máu</div>
                <div class="value">${patient.nhomMau != null ? patient.nhomMau : 'N/A'}</div>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty aiReport}">
        <div class="report-actions">
            <a href="ai-report?patientId=${patient.id}" class="btn-ai primary">
                <i class="fas fa-sync-alt"></i> Tạo lại báo cáo
            </a>
            <button class="btn-ai secondary" onclick="window.print()">
                <i class="fas fa-print"></i> In báo cáo
            </button>
        </div>

        <div class="ai-report-content" id="reportContent">
            <!-- AI report content rendered here -->
        </div>

        <script>
            // Simple markdown-to-HTML renderer for AI response
            function renderMarkdown(text) {
                return text
                    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                    .replace(/\*(.*?)\*/g, '<em>$1</em>')
                    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
                    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
                    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
                    .replace(/^\- (.*$)/gim, '<li>$1</li>')
                    .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
                    .replace(/\n\n/g, '</p><p>')
                    .replace(/\n/g, '<br>');
            }

            const reportEl = document.getElementById('reportContent');
            const rawReport = `${aiReport}`;
            reportEl.innerHTML = '<p>' + renderMarkdown(rawReport) + '</p>';
        </script>
    </c:if>

    <div class="disclaimer">
        ⚠️ Báo cáo này được tạo bởi AI và chỉ mang tính tham khảo. Mọi quyết định lâm sàng phải do bác sĩ điều trị đưa ra.<br>
        © 2024 DiabCare AI. Dữ liệu y tế bảo mật.
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
