package mate.StudyMate.repository;

import mate.StudyMate.domain.study.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyQueryRepository {

}