-- =====================================================================
-- PAY_HIST 토스페이먼츠 지원 컬럼 추가
-- ※ ORDERS 테이블 생성(create_orders_table.sql) 후 실행
-- =====================================================================

ALTER TABLE PAY_HIST ADD TOSS_PAYMENT_KEY VARCHAR2(200) NULL; -- [Toss]paymentKey · 결제 취소 API에 필수
ALTER TABLE PAY_HIST ADD ORD_SN NUMBER(20) NULL;              -- ORDERS.ORD_SN 참조 · 주문↔결제 연결

ALTER TABLE PAY_HIST ADD CONSTRAINT FK_PAY_HIST_ORDERS
    FOREIGN KEY (ORD_SN) REFERENCES ORDERS (ORD_SN);

COMMENT ON COLUMN PAY_HIST.TOSS_PAYMENT_KEY IS '[Toss]paymentKey · 취소/조회 API 필수 키';
COMMENT ON COLUMN PAY_HIST.ORD_SN           IS 'ORDERS.ORD_SN 참조(FK)';
