# Doctor Module — Workflow & Swimlane Documentation

> **Phạm vi:** Chỉ mô tả chức năng Doctor **đã implement** trong source hiện tại.  
> **Kiến trúc:** Doctor → JSP → Controller → Service → DAO → Database → Response  
> **Không bao gồm:** Approve/reject appointment, CRUD cảnh báo AI, quản lý ngưỡng, trang sidebar placeholder.

---

## Mục lục

1. [A. Doctor System Overall Workflow Diagram](#a-doctor-system-overall-workflow-diagram)
2. [B. Doctor Module Swimlane Diagram (Tổng hợp)](#b-doctor-module-swimlane-diagram-tổng-hợp)
3. [C. Swimlane Diagrams Chi Tiết](#c-swimlane-diagrams-chi-tiết)
   - [C1. Patient Management](#c1-patient-management-swimlane)
   - [C2. Patient Detail & Medical Record View](#c2-patient-detail--medical-record-view-swimlane)
   - [C3. Medical History Filter](#c3-medical-history-filter-swimlane)
   - [C4. PDF Medical Report Export](#c4-pdf-medical-report-export-swimlane)
   - [C5. Medical Encounter Management (Create / View)](#c5-medical-encounter-management-swimlane)
   - [C6. Laboratory Result Viewing](#c6-laboratory-result-viewing-swimlane)
   - [C7. Prescription / Treatment Plan](#c7-prescription--treatment-plan-swimlane)
   - [C8. AI Analysis & Monitoring](#c8-ai-analysis--monitoring-swimlane)
   - [C9. Appointment Management](#c9-appointment-management-swimlane)
   - [C10. High-Risk Patient Monitoring](#c10-high-risk-patient-monitoring-swimlane)
4. [Bảng Endpoint Tham Chiếu](#bảng-endpoint-tham-chiếu)

---

## A. Doctor System Overall Workflow Diagram

### Mermaid

```mermaid
flowchart TD
    START([Start]) --> LOGIN[Doctor nhập username/password]
    LOGIN --> AUTH{Validate account<br/>UserDAO + Encode}
    AUTH -->|Invalid| ERR_LOGIN[Hiển thị lỗi login.jsp]
    ERR_LOGIN --> LOGIN
    AUTH -->|Valid & vai_tro = bac_si| SESS[Lưu session user + status=3]
    SESS --> DASH[Redirect GET /doctor-dashboard]

    DASH --> AUTHZ{AuthContext.requireDoctor}
    AUTHZ -->|Fail| FORBIDDEN[HTTP 403 / redirect login]
    AUTHZ -->|Pass| LOAD_DASH[Load stats + dangerous patients]
    LOAD_DASH --> DB_READ1[(Read v_patient_summary<br/>patients, medical_encounters)]
    LOAD_DASH --> AI_BATCH[Gemini enrichment optional]
    AI_BATCH --> SHOW_DASH[Render doctordashboard.jsp]

    SHOW_DASH --> SELECT{Doctor chọn chức năng}

    SELECT -->|Patient List| PM[GET /doctor/patient-list]
    SELECT -->|Medical Records| MR[GET /doctor/patient-records]
    SELECT -->|Appointments| AP[GET /doctor/appointments]
    SELECT -->|High-Risk Detail| HR[POST /doctor-dashboard id=]

    PM --> PROC_PM[Controller → DAO/Service → DB]
    MR --> PROC_MR[Controller → Service → DAO → DB]
    AP --> PROC_AP[Controller → AppointmentDAO → DB]
    HR --> PROC_HR[DangerousPatientService → DB + Gemini]

    PROC_PM --> OUT_PM[patientmanagement.jsp / patientdetail.jsp]
    PROC_MR --> OUT_MR[medicalrecordmanagement.jsp / detail / create]
    PROC_AP --> OUT_AP[doctorappointmentmanagement.jsp]
    PROC_HR --> OUT_HR[dangerouspatientanalysis.jsp]

    OUT_PM --> SELECT
    OUT_MR --> SELECT
    OUT_AP --> SELECT
    OUT_HR --> SELECT

    SELECT -->|Logout| LOGOUT[GET /Logincontroller?service=logout]
    LOGOUT --> END([End session])
```

### PlantUML

```plantuml
@startuml Doctor_Overall_Workflow
start
:Doctor nhập username/password;
:LoginController xác thực (UserDAO + Encode);

if (Tài khoản hợp lệ và vai_tro = bac_si?) then (Có)
  :Lưu session user, status=3;
  :Redirect /doctor-dashboard;
  :DoctorDashboardController.doGet;
  :AuthContext.requireDoctor;
  :Đọc DB (v_patient_summary, patients...);
  :DangerousPatientService phân tích rủi ro;
  :Hiển thị doctordashboard.jsp;
  repeat
    :Doctor chọn chức năng;
    switch (Chức năng?)
    case (Patient Management)
      :GET/POST /doctor/patient-list;
      :PatientDAO / PatientDetailService;
    case (Medical Records)
      :GET/POST /doctor/patient-records;
      :MedicalRecordService / MedicalEncounterDAO;
    case (Appointments)
      :GET/POST /doctor/appointments;
      :AppointmentDAO;
    case (High-Risk Detail)
      :POST /doctor-dashboard;
      :DangerousPatientService + Gemini;
    endswitch
    :Render JSP / PDF / JSON;
  repeat while (Tiếp tục?) is (Có)
  ->Không;
  :Logout;
else (Không)
  :Hiển thị lỗi login.jsp;
endif
stop
@enduml
```

### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor (`vai_tro = bac_si`) |
| **Trigger** | Truy cập `/Logincontroller`, submit form đăng nhập |
| **Main Flow** | Login → session → Dashboard → chọn module → Controller xử lý → hiển thị JSP/PDF/JSON |
| **Database Operation** | Read: `users`, `v_patient_summary`, `patients`, `medical_encounters`, `health_records`, `lab_results`, `prescriptions`, `medications`, `appointments`. Write: khi tạo encounter, treatment plan, cập nhật appointment |
| **Final Result** | Doctor thao tác trên dashboard, danh sách BN, hồ sơ khám, lịch hẹn, phân tích rủi ro |

---

## B. Doctor Module Swimlane Diagram (Tổng hợp)

### Mermaid

```mermaid
flowchart TB
    subgraph DOCTOR["Lane 1 — Doctor"]
        D1[Đăng nhập]
        D2[Xem Dashboard]
        D3[Quản lý BN / Hồ sơ / Lịch hẹn]
        D4[Xem phân tích AI]
    end

    subgraph WEB["Lane 2 — Web Application (JSP + Controller + Service)"]
        W1[LoginController]
        W2[DoctorDashboardController]
        W3[PatientListController]
        W4[MedicalEncounterController]
        W5[TreatmentPlanController]
        W6[DoctorAppointmentController]
        W7[PatientPdfExportController]
        W8[MedicalRecordPdfExportController]
        S1[PatientDetailService]
        S2[MedicalRecordService]
        S3[DangerousPatientService]
        S4[EncounterAiAnalysis]
        S5[PatientDetailPdfService]
    end

    subgraph DB["Lane 3 — Database (MySQL)"]
        DB1[(users)]
        DB2[(patients)]
        DB3[(v_patient_summary)]
        DB4[(medical_encounters)]
        DB5[(health_records)]
        DB6[(lab_results)]
        DB7[(prescriptions / medications)]
        DB8[(appointments)]
    end

    subgraph AI["Lane 4 — AI Service (Gemini)"]
        AI1[Gemini API via GeminiConfig]
        AI2[Rule-based fallback]
    end

    D1 --> W1 --> DB1
    W1 --> D2
    D2 --> W2
    W2 --> S3
    S3 --> DB2 & DB5 & DB6
    S3 --> AI1
    AI1 -.->|fail| AI2
    W2 --> D3

    D3 --> W3 & W4 & W6 & W7 & W8
    W3 --> S1 --> DB2 & DB4 & DB5 & DB6 & DB7
    W4 --> S2 --> DB4 & DB5 & DB6 & DB7
    W4 --> S4 --> AI1
    W5 --> DB4 & DB7
    W6 --> DB8
    W7 --> S5
    S5 --> S1

    W3 & W4 & W6 & W2 --> D4
    S4 & S3 --> D4
```

### PlantUML

```plantuml
@startuml Doctor_Module_Swimlane
|Doctor|
start
:Đăng nhập;
|Web Application|
:LoginController → UserDAO;
|Database|
:SELECT users;
|Web Application|
:Redirect /doctor-dashboard;
|Doctor|
:Xem Dashboard, chọn chức năng;

|Web Application|
fork
  :PatientListController;
  :PatientDetailService;
fork again
  :MedicalEncounterController;
  :MedicalRecordService;
fork again
  :DoctorAppointmentController;
  :AppointmentDAO;
fork again
  :DoctorDashboardController;
  :DangerousPatientService;
end fork
|Database|
:Query patients, encounters,\nhealth_records, lab_results,\nappointments;
|AI Service|
:Gemini API (optional)\n+ rule fallback;
|Web Application|
:Forward JSP / stream PDF / JSON;
|Doctor|
:Nhận kết quả hiển thị;
stop
@enduml
```

### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | Mọi HTTP request tới servlet `/doctor-*` sau khi đăng nhập |
| **Main Flow** | Doctor tương tác JSP → Controller kiểm tra `AuthContext` → Service (nếu có) → DAO → DB; AI lane tham gia khi phân tích encounter hoặc BN nguy hiểm |
| **Database Operation** | JDBC qua `DBContext.getConnection()`; view `v_patient_summary` cho dashboard/filter |
| **Final Result** | HTML JSP, file PDF (`application/pdf`), hoặc JSON (`action=analyze`) |

---

## C. Swimlane Diagrams Chi Tiết

---

### C1. Patient Management Swimlane

#### Mermaid

```mermaid
sequenceDiagram
    actor Doctor
    participant JSP as patientmanagement.jsp
    participant Ctrl as PatientListController
    participant DAO as PatientDAO
    participant DB as MySQL

    Doctor->>JSP: Mở /doctor/patient-list
    JSP->>Ctrl: GET /doctor/patient-list
    Ctrl->>Ctrl: AuthContext.requirePatientDataAccess
    Ctrl->>Ctrl: scopeDoctorId(user) → bac_si_id
    Ctrl->>DAO: searchPatients(keyword, glucose, hba1c, bmi, age, gender, diabetesType, action)
    DAO->>DB: SELECT v_patient_summary JOIN patients, users<br/>WHERE bac_si_id = ?
    DB-->>DAO: List Patient
    DAO-->>Ctrl: patients
    Ctrl->>JSP: forward + setAttribute patients
    JSP-->>Doctor: Hiển thị bảng BN + filter dropdown

    Doctor->>JSP: Chọn filter / nhập keyword
    JSP->>Ctrl: GET với query params
    Note over Ctrl,DAO: bloodPressure param có UI<br/>nhưng chưa có SQL effect
    Ctrl->>DAO: searchPatients(...)
    DAO->>DB: SELECT filtered
    DB-->>Doctor: Danh sách đã lọc
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor (admin `quan_tri_vien` cũng truy cập được list) |
| **Trigger** | GET `/doctor/patient-list` |
| **Main Flow** | Load danh sách BN được gán (`patients.bac_si_id`) → áp dụng filter client → re-query |
| **Database Operation** | `SELECT` trên `v_patient_summary`, `patients`, `users` |
| **Final Result** | `patientmanagement.jsp` với danh sách và trạng thái filter |

**Filter đã implement:** keyword, glucose, HbA1c, BMI, age, gender, diabetesType, action (no-update / no-followup).  
**Chưa implement:** filter huyết áp (UI only).

---

### C2. Patient Detail & Medical Record View Swimlane

#### Mermaid

```mermaid
sequenceDiagram
    actor Doctor
    participant JSP as patientdetail.jsp
    participant Ctrl as PatientListController
    participant Svc as PatientDetailService
    participant DAO as PatientDAO / MedicalEncounterDAO / HealthRecordDAO / LabResultDAO / PrescriptionDAO
    participant DB as MySQL

    Doctor->>JSP: Click "Xem chi tiết" (id=patientUuid)
    JSP->>Ctrl: GET /doctor/patient-list?id=
    Ctrl->>Ctrl: AuthContext + ensurePatientAccess
    Ctrl->>Svc: load(patientId, scopeDoctorId, fromDate, toDate)
    Svc->>DAO: findById, getLatestEncounter, getHealthRecord, getLatestLabSummary
    Svc->>DAO: getHistoryByPatientAndDateRange(fromDate, toDate)
    DAO->>DB: SELECT patients, medical_encounters, health_records, lab_results, prescriptions
    DB-->>Svc: DetailBundle
    Svc-->>Ctrl: patient, encounter, healthRecord, history
    Ctrl->>JSP: forward patientdetail.jsp
    JSP-->>Doctor: Profile + health summary + lịch sử khám
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | GET `/doctor/patient-list?id={uuid}` hoặc POST cùng URL |
| **Main Flow** | `PatientDetailService.load()` tổng hợp profile, encounter mới nhất, health record, lab summary, prescription advice, history |
| **Database Operation** | Multi-table READ; history filter qua `MedicalEncounterDAO.getHistoryByPatientAndDateRange` |
| **Final Result** | Trang chi tiết BN với card thông tin và bảng lịch sử khám |

---

### C3. Medical History Filter Swimlane

#### Mermaid

```mermaid
flowchart LR
    subgraph Doctor
        A1[Chọn quick range<br/>hoặc custom date]
    end
    subgraph JSP
        B1[patientdetail.jsp<br/>history-range-filter<br/>history-custom-filter]
    end
    subgraph Controller
        C1[PatientListController<br/>forwardPatientDetail]
        C2{Validate fromDate/toDate}
    end
    subgraph Service
        D1[PatientDetailService.load]
    end
    subgraph DAO
        E1[MedicalEncounterDAO<br/>getHistoryByPatientAndDateRange]
    end
    subgraph Database
        F1[(medical_encounters<br/>WHERE ngay_kham BETWEEN ? AND ?)]
    end

    A1 --> B1
    B1 -->|GET id + fromDate + toDate| C1
    C1 --> C2
    C2 -->|Invalid partial dates| C1
    C2 -->|Set historyFilterError| B1
    C2 -->|Valid| D1
    D1 --> E1 --> F1
    F1 --> B1
    B1 --> A1
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | Quick link GET hoặc submit form custom `fromDate`/`toDate` |
| **Main Flow** | Controller parse date → validate (cả hai hoặc không có) → resolve `activeQuickRange` → Service load history filtered |
| **Database Operation** | `SELECT medical_encounters WHERE benh_nhan_id = ? AND ngay_kham BETWEEN ? AND ?` |
| **Final Result** | Bảng lịch sử khám theo khoảng thời gian; empty state nếu không có bản ghi |

**Quick ranges:** 5 / 10 / 30 ngày (bao gồm hôm nay), Tất cả (không gửi date param).

---

### C4. PDF Medical Report Export Swimlane

#### Mermaid

```mermaid
sequenceDiagram
    actor Doctor
    participant JSP as patientdetail.jsp
    participant Ctrl as PatientPdfExportController
    participant Svc as PatientDetailService
    participant PDF as PatientDetailPdfService
    participant DAO as MedicalEncounterDAO
    participant DB as MySQL

    Doctor->>JSP: Click "Xuất PDF"
    Note over JSP: URL gồm id + fromDate + toDate<br/>(cùng filter đang active)
    JSP->>Ctrl: GET /doctor/export-patient-pdf?id=&fromDate=&toDate=
    Ctrl->>Ctrl: requirePatientDataAccess + ensurePatientAccess
    Ctrl->>Svc: load(patientId, scopeDoctorId, fromDate, toDate)
    Svc->>DAO: getHistoryByPatientAndDateRange(fromDate, toDate)
    DAO->>DB: SELECT filtered encounters + related data
    DB-->>Svc: DetailBundle (history đã filter)
    Svc-->>Ctrl: bundle
    Ctrl->>PDF: generatePdf(bundle)
    PDF-->>Ctrl: byte[] PDF
    Ctrl-->>Doctor: application/pdf download
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | GET `/doctor/export-patient-pdf?id=&fromDate=&toDate=` |
| **Main Flow** | Dùng **cùng** `PatientDetailService.load()` như trang detail → PDF chỉ chứa history đã filter |
| **Database Operation** | READ giống patient detail; **không** query full history khi có fromDate/toDate |
| **Final Result** | File PDF tải về (`patient-{code}-detail.pdf`) |

> **Lưu ý:** Nếu `fromDate > toDate`, controller reset cả hai về null (export toàn bộ).

---

### C5. Medical Encounter Management Swimlane

#### Mermaid

```mermaid
flowchart TB
    subgraph Doctor
        D1[Xem danh sách hồ sơ]
        D2[Tạo hồ sơ mới]
        D3[Xem chi tiết encounter]
        D4[Hoàn thành phác đồ Bước 2]
    end

    subgraph Web["Controller + Service"]
        C1[MedicalEncounterController GET]
        C2[action=add → add-medical-encounter.jsp]
        C3[action=form → MedicalRecordService.create]
        C4[action=detail → load DTO]
        C5[action=analyze → EncounterAiAnalysis JSON]
        C6[TreatmentPlanController POST]
    end

    subgraph DB
        DB1[(medical_encounters)]
        DB2[(health_records)]
        DB3[(lab_results)]
        DB4[(prescriptions / medications)]
    end

    D1 --> C1 --> DB1
    D2 --> C2 --> D2
    D2 --> C5
    D2 --> C3 --> DB1 & DB2 & DB3
    C3 -->|tai_kham_noi_tiet| C6
    D4 --> C6 --> DB1 & DB4
    D3 --> C4 --> DB1 & DB2 & DB3 & DB4
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | GET/POST `/doctor/patient-records`, GET/POST `/doctor/treatment-plan` |
| **Main Flow** | List → Create (3 loại: nội tiết / CBC / sinh hóa) → optional AI analyze → save step 1 → treatment plan step 2 (chỉ nội tiết) |
| **Database Operation** | INSERT encounter + health_records/lab_results; UPDATE diagnosis/prescription ở step 2; DELETE cascade qua `action=delete` (backend only, **không có UI**) |
| **Final Result** | `medicalrecordmanagement.jsp`, `add-medical-encounter.jsp`, `medicalrecorddetail.jsp`, `treatment-plan.jsp` |

**Encounter types:** `tai_kham_noi_tiet`, `mau_tong_quat`, `sinh_hoa_mau`.

---

### C6. Laboratory Result Viewing Swimlane

#### Mermaid

```mermaid
sequenceDiagram
    actor Doctor
    participant JSP as medicalrecorddetail.jsp
    participant Ctrl as MedicalEncounterController
    participant Svc as MedicalRecordService
    participant DAO as LabResultDAO
    participant DB as MySQL

    Doctor->>JSP: POST action=detail id=encounterId
    JSP->>Ctrl: POST /doctor/patient-records
    Ctrl->>Ctrl: ensureEncounterAccess
    Ctrl->>Svc: loadEncounterDetail(encounterId)
    Svc->>DAO: findByEncounterId(encounterId)
    DAO->>DB: SELECT lab_results WHERE encounter_id = ?
    DB-->>Svc: LabResult (CBC / Biochemistry fields)
    Svc-->>Ctrl: MedicalEncounterDTO.bloodCount / .biochemistry
    Ctrl->>JSP: forward medicalrecorddetail.jsp
    JSP-->>Doctor: Hiển thị WBC, RBC, glucose, HbA1c, cholesterol, creatinine...
    Note over JSP: Highlight giá trị bất thường<br/>Không có module lab riêng
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | Xem chi tiết encounter loại `mau_tong_quat` hoặc `sinh_hoa_mau` |
| **Main Flow** | Lab data gắn 1:1 với encounter (`lab_results.encounter_id` unique) |
| **Database Operation** | READ `lab_results`; WRITE khi tạo encounter (MedicalRecordService) |
| **Final Result** | Lab sections trong encounter detail; PDF export `type=blood` hoặc `type=biochemistry` |

**Không implement:** Quản lý lab độc lập, biểu đồ xu hướng lab theo thời gian.

---

### C7. Prescription / Treatment Plan Swimlane

#### Mermaid

```mermaid
sequenceDiagram
    actor Doctor
    participant JSP as treatment-plan.jsp
    participant Ctrl as TreatmentPlanController
    participant DAO as MedicalEncounterDAO / PrescriptionDAO / MedicationDAO / PatientDAO
    participant DB as MySQL

    Doctor->>JSP: Redirect sau Bước 1 (tai_kham_noi_tiet)
    JSP->>Ctrl: GET /doctor/treatment-plan?id=encounterId
    Ctrl->>DAO: load encounter + existing prescription/medications
    DAO->>DB: SELECT medical_encounters, prescriptions, medications
    DB-->>JSP: Form với chẩn đoán, thuốc, AI summary (session)

    Doctor->>JSP: Nhập chẩn đoán + thuốc → Submit
    JSP->>Ctrl: POST /doctor/treatment-plan
    Ctrl->>Ctrl: ensureEncounterAccess
    Ctrl->>DAO: updateEncounterDiagnosis
    Ctrl->>DAO: updatePatientDiabetesType
    Ctrl->>DAO: replacePrescription + insertMedications
    DAO->>DB: UPDATE medical_encounters, patients<br/>DELETE+INSERT prescriptions, medications
    DB-->>Ctrl: success
    Ctrl-->>Doctor: redirect /doctor/patient-records?success=1
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | GET/POST `/doctor/treatment-plan?id={encounterId}` sau khi tạo encounter nội tiết |
| **Main Flow** | Cập nhật chẩn đoán chính/phụ, hướng xử trí, phân loại tiểu đường, danh sách thuốc |
| **Database Operation** | UPDATE `medical_encounters`, `patients`; replace `prescriptions` + `medications` |
| **Final Result** | Đơn thuốc lưu DB; BN thấy advice trên patient dashboard qua prescription read |

**Không implement:** Workflow duyệt đơn, trang quản lý prescription độc lập.

---

### C8. AI Analysis & Monitoring Swimlane

#### Mermaid

```mermaid
flowchart TB
    subgraph Doctor
        D1[Phân tích AI khi tạo hồ sơ]
        D2[Xem BN nguy hiểm trên Dashboard]
        D3[Xem phân tích chi tiết BN]
    end

    subgraph Web
        W1[MedicalEncounterController action=analyze]
        W2[EncounterAiAnalysis]
        W3[DoctorDashboardController]
        W4[DangerousPatientService]
    end

    subgraph AI
        G1[Gemini API]
        R1[Rule-based HealthMetricAssessment]
    end

    subgraph DB
        DB1[(health_records / lab_results / patients)]
    end

    D1 --> W1 --> W2 --> G1
    G1 -.->|fail/invalid JSON| R1
    W2 --> D1

    D2 --> W3 --> W4 --> DB1
    W4 --> G1
    W4 --> R1
    W4 --> D2

    D3 --> W3
    W4 --> D3
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | AJAX `action=analyze` trên form tạo hồ sơ; load dashboard; POST drill-down BN nguy hiểm |
| **Main Flow** | Gemini phân tích chỉ số lâm sàng → fallback rule nếu lỗi; dashboard batch tối đa 20 BN + enrich Gemini |
| **Database Operation** | **Chỉ READ** — không lưu AI result vào DB (session `aiSummary:{encounterId}` cho treatment plan) |
| **Final Result** | JSON trên form create; cards trên dashboard; `dangerouspatientanalysis.jsp` |

**Không implement:** CRUD cảnh báo, cập nhật trạng thái alert, lưu persistent AI analysis, gửi notification.

---

### C9. Appointment Management Swimlane

#### Mermaid

```mermaid
sequenceDiagram
    actor Doctor
    participant JSP as doctorappointmentmanagement.jsp
    participant Ctrl as DoctorAppointmentController
    participant DAO as AppointmentDAO
    participant DB as MySQL

    Doctor->>JSP: Mở /doctor/appointments
    JSP->>Ctrl: GET status, keyword, fromDate, toDate
    Ctrl->>Ctrl: requirePatientDataAccess
    Ctrl->>DAO: findAll(scopeDoctorId, filters)
    DAO->>DB: SELECT appointments JOIN patients, users
    DB-->>JSP: Danh sách lịch hẹn

    Doctor->>JSP: Click "Đã khám" hoặc "Hủy"
    JSP->>Ctrl: POST id, status=da_kham|da_huy
    Ctrl->>Ctrl: requireDoctor
    alt status = da_kham AND current = cho_kham
        Ctrl->>DAO: updateStatus(id, da_kham)
    else status = da_huy AND current = cho_kham
        Ctrl->>DAO: updateStatus(id, da_huy)
    else Invalid transition
        Ctrl->>JSP: redirect + error
    end
    DAO->>DB: UPDATE appointments SET trang_thai = ?
    DB-->>Doctor: Redirect danh sách đã cập nhật
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | GET/POST `/doctor/appointments` |
| **Main Flow** | Xem lịch → lọc theo status/keyword/date → đánh dấu hoàn thành hoặc hủy |
| **Database Operation** | SELECT + UPDATE `appointments.trang_thai` |
| **Final Result** | `doctorappointmentmanagement.jsp` với trạng thái mới |

**Không implement:** Approve/reject yêu cầu đặt lịch, tạo/reschedule lịch, filter `type` (UI only), thông báo patient.

**Trạng thái:** `cho_kham` → `da_kham` | `da_huy`.

---

### C10. High-Risk Patient Monitoring Swimlane

#### Mermaid

```mermaid
sequenceDiagram
    actor Doctor
    participant Dash as doctordashboard.jsp
    participant Ctrl as DoctorDashboardController
    participant Svc as DangerousPatientService
    participant DAO as PatientDAO / HealthRecordDAO / LabResultDAO
    participant AI as Gemini API
    participant Detail as dangerouspatientanalysis.jsp
    participant DB as MySQL

    Doctor->>Dash: GET /doctor-dashboard
    Dash->>Ctrl: doGet
    Ctrl->>Svc: analyzeDangerousPatients(scopeDoctorId)
    Svc->>DAO: load all assigned patients + latest metrics
    DAO->>DB: SELECT patients, health_records, lab_results
    Svc->>Svc: Rule scoring (glucose, HbA1c, BMI, BP, monitoring gap)
    Svc->>AI: Batch enrich top candidates (optional)
    Svc-->>Dash: urgentPatients (max 20), analysisResult

    Doctor->>Dash: Click card BN nguy hiểm
    Dash->>Ctrl: POST id=patientUuid
    Ctrl->>Svc: getDangerousPatientDetail(patientId)
    Svc->>DAO: load patient profile + recent records
    Svc->>AI: analyzePatientDetail or rule fallback
    alt patient not dangerous
        Ctrl-->>Doctor: redirect dashboard
    else dangerous
        Ctrl->>Detail: forward HighRiskPatientDTO
        Detail-->>Doctor: Risk score, factors, recommendations
    end
```

#### Giải thích

| Mục | Nội dung |
|---|---|
| **Actor** | Doctor |
| **Trigger** | Load dashboard; POST drill-down với `id` |
| **Main Flow** | Rule engine xếp hạng rủi ro → hiển thị top urgent → chi tiết AI/rule per patient |
| **Database Operation** | READ `patients`, `health_records`, `lab_results`, `v_patient_summary` |
| **Final Result** | Dashboard cards + trang `dangerouspatientanalysis.jsp` |

**Không implement:** URL `/doctor/high-risk` riêng, acknowledgment alert, scheduled monitoring job.

---

## Bảng Endpoint Tham Chiếu

| Chức năng | Method | URL | Controller | Service chính | DB Tables |
|---|---|---|---|---|---|
| Login | GET/POST | `/Logincontroller` | `LoginController` | — | `users` |
| Dashboard | GET | `/doctor-dashboard` | `DoctorDashboardController` | `DangerousPatientService` | `v_patient_summary`, `patients`, `medical_encounters` |
| High-risk detail | POST | `/doctor-dashboard` | `DoctorDashboardController` | `DangerousPatientService` | `patients`, `health_records`, `lab_results` |
| Patient list | GET | `/doctor/patient-list` | `PatientListController` | — | `v_patient_summary`, `patients`, `users` |
| Patient detail | GET/POST | `/doctor/patient-list?id=` | `PatientListController` | `PatientDetailService` | `patients`, `medical_encounters`, `health_records`, `lab_results`, `prescriptions` |
| Export patient PDF | GET | `/doctor/export-patient-pdf` | `PatientPdfExportController` | `PatientDetailService`, `PatientDetailPdfService` | (same as detail, filtered history) |
| Medical records list | GET | `/doctor/patient-records` | `MedicalEncounterController` | — | `medical_encounters`, `patients` |
| Create / AI / Detail | POST | `/doctor/patient-records?action=` | `MedicalEncounterController` | `MedicalRecordService`, `EncounterAiAnalysis` | `medical_encounters`, `health_records`, `lab_results` |
| Treatment plan | GET/POST | `/doctor/treatment-plan` | `TreatmentPlanController` | — | `medical_encounters`, `prescriptions`, `medications`, `patients` |
| Export encounter PDF | GET | `/doctor/record-export-pdf` | `MedicalRecordPdfExportController` | `MedicalRecordService`, PDF service | `medical_encounters` + related |
| Appointments | GET/POST | `/doctor/appointments` | `DoctorAppointmentController` | — | `appointments`, `patients`, `users` |

---

## Phụ lục — Chức năng KHÔNG implement (tránh mô tả sai trong báo cáo)

| Mục trong yêu cầu gốc | Thực tế trong hệ thống |
|---|---|
| Approve/Reject appointment | Chỉ **Mark Completed** (`da_kham`) và **Cancel** (`da_huy`) |
| Update alert status | Không có bảng/DAO alert; `canh_bao_chua_doc` chỉ đọc từ view |
| Manage Threshold Settings | Không có |
| Standalone Laboratory module | Lab nằm trong encounter create/detail |
| Standalone Prescription list | Chỉ qua Treatment Plan (Bước 2) |
| Delete medical record (UI) | Backend `action=delete` có; JSP chưa có nút x xóa |
| Sidebar Cảnh báo khẩn cấp / Phân tích dữ liệu | Placeholder, không có servlet |

---

*Tài liệu được sinh từ phân tích source tại `src/main/java/com/example/diabetesmanage/controller/doctor/` và JSP tương ứng. Cập nhật: 2026-07-20.*
