package mate.StudyMate.domain.study;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mate.StudyMate.domain.member.Member;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class Study {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member admin;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyStatus status; // 스터디 상태

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate; // 스터디 공개 여부

    @Column(name = "max_members", nullable = false)
    private int maxMembers;

    @Column(name = "current_members", nullable = false)
    private int currentMembers;


    // 스터디 생성메서드
    public static Study createStudy(Member admin, String name, String description, boolean isPrivate, int maxMembers) {
        Study study = new Study();
        study.admin = admin;
        study.name = name;
        study.description = description;
        study.isPrivate = isPrivate;
        study.maxMembers = maxMembers;
        study.createdAt = LocalDateTime.now();
        study.status = StudyStatus.RECRUITING;

        return study;
    }
}