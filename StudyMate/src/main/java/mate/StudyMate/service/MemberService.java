package mate.StudyMate.service;

import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder  passwordEncoder;

    @Transactional
    public Long join(Member member) {  //회원가입
        validateDuplicateMember(member); // 중복 회원 검증 로직
        String encodedPassword = passwordEncoder.encode(member.getPassword()); // 비번 암호화

        Member newMember = Member.createMember(member.getName(), encodedPassword, member.getEmail(), member.getPhoneNumber());

        memberRepository.save(newMember);
        return newMember.getId();
    }

    @Transactional
    public void changePassword(Long memberId, String oldPassword, String newPassword) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(oldPassword, findMember.getPassword())) {
            throw new IllegalArgumentException("입력한 비밀번호가 동일하지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        findMember.changePassword(encodedNewPassword);
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembersByName = memberRepository.findByName(member.getName());
        Optional<Member> findMembersByEmail = memberRepository.findByEmail(member.getEmail());

        if (!findMembersByName.isEmpty() && findMembersByEmail.isPresent()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findById(memberId).get();
    }

}