package com.example.diabetesmanage.model;

public class DietPlanDetail {
    private String id;
    private String dietPlanId;
    private String foodId;
    private String buaAn;
    private String ghiChu;

    // Đối tượng thực phẩm gốc phục vụ việc hiển thị tên, carbs, calo...
    private MasterFood thucPhamGoc;

    public DietPlanDetail() {}

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDietPlanId() { return dietPlanId; }
    public void setDietPlanId(String dietPlanId) { this.dietPlanId = dietPlanId; }
    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public String getBuaAn() { return buaAn; }
    public void setBuaAn(String buaAn) { this.buaAn = buaAn; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public MasterFood getThucPhamGoc() { return thucPhamGoc; }
    public void setThucPhamGoc(MasterFood thucPhamGoc) { this.thucPhamGoc = thucPhamGoc; }
}