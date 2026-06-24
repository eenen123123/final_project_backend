package kr.or.ddit.finalProject.dto.textbook;

public enum InventoryStatus {
    /** 정상 — 판매 가능한 재고 있음 */
    NORMAL,
    /** 품절 — 판매 가능 재고 없음 */
    SOLD_OUT,
    /** 재고 부족 — 최소 유지 수량 이하 */
    SHORTAGE,
    /** 입고대기 — 재고 소진, 입고 예정 */
    WAITING;
}
