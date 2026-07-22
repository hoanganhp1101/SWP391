package controller.doctor;

import dal.DoctorAlertDAO;
import dal.DoctorAnalyticsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.ThresholdSettings;
import model.User;

@WebServlet(name = "DoctorAnalyticsServlet", urlPatterns = {"/doctor/analytics"})
public class DoctorAnalyticsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null || !"bac_si".equalsIgnoreCase(user.getVaiTro())) {
            response.sendRedirect(request.getContextPath() + "/Logincontroller");
            return;
        }
        String doctorId = user.getId().toString();

        int days = parseDays(request.getParameter("days"));

        DoctorAnalyticsDAO dao = new DoctorAnalyticsDAO(doctorId);

        // KPI
        request.setAttribute("days", days);
        request.setAttribute("totalPatients", dao.countPatients());
        request.setAttribute("avgGlucose", round(dao.averageGlucose(days), 0));
        request.setAttribute("timeInRange", round(dao.timeInRange(days), 1));
        request.setAttribute("pctHba1c", round(dao.pctHba1cAtTarget(), 1));
        request.setAttribute("unresolvedAlerts", dao.unresolvedAlerts());

        // Action list — đếm TRỰC TIẾP từ bảng alerts bằng đúng hàm của màn Alert,
        // để con số khớp 100% với số cảnh báo hiển thị khi bấm vào filter tương ứng.
        DoctorAlertDAO alertDao = new DoctorAlertDAO();
        request.setAttribute("alertDanger", alertDao.countAlerts("danger", "all", "all", "", doctorId));
        request.setAttribute("alertHigh", alertDao.countAlerts("high", "all", "all", "", doctorId));
        request.setAttribute("alertUnread", alertDao.countAlerts("all", "unread", "all", "", doctorId));
        request.setAttribute("alertProcessing", alertDao.countAlerts("all", "processing", "all", "", doctorId));

        // Theo dõi lâm sàng — chỉ số tổng hợp từ health_records/prescriptions (thông tin, không gắn filter alert)
        request.setAttribute("hypoPatients", dao.patientsWithHypo(days));
        request.setAttribute("highHba1c", dao.patientsHighHba1c());
        request.setAttribute("notMeasured", dao.patientsNotMeasured());

        ThresholdSettings thresholds = dao.getThresholds();
        request.setAttribute("glucoseLow", thresholds.getGlucoseLow());
        request.setAttribute("glucoseHigh", thresholds.getGlucoseHigh());
        request.setAttribute("hba1cTarget", thresholds.getHba1cTarget());
        request.setAttribute("hba1cPoor", thresholds.getHba1cPoor());
        request.setAttribute("daysNoMeasure", thresholds.getDaysNoMeasure());
        request.setAttribute("overdueFollowups", dao.overdueFollowups());

        // Charts (JSON)
        List<Object[]> glucose = dao.glucoseByDay(days);
        request.setAttribute("glucoseLabels", jsonLabels(glucose));
        request.setAttribute("glucoseData", jsonData(glucose));

        List<Object[]> alertType = dao.alertsByType();
        request.setAttribute("alertTypeLabels", jsonLabels(alertType));
        request.setAttribute("alertTypeData", jsonData(alertType));

        List<Object[]> alertSeverity = dao.alertsBySeverity();
        request.setAttribute("alertSeverityLabels", jsonLabels(alertSeverity));
        request.setAttribute("alertSeverityData", jsonData(alertSeverity));

        List<Object[]> hba1c = dao.hba1cDistribution();
        request.setAttribute("hba1cLabels", jsonLabels(hba1c));
        request.setAttribute("hba1cData", jsonData(hba1c));

        request.getRequestDispatcher("/WEB-INF/views/doctor/analytics.jsp").forward(request, response);
    }

    private int parseDays(String raw) {
        if (raw == null) {
            return 30;
        }
        try {
            int d = Integer.parseInt(raw.trim());
            return (d == 7 || d == 30 || d == 90) ? d : 30;
        } catch (NumberFormatException ex) {
            return 30;
        }
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    private String jsonLabels(List<Object[]> rows) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(escape(String.valueOf(rows.get(i)[0]))).append("\"");
        }
        return sb.append("]").toString();
    }

    private String jsonData(List<Object[]> rows) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            Object v = rows.get(i)[1];
            double d = v instanceof Number ? ((Number) v).doubleValue() : 0d;
            sb.append(Math.round(d * 10) / 10.0);
        }
        return sb.append("]").toString();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
