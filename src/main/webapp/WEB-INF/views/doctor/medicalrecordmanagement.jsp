<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <title>Quản lý hồ sơ y tế</title>

    <style>
        *{
            margin:0;
            padding:0;
            box-sizing:border-box;
            font-family:'Segoe UI',sans-serif;
        }

        body{
            background:#f8fafc;
            color:#1f2937;
        }

        /* LAYOUT */

        .layout{
            display:flex;
            min-height:100vh;
        }

        /* SIDEBAR */

        .sidebar{
            width:280px;
            background:#fff;
            border-right:1px solid #e5e7eb;
            position:fixed;
            top:0;
            left:0;
            height:100vh;
            overflow-y:auto;
        }

        .logo{
            padding:30px;
            border-bottom:1px solid #f1f5f9;
        }

        .logo h2{
            font-size:34px;
            font-weight:700;
        }

        .logo p{
            margin-top:6px;
            color:#64748b;
        }

        .sidebar nav{
            padding:20px;
        }

        .sidebar nav a{
            display:flex;
            align-items:center;
            gap:14px;

            padding:14px 18px;
            margin-bottom:10px;

            border-radius:14px;

            text-decoration:none;

            color:#374151;
            font-weight:500;

            transition:.2s;
        }

        .sidebar nav a:hover{
            background:#f3f4f6;
        }

        .sidebar nav a.active{
            background:#eef2ff;
            color:#2563eb;
            font-weight:600;
        }

        .sidebar nav a i{
            width:20px;
            font-size:18px;
        }

        /* MAIN */

        .main-content{
            flex:1;
            margin-left:280px;
            min-height:100vh;
        }

        /* TOPBAR */

        .topbar{
            height:90px;

            background:#fff;

            border-bottom:1px solid #e5e7eb;

            display:flex;
            justify-content:flex-end;
            align-items:center;

            padding:0 40px;

            position:sticky;
            top:0;
            z-index:100;
        }

        .profile{
            display:flex;
            align-items:center;
            gap:14px;
        }

        .profile-info{
            display:flex;
            flex-direction:column;
            align-items:flex-end;
        }

        .profile-info span{
            font-weight:600;
        }

        .profile-info small{
            color:#64748b;
            font-size:13px;
        }

        .profile-avatar{
            width:46px;
            height:46px;

            border-radius:50%;

            background:#2563eb;
            color:#fff;

            display:flex;
            align-items:center;
            justify-content:center;

            font-size:18px;
        }

        /* PAGE HEADER */

        .page-header{
            margin-bottom:28px;
        }

        .page-header h1{
            font-size:38px;
            font-weight:700;
            margin-bottom:10px;
        }

        .page-header p{
            color:#64748b;
            font-size:16px;
        }

        /* CARD */

        .card{
            background:white;
            border:1px solid #e5e7eb;
            border-radius:24px;
            overflow:hidden;
        }

        /* CARD TOP */

        .card-top{
            padding:26px;
            display:flex;
            justify-content:space-between;
            align-items:center;
            gap:20px;
            border-bottom:1px solid #e5e7eb;
        }

        .search-box{
            flex:1;
            position:relative;
        }

        .search-box i{
            position:absolute;
            top:50%;
            left:16px;
            transform:translateY(-50%);
            color:#94a3b8;
        }

        .search-box input{
            width:100%;
            padding:16px 18px 16px 48px;
            border:1px solid #dbe2ea;
            border-radius:14px;
            outline:none;
            font-size:15px;
            transition:0.2s;
        }

        .search-box input:focus{
            border-color:#2563eb;
        }

        .actions{
            display:flex;
            gap:14px;
        }

        /* BUTTONS */

        .btn{
            border:none;
            padding:14px 22px;
            border-radius:14px;
            font-size:15px;
            font-weight:600;
            cursor:pointer;
            display:flex;
            align-items:center;
            gap:10px;
            transition:0.2s;
        }

        .btn:hover{
            transform:translateY(-1px);
        }

        .btn-outline{
            background:white;
            border:1px solid #dbe2ea;
            color:#374151;
        }

        .btn-primary{
            background:#2563eb;
            color:white;
        }

        /* TABLE */

        .table-wrapper{
            overflow-x:auto;
        }

        table{
            width:100%;
            border-collapse:collapse;
            min-width:1200px;
        }

        thead{
            background:#f8fafc;
        }

        th{
            text-align:left;
            padding:20px 26px;
            font-size:13px;
            font-weight:600;
            color:#64748b;
            border-bottom:1px solid #e5e7eb;
        }

        td{
            padding:22px 26px;
            border-bottom:1px solid #eef2f7;
            color:#374151;
            font-size:15px;
        }

        tbody tr{
            transition:0.2s;
        }

        tbody tr:hover{
            background:#f8fafc;
        }

        /* ACTION BUTTONS */

        .action-buttons{
            display:flex;
            align-items:center;
            gap:12px;
        }

        .icon-btn{
            width:40px;
            height:40px;
            border:none;
            border-radius:12px;
            display:flex;
            align-items:center;
            justify-content:center;
            cursor:pointer;
            transition:0.2s;
            font-size:15px;
        }

        .icon-btn:hover{
            transform:translateY(-2px);
        }

        .edit-btn{
            background:#eff6ff;
            color:#2563eb;
        }

        .delete-btn{
            background:#fef2f2;
            color:#dc2626;
        }

        /* CONFIRM DELETE */

        .confirm-toast{
            position:fixed;
            top:30px;
            right:30px;
            width:340px;
            background:white;
            border-radius:22px;
            padding:22px;
            box-shadow:0 10px 30px rgba(0,0,0,0.15);

            opacity:0;
            visibility:hidden;
            transform:translateY(-20px);

            transition:0.3s;
            z-index:1000;
        }

        .confirm-toast.show{
            opacity:1;
            visibility:visible;
            transform:translateY(0);
        }

        .confirm-content{
            display:flex;
            gap:16px;
            align-items:flex-start;
        }

        .confirm-content i{
            color:#f97316;
            font-size:24px;
            margin-top:4px;
        }

        .confirm-text h4{
            font-size:18px;
            margin-bottom:6px;
        }

        .confirm-text p{
            color:#64748b;
            line-height:1.5;
        }

        .confirm-actions{
            margin-top:22px;
            display:flex;
            justify-content:flex-end;
            gap:12px;
        }

        .cancel-btn,
        .confirm-btn{
            border:none;
            padding:12px 18px;
            border-radius:12px;
            cursor:pointer;
            font-weight:600;
            transition:0.2s;
        }

        .cancel-btn{
            background:#f1f5f9;
            color:#374151;
        }

        .confirm-btn{
            background:#ef4444;
            color:white;
        }

        .cancel-btn:hover,
        .confirm-btn:hover{
            transform:translateY(-1px);
        }

        /* SUCCESS TOAST */

        .toast{
            position:fixed;
            bottom:30px;
            right:30px;
            background:#16a34a;
            color:white;
            padding:16px 22px;
            border-radius:14px;
            font-weight:600;

            opacity:0;
            visibility:hidden;

            transition:0.3s;
            z-index:999;
        }

        .toast.show{
            opacity:1;
            visibility:visible;
        }

        /* RESPONSIVE */

        @media(max-width:992px){

            .card-top{
                flex-direction:column;
                align-items:flex-start;
            }

            .search-box{
                width:100%;
            }

        }

        @media(max-width:768px){

            .sidebar{
                display:none;
            }

            .main-content{
                margin-left:0;
                padding:20px;
            }

            .page-header h1{
                font-size:30px;
            }

            .actions{
                width:100%;
                flex-direction:column;
            }

            .btn{
                width:100%;
                justify-content:center;
            }

        }
        .page-content {
            padding: 32px 40px;
        }

        /* HEADER */
        .page-header{
            margin-bottom:28px;
        }
    </style>

    <link
            rel="stylesheet"
            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
    />
</head>

<body>

<div class="container">

    <!-- SIDEBAR -->

    <aside class="sidebar">

        <div class="logo">
            <h2>MediCare</h2>
            <p>Doctor Portal</p>
        </div>

        <nav>

            <a href="doctordashboard.jsp" class="active">
                <i class="fa-solid fa-table-columns"></i>
                Dashboard
            </a>

            <a href="patientmanagement.jsp">
                <i class="fa-solid fa-users"></i>
                Patient List
            </a>

            <a href="medicalrecordmanagement.html">
                <i class="fa-regular fa-file-lines"></i>
                Medical Records
            </a>

            <a>
                <i class="fa-solid fa-pills"></i>
                Prescriptions
            </a>

            <a href="doctorappointment.jsp">
                <i class="fa-regular fa-calendar-check"></i>
                Appointments
            </a>

            <a>
                <i class="fa-solid fa-flask-vial"></i>
                Laboratory Results
            </a>

            <a>
                <i class="fa-solid fa-triangle-exclamation"></i>
                AI Alerts
            </a>

            <a href="highriskdashboard.jsp">
                <i class="fa-solid fa-heart-pulse"></i>
                High Risk Dashboard
            </a>

        </nav>

    </aside>

    <!-- MAIN CONTENT -->

    <main class="main-content">
        <header class="topbar">


            <div class="profile">

                <div class="profile-info">
                    <span>Dr. Sarah Johnson</span>
                    <small>Endocrinologist</small>
                </div>

                <div class="profile-avatar">
                    <i class="fa-solid fa-user-doctor"></i>
                </div>

            </div>

        </header>
        <div class="page-content">
            <div class="page-header">

                <h1>Quản lý hồ sơ y tế</h1>

                <p>
                    Danh sách và quản lý hồ sơ khám bệnh
                </p>

            </div>

            <!-- CARD -->

            <div class="card">

                <!-- TOP -->

                <div class="card-top">

                    <div class="search-box">

                        <i class="fa-solid fa-magnifying-glass"></i>

                        <input
                                type="text"
                                id="searchInput"
                                placeholder="Tìm kiếm theo tên bệnh nhân, mã BN, bác sĩ, chẩn đoán..."
                        />

                    </div>

                    <div class="actions">

                        <button class="btn btn-outline">
                            <i class="fa-solid fa-download"></i>
                            Xuất Excel
                        </button>

                        <button class="btn btn-primary">
                            <i class="fa-solid fa-plus"></i>
                            Thêm hồ sơ
                        </button>

                    </div>

                </div>

                <!-- TABLE -->

                <div class="table-wrapper">

                    <table>

                        <thead>

                        <tr>

                            <th>MÃ HS</th>
                            <th>MÃ BN</th>
                            <th>TÊN BỆNH NHÂN</th>
                            <th>NGÀY KHÁM</th>
                            <th>BÁC SĨ</th>
                            <th>CHẨN ĐOÁN</th>
                            <th>HUYẾT ÁP</th>
                            <th>NHIỆT ĐỘ</th>
                            <th>THAO TÁC</th>

                        </tr>

                        </thead>

                        <tbody id="medicalTable">

                        </tbody>

                    </table>

                </div>

            </div>
        </div>
    </main>

</div>
<div id="deleteToast" class="confirm-toast">

    <div class="confirm-content">

        <i class="fa-solid fa-triangle-exclamation"></i>

        <div class="confirm-text">

            <h4>Xác nhận xóa</h4>

            <p>Bạn có chắc muốn xóa hồ sơ này?</p>

        </div>

    </div>

    <div class="confirm-actions">

        <button class="cancel-btn" onclick="closeDeleteToast()">
            Hủy
        </button>

        <button class="confirm-btn" onclick="confirmDelete()">
            Xóa
        </button>

    </div>

</div>
<script>const medicalRecords = [

    {
        recordId: "#0001",
        patientId: "BN001",
        patientName: "Nguyễn Văn An",
        date: "20/5/2026",
        doctor: "BS. Trần Minh Tuấn",
        diagnosis: "Tiểu đường type 2",
        bloodPressure: "130/85",
        temperature: "36.8°C"
    },

    {
        recordId: "#0002",
        patientId: "BN002",
        patientName: "Trần Thị Bình",
        date: "19/5/2026",
        doctor: "BS. Nguyễn Thị Hoa",
        diagnosis: "Tiểu đường type 1",
        bloodPressure: "120/80",
        temperature: "37.0°C"
    },

    {
        recordId: "#0003",
        patientId: "BN003",
        patientName: "Lê Minh Cường",
        date: "18/5/2026",
        doctor: "BS. Trần Minh Tuấn",
        diagnosis: "Biến chứng thận do tiểu đường",
        bloodPressure: "140/90",
        temperature: "36.9°C"
    },

    {
        recordId: "#0004",
        patientId: "BN004",
        patientName: "Phạm Thu Dung",
        date: "17/5/2026",
        doctor: "BS. Lê Văn Nam",
        diagnosis: "Tiểu đường thai kỳ",
        bloodPressure: "125/82",
        temperature: "36.7°C"
    },

    {
        recordId: "#0005",
        patientId: "BN005",
        patientName: "Hoàng Văn Em",
        date: "16/5/2026",
        doctor: "BS. Nguyễn Thị Hoa",
        diagnosis: "Biến chứng mắt do tiểu đường",
        bloodPressure: "135/88",
        temperature: "36.6°C"
    }

];

const medicalTable = document.getElementById("medicalTable");
const searchInput = document.getElementById("searchInput");

function renderTable(data) {

    medicalTable.innerHTML = "";

    data.forEach(record => {

        medicalTable.innerHTML += `

            <tr>

              <td>${record.recordId}</td>

              <td>${record.patientId}</td>

              <td>${record.patientName}</td>

              <td>${record.date}</td>

              <td>${record.doctor}</td>

              <td>${record.diagnosis}</td>

              <td>${record.bloodPressure}</td>

              <td>${record.temperature}</td>

              <td class="action-buttons">

                <button
                    class="icon-btn edit-btn"
                    onclick="window.location.href='medicalrecorddetail.html'"
                >
                    <i class="fa-solid fa-pen"></i>
                </button>

                <button
                    class="icon-btn delete-btn"
                    onclick="deleteRecord(this)"
                >
                    <i class="fa-solid fa-trash"></i>
                </button>

            </td>
            </tr>

          `;
    });
}

renderTable(medicalRecords);

/* SEARCH */

searchInput.addEventListener("keyup", () => {

    const keyword = searchInput.value.toLowerCase();

    const filtered = medicalRecords.filter(record => {

        return (
            record.patientName.toLowerCase().includes(keyword) ||
            record.patientId.toLowerCase().includes(keyword) ||
            record.doctor.toLowerCase().includes(keyword) ||
            record.diagnosis.toLowerCase().includes(keyword)
        );

    });

    renderTable(filtered);

});
let selectedRow = null;

function deleteRecord(button) {

    selectedRow = button.closest("tr");

    const toast = document.getElementById("deleteToast");

    toast.classList.add("show");

}

function closeDeleteToast() {

    const toast = document.getElementById("deleteToast");

    toast.classList.remove("show");

}

function confirmDelete() {

    if (selectedRow) {

        selectedRow.remove();

    }

    closeDeleteToast();

    showToast("Xóa hồ sơ thành công");

}

function showToast(message) {

    const toast = document.getElementById("toast");

    toast.innerText = message;

    toast.classList.add("show");

    setTimeout(() => {

        toast.classList.remove("show");

    }, 2500);

}
</script>

</body>
</html>