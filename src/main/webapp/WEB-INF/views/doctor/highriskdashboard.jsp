<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <title>High-Risk Dashboard</title>

    <link rel="stylesheet" href="css/highriskdashboard.css"/>

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

            <a href="highriskdashboard.html">
                <i class="fa-solid fa-heart-pulse"></i>
                High Risk Dashboard
            </a>

        </nav>

    </aside>

    <!-- MAIN -->

    <main class="main-content">

        <!-- HEADER -->

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

        <!-- STATS -->
        <div class="page-content">
            <div class="stats-grid">

                <div class="stat-card orange">

                    <div class="stat-icon">
                        <i class="fa-solid fa-users"></i>
                    </div>

                    <h2>23</h2>

                    <p>High-Risk Patients</p>

                </div>

                <div class="stat-card red">

                    <div class="stat-icon">
                        <i class="fa-solid fa-circle-exclamation"></i>
                    </div>

                    <h2>5</h2>

                    <p>Critical Cases</p>

                </div>

                <div class="stat-card blue">

                    <div class="stat-icon">
                        <i class="fa-regular fa-bell"></i>
                    </div>

                    <h2>8</h2>

                    <p>New AI Alerts</p>

                </div>

                <div class="stat-card purple">

                    <div class="stat-icon">
                        <i class="fa-solid fa-arrow-trend-up"></i>
                    </div>

                    <h2>81%</h2>

                    <p>Avg Risk Score</p>

                </div>

            </div>

            <!-- CONTENT -->

            <div class="content-grid">

                <!-- TABLE -->

                <div class="table-card">

                    <div class="table-header">

                        <h2>High-Risk Patient List</h2>

                        <p>
                            Danh sách bệnh nhân có nguy cơ cao
                        </p>

                    </div>

                    <div class="table-wrapper">

                        <table>

                            <thead>

                            <tr>

                                <th>PATIENT</th>
                                <th>AGE</th>
                                <th>GLUCOSE</th>
                                <th>HBA1C</th>
                                <th>RISK SCORE</th>
                                <th>RISK LEVEL</th>
                                <th>ACTION</th>

                            </tr>

                            </thead>

                            <tbody id="patientTable">

                            </tbody>

                        </table>

                    </div>

                </div>

                <!-- CHART -->

                <div class="chart-card">

                    <div class="chart-top">

                        <div>
                            <h2>Risk Distribution</h2>
                            <p>Phân bố mức độ rủi ro</p>
                        </div>

                    </div>

                    <!-- BARS -->

                    <div class="bars">

                        <div class="bar-row">

                            <span>Critical</span>

                            <div class="bar-container">
                                <div class="bar critical-bar"></div>
                            </div>

                        </div>

                        <div class="bar-row">

                            <span>High</span>

                            <div class="bar-container">
                                <div class="bar high-bar"></div>
                            </div>

                        </div>

                        <div class="bar-row">

                            <span>Medium</span>

                            <div class="bar-container">
                                <div class="bar medium-bar"></div>
                            </div>

                        </div>

                        <div class="bar-row">

                            <span>Low</span>

                            <div class="bar-container">
                                <div class="bar low-bar"></div>
                            </div>

                        </div>

                    </div>

                    <!-- LEGEND -->

                    <div class="legend">

                        <div class="legend-item">
                            <span class="dot critical-dot"></span>
                            Critical
                            <strong>5</strong>
                        </div>

                        <div class="legend-item">
                            <span class="dot high-dot"></span>
                            High
                            <strong>23</strong>
                        </div>

                        <div class="legend-item">
                            <span class="dot medium-dot"></span>
                            Medium
                            <strong>15</strong>
                        </div>

                        <div class="legend-item">
                            <span class="dot low-dot"></span>
                            Low
                            <strong>8</strong>
                        </div>

                    </div>

                </div>

            </div>
        </div>
    </main>

</div>

<script>
    const patients = [

        {
            name: "David Wilson",
            id: "P0015",
            age: 61,
            glucose: "190 mg/dL",
            hba1c: "8.5%",
            riskScore: "91%",
            riskLevel: "Critical"
        },

        {
            name: "John Smith",
            id: "P0008",
            age: 58,
            glucose: "185 mg/dL",
            hba1c: "8.2%",
            riskScore: "87%",
            riskLevel: "High"
        },

        {
            name: "Michael Brown",
            id: "P0012",
            age: 63,
            glucose: "188 mg/dL",
            hba1c: "8.4%",
            riskScore: "89%",
            riskLevel: "Critical"
        },

        {
            name: "Robert Johnson",
            id: "P0004",
            age: 55,
            glucose: "172 mg/dL",
            hba1c: "7.8%",
            riskScore: "82%",
            riskLevel: "High"
        },

        {
            name: "James Davis",
            id: "P0009",
            age: 59,
            glucose: "178 mg/dL",
            hba1c: "8%",
            riskScore: "84%",
            riskLevel: "High"
        }

    ];

    const patientTable = document.getElementById("patientTable");

    function renderPatients() {

        patientTable.innerHTML = "";

        patients.forEach(patient => {

            patientTable.innerHTML += `

          <tr>

            <td>

              <div class="patient-name">
                ${patient.name}
              </div>

              <div class="patient-id">
                ${patient.id}
              </div>

            </td>

            <td>${patient.age}</td>

            <td class="glucose">
              ${patient.glucose}
            </td>

            <td class="hba">
              ${patient.hba1c}
            </td>

            <td>${patient.riskScore}</td>

            <td>

              <span class="badge ${
                patient.riskLevel === "Critical"
                    ? "critical"
                    : "high"
            }">

                ${patient.riskLevel}

              </span>

            </td>

            <td>
              <button class="action-btn">
                <i class="fa-regular fa-eye"></i>
                View Details
              </button>
            </td>

          </tr>

        `;
        });
    }

    renderPatients();
</script>

</body>
</html>