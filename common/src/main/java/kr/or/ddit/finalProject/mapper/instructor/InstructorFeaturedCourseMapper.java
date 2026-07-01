package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.profile.InstructorFeaturedCourseResponse;

@Mapper
public interface InstructorFeaturedCourseMapper {

    List<InstructorFeaturedCourseResponse> selectFeaturedCourses(@Param("instrUuid") String instrUuid);

    int countFeaturedCourses(@Param("instrUuid") String instrUuid);

    void insertFeaturedCourse(@Param("instrUuid") String instrUuid,
                              @Param("courseSn") Long courseSn,
                              @Param("displayOrder") int displayOrder);

    void deleteFeaturedCourse(@Param("instrUuid") String instrUuid,
                              @Param("courseSn") Long courseSn);

    void updateDisplayOrder(@Param("instrUuid") String instrUuid,
                             @Param("courseSn") Long courseSn,
                             @Param("displayOrder") int displayOrder);

}
