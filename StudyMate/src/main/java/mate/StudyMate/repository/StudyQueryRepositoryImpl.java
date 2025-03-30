package mate.StudyMate.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.study.Study;
import mate.StudyMate.domain.study.StudyStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static mate.StudyMate.domain.study.QStudy.*;

@Repository
@Transactional
@RequiredArgsConstructor
public class StudyQueryRepositoryImpl implements StudyQueryRepository{

    private final JPAQueryFactory query;

    @Override
    public List<Study> searchAll(StudySearchCond cond) {

        return query
                .select(study)
                .from(study)
                .where(
                        likeStudyName(cond.getStudyName()),
                        hasStatus(cond.getStatus()),
                        isPrivate(cond.isPrivate())
                )
                .fetch();
    }

    private BooleanExpression likeStudyName(String studyName) { // 조건 1
        if (StringUtils.hasText(studyName)) {
            return study.name.like("%" + studyName + "%");
        }
        return null;
    }

    private BooleanExpression hasStatus(StudyStatus status) {
        if (status != null) {
            return study.status.eq(status);
        }
        return null;
    }

    private BooleanExpression isPrivate(boolean isPrivate) {
        return study.isPrivate.eq(isPrivate);
    }
}