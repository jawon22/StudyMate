package mate.StudyMate.service;

import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.file.FileEntity;
import mate.StudyMate.domain.file.FileType;
import mate.StudyMate.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.dir}")
    private String fileDir;

    @Transactional
    public FileEntity saveFile(Long relatedId, FileType type, MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFileName); // 서버 저장용 파일명

        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return fileRepository.save(new FileEntity(relatedId, type, originalFileName, storeFileName,
                multipartFile.getSize(), extractExt(originalFileName)));
    }

    @Transactional
    public void deleteFile(Long relatedId, FileType type) throws IOException {
        FileEntity file = fileRepository.findByRelatedIdAndType(relatedId, type)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 파일이 존재하지 않습니다."));

        String fullPath = getFullPath(file.getStoreFileName());
        File physicalFile = new File(fullPath);
        if(physicalFile.exists()){
            boolean deleted = physicalFile.delete();
            if(!deleted) throw new IOException("파일삭제에 실패했습니다: " + fullPath);
        }

        fileRepository.delete(file);
    }

    private String createStoreFileName(String originalFileName) { // 확장자를 붙인 db 저장용 파일명 생성
        String ext = extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFileName) { // File 이름 뒤의 확장자 가져오기
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(pos + 1);
    }

    public String getFullPath(String fileName) { // 파일명을 받아 FullPath 반환
        return fileDir + fileName;
    }
}