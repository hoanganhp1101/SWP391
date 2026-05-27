<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Chỉnh sửa bệnh nhân</title>

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: Arial, sans-serif;
        }

        body {
            background: #000000;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }

        .modal {
            width: 100%;
            max-width: 1050px;
            background: #fff;
            border-radius: 24px;
            overflow: hidden;
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 28px 36px;
            border-bottom: 1px solid #e5e7eb;
        }

        .modal-header h2 {
            font-size: 22px;
            font-weight: 700;
            color: #111827;
        }

        .close-btn {
            border: none;
            background: transparent;
            font-size: 34px;
            color: #6b7280;
            cursor: pointer;
        }

        .form-container {
            padding: 36px;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 32px 36px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
        }

        .form-group label {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 12px;
            color: #1f2937;
        }

        .form-group label span {
            color: red;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            height: 62px;
            border: 1px solid #d1d5db;
            border-radius: 16px;
            padding: 0 24px;
            font-size: 18px;
            outline: none;
            transition: 0.2s;
            background: #fff;
        }

        .form-group input:focus,
        .form-group select:focus {
            border-color: #2563eb;
        }

        .full-width {
            grid-column: span 2;
        }

        .button-group {
            grid-column: span 2;
            display: flex;
            justify-content: flex-end;
            gap: 18px;
            margin-top: 10px;
        }

        .cancel-btn,
        .submit-btn {
            min-width: 120px;
            height: 60px;
            border-radius: 16px;
            font-size: 18px;
            font-weight: 600;
            cursor: pointer;
            transition: 0.2s;
        }

        .cancel-btn {
            background: #fff;
            border: 1px solid #d1d5db;
            color: #111827;
        }

        .cancel-btn:hover {
            background: #f3f4f6;
        }

        .submit-btn {
            background: #2563eb;
            border: none;
            color: white;
        }

        .submit-btn:hover {
            background: #1d4ed8;
        }

        @media (max-width: 768px) {
            .form-container {
                grid-template-columns: 1fr;
            }

            .full-width,
            .button-group {
                grid-column: span 1;
            }

            .button-group {
                justify-content: stretch;
            }

            .cancel-btn,
            .submit-btn {
                width: 100%;
            }
        }
    </style>
</head>
<body>

<div class="modal">
    <div class="modal-header">
        <h2>Chỉnh sửa bệnh nhân</h2>
        <button class="close-btn">&times;</button>
    </div>

    <form class="form-container">

        <div class="form-group">
            <label>Họ và tên <span>*</span></label>
            <input type="text" value="Nguyễn Văn An">
        </div>

        <div class="form-group">
            <label>Tuổi <span>*</span></label>
            <input type="number" value="45">
        </div>

        <div class="form-group">
            <label>Giới tính <span>*</span></label>
            <select>
                <option>Nam</option>
                <option>Nữ</option>
            </select>
        </div>

        <div class="form-group">
            <label>Số điện thoại <span>*</span></label>
            <input type="text" value="0901234567">
        </div>

        <div class="form-group">
            <label>Email <span>*</span></label>
            <input type="email" value="an.nguyen@email.com">
        </div>

        <div class="form-group">
            <label>Lần khám gần nhất <span>*</span></label>
            <input type="date" value="2026-05-15">
        </div>

        <div class="form-group full-width">
            <label>Chẩn đoán <span>*</span></label>
            <input type="text" value="Tiểu đường type 2">
        </div>

        <div class="button-group">
            <button type="button" class="cancel-btn">Hủy</button>
            <button type="submit" class="submit-btn">Cập nhật</button>
        </div>

    </form>
</div>

</body>
</html>