package kr.or.ddit.finalProject.service.board.notice;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.finalProject.dto.board.NoticeDto;

@SpringBootTest
class NoticeServiceTest {

    @Autowired
    NoticeService noticeService;

    @Test
    void getNoticeList() {
        List<NoticeDto> list = noticeService.getNoticeList(null);
        System.out.println("공지사항 목록 크기: " + list.size());
        list.forEach(notice -> System.out.println("NOTICE: " + notice));
    }

    @Test
    void getNoticeById() {
        NoticeDto notice = noticeService.getNoticeById(1L);
        System.out.println("NOTICE: " + notice);
    }

}
