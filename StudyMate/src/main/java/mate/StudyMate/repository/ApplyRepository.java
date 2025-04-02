package mate.StudyMate.repository;

import mate.StudyMate.domain.apply.Apply;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.study.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplyRepository extends JpaRepository<Apply, Long> {
    boolean existsByMemberAndStudy(Member member, Study study);
    List<Apply> findByStudy(Study study);
    List<Apply> findByMember(Member member);
}