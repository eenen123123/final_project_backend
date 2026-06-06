package kr.or.ddit.service.event;

public class ApprovalCompletedEvent {

    private final Long aprvlDocSn;

    public ApprovalCompletedEvent(Long aprvlDocSn) {
        this.aprvlDocSn = aprvlDocSn;
    }

    public Long getAprvlDocSn() {
        return aprvlDocSn;
    }
}
