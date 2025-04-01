package mate.StudyMate.service;

import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.study.Study;
import mate.StudyMate.domain.study.StudyMember;
import mate.StudyMate.domain.study.StudyRole;
import mate.StudyMate.domain.study.StudyStatus;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.repository.StudyRepository;
import mate.StudyMate.repository.StudySearchCond;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    /**
     * 스터디 생성
     */
    @Transactional
    public Long createStudy(Long adminId, String name, String description, boolean isPrivate, int maxMembers) {
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if(maxMembers <= 1) {
            throw new IllegalStateException("인원 수는 최소 2명 이상이어야 합니다.");
        }

        Study study = Study.createStudy(admin, name, description, isPrivate, maxMembers);

        studyRepository.save(study);
        return study.getId();
    }

    /**
     * 스터디 수정 (이름, 설명, 최대 인원 변경) - 관리자만 가능
     */
    @Transactional
    public void updateStudy(Long studyId, Long adminId, String name, String description, int maxMembers) {
        Study findStudy = validationExistStudyAndStudyOwner(studyId, adminId);
        if(maxMembers <= 1) {
            throw new IllegalStateException("인원 수는 최소 2명 이상이어야 합니다.");
        }

        findStudy.update(name, description, maxMembers);
    }

    /**
     * 스터디 관리자 변경 - 관리자만 가능
     */
    @Transactional
    public void changeStudyAdmin(Long studyId, Long adminId, Long newAdminId) {
        Study study = validationExistStudyAndStudyOwner(studyId, adminId);

        // 새로운 관리자가 스터디에 존재 유무
        StudyMember newAdmin = study.getMembers().stream()
                .filter(sm -> sm.getMember().getId().equals(newAdminId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("해당 회원은 스터디 내에 존재하지 않습니다."));

        // 기존 관리자
        StudyMember currentAdmin = study.getMembers().stream()
                .filter(sm -> sm.getMember().getId().equals(adminId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("현재 관리자가 존재하지 않습니다."));

        // 관리자 역할 변경
        currentAdmin.changeRole(StudyRole.MEMBER);
        study.changeAdmin(newAdmin.getMember());
    }


    /**
     * 스터디 삭제 - 관리자만 가능
     */
    @Transactional
    public void deleteStudy(Long studyId, Long adminId) {
        Study study = validationExistStudyAndStudyOwner(studyId, adminId);

        studyRepository.delete(study);
    }

    /**
     * 스터디 동적조회 (필터링: 이름, 공개/비공개, 상태)
     */
    public List<Study> findAll(StudySearchCond cond) {
        return studyRepository.searchAll(cond);
    }


    /**
     * 스터디 참가
     */
    @Transactional
    public void joinStudy(Long studyId, Long memberId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
        Member existMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (!study.getStatus().equals(StudyStatus.RECRUITING)) {
            throw new IllegalStateException("스터디 모집중이 아닙니다.");
        }
        if (isAlreadyJoined(memberId, study)) {
            throw new IllegalStateException("스터디에 참가한 회원입니다.");
        }
        if (study.getCurrentMembers() >= study.getMaxMembers()) {
            throw new IllegalStateException("스터디 정원이 초과되었습니다.");
        }

        study.getMembers().add(StudyMember.createMember(study, existMember, StudyRole.MEMBER));
        study.addCurrentMember(1);
    }

    /**
     * 스터디 탈퇴
     */
    @Transactional
    public void leaveStudy(Long studyId, Long memberId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));

        if (study.getAdmin().getId().equals(memberId)) {
            throw new IllegalStateException("스터디 관리자는 탈퇴할 수 없습니다. 관리자를 변경해주세요.");
        }

        StudyMember studyMember = study.getMembers().stream().
                filter(sm -> sm.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("스터디에 존재하지 않는 회원입니다."));

            study.getMembers().remove(studyMember);
            study.reduceCurrentMember(1);
    }

    /**
     * 이미 참가한 회원인지 검증
     */
    private boolean isAlreadyJoined(Long memberId, Study study) {
        return study.getMembers().stream().anyMatch(sm -> sm.getMember().getId().equals(memberId));
    }

    /**
     * 스터디 존재 유무, 관리자 인증
     */
    private Study validationExistStudyAndStudyOwner(Long studyId, Long adminId) {
        Study findStudy = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));

        if (!findStudy.getAdmin().getId().equals(adminId)) {
            throw new IllegalStateException("해당 작업은 스터디 관리자만 수행할 수 있습니다.");
        }
        return findStudy;
    }
}