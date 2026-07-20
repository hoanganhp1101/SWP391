# Báo cáo sửa bug Gemini trả JSON null

## Nguyên nhân bug

### 1. Parse trực tiếp raw response (không làm sạch)
`DangerousPatientService` gọi `JsonParser.parseString(rawText)` trực tiếp trên text Gemini trả về. Khi Gemini bọc JSON trong markdown hoặc thêm text giải thích, parse thất bại → exception → `geminiUsed=false`, các field AI (`summary`, `riskLevel`...) trống/null.

### 2. Validation retry không strip markdown
`generateGeminiJsonResponse()` retry 3 lần nhưng chỉ validate bằng `JsonParser.parseString(rawText)` trên raw text. Response dạng:

```
Đây là phân tích:
```json
{"riskLevel":"high",...}
```
```

→ luôn fail validation dù JSON bên trong hợp lệ.

### 3. Throw exception thay vì fallback
`parseGeminiResponse()` và `parseDetailResponse()` throw khi parse lỗi → toàn bộ luồng Gemini fail, không chuyển sang Rule-based Analysis.

### 4. Thiếu xử lý edge cases từ API
`extractTextFromGeminiResponse()` không kiểm tra:
- `promptFeedback.blockReason` (safety block)
- `finishReason=SAFETY|RECITATION|BLOCKLIST`
- `candidates` rỗng
- `text` null/rỗng

### 5. EncounterAiAnalysis throw khi parse fail
`applyGeminiTextToAnalysis()` throw `IllegalStateException` → chỉ fallback sau 3 attempt, không parse an toàn từng response.

---

## Raw response của Gemini (các dạng đã gặp / có thể gặp)

Sau khi sửa, mọi response được log:

```
========== GEMINI RAW RESPONSE ==========
<raw text>
=========================================
```

| Dạng response | Ví dụ | Trước đây | Sau khi sửa |
|---|---|---|---|
| Markdown fence | ` ```json\n{...}\n``` ` | Parse fail | Strip fence → parse OK |
| Text trước JSON | `Phân tích:\n{"riskLevel":"high"}` | Parse fail | Trích object đầu tiên → OK |
| Text sau JSON | `{"riskLevel":"high"}\nChú thích.` | Parse fail | Trích object cân bằng `{}` → OK |
| Nhiều JSON | `{...}{...}` | Parse fail | Lấy object đầu tiên |
| Null / rỗng | `""` hoặc `null` | NPE / crash | Fallback rule-based |
| Malformed JSON | `{"riskLevel":` | Exception | Log + fallback |
| Safety block | `candidates=[]`, `blockReason=SAFETY` | "không trả kết quả" mơ hồ | Log rõ + fallback |
| Timeout | HTTP error / empty body | Exception | Retry model + fallback |
| Field thiếu | `{"riskLevel":"high"}` (thiếu score) | NPE tiềm ẩn | Default values |

---

## Cách xử lý

### Utility mới: `GeminiJsonUtil`

File: `src/main/java/com/example/diabetesmanage/util/GeminiJsonUtil.java`

Pipeline parse an toàn:

1. **Log raw** — `System.out.println` theo format yêu cầu
2. **trim()**
3. **Bỏ markdown** — ` ```json `, ` ``` `
4. **Trích JSON object** — tìm `{` đầu tiên, cân bằng ngoặc; fallback `first '{'` → `last '}'`
5. **ObjectMapper.readTree()** — validate trước khi convert
6. **Trả ParseResult** — success/failure, không throw

### DangerousPatientService

- `generateGeminiJsonResponse()` — validate bằng `GeminiJsonUtil.parse(raw, true)`, retry 3×, trả extracted JSON
- `enrichWithGemini()` — parse fail → `applyRuleBasedGeminiAnalysis()`, không crash
- `analyzePatientDetail()` — parse fail → `geminiUsed=false`, UI dùng fallback có sẵn
- `parseGeminiResponse()` / `parseDetailResponse()` — đọc field qua `readNodeString/Int/List` với default
- Prompt — thêm `ONLY RETURN VALID JSON`, `DO NOT RETURN MARKDOWN`, v.v.

### EncounterAiAnalysis

- `applyGeminiTextToAnalysis()` — trả `boolean`, dùng `GeminiJsonUtil`, không throw
- `parseResponseSafely()` — nhận `JsonNode` từ `readTree()`
- `parseGeminiHttpBody()` — detect safety block, empty text
- Parse lỗi → `applyRuleBasedFallback()` (giữ nguyên business logic)

### Dependency

- Thêm `jackson-databind` 2.17.2 vào `pom.xml` cho `ObjectMapper.readTree()`

---

## Các trường hợp đã được cover

| # | Trường hợp | Xử lý |
|---|---|---|
| 1 | Gemini không trả JSON | Log raw → fallback rule-based |
| 2 | Markdown ` ```json ` | `cleanRawResponse()` strip |
| 3 | Text trước JSON | `extractFirstBalancedObject()` |
| 4 | Text sau JSON | Cân bằng ngoặc, cắt đúng `}` |
| 5 | Nhiều JSON object | Lấy object đầu tiên |
| 6 | JSON field null | `readNode*` trả default |
| 7 | Response rỗng | `ParseResult.failure` → fallback |
| 8 | Timeout / API lỗi | Retry model 3× → fallback |
| 9 | Safety block | Detect `blockReason` / `finishReason` → fallback |
| 10 | Malformed JSON | Log lỗi + raw → fallback |
| 11 | Thiếu field trong JSON | Default: `""`, `0`, `[]` |
| 12 | Truncation (MAX_TOKENS) | Retry compact prompt (Encounter) |

---

## File đã thay đổi

| File | Thay đổi |
|---|---|
| `util/GeminiJsonUtil.java` | **Mới** — parse an toàn + logging |
| `service/DangerousPatientService.java` | Parse safe, fallback, prompt, extract API |
| `service/EncounterAiAnalysis.java` | Dùng GeminiJsonUtil, không throw parse |
| `pom.xml` | Thêm jackson-databind |

**Không thay đổi:** endpoint, UI, business logic scoring/rule engine.

---

## Kiểm tra

```bash
.\mvnw.cmd compile -DskipTests
```

Khi chạy app, xem console log `========== GEMINI RAW RESPONSE ==========` để debug response thực tế từ Gemini.
