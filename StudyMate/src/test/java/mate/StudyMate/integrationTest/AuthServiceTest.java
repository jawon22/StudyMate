package mate.StudyMate.integrationTest;

import mate.StudyMate.domain.member.Member;
import mate.StudyMate.repository.MemberRepository;
import mate.StudyMate.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthServiceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        Member member = Member.createMember("user", "password1", "test@email.com", "010-1231-6234");
        memberService.join(member);
    }

    @Test
    public void 로그인_성공테스트() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("name", "user")
                        .param("password", "password1"))
                .andExpect(status().is3xxRedirection()) // 리다이렉트 확인
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void 로그인_실패테스트() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("name", "user")
                        .param("password", "wrongPassword"))
                .andExpect(status().isOk()) // 로그인 페이지 다시 로딩됨
                .andExpect(view().name("auth/loginForm")) // 로그인 폼 다시 보여줌
                .andExpect(model().attributeHasErrors("loginForm")); // 글로벌 에러가 존재하는지 확인
    }

    @Test
    public void 로그아웃_테스트() throws Exception {
        // 로그인 수행
        mockMvc.perform(post("/auth/login")
                        .param("name", "user")
                        .param("password", "password1"))
                .andExpect(status().is3xxRedirection()) // 리다이렉트 확인
                .andExpect(redirectedUrl("/"));

        // 로그아웃 수행
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}