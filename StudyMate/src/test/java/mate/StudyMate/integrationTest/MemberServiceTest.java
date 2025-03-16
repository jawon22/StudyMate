package mate.StudyMate.integrationTest;

import lombok.extern.slf4j.Slf4j;
import mate.StudyMate.StudyMateApplication;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.service.MemberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = StudyMateApplication.class)
@Transactional
@Slf4j
public class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;

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
        Assertions.assertThrows(IllegalStateException.class, () -> memberService.join(member2));
    }
}