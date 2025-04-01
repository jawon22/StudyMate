package mate.StudyMate.domain.study;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mate.StudyMate.domain.member.Member;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<StudyMember> members = new ArrayList<>();

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

    // 연관관계 메서드 //
    public void addStudyMember(Member member, StudyRole role) {
        this.members.add(StudyMember.createMember(this, member, role));
    }

    // 스터디 생성메서드 //
    public static Study createStudy(Member admin, String name, String description,
                                    boolean isPrivate, int maxMembers) {
        Study study = new Study();
        study.admin = admin;
        study.name = name;
        study.description = description;
        study.isPrivate = isPrivate;
        study.maxMembers = maxMembers;
        study.createdAt = LocalDateTime.now();
        study.status = StudyStatus.RECRUITING;

        study.addStudyMember(admin,StudyRole.ADMIN);
        study.currentMembers = 1;

        return study;
    }

    // 스터디 수정 메서드
    public void update(String name, String description, int maxMembers) {
        this.name = name;
        this.description = description;
        this.maxMembers = maxMembers;
    }

    // 관리자 변경 메서드
    public void changeAdmin(Member newAdmin) {
        this.admin = newAdmin;
    }

    // 비즈니스 로직 //
    /**
     * 스터디 인원수 증가 (신청한 사람들 중에 승인된 사람)
     */
    public void addCurrentMember(int count) {
        if(currentMembers >= maxMembers) {
            throw new IllegalStateException("스터디 내의 인원수가 다 찼습니다.");
        }
        currentMembers += count;
    }

    /**
     * 스터디 인원수 감소 ( 스터디 내의 사람 중 나간사람, 회원 탈퇴한 사람)
     */
    public void reduceCurrentMember(int count) {
        int restMembers = currentMembers - count;
        if(restMembers < 1){
            throw new IllegalStateException("스터디의 인원은 최소 1명입니다.");
        }
        currentMembers = restMembers;
    }
}