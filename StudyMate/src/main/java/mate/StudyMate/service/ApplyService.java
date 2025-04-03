package mate.StudyMate.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.apply.Apply;
import mate.StudyMate.domain.apply.ApplyStatus;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.study.Study;
import mate.StudyMate.domain.study.StudyStatus;
import mate.StudyMate.repository.ApplyRepository;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplyService {

    private final ApplyRepository applyRepository;
    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;

    /**
     * 스터디 신청
     */
    @Transactional
    public Long apply(Long memberId, Long studyId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new EntityNotFoundException("스터디가 존재하지 않습니다."));

        if (!study.getStatus().equals(StudyStatus.RECRUITING)) {
            throw new IllegalStateException("스터디 모집중이 아닙니다.");
        }
        if (study.getAdmin().getId().equals(memberId)) {
            throw new IllegalStateException("스터디장은 신청할 수 없습니다.");
        }
        if (applyRepository.existsByMemberAndStudy(member, study)) {
            throw new IllegalStateException("이미 신청한 스터디입니다.");
        }

        Apply apply = Apply.createApply(member, study);
        applyRepository.save(apply);

        return apply.getId();
    }

    /**
     * 신청 승인 (관리자)
     */
    @Transactional
    public void approve(Long applyId, Long adminId) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 신청입니다."));

        validateAdminPermission(apply.getStudy(), adminId); // 관리자 검증

        apply.approve();
    }

    /**
     * 신청 거절 (관리자)
     */
    @Transactional
    public void reject(Long applyId, Long adminId) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 신청입니다."));

        validateAdminPermission(apply.getStudy(), adminId); // 관리자 검증

        apply.reject();
    }

    /**
     * 신청 취소 (회원)
     */
    @Transactional
    public void cancel(Long applyId, Long memberId) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 신청입니다."));
        if (!apply.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인의 신청만 취소할 수 있습니다.");
        }
        if (apply.getStatus() == ApplyStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 신청은 취소할 수 없습니다.");
        }

        applyRepository.delete(apply);
    }

    /**
     * 특정 스터디의 신청 목록 조회
     */
    public List<Apply> findApplyByStudy(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalStateException("스터디가 존재하지 않습니다."));
        return applyRepository.findByStudy(study);
    }

    /**
     * 특정 회원의 신청 목록 조회
     */
    public List<Apply> findApplyByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));
        return applyRepository.findByMember(member);
    }

    // 관리자 권한 검증
    private void validateAdminPermission(Study study, Long adminId) {
        if (!study.getAdmin().getId().equals(adminId)) {
            throw new IllegalStateException("관리자만 승인/거절할 수 있습니다.");
        }
    }
}