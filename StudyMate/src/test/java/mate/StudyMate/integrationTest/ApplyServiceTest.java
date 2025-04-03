package mate.StudyMate.integrationTest;

import lombok.extern.slf4j.Slf4j;
import mate.StudyMate.domain.apply.Apply;
import mate.StudyMate.domain.apply.ApplyStatus;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.study.Study;
import mate.StudyMate.domain.study.StudyStatus;
import mate.StudyMate.repository.ApplyRepository;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.repository.StudyRepository;
import mate.StudyMate.service.ApplyService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Slf4j
public class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ApplyRepository applyRepository;

    private Member member1;
    private Member member2;
    private Study study;

    @BeforeEach
    void setup() {
        member1 = memberRepository.save(Member.createMember("user1", "password1", "user1@email.com", "010-1231-6234"));
        member2 = memberRepository.save(Member.createMember("user2", "password2", "user2@email.com", "010-1234-6234"));
        study = studyRepository.save(Study.createStudy(member1, "스터디", "description", false, 4));
    }

    @Test
    void 스터디_신청_정상신청() {
        // when
        Long applyId = applyService.apply(member2.getId(), study.getId());

        // then
        assertThat(applyId).isNotNull();
        assertThat(applyRepository.existsById(applyId)).isTrue();

        // 동일한 스터디에 중복 신청시 예외 발생
        assertThatThrownBy(() -> applyService.apply(member2.getId(), study.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 신청한 스터디입니다.");
    }

    @Test
    void 스터디_신청_모집중이_아닌경우_신청시_예외발생() {
        //when
        study.changeStatus(StudyStatus.CLOSED);

        //then
        assertThatThrownBy(() -> applyService.apply(member2.getId(), study.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스터디 모집중이 아닙니다.");
    }

    //--------------- 신청 취소 --------------
    @Test
    void 스터디_신청취소_정상취소() {
        // given
        Long applyId = applyService.apply(member2.getId(), study.getId());

        // when
        applyService.cancel(applyId,member2.getId());

        // then
        assertThat(applyService.findApplyByMember(member2.getId())).hasSize(0);
    }

    @Test
    void 스터디_신청취소_본인이_아닌경우_예외발생() {
        // given
        Long applyId = applyService.apply(member2.getId(), study.getId());
        Member otherMember = memberRepository.save
                (Member.createMember("user3", "password3", "user3@email.com", "010-1234-6734"));

        // when & then
        assertThatThrownBy(() -> applyService.cancel(applyId, otherMember.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("본인의 신청만 취소할 수 있습니다.");
    }

    @Test
    void 스터디_신청취소_승인된_신청은_취소불가() {
        // given
        Long applyId = applyService.apply(member2.getId(), study.getId());

        // when
        applyService.approve(applyId, member1.getId());

        // then
        assertThatThrownBy(() -> applyService.cancel(applyId, member2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 승인된 신청은 취소할 수 없습니다.");
    }

    // ----------- 신청 승인/거절 ----------------
    @Test
    void 스터디_신청_승인_정상승인() {
        // given
        Long applyId = applyService.apply(member2.getId(), study.getId());

        // when
        applyService.approve(applyId,member1.getId());

        // then
        Apply approveApply = applyRepository.findById(applyId).orElseThrow();
        assertThat(approveApply.getStatus()).isEqualTo(ApplyStatus.APPROVED);
    }

    @Test
    void 스터디_신청_거절_정상거절() {
        // given
        Long applyId = applyService.apply(member2.getId(), study.getId());

        // when
        applyService.reject(applyId,member1.getId());

        // then
        Apply rejectApply = applyRepository.findById(applyId).orElseThrow();
        assertThat(rejectApply.getStatus()).isEqualTo(ApplyStatus.REJECTED);
    }

    @Test
    void 스터디_신청_승인_거절_괸리자가_아닌경우_예외발생() {
        // given
        Long applyId = applyService.apply(member2.getId(), study.getId());
        Member otherMember = memberRepository.save
                (Member.createMember("user3", "password3", "user3@email.com", "010-1234-6734"));

        // when & then
        assertThatThrownBy(() -> applyService.approve(applyId, otherMember.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("관리자만 승인/거절할 수 있습니다.");

        assertThatThrownBy(() -> applyService.reject(applyId, otherMember.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("관리자만 승인/거절할 수 있습니다.");
    }

    // --------------------- 신청 목록 조회 -----------------
    @Test
    void 특정_스터디의_신청목록_조회() {
        // given
        Member otherMember = memberRepository.save
                (Member.createMember("user3", "password3", "user3@email.com", "010-1234-6734"));

        Long applyId = applyService.apply(member2.getId(), study.getId());
        applyService.apply(otherMember.getId(), study.getId());

        // when
        Apply apply = applyRepository.findById(applyId).orElseThrow();
        List<Apply> applyByStudys = applyService.findApplyByStudy(apply.getStudy().getId());

        // then
        assertThat(applyByStudys).hasSize(2);
    }

    @Test
    void 특정_회원의_신청목록_조회() {
        // given
        applyService.apply(member2.getId(), study.getId());

        // when
        List<Apply> applyByMemberList = applyService.findApplyByMember(member2.getId());

        // then
        assertThat(applyByMemberList).hasSize(1);
    }
}