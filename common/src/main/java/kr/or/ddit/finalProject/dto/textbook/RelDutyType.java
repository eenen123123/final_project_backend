package kr.or.ddit.finalProject.dto.textbook;

public enum RelDutyType {
    STOCK_IN("재고 입고"),
    RETURN_IN("반품 입고"),
    DAMAGE("파손/폐기"),
    SALE_OUT("판매 출고");

    private final String label;

    RelDutyType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
