package kr.or.ddit.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.or.ddit.finalProject.mapper.log.MemberActivityLogMapper;

@Configuration
public class ActivityAspectConfig {

    @Bean
    public MemberActivityAspect memberActivityAspect(MemberActivityLogMapper activityLogMapper) {
        return new MemberActivityAspect(activityLogMapper);
    }
}
