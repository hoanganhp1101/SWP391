/* =========================================================
   HealthAlert — Shared Filter Dropdown Controller
   Mở/đóng bằng click, chỉ 1 dropdown mở, click ngoài đóng, ESC đóng.
   ========================================================= */
(function () {
    "use strict";

    if (window.__healthAlertFiltersInit) {
        return;
    }
    window.__healthAlertFiltersInit = true;

    function panelOf(dropdown) {
        return dropdown ? dropdown.querySelector(".filter-menu, .filter-popup") : null;
    }

    function setDropdownOpen(dropdown, open) {
        if (!dropdown) {
            return;
        }
        var panel = panelOf(dropdown);
        dropdown.classList.toggle("open", open);
        if (panel) {
            panel.classList.toggle("show", open);
        }
    }

    function closeAll(exceptDropdown) {
        document.querySelectorAll(".filter-dropdown.open").forEach(function (dropdown) {
            if (dropdown !== exceptDropdown) {
                setDropdownOpen(dropdown, false);
            }
        });
        document.querySelectorAll(".filter-menu.show, .filter-popup.show").forEach(function (panel) {
            var owner = panel.closest(".filter-dropdown");
            if (owner !== exceptDropdown) {
                panel.classList.remove("show");
            }
        });
    }

    function onClick(e) {
        var button = e.target.closest(".filter-button");
        if (button) {
            var dropdown = button.closest(".filter-dropdown");
            if (!dropdown) {
                return;
            }
            e.preventDefault();
            e.stopPropagation();

            var willOpen = !dropdown.classList.contains("open");
            closeAll(willOpen ? dropdown : null);
            setDropdownOpen(dropdown, willOpen);
            return;
        }

        var insideDropdown = e.target.closest(".filter-dropdown");
        if (insideDropdown) {
            e.stopPropagation();
            return;
        }

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
