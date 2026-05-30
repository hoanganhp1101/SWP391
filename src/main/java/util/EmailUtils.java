package util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ImportDetailDTO;

public class EmailUtils {

    // ===================== CONFIG =====================
    private static final String SENDER_EMAIL = "iac2612003@gmail.com";
    private static final String SENDER_PASSWORD = "zawt wrdy vdnq dmee";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    // ==================================================

    /**
     * Gửi email văn bản thường.
     *
     * @param toEmail địa chỉ email người nhận
     * @param subject tiêu đề email
     * @param body    nội dung email (plain text)
     * @throws MessagingException nếu gửi thất bại
     */
    public static void sendEmail(String toEmail, String subject, String body)
            throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL, "Fashion Warehouse System"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        Logger.getLogger(EmailUtils.class.getName()).log(Level.INFO, "Email sent to {0}", toEmail);
    }

    /**
     * Gửi email HTML.
     */
    public static void sendHtmlEmail(String toEmail, String subject, String htmlBody)
            throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL, "Fashion Warehouse System"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=utf-8");

        Transport.send(message);
    }
public static String buildOrderEmailTemplate(String expectedDate, List<ImportDetailDTO> details, double totalAmountPreTax) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; color: #333;'>");
        html.append("<h2 style='color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px;'>YÊU CẦU ĐẶT HÀNG MỚI</h2>");
        html.append("<p style='font-size: 16px;'>Chào Quý đối tác,</p>");
        html.append("<p style='font-size: 15px;'><strong>Fashion Warehouse</strong> xin gửi danh sách yêu cầu đặt hàng mới. Vui lòng chuẩn bị và giao hàng với thông tin dự kiến như sau:</p>");
        html.append("<p style='font-size: 15px; padding: 10px; background-color: #f8f9fa; border-left: 4px solid #e74c3c;'>");
        html.append("<strong>Ngày giao hàng dự kiến: </strong> <span style='color: #e74c3c; font-size: 16px; font-weight: bold;'>").append(expectedDate).append("</span>");
        html.append("</p>");
        
        html.append("<table border='1' cellpadding='10' cellspacing='0' style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
        html.append("<tr style='background-color: #34495e; color: white; text-align: center;'>");
        html.append("<th>Mã SP</th><th>Màu Sắc</th><th>Kích Cỡ</th><th>Số Lượng</th><th>Đơn Giá</th>");
        html.append("</tr>");
        
        for (ImportDetailDTO item : details) {
            html.append("<tr>");
            html.append("<td style='text-align: center;'>").append(item.getProductId()).append("</td>");
            html.append("<td style='text-align: center;'>").append(item.getColor().isEmpty() ? "-" : item.getColor()).append("</td>");
            html.append("<td style='text-align: center;'>").append(item.getSize().isEmpty() ? "-" : item.getSize()).append("</td>");
            
            // Đã sửa lại đúng tên Getter theo Model của bạn
            html.append("<td style='text-align: center; font-weight: bold; color: #2980b9;'>").append(item.getOrderQuantity()).append("</td>");
            html.append("<td style='text-align: right;'>").append(String.format("%,.0f VNĐ", item.getUnitPrice())).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        
        html.append("<h3 style='color: #c0392b; text-align: right; margin-top: 20px;'>Tổng tiền dự kiến (Chưa VAT): ").append(String.format("%,.0f VNĐ", totalAmountPreTax)).append("</h3>");
        
        html.append("<hr style='border: 1px solid #eee; margin-top: 30px;'>");
        html.append("<p style='font-size: 13px; color: #7f8c8d; text-align: center;'>Email này được gửi tự động từ hệ thống quản lý kho Fashion Warehouse. Vui lòng không phản hồi trực tiếp vào địa chỉ này.</p>");
        html.append("</div>");
        
        return html.toString();
    }
}
