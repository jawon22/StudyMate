package mate.StudyMate.service;

import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member loginAuthenticate(String name, String password) { // 로그인 인증(아이디, 비밀번호)
        Member member = memberRepository.findByName(name).stream().findAny()
                .orElseThrow(() -> new IllegalArgumentException("입력한 아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("입력한 비밀번호가 올바르지 않습니다.");
        }

        return member;
    }
}