document.addEventListener("DOMContentLoaded", function() {
    // Xử lý sự kiện cho thanh trượt Latency Range
    const latencyRange = document.getElementById("latencyRange");
    const latencyDisplay = document.getElementById("latencyDisplay");

    if (latencyRange && latencyDisplay) {
        latencyRange.addEventListener("input", function() {
            latencyDisplay.textContent = this.value + "ms";
        });
    }

    // Các logic JS tùy chỉnh khác có thể được thêm vào đây trong tương lai
    // Ví dụ: Xử lý sự kiện nút "Refresh Data", mở menu ẩn hiện (dropdowns), v.v.
});