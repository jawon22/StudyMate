package mate.StudyMate.repository;

import mate.StudyMate.domain.study.Study;

import java.util.List;

public interface StudyQueryRepository {
    List<Study> searchAll(StudySearchCond cond);
}