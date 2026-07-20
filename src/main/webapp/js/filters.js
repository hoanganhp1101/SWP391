/* =========================================================
   HealthAlert — Shared Filter Dropdown Controller
   Điều khiển toàn bộ dropdown/filter trong project bằng
   event delegation: mở/đóng bằng click, chỉ 1 cái mở tại một
   thời điểm, click ngoài đóng, ESC đóng. Không dùng :hover.
   Không phụ thuộc jQuery.
   ========================================================= */
(function () {
    "use strict";

    function panelOf(button) {
        // Panel là phần tử .filter-menu / .filter-popup nằm cùng .filter-dropdown
        var dropdown = button.closest(".filter-dropdown");
        return dropdown ? dropdown.querySelector(".filter-menu, .filter-popup") : null;
    }

    function closeAll(except) {
        document.querySelectorAll(".filter-menu.show, .filter-popup.show")
            .forEach(function (panel) {
                if (panel !== except) {
                    panel.classList.remove("show");
                }
            });
    }

    function onClick(e) {
        var button = e.target.closest(".filter-button");
        if (button) {
            // Toggle panel của nút được bấm
            e.preventDefault();
            var panel = panelOf(button);
            if (!panel) {
                return;
            }
            var willOpen = !panel.classList.contains("show");
            closeAll(willOpen ? panel : null);
            panel.classList.toggle("show", willOpen);
            return;
        }

        // Bấm bên trong panel (chọn ngày, submit...) => giữ nguyên,
        // link .filter-item vẫn điều hướng bình thường.
        if (e.target.closest(".filter-menu, .filter-popup")) {
            return;
        }

        // Bấm ra ngoài => đóng tất cả
        closeAll(null);
    }

    function onKeydown(e) {
        if (e.key === "Escape" || e.key === "Esc") {
            closeAll(null);
        }
    }

    function init() {
        document.addEventListener("click", onClick);
        document.addEventListener("keydown", onKeydown);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
