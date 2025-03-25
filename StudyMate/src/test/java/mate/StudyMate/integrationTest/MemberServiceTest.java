package mate.StudyMate.integrationTest;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import mate.StudyMate.StudyMateApplication;
import mate.StudyMate.controller.DeleteMemberForm;
import mate.StudyMate.domain.file.FileEntity;
import mate.StudyMate.domain.file.FileType;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.domain.member.MemberStatus;
import mate.StudyMate.repository.FileRepository;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.service.FileService;
import mate.StudyMate.service.MemberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = StudyMateApplication.class)
@Transactional
@Slf4j
public class MemberServiceTest {
    // 회원 프로필 테스트시 주석으로 변경
    @Autowired // 회원 프로필시 @InjectMocks
    MemberService memberService;
    @Autowired // @Mock
    MemberRepository memberRepository;
    @Autowired // @Mock
    PasswordEncoder passwordEncoder;
    @Autowired // @Mock
    FileService fileService;
    @Autowired // @Mock
    FileRepository fileRepository;

    @Value("${file.dir}")
    private String fileDir;

    private MultipartFile oldFile;
    private MultipartFile newFile;

    @BeforeEach
    void setup() {
        // 기존 프로필 파일
        oldFile = mock(MultipartFile.class);
        lenient().when(oldFile.getOriginalFilename()).thenReturn("old.jpg");
        lenient().when(oldFile.getSize()).thenReturn(1024L);

        // 변경할 새 프로필 파일
        newFile = mock(MultipartFile.class);
        lenient().when(newFile.getOriginalFilename()).thenReturn("new.jpg");
        lenient().when(newFile.getSize()).thenReturn(2048L);
    }

    @Test
    public void 회원가입() {
        //given
        Member member = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");
        Member member2 = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");

        //when
        Long savedId = memberService.join(member);

        //then
        Optional<Member> findMember = memberRepository.findById(savedId);
        assertThat(findMember.get().getName()).isEqualTo(member.getName());
        assertThat(findMember.get().getPassword()).isNotEqualTo(member.getPassword());
    }

    @Test
    public void 중복_회원_예외() {
        // given
        Member member = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");
        Member member2 = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");

        // when
        memberService.join(member);

        // then
        Assertions.assertThrows(IllegalStateException.class, () -> memberService.validateDuplicateMember(member2));
    }

    @Test
    public void 회원_비밀번호_변경() {
        // given
        Member member = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");
        Long joinId = memberService.join(member);

        String newPassword = "newPassword"; // 새 비밀번호

        // when
        memberService.changePassword(joinId,"password1",newPassword);
        Member changeMember = memberRepository.findById(joinId).get();

        // then
        assertThat(!passwordEncoder.matches(newPassword,changeMember.getPassword()));
    }

    @Test
    public void 회원탈퇴() {
        // given
        Member member = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");
        Long joinId = memberService.join(member);

        Member findMember = memberRepository.findById(joinId)
                    .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        DeleteMemberForm form = new DeleteMemberForm();
        form.setName("user");
        form.setPassword("password1");
        form.setEmail("test@email.com");
        // when
        memberService.deleteMember(findMember.getId(),form);

        // then
        assertThat(findMember.getStatus()).isEqualTo(MemberStatus.INACTIVE);
    }

    @Test
    void 프로필_이미지_변경() throws IOException {
        // given
        Member member = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");
        Long joinId = memberService.join(member);

        when(memberRepository.findById(joinId)).thenReturn(Optional.of(member));

        // 기존 프로필 저장
        FileEntity oldFileEntity = new FileEntity(joinId, FileType.PROFILE, "old.jpg", "oldUUID.jpg", 1024L, "jpg");
        when(fileService.saveFile(joinId, FileType.PROFILE, oldFile)).thenReturn(oldFileEntity);

        // 기존 파일이 있다고 가정
        doNothing().when(fileService).deleteFile(joinId, FileType.PROFILE);

        // 새로운 프로필 저장 (Mock)
        FileEntity newFileEntity = new FileEntity(joinId, FileType.PROFILE, "new.jpg", "newUUID.jpg", 2048L, "jpg");
        when(fileService.saveFile(joinId, FileType.PROFILE, newFile)).thenReturn(newFileEntity);

        // When: 프로필 변경 실행
        memberService.changeProfile(joinId, newFile);

        // then
        verify(fileService, times(1)).deleteFile(joinId, FileType.PROFILE); // 기존 파일 삭제 검증
        verify(fileService, times(1)).saveFile(joinId, FileType.PROFILE, newFile); // 새 파일 저장 검증
    }
}