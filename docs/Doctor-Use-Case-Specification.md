# Doctor Module — Use Case Specification

> **Document Type:** Software Requirements Specification (SRS) — Use Case Specification  
> **Version:** 1.0  
> **Scope:** Role **Doctor** (`vai_tro = bac_si`) — chỉ chức năng **đã implement**  
> **Architecture:** Jakarta Servlet → JSP → Controller → Service → DAO → MySQL  
> **Reference:** [`Use-Case-Documentation.md`](Use-Case-Documentation.md), [`Doctor-Module-Documentation.md`](Doctor-Module-Documentation.md)

---

## Mục lục Use Case

| ID | Use Case Name | Priority |
|---|---|---|
| [UC-D01](#uc-d01-authenticate) | Authenticate | High |
| [UC-D02](#uc-d02-view-doctor-dashboard) | View Doctor Dashboard | High |
| [UC-D03](#uc-d03-monitor-high-risk-patients) | Monitor High-Risk Patients | High |
| [UC-D04](#uc-d04-view-high-risk-patient-analysis) | View High-Risk Patient Analysis | High |
| [UC-D05](#uc-d05-manage-patient-list) | Manage Patient List | High |
| [UC-D06](#uc-d06-search-and-filter-patient-list) | Search and Filter Patient List | High |
| [UC-D07](#uc-d07-view-patient-detail) | View Patient Detail | High |
| [UC-D08](#uc-d08-filter-encounter-history) | Filter Encounter History | Medium |
| [UC-D09](#uc-d09-export-patient-detail-pdf) | Export Patient Detail PDF | Medium |
| [UC-D10](#uc-d10-manage-medical-encounters) | Manage Medical Encounters | High |
| [UC-D11](#uc-d11-search-and-filter-medical-encounters) | Search and Filter Medical Encounters | Medium |
| [UC-D12](#uc-d12-create-medical-encounter) | Create Medical Encounter | High |
| [UC-D13](#uc-d13-analyze-encounter-with-ai) | Analyze Encounter with AI | Medium |
| [UC-D14](#uc-d14-view-medical-encounter-detail) | View Medical Encounter Detail | High |
| [UC-D15](#uc-d15-view-laboratory-results) | View Laboratory Results | High |
| [UC-D16](#uc-d16-delete-medical-encounter) | Delete Medical Encounter | Low |
| [UC-D17](#uc-d17-export-medical-encounter-pdf) | Export Medical Encounter PDF | Medium |
| [UC-D18](#uc-d18-complete-treatment-plan) | Complete Treatment Plan | High |
| [UC-D19](#uc-d19-manage-appointments) | Manage Appointments | High |
| [UC-D20](#uc-d20-filter-appointment-list) | Filter Appointment List | Medium |
| [UC-D21](#uc-d21-mark-appointment-as-completed) | Mark Appointment as Completed | Medium |
| [UC-D22](#uc-d22-cancel-appointment) | Cancel Appointment | Medium |

---

## Template chuẩn (tham chiếu)

Mỗi Use Case Specification gồm:

| Mục | Ý nghĩa |
|---|---|
| **Primary Actor** | Người khởi tạo use case |
| **Goal** | Mục tiêu nghiệp vụ |
| **Preconditions** | Điều kiện trước khi bắt đầu |
| **Postconditions** | Trạng thái hệ thống sau khi thành công |
| **Trigger** | Sự kiện khởi phát |
| **Main Success Scenario** | Luồng chính (happy path) |
| **Alternative Flows** | Luồng thay thế hợp lệ |
| **Exception Flows** | Luồng lỗi / từ chối |
| **Business Rules** | Quy tắc nghiệp vụ |
| **Special Requirements** | Phi chức năng, ràng buộc kỹ thuật |
| **Result** | Kết quả đầu ra |

---

## UC-D01: Authenticate

| | |
|---|---|
| **Use Case ID** | UC-D01 |
| **Use Case Name** | Authenticate |
| **Primary Actor** | Doctor |
| **Secondary Actors** | System (LoginController, UserDAO) |
| **Priority** | High |

### Goal

Doctor đăng nhập an toàn vào hệ thống và được chuyển tới khu vực làm việc Doctor Dashboard.

### Preconditions

- Doctor đã có tài khoản trong bảng `users` với `vai_tro = bac_si`.
- Tài khoản đang active (hệ thống cho phép đăng nhập).
- Trình duyệt hỗ trợ HTTP session cookie.

### Postconditions (Success)

- Session chứa `user` (`User` object) và `status = 3`.
- Doctor được redirect tới `/doctor-dashboard`.

### Trigger

Doctor truy cập `/Logincontroller` và submit form đăng nhập.

### Main Success Scenario

1. Doctor mở trang `login.jsp`.
2. Doctor nhập `UserName` và `password`.
3. Doctor nhấn **Login**.
4. Hệ thống gọi `LoginController` với `service=checkaccount`.
5. Hệ thống tra cứu `users` qua `UserDAO` và so khớp mật khẩu (`Encode`).
6. Hệ thống xác nhận `vai_tro = bac_si`.
7. Hệ thống tạo HTTP session, lưu `user`, set `status=3`.
8. Hệ thống redirect `/doctor-dashboard`.
9. Use case kết thúc thành công.

### Alternative Flows

**AF-01: Logout**

1. Doctor đã đăng nhập chọn **Đăng xuất** (`/Logincontroller?service=logout`).
2. Hệ thống invalidate session.
3. Hệ thống redirect về trang login.
4. Use case kết thúc.

### Exception Flows

**EF-01: Sai username hoặc password**

- Tại bước 5: credentials không khớp.
- Hệ thống forward `login.jsp` với `AccountError`.
- Use case kết thúc thất bại.

**EF-02: Role không phải bác sĩ**

- Tại bước 6: role khác `bac_si` → redirect dashboard tương ứng role (admin/patient).
- Use case kết thúc (không vào module Doctor).

**EF-03: Truy cập Doctor endpoint khi chưa login**

- `AuthContext.requireDoctor` / `requireLogin` redirect `/Logincontroller`.
- Use case Authenticate được kích hoạt gián tiếp.

### Business Rules

- BR-D01-01: Mật khẩu lưu dạng hash, không so sánh plain text.
- BR-D01-02: Mỗi servlet Doctor tự gọi `AuthContext`; không có global filter trong `web.xml`.

### Special Requirements

- UTF-8 encoding trên request/response.
- Session-based authentication (không JWT).

### Result

Doctor đã xác thực, có quyền truy cập các endpoint `/doctor-*`.

---

## UC-D02: View Doctor Dashboard

| | |
|---|---|
| **Use Case ID** | UC-D02 |
| **Use Case Name** | View Doctor Dashboard |
| **Primary Actor** | Doctor |
| **Secondary Actors** | DoctorDashboardDAO, DangerousPatientService |
| **Priority** | High |
| **Include** | UC-D03 (Monitor High-Risk Patients) |

### Goal

Doctor xem tổng quan hoạt động điều trị: thống kê BN, phân bố rủi ro, số cảnh báo, lần khám trong khoảng ngày, và danh sách BN nguy hiểm.

### Preconditions

- UC-D01 hoàn thành; Doctor đã đăng nhập.
- Doctor có `vai_tro = bac_si`.

### Postconditions

- Dashboard hiển thị `stats`, `urgentPatients`, `analysisResult`.
- Không thay đổi dữ liệu DB (read-only).

### Trigger

Doctor truy cập `/doctor-dashboard` (GET) hoặc chọn menu **Tổng quan**.

### Main Success Scenario

1. Doctor gửi GET `/doctor-dashboard`.
2. `DoctorDashboardController.doGet` gọi `AuthContext.requireDoctor`.
3. Hệ thống đọc `startDate`, `endDate` (optional).
4. `DoctorDashboardDAO.getDashboardStats(doctorId, startDate, endDate)` truy vấn `v_patient_summary`, `medical_encounters`.
5. `DangerousPatientService.analyzeDangerousPatients(doctorId)` tính BN nguy hiểm.
6. Hệ thống gán attribute và forward `doctordashboard.jsp`.
7. Doctor xem KPI cards, biểu đồ phân bố rủi ro, danh sách hồ sơ nguy hiểm.
8. Use case kết thúc thành công.

### Alternative Flows

**AF-01: Lọc thống kê theo khoảng ngày**

1. Doctor chọn `startDate` và `endDate` trên stat card "Hồ sơ khám bệnh".
2. Submit GET với params.
3. Bước 4–7 Main Flow lặp lại với khoảng ngày mới.

### Exception Flows

**EF-01: Chưa đăng nhập / không phải bác sĩ**

- `requireDoctor` redirect login hoặc HTTP 403.
- Dashboard không hiển thị.

**EF-02: Lỗi SQL stats**

- DAO fallback `loadStatsWithoutAlertsColumn` (nếu cột alert không tồn tại).
- Dashboard vẫn hiển thị với số liệu giảm thiểu.

**EF-03: Gemini lỗi**

- `DangerousPatientService` fallback rule-based; dashboard vẫn load.
- Hiển thị banner trạng thái Gemini trên UI.

### Business Rules

- BR-D02-01: Dashboard chỉ hiển thị BN có `patients.bac_si_id = doctor.id`.
- BR-D02-02: Phân nhóm rủi ro glucose: &lt;140 / 140–179 / 180–249 / ≥250 mg/dL.

### Special Requirements

- Chart.js render donut chart phía client.
- Response time phụ thuộc batch AI (tối đa ~15 BN gọi Gemini).

### Result

Doctor có cái nhìn tổng quan và danh sách ưu tiên BN cần theo dõi.

---

## UC-D03: Monitor High-Risk Patients

| | |
|---|---|
| **Use Case ID** | UC-D03 |
| **Use Case Name** | Monitor High-Risk Patients |
| **Primary Actor** | Doctor |
| **Secondary Actors** | DangerousPatientService, Gemini API (optional) |
| **Priority** | High |
| **Relationship** | «include» UC-D02 |

### Goal

Doctor theo dõi danh sách BN có chỉ số sức khỏe bất thường, được xếp hạng theo mức rủi ro.

### Preconditions

- UC-D02 đang thực thi hoặc Doctor đã mở dashboard.

### Postconditions

- Tối đa 20 BN nguy hiểm hiển thị dưới section "Hồ sơ nguy hiểm".
- Không ghi DB (analysis runtime only).

### Trigger

Dashboard load thành công (included trong UC-D02).

### Main Success Scenario

1. `DangerousPatientService.analyzeDangerousPatients(doctorId)` load BN được gán.
2. Service đọc `health_records`, `lab_results` mới nhất.
3. Rule engine chấm điểm: glucose, HbA1c, BP, BMI, monitoring gap, trend.
4. Service lọc BN `isDangerous() == true`.
5. (Optional) Gemini enrich insight cho top candidates.
6. Hệ thống render danger cards: tên, mã BN, vital, metric tags, AI text.
7. Doctor đọc và ưu tiên xử lý.
8. Use case kết thúc.

### Alternative Flows

**AF-01: Không có BN nguy hiểm**

- Bước 4: danh sách rỗng.
- UI hiển thị "Không có hồ sơ bệnh án nguy hiểm."

### Exception Flows

**EF-01: Gemini không cấu hình**

- Fallback hoàn toàn rule-based; banner cảnh báo trên UI.

### Business Rules

- BR-D03-01: Ngưỡng rule hardcoded trong Java (`HealthMetricAssessment`).
- BR-D03-02: `canh_bao_chua_doc` trên view chỉ đếm read-only; không có CRUD alert.

### Result

Doctor nhận danh sách BN ưu tiên kèm insight (AI hoặc rule).

---

## UC-D04: View High-Risk Patient Analysis

| | |
|---|---|
| **Use Case ID** | UC-D04 |
| **Use Case Name** | View High-Risk Patient Analysis |
| **Primary Actor** | Doctor |
| **Secondary Actors** | DangerousPatientService, Gemini API |
| **Priority** | High |
| **Extend** | UC-D03 |

### Goal

Doctor xem phân tích chi tiết (AI/rule) cho một BN nguy hiểm được chọn từ dashboard.

### Preconditions

- UC-D01, UC-D02 hoàn thành.
- BN thuộc quyền doctor (`ensurePatientAccess`).

### Postconditions

- Trang `dangerouspatientanalysis.jsp` hiển thị `HighRiskPatientDTO`.
- Không persist AI result.

### Trigger

Doctor nhấn **Xem phân tích chi tiết** trên danger card → POST `/doctor-dashboard` với `id=patientUuid`.

### Main Success Scenario

1. Doctor submit POST `/doctor-dashboard` với `id`.
2. `DoctorDashboardController.doPost` gọi `requireDoctor`.
3. Hệ thống gọi `ensurePatientAccess`.
4. `DangerousPatientService.getDangerousPatientDetail(patientId, doctorId)`.
5. Service build risk profile + gọi Gemini hoặc fallback.
6. Nếu BN vẫn được đánh dấu nguy hiểm → forward `dangerouspatientanalysis.jsp`.
7. Doctor xem score, factors, recommendations, lịch sử đo.
8. Use case kết thúc.

### Alternative Flows

**AF-01: Quay lại dashboard**

1. Doctor nhấn link quay lại.
2. GET `/doctor-dashboard`.

### Exception Flows

**EF-01: Thiếu `id`**

- Redirect `/doctor-dashboard`.

**EF-02: BN không nguy hiểm hoặc không truy cập được**

- `getDangerousPatientDetail` trả `null` → redirect dashboard.

**EF-03: BN không thuộc doctor**

- `ensurePatientAccess` fail → HTTP 403 / redirect.

### Business Rules

- BR-D04-01: Chỉ BN pass rule `isDangerous()` mới vào trang chi tiết.

### Result

Doctor có báo cáo phân tích sâu để ra quyết định lâm sàng (ngoài hệ thống).

---

## UC-D05: Manage Patient List

| | |
|---|---|
| **Use Case ID** | UC-D05 |
| **Use Case Name** | Manage Patient List |
| **Primary Actor** | Doctor |
| **Secondary Actors** | PatientDAO |
| **Priority** | High |

### Goal

Doctor xem danh sách BN được gán (`bac_si_id`) để theo dõi và chọn BN cần xử lý.

### Preconditions

- UC-D01 hoàn thành.

### Postconditions

- Bảng BN hiển thị trên `patientmanagement.jsp`.

### Trigger

Doctor truy cập GET `/doctor/patient-list` (không có param `id`).

### Main Success Scenario

1. Doctor chọn menu **Danh sách bệnh nhân**.
2. `PatientListController.doGet` gọi `requirePatientDataAccess`.
3. `scopeDoctorId = AuthContext.scopeDoctorId(user)`.
4. `PatientDAO.searchPatients(...)` với filter rỗng.
5. Forward `patientmanagement.jsp` với `patients`.
6. Doctor xem bảng: mã BN, họ tên, tuổi, giới, email, loại tiểu đường, ngày cập nhật.
7. Use case kết thúc.

### Alternative Flows

**AF-01: Mở chi tiết BN**

1. Doctor nhấn icon 👁 (POST `id`).
2. Chuyển UC-D07 View Patient Detail.

### Exception Flows

**EF-01: Chưa đăng nhập**

- Redirect login.

**EF-02: Danh sách rỗng**

- Bảng không có dòng; không lỗi.

### Business Rules

- BR-D05-01: Doctor chỉ thấy BN có `patients.bac_si_id = doctor.id`.
- BR-D05-02: Admin (`quan_tri_vien`) có `scopeDoctorId=null` → thấy tất cả BN.

### Result

Doctor có danh sách BN được phân công.

---

## UC-D06: Search and Filter Patient List

| | |
|---|---|
| **Use Case ID** | UC-D06 |
| **Use Case Name** | Search and Filter Patient List |
| **Primary Actor** | Doctor |
| **Priority** | High |
| **Extend** | UC-D05 |

### Goal

Doctor thu hẹp danh sách BN theo keyword và các tiêu chí lâm sàng/demographic.

### Preconditions

- UC-D05 — Doctor đang ở trang patient list.

### Postconditions

- Danh sách reload theo filter; query params phản ánh trên URL.

### Trigger

Doctor nhập keyword hoặc chọn option trong filter dropdown.

### Main Success Scenario

1. Doctor nhập keyword và/hoặc chọn filter (glucose, HbA1c, BMI, age, gender, diabetesType, action).
2. Browser gửi GET `/doctor/patient-list?keyword=&glucose=&...`.
3. `PatientDAO.searchPatients` build dynamic SQL trên `v_patient_summary`.
4. Hệ thống render bảng đã lọc; filter active highlight cam.
5. Use case kết thúc.

### Alternative Flows

**AF-01: Xóa filter**

1. Doctor chọn "Tất cả" trên dropdown.
2. Param rỗng → full list trong scope doctor.

**AF-02: Kết hợp nhiều filter**

- Tất cả params gửi đồng thời; SQL AND các điều kiện.

### Exception Flows

**EF-01: Filter huyết áp (`bloodPressure`)**

- UI có dropdown nhưng **DAO chưa áp SQL** — param bị bỏ qua.
- Danh sách không thay đổi theo BP.

### Business Rules

- BR-D06-01: `action=no-update` → BN chưa cập nhật 7 ngày; `no-followup` → 30 ngày.
- BR-D06-02: Filter glucose/HbA1c/BMI dùng ngưỡng trên view summary.

### Result

Doctor tìm nhanh BN theo nhóm rủi ro hoặc tiêu chí quản lý.

---

## UC-D07: View Patient Detail

| | |
|---|---|
| **Use Case ID** | UC-D07 |
| **Use Case Name** | View Patient Detail |
| **Primary Actor** | Doctor |
| **Secondary Actors** | PatientDetailService |
| **Priority** | High |
| **Extend** | UC-D05 |

### Goal

Doctor xem hồ sơ đầy đủ một BN: thông tin cá nhân, health record tổng hợp, lịch sử khám.

### Preconditions

- Doctor có quyền truy cập BN (`ensurePatientAccess`).
- Biết `patientId` (UUID).

### Postconditions

- `patientdetail.jsp` hiển thị `DetailBundle`.

### Trigger

GET/POST `/doctor/patient-list?id={patientUuid}`.

### Main Success Scenario

1. Doctor mở chi tiết BN (GET hoặc POST `id`).
2. Controller validate access.
3. `PatientDetailService.load(patientId, scopeDoctorId, fromDate, toDate)`.
4. Service load: profile, latest encounter, health record, lab overlay, history.
5. Forward `patientdetail.jsp`.
6. Doctor xem 3 vùng: Thông tin BN, Hồ sơ sức khỏe, Lịch sử khám.
7. Use case kết thúc.

### Alternative Flows

**AF-01: BN chưa có health record**

- Section hiển thị empty state "Chưa có dữ liệu".

**AF-02: Mở từ patient list qua POST**

- Form POST `id` → cùng forward logic.

**AF-03: Kèm filter lịch sử**

- Chuyển UC-D08 nếu có `fromDate`/`toDate`.

### Exception Flows

**EF-01: BN không tồn tại / không thuộc doctor**

- `ensurePatientAccess` fail.

**EF-02: `id` rỗng (POST)**

- Redirect `/doctor/patient-list`.

### Business Rules

- BR-D07-01: Health record read-only; cập nhật qua tạo encounter mới.
- BR-D07-02: Lab summary overlay từ `lab_results` mới nhất.

### Result

Doctor nắm toàn cảnh BN trước khi khám hoặc kê đơn.

---

## UC-D08: Filter Encounter History

| | |
|---|---|
| **Use Case ID** | UC-D08 |
| **Use Case Name** | Filter Encounter History |
| **Primary Actor** | Doctor |
| **Priority** | Medium |
| **Extend** | UC-D07 |

### Goal

Doctor lọc bảng lịch sử khám theo khoảng thời gian (quick hoặc custom).

### Preconditions

- UC-D07 — Doctor đang xem patient detail.

### Postconditions

- `history` list phản ánh filter; `activeQuickRange` set đúng.

### Trigger

Doctor chọn quick range hoặc submit custom dates.

### Main Success Scenario

1. Doctor chọn **30 ngày gần nhất** (quick dropdown).
2. Browser GET `?id=&fromDate={today-4}&toDate={today}`.
3. Controller parse dates; `resolveActiveQuickRange` → `"30"`.
4. `MedicalEncounterDAO.getHistoryByPatientAndDateRange` — SQL `BETWEEN ngay_kham`.
5. Reload `patientdetail.jsp` với history filtered.
6. Use case kết thúc.

### Alternative Flows

**AF-01: Tất cả lịch sử**

- GET chỉ `id` — không gửi dates → full history.

**AF-02: Custom date range**

1. Doctor mở dropdown **Tùy chỉnh thời gian**.
2. Nhập from/to → **Áp dụng** (GET form).
3. Main flow bước 3–6; `activeQuickRange = custom`.

**AF-03: Quick 5 / 10 ngày**

- Tương tự AF quick với inclusive day count (5 ngày = today-4..today).

### Exception Flows

**EF-01: Chỉ nhập một trong hai ngày**

- `historyFilterError` = "Vui lòng chọn đủ từ ngày và đến ngày."
- Load full history; hiển thị lỗi trên form custom.

**EF-02: fromDate &gt; toDate**

- `historyFilterError` = "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc."
- Không filter; giữ giá trị input.

**EF-03: Date parse invalid**

- Coi như null; có thể rơi vào EF-01.

**EF-04: Không có encounter trong khoảng**

- Empty state: "Không có lần khám trong khoảng thời gian đã chọn."

### Business Rules

- BR-D08-01: Quick range chỉ match khi `toDate = today` và inclusive days = 5/10/30.
- BR-D08-02: Client JS validate custom form trước submit.

### Result

Doctor xem lịch sử khám đúng khoảng thời quan tâm.

---

## UC-D09: Export Patient Detail PDF

| | |
|---|---|
| **Use Case ID** | UC-D09 |
| **Use Case Name** | Export Patient Detail PDF |
| **Primary Actor** | Doctor |
| **Secondary Actors** | PatientDetailPdfService |
| **Priority** | Medium |
| **Extend** | UC-D07 |

### Goal

Doctor tải báo cáo PDF hồ sơ BN **theo cùng filter lịch sử** đang hiển thị.

### Preconditions

- UC-D07; Doctor đang xem patient detail.
- BN tồn tại và accessible.

### Postconditions

- File PDF tải về browser; DB không đổi.

### Trigger

Doctor nhấn **Xuất PDF** trên header patient detail.

### Main Success Scenario

1. Doctor click link `/doctor/export-patient-pdf?id=&fromDate=&toDate=`.
2. `PatientPdfExportController` auth + `ensurePatientAccess`.
3. Parse `fromDate`, `toDate` (cùng logic filter).
4. `PatientDetailService.load(...)` — **cùng bundle** như màn hình.
5. `PatientDetailPdfService.generatePdf(bundle)`.
6. Response `application/pdf` attachment.
7. Use case kết thúc.

### Alternative Flows

**AF-01: Export toàn bộ lịch sử**

- Không có fromDate/toDate trên URL → PDF chứa full history.

### Exception Flows

**EF-01: Thiếu `id`**

- HTTP 400 Bad Request.

**EF-02: BN not found**

- HTTP 404.

**EF-03: fromDate &gt; toDate**

- Controller reset cả hai về null → export full history.

### Business Rules

- BR-D09-01: PDF **phải** dùng cùng service load như UI — không query history riêng full.

### Result

File PDF `patient-{code}-detail.pdf` với demographics + health summary + history filtered.

---

## UC-D10: Manage Medical Encounters

| | |
|---|---|
| **Use Case ID** | UC-D10 |
| **Use Case Name** | Manage Medical Encounters |
| **Primary Actor** | Doctor |
| **Secondary Actors** | MedicalEncounterDAO |
| **Priority** | High |

### Goal

Doctor xem và điều hướng quản lý các lần khám (encounters) thuộc BN được gán.

### Preconditions

- UC-D01 hoàn thành.

### Postconditions

- Danh sách encounters hiển thị trên `medicalrecordmanagement.jsp`.

### Trigger

GET `/doctor/patient-records`.

### Main Success Scenario

1. Doctor chọn **Hồ sơ khám bệnh**.
2. `MedicalEncounterController.doGet` auth + `searchEncounters`.
3. Forward list JSP với cột: mã encounter, loại, BN, BS, ngày khám, trạng thái, thời gian tạo.
4. Doctor có thể tạo mới, xem chi tiết (extend UCs).
5. Use case kết thúc.

### Alternative Flows

**AF-01: Tạo hồ sơ mới**

→ UC-D12.

**AF-02: Xem chi tiết**

→ UC-D14.

### Exception Flows

**EF-01: Unauthorized**

- Redirect / 403.

### Result

Doctor quản lý lifecycle encounters từ hub list.

---

## UC-D11: Search and Filter Medical Encounters

| | |
|---|---|
| **Use Case ID** | UC-D11 |
| **Use Case Name** | Search and Filter Medical Encounters |
| **Primary Actor** | Doctor |
| **Priority** | Medium |
| **Extend** | UC-D10 |

### Goal

Doctor lọc danh sách encounters theo ngày, keyword, loại, trạng thái, patientId.

### Preconditions

- UC-D10 active.

### Postconditions

- `records` list filtered.

### Trigger

Doctor áp dụng filter trên medical record list page.

### Main Success Scenario

1. Doctor nhập `startDate`, `endDate`, `keyword`, `type`, `status`, `patientId`.
2. GET `/doctor/patient-records?...`.
3. `MedicalEncounterDAO.searchEncounters` query DB.
4. Type/status filter bổ sung in-memory sau query (nếu cần).
5. Render filtered table.
6. Use case kết thúc.

### Exception Flows

**EF-01: Không có kết quả**

- Bảng rỗng.

### Business Rules

- BR-D11-01: Encounter types: `tai_kham_noi_tiet`, `mau_tong_quat`, `sinh_hoa_mau`.

### Result

Doctor tìm nhanh encounter cần xử lý.

---

## UC-D12: Create Medical Encounter

| | |
|---|---|
| **Use Case ID** | UC-D12 |
| **Use Case Name** | Create Medical Encounter |
| **Primary Actor** | Doctor |
| **Secondary Actors** | MedicalRecordService |
| **Priority** | High |
| **Extend** | UC-D10 |

### Goal

Doctor tạo lần khám mới (Bước 1): nhập dữ liệu lâm sàng/lab và lưu vào DB.

### Preconditions

- Doctor authenticated; có BN trong scope.
- Form create accessible (`action=add`).

### Postconditions

- Row mới trong `medical_encounters` + related tables.
- Endocrine → redirect treatment plan; Lab-only → redirect list.

### Trigger

Doctor submit form POST `action=form` trên `add-medical-encounter.jsp`.

### Main Success Scenario

1. Doctor mở form (`POST action=add`).
2. Chọn BN, loại hồ sơ, ngày khám, điền fields theo type.
3. (Optional) UC-D13 Analyze AI.
4. Doctor submit **Lưu**.
5. `MedicalRecordService.validateStep1` + `create()` transaction.
6. INSERT `medical_encounters`; INSERT `health_records` và/hoặc `lab_results`.
7. Nếu `tai_kham_noi_tiet` → redirect `/doctor/treatment-plan?id=`.
8. Else → redirect `/doctor/patient-records?success=1`.
9. Use case kết thúc.

### Alternative Flows

**AF-01: Chỉ tạo lab (CBC / sinh hóa)**

- Không redirect treatment plan; placeholder diagnosis "Đang cập nhật".

**AF-02: Mở từ sidebar / patient detail success banner**

- Entry paths khác nhau, cùng form.

### Exception Flows

**EF-01: Validation fail**

- Forward lại form với error messages.

**EF-02: Transaction / SQL error**

- Rollback; hiển thị lỗi hệ thống.

**EF-03: Unauthorized**

- `requireDoctor` fail.

### Business Rules

- BR-D12-01: `chan_doan_chinh` NOT NULL — placeholder "Đang cập nhật" khi chưa có chẩn đoán.
- BR-D12-02: Một `lab_results` row per encounter (unique `encounter_id`).

### Result

Encounter mới lưu DB; sẵn sàng Bước 2 (nội tiết) hoặc xem detail.

---

## UC-D13: Analyze Encounter with AI

| | |
|---|---|
| **Use Case ID** | UC-D13 |
| **Use Case Name** | Analyze Encounter with AI |
| **Primary Actor** | Doctor |
| **Secondary Actors** | EncounterAiAnalysis, Gemini API |
| **Priority** | Medium |
| **Extend** | UC-D12 |

### Goal

Doctor nhận phân tích AI/rule **trước khi lưu** encounter để hỗ trợ quyết định lâm sàng.

### Preconditions

- Doctor đang ở form create encounter (chưa lưu hoặc đang điền).

### Postconditions

- JSON analysis hiển thị trên form; **không** ghi DB.
- (Optional) Session `aiSummary:{encounterId}` sau khi lưu — cho treatment plan.

### Trigger

Doctor nhấn **Phân tích AI** → AJAX POST `action=analyze`.

### Main Success Scenario

1. JS thu thập form data.
2. POST `/doctor/patient-records?action=analyze`.
3. `EncounterAiAnalysis.analyze(dto)` gọi Gemini structured JSON.
4. Parse qua `GeminiJsonUtil`; render warnings, summary, recommendations.
5. Doctor đọc kết quả trên panel.
6. Use case kết thúc.

### Alternative Flows

**AF-01: Gemini fail / invalid JSON**

- Fallback `HealthMetricAssessment` rule-based.
- Panel vẫn hiển thị; ghi nguồn "Quy tắc y khoa".

**AF-02: Gemini chưa cấu hình**

- Chỉ rule-based.

### Exception Flows

**EF-01: Unauthenticated analyze call**

- `AuthContext.getUser()` có thể null — response lỗi (edge case).

### Business Rules

- BR-D13-01: AI không kê đơn; disclaimer trên UI treatment plan.
- BR-D13-02: Chỉ phân tích fields present trên form (theo encounter type).

### Result

Insight tức thì trên browser; không persist analysis table.

---

## UC-D14: View Medical Encounter Detail

| | |
|---|---|
| **Use Case ID** | UC-D14 |
| **Use Case Name** | View Medical Encounter Detail |
| **Primary Actor** | Doctor |
| **Secondary Actors** | MedicalRecordService |
| **Priority** | High |
| **Include** | UC-D15 (View Laboratory Results) |
| **Extend** | UC-D10, UC-D07 (history eye icon) |

### Goal

Doctor xem toàn bộ nội dung một lần khám: lâm sàng, đơn thuốc, lab.

### Preconditions

- Encounter tồn tại; doctor/admin có access.

### Postconditions

- `medicalrecorddetail.jsp` với `MedicalEncounterDTO`.

### Trigger

POST `/doctor/patient-records?action=detail&id={encounterId}`.

### Main Success Scenario

1. Doctor nhấn xem chi tiết từ list hoặc patient history.
2. `ensureEncounterAccess`.
3. `MedicalRecordService.loadMedicalRecordDetail(encounterId)`.
4. Assemble DTO: internal medicine, prescription, bloodCount, biochemistry.
5. Forward detail JSP với core metrics (Glucose, HbA1c).
6. Doctor đọc từng section; UC-D15 included nếu có lab data.
7. Use case kết thúc.

### Alternative Flows

**AF-01: Export PDF subsection**

→ UC-D17.

### Exception Flows

**EF-01: Encounter not found / no access**

- Error redirect hoặc 403.

### Result

Doctor review đầy đủ hồ sơ một lần khám.

---

## UC-D15: View Laboratory Results

| | |
|---|---|
| **Use Case ID** | UC-D15 |
| **Use Case Name** | View Laboratory Results |
| **Primary Actor** | Doctor |
| **Priority** | High |
| **Relationship** | «include» UC-D14 |

### Goal

Doctor xem và đánh giá kết quả xét nghiệm (CBC + sinh hóa) của encounter.

### Preconditions

- UC-D14 đang thực thi.
- Encounter type lab hoặc có `lab_results` row.

### Postconditions

- Lab sections rendered; abnormal values highlighted.

### Trigger

Included khi load encounter detail.

### Main Success Scenario

1. `LabResultDAO.findByEncounterId`.
2. Map vào `MedicalEncounterDTO.bloodCount` / `.biochemistry`.
3. Render lab-grid; `.abnormal` class cho giá trị ngoài ngưỡng.
4. Doctor review WBC, RBC, HGB, HCT, PLT, glucose, HbA1c, lipid, kidney markers.
5. Use case kết thúc.

### Alternative Flows

**AF-01: Không có lab data**

- Section ẩn hoặc empty.

### Business Rules

- BR-D15-01: Không có module lab độc lập — luôn gắn encounter.

### Result

Doctor đánh giá xét nghiệm trong ngữ cảnh lần khám.

---

## UC-D16: Delete Medical Encounter

| | |
|---|---|
| **Use Case ID** | UC-D16 |
| **Use Case Name** | Delete Medical Encounter |
| **Primary Actor** | Doctor |
| **Priority** | Low |
| **Extend** | UC-D10 |

### Goal

Doctor xóa vĩnh viễn một encounter và dữ liệu liên quan.

### Preconditions

- Encounter tồn tại; doctor có quyền.
- **Lưu ý triển khai:** Backend hỗ trợ POST `action=delete`; **JSP chưa có nút xóa**.

### Postconditions

- Cascade delete: medications → prescriptions → lab_results → health_records → medical_encounters.
- Redirect list với thông báo.

### Trigger

POST `/doctor/patient-records?action=delete&id={encounterId}` (API/direct call).

### Main Success Scenario

1. Request delete với encounter id.
2. `ensureEncounterAccess`.
3. JDBC transaction cascade delete qua DAOs.
4. Commit; redirect `/doctor/patient-records?deleted=1`.
5. Use case kết thúc.

### Exception Flows

**EF-01: SQL error**

- Rollback; error message.

**EF-02: No access**

- 403 / redirect.

### Business Rules

- BR-D16-01: Xóa không khôi phục (hard delete).

### Result

Encounter và dữ liệu phụ thuộc bị xóa khỏi DB.

---

## UC-D17: Export Medical Encounter PDF

| | |
|---|---|
| **Use Case ID** | UC-D17 |
| **Use Case Name** | Export Medical Encounter PDF |
| **Primary Actor** | Doctor |
| **Priority** | Medium |
| **Extend** | UC-D14 |

### Goal

Doctor tải PDF một encounter theo section (`full`, `internal`, `prescription`, `blood`, `biochemistry`).

### Preconditions

- UC-D14; encounter accessible.

### Postconditions

- PDF stream; DB unchanged.

### Trigger

GET `/doctor/record-export-pdf?id=&type=`.

### Main Success Scenario

1. Doctor chọn loại export trên detail page.
2. Controller auth + load encounter DTO.
3. `MedicalRecordPdfService` render section tương ứng.
4. Browser download PDF.
5. Use case kết thúc.

### Exception Flows

**EF-01: Invalid type / missing id**

- HTTP 400/404.

### Result

File PDF encounter theo section đã chọn.

---

## UC-D18: Complete Treatment Plan

| | |
|---|---|
| **Use Case ID** | UC-D18 |
| **Use Case Name** | Complete Treatment Plan |
| **Primary Actor** | Doctor |
| **Secondary Actors** | PrescriptionDAO, MedicationDAO |
| **Priority** | High |
| **Extend** | UC-D12 (endocrine only) |

### Goal

Doctor hoàn thiện Bước 2: chẩn đoán chính thức, hướng xử trí, đơn thuốc.

### Preconditions

- Encounter type `tai_kham_noi_tiet` đã tạo ở UC-D12.
- Doctor authenticated.

### Postconditions

- `medical_encounters` updated (diagnosis, treatment).
- `prescriptions` + `medications` replaced.
- `patients.loai_tieu_duong` updated nếu có.

### Trigger

GET/POST `/doctor/treatment-plan?id={encounterId}`.

### Main Success Scenario

1. Sau UC-D12 redirect, doctor mở treatment plan form.
2. GET load encounter, patient, existing meds, AI summary (session).
3. Doctor nhập `chanDoanChinh*`, `chanDoanPhu`, `huongXuTri`, medication lines.
4. POST submit.
5. Validate — `chanDoanChinh` required.
6. Transaction: update encounter, replace prescription/medications, update patient type.
7. Redirect `/doctor/patient-records?success=1`.
8. Use case kết thúc.

### Alternative Flows

**AF-01: Thêm nhiều dòng thuốc**

- Dynamic rows `medTenThuoc[]`, `medLieuLuong[]`, ...

**AF-02: AI summary hết session**

- Hiển thị placeholder "phiên có thể đã hết hạn".

### Exception Flows

**EF-01: Validation errors**

- Re-render form với `errors`, `fieldErrors`.

**EF-02: Encounter not found**

- Redirect list + error param.

**EF-03: NumberFormatException on med days**

- Message lỗi dòng thuốc cụ thể.

### Business Rules

- BR-D18-01: Replace strategy — xóa meds/prescription cũ rồi insert mới.
- BR-D18-02: BN có thể xem advice trên patient dashboard qua prescription read.

### Result

Đơn thuốc và chẩn đoán chính thức lưu DB.

---

## UC-D19: Manage Appointments

| | |
|---|---|
| **Use Case ID** | UC-D19 |
| **Use Case Name** | Manage Appointments |
| **Primary Actor** | Doctor |
| **Secondary Actors** | AppointmentDAO |
| **Priority** | High |

### Goal

Doctor xem danh sách lịch hẹn của BN được gán.

### Preconditions

- UC-D01.

### Postconditions

- `doctorappointmentmanagement.jsp` với appointment table.

### Trigger

GET `/doctor/appointments`.

### Main Success Scenario

1. Doctor chọn **Quản lý lịch khám**.
2. `AppointmentDAO.findAll(scopeDoctorId, ...)`.
3. Render table: tên BN, nội dung, thời gian, địa điểm, trạng thái, thao tác.
4. Use case kết thúc.

### Business Rules

- BR-D19-01: Trạng thái: `cho_kham`, `da_kham`, `da_huy`.
- BR-D19-02: **Không** có calendar view; **không** tạo/reschedule từ doctor UI.

### Result

Doctor nắm lịch hẹn sắp tới và trạng thái.

---

## UC-D20: Filter Appointment List

| | |
|---|---|
| **Use Case ID** | UC-D20 |
| **Use Case Name** | Filter Appointment List |
| **Primary Actor** | Doctor |
| **Priority** | Medium |
| **Extend** | UC-D19 |

### Goal

Doctor lọc lịch hẹn theo status, keyword, date range, type.

### Preconditions

- UC-D19 active.

### Postconditions

- Filtered appointment list.

### Trigger

Doctor chọn filter → GET với query params.

### Main Success Scenario

1. Doctor chọn status / keyword / fromDate / toDate.
2. GET `/doctor/appointments?...`.
3. DAO query với filters (status, keyword, dates).
4. Re-render table.
5. Use case kết thúc.

### Exception Flows

**EF-01: Filter `type`**

- UI gửi param `type` nhưng **SQL chưa filter theo type** — không ảnh hưởng kết quả.

### Result

Danh sách lịch hẹn thu hẹp theo tiêu chí (trừ type).

---

## UC-D21: Mark Appointment as Completed

| | |
|---|---|
| **Use Case ID** | UC-D21 |
| **Use Case Name** | Mark Appointment as Completed |
| **Primary Actor** | Doctor |
| **Priority** | Medium |
| **Extend** | UC-D19 |

### Goal

Doctor đánh dấu lịch **Chờ khám** đã hoàn thành khám.

### Preconditions

- Appointment `trang_thai = cho_kham`.
- Appointment thuộc BN của doctor.

### Postconditions

- `trang_thai = da_kham`.

### Trigger

POST `/doctor/appointments` với `id`, `status=da_kham`.

### Main Success Scenario

1. Doctor nhấn **Đã khám** trên row `cho_kham`.
2. POST với appointment id.
3. `requireDoctor`; load appointment scoped.
4. Verify current status `cho_kham`.
5. `updateStatus(id, da_kham)`.
6. Redirect list `?updated=1`.
7. Use case kết thúc.

### Exception Flows

**EF-01: Status không phải cho_kham**

- HTTP 400 "Khong the danh dau da kham".

**EF-02: Appointment not found**

- Update fail → 400.

### Business Rules

- BR-D21-01: **Không** có workflow approve booking — chỉ đổi trạng thái sau khi lịch đã tồn tại.

### Result

Lịch chuyển sang **Đã khám** (badge xanh).

---

## UC-D22: Cancel Appointment

| | |
|---|---|
| **Use Case ID** | UC-D22 |
| **Use Case Name** | Cancel Appointment |
| **Primary Actor** | Doctor |
| **Priority** | Medium |
| **Extend** | UC-D19 |

### Goal

Doctor hủy lịch hẹn đang **Chờ khám**.

### Preconditions

- Appointment `trang_thai = cho_kham`.

### Postconditions

- `trang_thai = da_huy`.

### Trigger

POST `/doctor/appointments` với `status=da_huy` (confirm dialog).

### Main Success Scenario

1. Doctor nhấn **Hủy lịch**; confirm JS.
2. POST id + status.
3. `updateStatus(id, da_huy)`.
4. Redirect `?updated=1`.
5. Use case kết thúc.

### Exception Flows

**EF-01: Không hủy được (không phải cho_kham)**

- HTTP 400.

### Business Rules

- BR-D22-01: Không gửi notification cho patient (chưa implement).

### Result

Lịch chuyển **Đã hủy** (badge đỏ).

---

## Phụ lục A — Ma trận Actor × Use Case

| Use Case | Doctor | System | Gemini AI | MySQL |
|---|:---:|:---:|:---:|:---:|
| D01 Authenticate | ● | ● | | ● |
| D02 Dashboard | ● | ● | ○ | ● |
| D03 Monitor High-Risk | ● | ● | ○ | ● |
| D04 High-Risk Detail | ● | ● | ○ | ● |
| D05–D09 Patient | ● | ● | | ● |
| D10–D18 Medical Record | ● | ● | ○ | ● |
| D19–D22 Appointment | ● | ● | | ● |

● = bắt buộc · ○ = tùy chọn

---

## Phụ lục B — Chức năng KHÔNG có Use Case (chưa implement)

| Mô tả nghiệp vụ thường gặp | Lý do loại trừ |
|---|---|
| Manage AI Alerts / update alert status | Không DAO/UI CRUD |
| Manage Threshold Settings | Không backend |
| Approve / Reject appointment request | Chỉ Mark Completed / Cancel |
| Standalone Laboratory module | «include» UC-D15 |
| Standalone Medication History page | Trong UC-D14, UC-D18, UC-D07 |
| Sidebar Cảnh báo khẩn cấp / Phân tích dữ liệu | Không route |
| Edit encounter Step 1 sau khi lưu | Không implement |
| Delete encounter từ UI | UC-D16 backend only |

---

## Phụ lục C — Traceability (Use Case → Implementation)

| UC ID | Controller / Service | JSP | HTTP |
|---|---|---|---|
| D01 | LoginController | login.jsp | /Logincontroller |
| D02–D04 | DoctorDashboardController | doctordashboard.jsp, dangerouspatientanalysis.jsp | /doctor-dashboard |
| D05–D08 | PatientListController | patientmanagement.jsp, patientdetail.jsp | /doctor/patient-list |
| D09 | PatientPdfExportController | — | /doctor/export-patient-pdf |
| D10–D16 | MedicalEncounterController | medicalrecordmanagement.jsp, add-medical-encounter.jsp, medicalrecorddetail.jsp | /doctor/patient-records |
| D17 | MedicalRecordPdfExportController | — | /doctor/record-export-pdf |
| D18 | TreatmentPlanController | treatment-plan.jsp | /doctor/treatment-plan |
| D19–D22 | DoctorAppointmentController | doctorappointmentmanagement.jsp | /doctor/appointments |

---

*Tài liệu đặc tả Use Case — Doctor Module. Cập nhật: 2026-07-20.*
