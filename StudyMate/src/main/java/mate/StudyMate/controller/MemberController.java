package mate.StudyMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.StudyMate.domain.member.Member;
import mate.StudyMate.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("memberForm", new AddMemberForm());
        return "members/addForm";
    }

    @PostMapping("/add")
    public String save(@Valid @ModelAttribute("memberForm") AddMemberForm addMemberForm, BindingResult result) {
        if (result.hasErrors()) {
            return "members/addForm";
        }

        try {
            Member newMember = Member.createMember(
                    addMemberForm.getName(),
                    addMemberForm.getPassword(),
                    addMemberForm.getEmail(),
                    addMemberForm.getPhoneNumber()
            );
            memberService.join(newMember);

            return "redirect:/";
        } catch (IllegalStateException e) {
            result.reject("addFail", e.getMessage());
            return "members/addForm";
        }
    }
}