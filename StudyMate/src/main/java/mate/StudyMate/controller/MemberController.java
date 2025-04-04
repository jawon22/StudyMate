package mate.StudyMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.service.MailService;
import mate.StudyMate.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MailService mailService;

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("memberForm", new AddMemberForm());
        return "members/addForm";
    }

    @PostMapping("/send-code")
    @ResponseBody
    public ResponseEntity<?> sendVerifyCode(@RequestParam String email, @RequestParam String name) {
        try {
            Member dummy = Member.createMember(name, "temp123", email, "01000000000");
            memberService.sendEmailVerify(dummy);
            return ResponseEntity.ok("인증 코드가 전송되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add")
    public String save(@Valid @ModelAttribute("memberForm") AddMemberForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "members/addForm";
        }

        // 인증코드 검증
        boolean isVerified = mailService.verifyEmail(form.getEmail(), form.getInputCode());
        if (!isVerified) {
            result.rejectValue("inputCode", "invalid", "인증 코드가 일치하지 않습니다.");
            return "members/addForm";
        }

        try {
            Member newMember = Member.createMember(
                    form.getName(),
                    form.getPassword(),
                    form.getEmail(),
                    form.getPhoneNumber()
            );
            memberService.join(newMember);

            return "redirect:/";
        } catch (IllegalStateException e) {
            result.reject("addFail", e.getMessage());
            return "members/addForm";
        }
    }
}