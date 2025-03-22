package mate.StudyMate.integrationTest;

import lombok.extern.slf4j.Slf4j;
import mate.StudyMate.domain.file.FileEntity;
import mate.StudyMate.domain.file.FileType;
import mate.StudyMate.repository.FileRepository;
import mate.StudyMate.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    @Value("${file.dir}")
    private String fileDir;

    private MultipartFile multipartFile;

    @BeforeEach
    public void setup() {
        multipartFile = mock(MultipartFile.class);
        lenient().when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        lenient().when(multipartFile.getSize()).thenReturn(1024L);
    }

    @Test
    void 파일저장() throws IOException {
        //given
        String storeFileName = "randomUUID.jpg";

        FileEntity mockFile = new FileEntity(1L, FileType.PROFILE, "test.jpg", storeFileName, 1024L, "image/jpeg");
        when(fileRepository.save(any(FileEntity.class))).thenReturn(mockFile);

        //when
        FileEntity file = fileService.saveFile(1L, FileType.PROFILE, multipartFile);

        //then
        assertNotNull(file);
        assertEquals("test.jpg",file.getFilename());
        assertTrue(file.getStoreFileName().startsWith("randomUUID"));
        assertEquals(1024L,file.getFileSize());
    }

/*
    @Test
    void 파일삭제() throws IOException {
        // given
        FileEntity realFile = new FileEntity(1L, FileType.PROFILE, "test.jpg", "randomUUID.jpg", 1024L, "image/jpeg");
        when(fileRepository.save(any(FileEntity.class))).thenReturn(realFile);
        when(fileRepository.findByRelatedIdAndType(1L, FileType.PROFILE)).thenReturn(Optional.of(realFile));

        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.delete()).thenReturn(true);

        // when
        fileService.deleteFile(1L,FileType.PROFILE);

        // then
        verify(fileRepository,times(1)).delete(realFile); // db에서 삭제가 호출되었는지 확인
        verify(mockFile, times(1)).delete(); // 실제 파일 삭제가 호출되었는지 확인
    }
*/

    @Test
    void 파일경로_확인() {
        // given
        String fileName = "test.jpg";
        String fullPath = fileService.getFullPath(fileName);

        // then
        // 경로가 fileDir까지 결합된 형태인지 확인
        assertEquals(fileDir+fileName,fullPath);
    }
}