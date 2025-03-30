package mate.StudyMate.repository;

import lombok.Data;
import mate.StudyMate.domain.study.StudyStatus;

@Data
public class StudySearchCond {

    private String studyName;
    private StudyStatus status;
    private boolean isPrivate;

    public StudySearchCond() {
    }

    public StudySearchCond(String studyName, StudyStatus status, boolean isPrivate) {
        this.studyName = studyName;
        this.status = status;
        this.isPrivate = isPrivate;
    }
}