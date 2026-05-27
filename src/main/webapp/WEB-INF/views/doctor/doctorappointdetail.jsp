<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Appointment Details</title>

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

    <style>
        *{
            margin:0;
            padding:0;
            box-sizing:border-box;
            font-family:Segoe UI, sans-serif;
        }

        body{
            background:#f5f7fb;
        }

        .container{
            display:flex;
            min-height:100vh;
        }

        /* SIDEBAR */

        .sidebar{
            width:280px;
            background:#fff;
            border-right:1px solid #e5e7eb;
        }


        .menu{
            padding:10px;
        }

        .menu-item{
            display:flex;
            align-items:center;
            gap:15px;
            padding:16px 20px;
            margin-bottom:8px;
            border-radius:14px;
            cursor:pointer;
            color:#1f2937;
            transition:.2s;
        }

        .menu-item:hover{
            background:#eef2ff;
        }

        .menu-item.active{
            background:#e8f0ff;
            color:#2563eb;
        }

        /* MAIN */

        .main{
            flex:1;

        }

        .topbar{
            display:flex;
            justify-content:space-between;
            align-items:center;
            margin-bottom:30px;
        }

        .back-btn{
            color:#374151;
            font-size:22px;
            cursor:pointer;
        }


        .page-title{
            margin-bottom:25px;
        }

        .page-title h1{
            font-size:48px;
            font-weight:700;
        }

        .page-title p{
            color:#6b7280;
            margin-top:8px;
        }

        .content{
            display:grid;
            grid-template-columns:2fr 1fr;
            gap:25px;
        }

        .card{
            background:#fff;
            border-radius:20px;
            padding:30px;
            box-shadow:0 2px 8px rgba(0,0,0,.05);
        }

        .card-title{
            font-size:20px;
            margin-bottom:30px;
            font-weight:600;
        }

        .info-grid{
            display:grid;
            grid-template-columns:1fr 1fr;
            gap:35px;
        }

        .label{
            color:#6b7280;
            margin-bottom:8px;
        }

        .value{
            font-size:18px;
            font-weight:500;
        }

        .status{
            display:inline-flex;
            align-items:center;
            gap:8px;
            padding:8px 16px;
            border-radius:12px;
            background:#eaf9ef;
            color:#15803d;
        }

        .action-card button{
            width:100%;
            padding:16px;
            margin-bottom:15px;
            border:none;
            border-radius:14px;
            font-size:18px;
            cursor:pointer;
        }

        .btn-green{
            background:#10b981;
            color:white;
        }

        .btn-blue{
            background:#2563eb;
            color:white;
        }

        .btn-white{
            background:white;
            border:1px solid #d1d5db !important;
        }

        .btn-red{
            background:white;
            color:red;
            border:1px solid #fca5a5 !important;
        }

        .upcoming{
            background:linear-gradient(135deg,#2563eb,#2563eb);
            color:white;
        }

        .upcoming h3{
            margin-bottom:20px;
        }

        .upcoming .time{
            font-size:48px;
            font-weight:700;
        }

        .followup{
            margin-top:25px;
        }

        .follow-header{
            display:flex;
            align-items:center;
            gap:15px;
            margin-bottom:20px;
        }

        .follow-icon{
            width:45px;
            height:45px;
            background:#d1fae5;
            color:#0f766e;
            border-radius:12px;
            display:flex;
            justify-content:center;
            align-items:center;
        }

        .follow-content{
            background:#eef9f5;
            border-radius:16px;
            padding:25px;
        }

        .follow-content p{
            margin-bottom:22px;
            color:#6b7280;
        }

        .follow-content h3{
            margin-top:5px;
        }

        @media(max-width:1100px){

            .content{
                grid-template-columns:1fr;
            }

            .sidebar{
                display:none;
            }

        }
        .logo{
            padding:30px;
        }

        .logo h2{
            font-size:34px;
            font-weight:700;
            margin-bottom:6px;
        }

        .logo p{
            color:#64748b;
            font-size:16px;
        }

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
        .main-1{
            padding: 25px 35px;
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
    </style>
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

            <a href="medicalrecordmanagement.jsp">
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

    <!-- MAIN -->

    <main class="main">
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
        <div class="main-1">

            <div class="back-btn">
                <i class="fa-solid fa-arrow-left"></i>
                Back to Appointments
            </div>



            <div class="page-title">
                <h1>Appointment Details</h1>
                <p>View and manage patient appointment information</p>
            </div>

            <div class="content">

                <!-- LEFT -->

                <div>

                    <div class="card">

                        <div class="card-title">
                            Appointment Information
                        </div>

                        <div class="info-grid">

                            <div>
                                <div class="label">Appointment ID</div>
                                <div class="value">AP-2026-001</div>
                            </div>

                            <div>
                                <div class="label">Date & Time</div>
                                <div class="value">
                                    24/05/2026 - 09:30 AM
                                </div>
                            </div>

                            <div>
                                <div class="label">Patient Name</div>
                                <div class="value">
                                    Nguyễn Văn An
                                </div>
                            </div>

                            <div>
                                <div class="label">Doctor</div>
                                <div class="value">
                                    Dr. Sarah Johnson
                                </div>
                            </div>

                            <div>
                                <div class="label">Appointment Type</div>
                                <div class="value">
                                    Follow-up
                                </div>
                            </div>

                            <div>
                                <div class="label">Status</div>

                                <div class="status">
                                    <i class="fa-solid fa-circle-check"></i>
                                    Confirmed
                                </div>
                            </div>

                            <div>
                                <div class="label">Reason for Visit</div>
                                <div class="value">
                                    Diabetes Monitoring
                                </div>
                            </div>

                        </div>

                    </div>

                    <div class="card followup">

                        <div class="follow-header">

                            <div class="follow-icon">
                                <i class="fa-solid fa-clipboard-list"></i>
                            </div>

                            <h2>Follow-up Appointment</h2>

                        </div>

                        <div class="follow-content">

                            <p>Department</p>
                            <h3>Endocrinology</h3>

                            <br>

                            <p>Consultation Room</p>
                            <h3>Room 204</h3>

                            <br>

                            <p>Hospital Location</p>
                            <h3>Central Medical Hospital</h3>
                            <span>2nd Floor, Building A</span>

                        </div>

                    </div>

                </div>

                <!-- RIGHT -->

                <div>

                    <div class="card action-card">

                        <div class="card-title">
                            Actions
                        </div>

                        <button class="btn-green">
                            ✔ Confirm Appointment
                        </button>

                        <button class="btn-red">
                            Cancel Appointment
                        </button>

                    </div>

                    <div class="card upcoming" style="margin-top:25px">

                        <h3>
                            <i class="fa-regular fa-clock"></i>
                            Upcoming
                        </h3>

                        <div class="time">
                            30 minutes
                        </div>

                        <p>
                            until appointment starts
                        </p>

                    </div>

                </div>

            </div>
        </div>
    </main>

</div>

<script>

    document.querySelector(".btn-green").onclick = () => {
        alert("Appointment Confirmed");
    }

    document.querySelector(".btn-red").onclick = () => {
        if (confirm("Cancel this appointment?")) {
            alert("Appointment Cancelled");
        }
    }


</script>

</body>
</html>