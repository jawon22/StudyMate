package mate.StudyMate.domain.apply;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.study.Study;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "apply")
public class Apply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 신청자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study; // 신청한 스터디

    @Enumerated(EnumType.STRING)
    @Column(name= "status", nullable = false, length = 10)
    private ApplyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 신청시간

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt; // 상태 변경 시간 (db에서 트리거로 설정)


    // 신청 생성 메서드
    public static Apply createApply(Member member, Study study) {
        Apply apply = new Apply();
        apply.member = member;
        apply.study = study;
        apply.status = ApplyStatus.PENDING;
        apply.createdAt = LocalDateTime.now();
        return apply;
    }

    // 비즈니스 메서드
    public void approve() { // 승인 상태 변경
        this.status = ApplyStatus.APPROVED;
    }
    public void reject() { // 거절 상태 변경
        this.status = ApplyStatus.REJECTED;
    }
}
