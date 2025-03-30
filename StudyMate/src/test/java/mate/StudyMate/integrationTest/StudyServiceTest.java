package mate.StudyMate.integrationTest;

import lombok.extern.slf4j.Slf4j;
import mate.StudyMate.config.TestQueryDslConfig;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.study.Study;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.repository.StudyRepository;
import mate.StudyMate.service.StudyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(TestQueryDslConfig.class)
@SpringBootTest
@Transactional
@Slf4j
class StudyServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyService studyService;

    @Test
    void 스터디_생성_성공() {
        // given
        Member member = Member.createMember("admin", "password1", "admin@email.com", "010-1231-6234");
        Member savedMember = memberRepository.save(member);

        String name = "Java 스터디";
        String description = "Spring 학습";
        boolean isPrivate = false;
        int maxMembers = 5;

        // when
        Long studyId = studyService.createStudy(savedMember.getId(), name, description, isPrivate, maxMembers);

        // then
        Study savedStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(savedStudy.getName()).isEqualTo(name);
    }

    @Test
    void 스터디_최소인원_미만일때_예외발생() {
        // given
        Member member = Member.createMember("admin", "password1", "admin@email.com", "010-1231-6234");
        Member savedMember = memberRepository.save(member);
        int maxMembers = 1;

        //when &  then
        assertThatThrownBy(() ->
                studyService.createStudy(savedMember.getId(), "스터디", "설명", false, maxMembers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("인원 수는 최소 2명 이상이어야 합니다.");
    }
}