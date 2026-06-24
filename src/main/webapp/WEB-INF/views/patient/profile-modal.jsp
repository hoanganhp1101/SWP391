<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<style>
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
        max-width: 720px;
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

    .profile-message {
        padding: 0.75rem 1rem;
        border-radius: 8px;
        margin-bottom: 1rem;
        font-size: 0.875rem;
        font-weight: 500;
    }

    .profile-message.error {
        background: var(--danger-light);
        color: var(--danger);
        border: 1px solid #fca5a5;
    }
</style>

<div class="modal-overlay" id="profileModal">
    <div class="modal">
        <div class="modal-header">
            <h3 class="modal-title">Cập nhật hồ sơ bệnh nhân</h3>
            <button type="button" class="close-btn" id="closeProfileModalBtn"><i class="fas fa-times"></i></button>
        </div>
        <c:if test="${param.profileUpdated == '0'}">
            <div class="profile-message error">
                <i class="fas fa-exclamation-circle"></i> ${not empty param.error ? param.error : 'Cập nhật hồ sơ thất bại.'}
            </div>
        </c:if>
        <form action="patient-dashboard" method="POST" id="profileForm" enctype="multipart/form-data">
            <input type="hidden" name="action" value="updateProfile">
            <input type="hidden" name="currentAnhDaiDien"
                value="${patientInfo.anhDaiDien != null ? patientInfo.anhDaiDien : ''}">
            <input type="hidden" name="returnUrl" value="${param.profileReturnUrl}">
            <div class="form-row">
                <div class="form-group">
                    <label>Họ và tên</label>
                    <input type="text" class="form-control" name="hoTen" required
                        value="${patientInfo.hoTen != null ? patientInfo.hoTen : ''}">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" class="form-control" name="email" required
                        value="${patientInfo.email != null ? patientInfo.email : ''}">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Số điện thoại</label>
                    <input type="text" class="form-control" name="soDienThoai" required
                        value="${patientInfo.soDienThoai != null ? patientInfo.soDienThoai : ''}">
                </div>
                <div class="form-group">
                    <label>Ngày sinh</label>
                    <input type="date" class="form-control" name="ngaySinh" required
                        value="${patientInfo.ngaySinh != null ? patientInfo.ngaySinh : ''}">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Giới tính</label>
                    <select class="form-control" name="gioiTinh">
                        <option value="">-- Chọn giới tính --</option>
                        <option value="nam" ${patientInfo.gioiTinh == 'nam' ? 'selected' : ''}>Nam</option>
                        <option value="nu" ${patientInfo.gioiTinh == 'nu' ? 'selected' : ''}>Nữ</option>
                        <option value="khac" ${patientInfo.gioiTinh == 'khac' ? 'selected' : ''}>Khác</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Loại tiểu đường</label>
                    <input type="text" class="form-control" name="loaiTieuDuong"
                        value="${patientInfo.loaiTieuDuong != null ? patientInfo.loaiTieuDuong : ''}">
                </div>
            </div>
            <div class="form-group">
                <label>Ảnh đại diện</label>
                <input type="file" class="form-control" name="avatarFile" id="avatarFile"
                    accept="image/png,image/jpeg,image/gif,image/webp">
                <div class="avatar-hint">Chọn ảnh từ máy tính (JPG, PNG, GIF hoặc WEBP, tối đa 5MB).</div>
            </div>
            <div class="form-group">
                <label>Địa chỉ</label>
                <textarea class="form-control" name="diaChi"
                    rows="2">${patientInfo.diaChi != null ? patientInfo.diaChi : ''}</textarea>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Tiền sử bệnh</label>
                    <textarea class="form-control" name="tienSuBenh"
                        rows="2">${patientInfo.tienSuBenh != null ? patientInfo.tienSuBenh : ''}</textarea>
                </div>
                <div class="form-group">
                    <label>Dị ứng</label>
                    <textarea class="form-control" name="diUng"
                        rows="2">${patientInfo.diUng != null ? patientInfo.diUng : ''}</textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-cancel" id="cancelProfileModalBtn">Hủy</button>
                <button type="submit" class="btn btn-save">Lưu hồ sơ</button>
            </div>
        </form>
    </div>
</div>

<script>
    (function () {
        const profileModal = document.getElementById('profileModal');
        const openProfileButtons = document.querySelectorAll('[data-open-profile-modal]');
        const closeProfileModalBtn = document.getElementById('closeProfileModalBtn');
        const cancelProfileModalBtn = document.getElementById('cancelProfileModalBtn');
        const avatarFileInput = document.getElementById('avatarFile');

        function openProfileModal(event) {
            if (event) event.preventDefault();
            if (profileModal) profileModal.classList.add('active');
        }

        function closeProfileModal() {
            if (profileModal) profileModal.classList.remove('active');
        }

        openProfileButtons.forEach(btn => btn.addEventListener('click', openProfileModal));
        if (closeProfileModalBtn) closeProfileModalBtn.addEventListener('click', closeProfileModal);
        if (cancelProfileModalBtn) cancelProfileModalBtn.addEventListener('click', closeProfileModal);
        if (profileModal) {
            profileModal.addEventListener('click', (e) => {
                if (e.target === profileModal) closeProfileModal();
            });
        }

        if (avatarFileInput) {
            avatarFileInput.addEventListener('change', function () {
                const file = this.files && this.files[0];
                if (!file) return;
                const previewUrl = URL.createObjectURL(file);
                document.querySelectorAll('.avatar-small, .profile-avatar').forEach(el => {
                    el.style.backgroundImage = "url('" + previewUrl + "')";
                });
            });
        }

        if ('${param.openProfileModal}' === '1') {
            openProfileModal();
            if (window.history && window.history.replaceState) {
                window.history.replaceState(null, '', '${param.profileReturnUrl}');
            }
        }
    })();
</script>
