package kr.or.ddit.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorMapper;
import kr.or.ddit.finalProject.mapper.textbook.TextbookMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class InputSampleCourses {

    // 리포지토리 루트에 있는 샘플 강좌 데이터 (json)을 파싱해 db에 넣음


    @Autowired
    private InstructorMapper instructorMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TextbookMapper textbookMapper;

    static int count = 0;

    @Test
    @Transactional
    void inputSampleCourses() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Path dir = Paths.get("../sample_courses");
        Map<Object, List<Object>> groupedBySubject;
        List<Path> jsonFiles;
        try (Stream<Path> paths = Files.walk(dir)) {
            jsonFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json")).toList();

            groupedBySubject = jsonFiles.stream().collect(Collectors.groupingBy(
                    path -> dir.relativize(path).getName(0).toString(), // 첫 번째 폴더명 = 과목명
                    Collectors.mapping(path -> readJson(mapper, path), Collectors.toList())));
            groupedBySubject.forEach((subject, list) -> {
                log.info("과목: {}, 강좌 수: {}", subject, list.size());
            });
        }

        List<InstructorDto> instructorDtos = instructorMapper.selectAllInstructor().stream()
                .filter(inst -> inst.getInstrUserId().startsWith("testuser")
                        && Integer.parseInt(inst.getInstrUserId().replace("testuser", "")) > 30)
                .toList();
        log.info("강사 수: {}", instructorDtos.size());

        Pattern pattern = Pattern.compile("\\]_([^_]+)의_");
        log.info("강좌 수 : {}", jsonFiles.size());
        jsonFiles.forEach(json -> {

            var instr = instructorDtos.get(count++ / 2);
            CourseDto courseDto = new CourseDto();
            TextbookDto textbookDto = new TextbookDto();

            if (dir.relativize(json).getName(0).toString().equals("국어")) {
                courseDto.setSubjId(1L);
            } else if (dir.relativize(json).getName(0).toString().equals("영어")) {
                courseDto.setSubjId(10L);
            } else if (dir.relativize(json).getName(0).toString().equals("수학")) {
                courseDto.setSubjId(5L);
            } else if (dir.relativize(json).getName(0).toString().equals("사회")) {
                courseDto.setSubjId(12L);
            } else if (dir.relativize(json).getName(0).toString().equals("과학")) {
                courseDto.setSubjId(21L);
            }
            courseDto.setInstrUserId(instr.getInstrUserId());
            String instrName =
                    memberMapper.findByUserId(instr.getInstrUserId()).orElseThrow().getUserName();
            String beforeTitle = json.getFileName().toString();
            Matcher matcher = pattern.matcher(beforeTitle);
            if (matcher.find()) {
                String changedFileName = matcher.replaceFirst("]_" + instrName + "의_");
                courseDto.setCourseNm(changedFileName.replace(".json", ""));
            } else {
                courseDto.setCourseNm(beforeTitle.replace(".json", ""));
            }

            JsonNode courseJson = readJson(mapper, json);
            String explain = courseJson.path("explain").asText();
            courseDto.setCourseExplnCn(explain);
            String imgSrc = courseJson.path("book").path("image").asText();
            courseDto.setThmbImg(imgSrc);
            courseDto.setCoursePrice(179000L);
            courseMapper.insertCourse(courseDto);
            var book = courseJson.path("book");
            textbookDto.setTextbookNm(book.path("title").asText());
            textbookDto.setThmbImg(imgSrc);
            textbookDto.setAuthrNm(instrName);
            textbookDto.setPubrNm(book.path("publisher").asText());
            textbookDto.setCourseSn(courseDto.getCourseSn());
            textbookDto.setSubjId(courseDto.getSubjId());
            textbookDto.setDlvrAmt(3000L);
            textbookMapper.insertTextbook(textbookDto);
            log.info("Inserted textbook for courseSn: {}", textbookDto.getCourseSn());
        });
    }

    private static JsonNode readJson(ObjectMapper mapper, Path path) {
        try {
            return mapper.readTree(path.toFile());
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + path, e);
        }
    }
}
