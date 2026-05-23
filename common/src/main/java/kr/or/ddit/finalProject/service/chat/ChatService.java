package kr.or.ddit.finalProject.service.chat;

import java.util.List;
import kr.or.ddit.finalProject.dto.message.MessageContentDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomDto;

/**
 * 채팅 서비스 인터페이스
 * 
 * 1. 채팅방 생성
 * 2. 채팅방 목록 조회
 * 3. 채팅방 입장
 * 4. 채팅 메시지 전송
 * 5. 채팅 메시지 수신
 * 6. 채팅방 나가기
 * 7. 채팅방 삭제
 * 8. 채팅방 정보 조회
 * 9. 채팅 메시지 기록 조회
 * 10. 채팅방 멤버 관리
 * 11. 채팅방 알림 설정
 * 12. 채팅방 검색
 * 
 * // 이하는 추가 기능 예시
 * // * 13. 채팅방 고정
 * // * 14. 채팅방 즐겨찾기
 * // * 15. 채팅방 권한 관리
 * // * 16. 채팅방 공지사항 관리
 * // * 17. 채팅방 파일 공유
 * // * 18. 채팅방 이모티콘 관리
 * // * 19. 채팅방 봇 관리
 * // * 20. 채팅방 통계 조회
 */

public interface ChatService {

    /**
     * 일대일 채팅방 조회 또는 생성 (회원 정보 보기 등에서 상대방과 일대일 채팅방이 필요한 경우)
     * - 이미 존재하는 일대일 채팅방이 있으면 해당 채팅방을 반환
     * 
     * @param myId    나의 사용자ID
     * @param otherId 상대방 사용자ID
     * @return 존재하는 일대일 채팅방이 있으면 해당 채팅방, 없으면 새로 생성된 채팅방
     */
    MessageRoomDto getOrCreateOneOnOneChatRoom(String myId, String otherId);

    /**
     * 채팅방 가져오기 (일대일 또는 그룹, 채팅방 목록에서 선택한 채팅방)
     * 
     * @param roomSn 채팅방 일련번호
     * @return 해당 채팅방 정보
     */
    MessageRoomDto getGroupChatRoom(long roomSn);

    /**
     * 채팅 메시지 기록 조회 (페이징 처리)
     * 
     * @param roomSn     채팅방 일련번호
     * @param page       메시지 페이지 번호 (1부터 시작)
     * @param screenSize 페이지당 메시지 수
     * @return 해당 채팅방의 메시지 목록 (페이징 처리된 결과)
     */
    List<MessageContentDto> getChatMessages(long roomSn, int page, int screenSize);

    /**
     * 그룹 채팅방 생성
     * - 그룹 채팅방은 채팅방 이름과 참여자 목록이 필요
     * 
     * @param roomNm      채팅방 이름
     * @param opnrUserId  채팅방 개설자 사용자ID
     * @param partUserIds 참여자 사용자ID 목록 (개설자 포함)
     * @return 생성된 그룹 채팅방
     */
    MessageRoomDto createGroupChatRoom(String roomNm, String opnrUserId, List<String> partUserIds);

    /**
     * 채팅 메시지 전송
     * 
     * @param roomSn      채팅방 일련번호
     * @param sendrUserId 발신자 사용자ID
     * @param msgTypeCd   메시지 유형 코드
     * @param msgCn       메시지 내용
     */
    void sendMessage(Long roomSn, String sendrUserId, String msgTypeCd, String msgCn);

    /**
     * 채팅방 목록 조회 (사용자가 참여한 채팅방 목록)
     * - 채팅방 목록에는 각 채팅방의 마지막 메시지 내용과 마지막 메시지 전송 일시도 포함하여 반환
     * 
     * @param userId 사용자ID (채팅방 목록을 조회할 사용자ID)
     * @return 사용자가 참여한 채팅방 목록 (각 채팅방에는 마지막 메시지 내용과 마지막 메시지 전송 일시 포함)
     */
    List<MessageRoomDto> getChatRoomList(String userId);

}
