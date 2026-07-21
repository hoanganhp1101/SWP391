CREATE TABLE IF NOT EXISTS educational_contents (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    title               VARCHAR(200)    NOT NULL,
    category            VARCHAR(60)     NOT NULL,
    summary             VARCHAR(500)    DEFAULT NULL,
    content             TEXT            NOT NULL,
    target_audience     VARCHAR(60)     DEFAULT 'benh_nhan',
    display_order       INT             NOT NULL DEFAULT 0,
    active              TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_edu_category (category),
    INDEX idx_edu_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO educational_contents
    (id, title, category, summary, content, target_audience, display_order, active)
VALUES
    (UUID(), 'Nhận biết dấu hiệu hạ đường huyết', 'tu_cham_soc',
     'Các dấu hiệu cần chú ý và cách xử trí ban đầu khi đường huyết xuống thấp.',
     'Theo dõi các dấu hiệu như run tay, vã mồ hôi, chóng mặt, đói cồn cào hoặc lú lẫn. Khi nghi ngờ hạ đường huyết, người bệnh nên đo đường huyết nếu có máy đo và bổ sung carbohydrate hấp thu nhanh theo hướng dẫn của nhân viên y tế.',
     'benh_nhan', 1, 1),
    (UUID(), 'Nguyên tắc chọn thực phẩm GI thấp', 'dinh_duong',
     'Gợi ý lựa chọn thực phẩm giúp hạn chế tăng đường huyết sau ăn.',
     'Ưu tiên ngũ cốc nguyên hạt, rau xanh, đạm nạc và thực phẩm giàu chất xơ. Hạn chế nước ngọt, bánh kẹo, tinh bột tinh chế và khẩu phần quá lớn trong một bữa.',
     'benh_nhan', 2, 1),
    (UUID(), 'Lưu ý khi sử dụng insulin', 'thuoc_insulin',
     'Các điểm cần nhớ khi bảo quản và sử dụng insulin.',
     'Insulin cần được dùng đúng loại, đúng liều, đúng thời điểm theo chỉ định. Không tự ý thay đổi liều. Kiểm tra hạn dùng, cách bảo quản và vị trí tiêm để giảm nguy cơ sai liều hoặc kích ứng.',
     'benh_nhan', 3, 1);
