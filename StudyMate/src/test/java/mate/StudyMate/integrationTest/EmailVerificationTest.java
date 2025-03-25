package mate.StudyMate.integrationTest;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.service.MailService;
import mate.StudyMate.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class EmailVerificationTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MailService mailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @MockBean // JavaMailSender를 Mock 객체로 등록
    private JavaMailSender mailSender;
    @Autowired
    MemberRepository memberRepository;

    private Member member;
    @BeforeEach
    public void setup() {
        member = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");
    }

    @Test
    void 회원가입_이메일_인증메일_발송() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class)); // 메일 전송 Mocking

        // when
        mailService.sendVerifyEmail(member.getEmail()); // 이메일 인증 요청

        // then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));  // 이메일이 한 번 전송되었는지 확인
    }

    @Test
    void 이메일_인증시_회원_컬럼인증_변경() {
        // given: 이메일 인증이 완료되었다고 가정 - verifyEmail이 true

        // when
        Long saveId = memberService.join(member);
        Member saveMember = memberRepository.findById(saveId).orElseThrow();

        // then
        assertTrue(saveMember.isEmailVerified(),"이메일 인증 후 상태가 true");
    }
}
