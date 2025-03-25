package mate.StudyMate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class MailService {

    private final JavaMailSender mailSender;
    private final Random random = new Random();

    private final Map<String, String> emailVerifyStore = new ConcurrentHashMap<>();

    /**
     * 이메일 인증 코드 전송
     * */
    public void sendVerifyEmail(String email) {
        String code = generateVerificationCode(); // 6자리 인증코드
        emailVerifyStore.put(email, code); // 인증 코드 저장

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("StudyMate 이메일 인증 코드");
        message.setText("인증코드: "+code);

        mailSender.send(message);
    }

    /**
     * 이메일 인증 코드 검증
     */
    public boolean verifyEmail(String email, String inputCode) {
        String storeCode = emailVerifyStore.get(email);
        if (storeCode != null && storeCode.equals(inputCode)) {
            emailVerifyStore.remove(email); // 인증 성공시 제거
            return true;
        }
        return false;
    }

    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
}