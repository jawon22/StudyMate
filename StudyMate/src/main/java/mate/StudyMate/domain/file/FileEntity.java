package mate.StudyMate.domain.file;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file")
public class FileEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(name = "related_id", nullable = false)
    private Long relatedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private FileType type; // 파일 용도

    @Column(nullable = false)
    private String url; // 저장된 파일 경로

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType; // MIME 타입 (png, jpg ...)

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;


    //생성자
    public FileEntity(Long relatedId, FileType type, String url, String filename, Long fileSize, String fileType) {
        this.relatedId = relatedId;
        this.type = type;
        this.url = url;
        this.filename = filename;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploadedAt = LocalDateTime.now();
    }
}