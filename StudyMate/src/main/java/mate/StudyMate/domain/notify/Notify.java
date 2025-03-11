package mate.StudyMate.domain.notify;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mate.StudyMate.domain.member.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notify")
public class Notify {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notify_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "related_id", nullable = false)
    private Long relatedId; // 관련된 엔티티 id

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotifyType type; // 알림 유형

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", insertable = false)
    private LocalDateTime deletedAt; // 일정기간 후 삭제


    // 생성자
    public Notify(Member member, Long relatedId, NotifyType type, String content) {
        this.member = member;
        this.relatedId = relatedId;
        this.type = type;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}