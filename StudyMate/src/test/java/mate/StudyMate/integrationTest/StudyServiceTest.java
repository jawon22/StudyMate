package mate.StudyMate.integrationTest;

import lombok.extern.slf4j.Slf4j;
import mate.StudyMate.config.QueryDslConfig;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.study.Study;
import mate.StudyMate.domain.study.StudyStatus;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.repository.StudyRepository;
import mate.StudyMate.repository.StudySearchCond;
import mate.StudyMate.service.StudyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(QueryDslConfig.class)
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

    // 조회 ------------------------------------------
    @Test
    void 조건별_조회() { // 이름, 상태, 공개 여부
        // given
        Member member = Member.createMember("test", "password1", "test@email.com", "010-1231-6234");
        Member savedMember = memberRepository.save(member);

        studyService.createStudy(savedMember.getId(), "Java Study", "스터디 설명", false, 5);
        studyService.createStudy(savedMember.getId(), "Spring Boot Study", "스터디 설명", true, 10);
        studyService.createStudy(savedMember.getId(), "JPA Study", "스터디 설명", false, 3);

        // when
        StudySearchCond cond = new StudySearchCond("Java", null, false);
        List<Study> result1 = studyService.findAll(cond);

        StudySearchCond cond2 = new StudySearchCond(null, StudyStatus.RECRUITING, false);
        List<Study> result2 = studyService.findAll(cond2);

        StudySearchCond cond3 = new StudySearchCond(null, null, false);
        List<Study> result3 = studyService.findAll(cond3);

        // then
        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getName()).contains("Java");

        assertThat(result2).hasSize(2);

        assertThat(result3).hasSize(2);
        assertThat(result3).allMatch(study -> !study.isPrivate());
    }

    @Test
    void 데이터_없는경우_비어있는_리스트_반환() {
        // given
        Member member = Member.createMember("test", "password1", "test@email.com", "010-1231-6234");
        Member savedMember = memberRepository.save(member);

        studyService.createStudy(savedMember.getId(), "Java Study", "스터디 설명", false, 5);
        studyService.createStudy(savedMember.getId(), "Spring Boot Study", "스터디 설명", true, 10);
        studyService.createStudy(savedMember.getId(), "JPA Study", "스터디 설명", false, 3);

        // when
        StudySearchCond cond = new StudySearchCond("NonExist", null, false);
        List<Study> result = studyService.findAll(cond);

        // then
        assertThat(result).isEmpty();
    }

    /**
     * 스터디 참가/탈퇴 테스트
     * * 정상적으로 참가 확인테스트
     * * 중복 참가 시 예외 발생여부 테스트
     * * 최대 인원 초과 시 예외 발생여부 테스트
     * * 탈퇴 시 멤버가 정상적으로 제거되는지 테스트
     */
    @Test
    void 정상적으로_스터디참가_확인_테스트() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);

        // when
        studyService.joinStudy(studyId, member2.getId());

        // then
        Study findStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(findStudy.getCurrentMembers()).isEqualTo(2);
    }

    @Test
    void 스터디_중복참가_예외발생() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);

        // when
        studyService.joinStudy(studyId, member2.getId());

        // then
        assertThatThrownBy(() -> studyService.joinStudy(studyId, member2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스터디에 참가한 회원입니다.");
    }

    @Test
    void 스터디_최대인원_초과시_예외발생() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        Member member3 = Member.createMember("user3", "password3", "user3@email.com", "010-1578-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);

        // when
        studyService.joinStudy(studyId, member2.getId());

        // then
        assertThatThrownBy(() -> studyService.joinStudy(studyId, member3.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스터디 정원이 초과되었습니다.");
    }

    @Test
    void 스터디_탈퇴시_멤버가_정상적으로_제거() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);
        studyService.joinStudy(studyId, member2.getId());

        // when
        studyService.leaveStudy(studyId, member2.getId());

        // then
        Study findStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(findStudy.getCurrentMembers()).isEqualTo(1);
    }

    @Test
    void 스터디장이_탈퇴시_예외발생() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);
        studyService.joinStudy(studyId, member2.getId());

        // when & then
        assertThatThrownBy(() -> studyService.leaveStudy(studyId, member1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스터디 관리자는 탈퇴할 수 없습니다. 관리자를 변경해주세요.");
    }

    /**
     * 스터디 수정 테스트
     * * 관리자가 아닌 회원이 수정시 예외발생
     * * 최대 인원 제한 검증(최소 2명)
     * * 스터디 관리자 정상적으로 변경
     * * * 관리자 변경시 스터디내의 회원으로 변경하지 않으면 예외발생
     */
    @Test
    void 관리자가_아닌_회원이_수정시_예외발생_및_관리자_수행시_정상수행() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);
        studyService.joinStudy(studyId, member2.getId());

        // when
        studyService.updateStudy(studyId, member1.getId(), "Study","몰라",2);

        // then
        Study findStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(findStudy.getName()).isEqualTo("Study");
        assertThatThrownBy(() -> studyService.updateStudy(studyId, member2.getId(), "Java", "차근차근해요", 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("해당 작업은 스터디 관리자만 수행할 수 있습니다.");
    }

    @Test
    void 수정시_최대인원_제한_검증() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        memberRepository.save(member1);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);

        // when
        studyService.updateStudy(studyId, member1.getId(), "Java", "설명", 2);

        // then
        assertThatThrownBy(() -> studyService.updateStudy(studyId, member1.getId(), "Java", "설명", 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("인원 수는 최소 2명 이상이어야 합니다.");
    }

    @Test
    void 스터디_관리자_정상적으로_변경() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        Member member3 = Member.createMember("user3", "password3", "user3@email.com", "010-1578-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);
        studyService.joinStudy(studyId, member2.getId());

        // when
        studyService.changeStudyAdmin(studyId, member1.getId(), member2.getId());

        // then
        Study findStudy = studyRepository.findById(studyId).orElseThrow();
        assertThat(findStudy.getAdmin().getName()).isEqualTo("user2");

        // 관리자 변경시 스터디내의 회원으로 변경하지 않으면 예외발생
        assertThatThrownBy(() -> studyService.changeStudyAdmin(studyId, member2.getId(), member3.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("해당 회원은 스터디 내에 존재하지 않습니다.");
    }

    /**
     * 스터디 삭제 테스트
     * * 스터디 정상적인 삭제
     * * * 관리자가 아닌 경우 삭제 불가
     */
    @Test
    void 스터디_정상적인_삭제() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        memberRepository.save(member1);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);

        // when
        studyService.deleteStudy(studyId, member1.getId());

        // then
        StudySearchCond cond = new StudySearchCond("Java Study", StudyStatus.RECRUITING, false);
        assertThat(studyService.findAll(cond)).hasSize(0);
    }

    @Test
    void 관리자가_아닌_스터디회원이_삭제할_경우_예외발생() {
        // given
        Member member1 = Member.createMember("user1", "password1", "user1@email.com", "010-1531-6234");
        Member member2 = Member.createMember("user2", "password2", "user2@email.com", "010-1572-6234");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long studyId = studyService.createStudy(member1.getId(), "Java Study", "스터디 설명", false, 2);
        studyService.joinStudy(studyId, member2.getId());

        // when & then
        assertThatThrownBy(() -> studyService.deleteStudy(studyId, member2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("해당 작업은 스터디 관리자만 수행할 수 있습니다.");
    }
}