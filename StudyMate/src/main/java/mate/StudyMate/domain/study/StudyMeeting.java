package mate.StudyMate.domain.study;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_meeting")
public class StudyMeeting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_meeting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description")
    private String description;

    // 생성자
    public StudyMeeting(Study study, LocalDateTime startTime,
                            LocalDateTime endTime, String title, String description) {
        this.study = study;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.description = description;
    }

    // 비즈니스 메서드 - 시간검증
    public void validateTime() {
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("종료시간이 시작시간보다 빠를 수 없음.");
        }
    }
}
