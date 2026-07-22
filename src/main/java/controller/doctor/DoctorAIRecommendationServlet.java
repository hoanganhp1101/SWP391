package controller.doctor;

import dal.AIRecommendationScanDAO;
import dal.AIRecommendationScanDAO.ScanResult;
import dal.DoctorAIRecommendationDAO;
import dal.ThresholdSettingsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.AIAnalysis;
import model.ThresholdSettings;
import model.User;

@WebServlet(name = "DoctorAIRecommendationServlet", urlPatterns = {"/doctor/ai-recommendations"})
public class DoctorAIRecommendationServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = requireDoctor(request, response);
        if (user == null) {
            return;
        }
        String doctorId = user.getId().toString();

        String id = request.getParameter("id");
        // Chỉ auto-sync khi mở danh sách không lọc (level/status = all hoặc không gửi)
        boolean hasActiveFilters = isActiveFilter(request.getParameter("level"))
                || isActiveFilter(request.getParameter("status"))
                || hasText(request.getParameter("keyword"))
                || hasText(request.getParameter("page"));
        if ((id == null || id.isBlank()) && !hasActiveFilters) {
            ThresholdSettings thresholds = new ThresholdSettingsDAO().getForDoctor(doctorId);
            new AIRecommendationScanDAO().scan(doctorId, thresholds);
        }

        DoctorAIRecommendationDAO dao = new DoctorAIRecommendationDAO();
        // Đảm bảo cột trạng thái tồn tại trước khi list/update
        dao.ensureStatusColumns();

        if (id != null && !id.isBlank()) {
            AIAnalysis detail = dao.findById(id, doctorId);
            if (detail == null) {
                response.sendRedirect(request.getContextPath() + "/doctor/ai-recommendations?error=1");
                return;
            }
            request.setAttribute("detail", detail);
            request.getRequestDispatcher("/WEB-INF/views/doctor/ai-recommendation-detail.jsp").forward(request, response);
            return;
        }

        String level = blankToAll(request.getParameter("level"));
        String status = blankToAll(request.getParameter("status"));
        String keyword = request.getParameter("keyword") == null ? "" : request.getParameter("keyword").trim();
        int page = parsePage(request.getParameter("page"));

        int total = dao.count(doctorId, level, status, keyword);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        List<AIAnalysis> list = dao.list(doctorId, level, status, keyword, page, PAGE_SIZE);

        request.setAttribute("list", list);
        request.setAttribute("total", total);
        request.setAttribute("levelFilter", level);
        request.setAttribute("statusFilter", status);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("fromIndex", total == 0 ? 0 : (page - 1) * PAGE_SIZE + 1);
        request.setAttribute("toIndex", Math.min(page * PAGE_SIZE, total));

        request.getRequestDispatcher("/WEB-INF/views/doctor/ai-recommendations.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User user = requireDoctor(request, response);
        if (user == null) {
            return;
        }
        String doctorId = user.getId().toString();

        if ("1".equals(request.getParameter("sync"))) {
            ThresholdSettings thresholds = new ThresholdSettingsDAO().getForDoctor(doctorId);
            ScanResult scan = new AIRecommendationScanDAO().scan(doctorId, thresholds);
            if (scan.isError() && scan.getCreated() == 0) {
                String msg = scan.getLastError() == null ? "" : scan.getLastError();
                if (msg.length() > 300) {
                    msg = msg.substring(0, 300);
                }
                response.sendRedirect(request.getContextPath()
                        + "/doctor/ai-recommendations?error=1&failed=" + scan.getInsertFailed()
                        + "&scanned=" + scan.getPatientsScanned()
                        + "&errmsg=" + java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8));
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/doctor/ai-recommendations?synced=1"
                        + "&created=" + scan.getCreated()
                        + "&skipped=" + scan.getSkipped()
                        + "&scanned=" + scan.getPatientsScanned()
                        + "&norisk=" + scan.getPatientsNoRisk());
            }
            return;
        }

        String id = request.getParameter("id");
        String status = request.getParameter("status");
        String note = request.getParameter("ghiChu");

        DoctorAIRecommendationDAO dao = new DoctorAIRecommendationDAO();
        String updateError = dao.updateStatus(id, doctorId, status, note, user.getId());
        if (updateError == null) {
            response.sendRedirect(request.getContextPath() + "/doctor/ai-recommendations?id=" + id + "&saved=1");
        } else {
            String msg = updateError.length() > 400 ? updateError.substring(0, 400) : updateError;
            response.sendRedirect(request.getContextPath() + "/doctor/ai-recommendations?id=" + id
                    + "&error=1&errmsg=" + java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private User requireDoctor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null || !"bac_si".equalsIgnoreCase(user.getVaiTro())) {
            response.sendRedirect(request.getContextPath() + "/Logincontroller");
            return null;
        }
        return user;
    }

    private String blankToAll(String raw) {
        if (raw == null || raw.isBlank()) {
            return "all";
        }
        return raw.trim();
    }

    private boolean hasText(String raw) {
        return raw != null && !raw.isBlank();
    }

    /** true nếu param lọc thực sự (không phải all / trống) */
    private boolean isActiveFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        return !"all".equalsIgnoreCase(raw.trim());
    }

    private int parsePage(String raw) {
        if (raw == null || raw.isBlank()) {
            return 1;
        }
        try {
            return Math.max(Integer.parseInt(raw.trim()), 1);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
