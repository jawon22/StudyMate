package mate.StudyMate.domain.study;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mate.StudyMate.domain.member.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_member")
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private StudyRole role;

    /**
     * 생성자 메서드
     */
    public static StudyMember createMember(Study study, Member member, StudyRole role) {
        StudyMember studyMember = new StudyMember();
        studyMember.study = study;
        studyMember.member = member;
        studyMember.role = role;
        return studyMember;
    }

    /**
     * 권한 변경 메서드
     */
    public void changeRole(StudyRole role) {
        this.role = role;
    }
}