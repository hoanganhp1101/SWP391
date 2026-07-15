package controller.doctor;

import dal.DoctorAlertDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import com.example.diabetesmanage.model.User;

@WebServlet(name = "AlertQuickActionController", urlPatterns = {"/doctor/alerts/quick-action"})
public class AlertQuickActionController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null || !"bac_si".equalsIgnoreCase(user.getVaiTro())) {
            response.sendRedirect(request.getContextPath() + "/Logincontroller");
            return;
        }

        String alertIdRaw = request.getParameter("alertId");
        String ghiChu = request.getParameter("ghiChu");
        boolean markResolved = "1".equals(request.getParameter("markResolved"));

        if (alertIdRaw == null || ghiChu == null || ghiChu.trim().isEmpty()) {
            redirectBack(request, response, false);
            return;
        }

        UUID alertId;
        try {
            alertId = UUID.fromString(alertIdRaw.trim());
        } catch (IllegalArgumentException ex) {
            redirectBack(request, response, false);
            return;
        }

        DoctorAlertDAO dao = new DoctorAlertDAO();
        boolean saved = dao.quickAction(alertId, UUID.fromString(user.getId()), ghiChu.trim(), markResolved);
        redirectBack(request, response, saved);
    }

    private void redirectBack(HttpServletRequest request, HttpServletResponse response, boolean saved)
            throws IOException {

        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/doctor/alerts?");

        appendParam(url, "severity", request.getParameter("severity"));
        appendParam(url, "status", request.getParameter("status"));
        appendParam(url, "type", request.getParameter("type"));
        appendParam(url, "keyword", request.getParameter("keyword"));
        appendParam(url, "page", request.getParameter("page"));

        if (saved) {
            url.append("&saved=1");
        } else {
            url.append("&error=1");
        }

        response.sendRedirect(url.toString());
    }

    private void appendParam(StringBuilder url, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        url.append(name).append("=").append(java.net.URLEncoder.encode(value.trim(), java.nio.charset.StandardCharsets.UTF_8)).append("&");
    }
}
