package service;

/**
 * Số liệu thô gửi Gemini — không có điểm/mức từ rule.
 */
public class GeminiRawInput {

    public int scanDays;
    public int totalReadings;
    public double avgGlucose;
    public double tirPercent;
    public int hypoCount;
    public int hyperCount;
    public int dangerCount;
    public Double hba1c;
    public boolean hasHba1c;
    public boolean measuredRecently;
    public int glucoseLow;
    public int glucoseHigh;
    public int glucoseDanger;
    public double hba1cTarget;
    public double hba1cPoor;
    public int daysNoMeasure;
}
