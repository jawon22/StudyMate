package mate.StudyMate.repository;

import mate.StudyMate.domain.file.FileEntity;
import mate.StudyMate.domain.file.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByRelatedIdAndType(Long relatedId, FileType type);
}