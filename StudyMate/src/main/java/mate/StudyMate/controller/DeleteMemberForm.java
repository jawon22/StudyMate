package mate.StudyMate.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteMemberForm {

    @NotEmpty(message = "아이디를 입력해주세요")
    private String name;

    @NotEmpty(message = "비밀번호를 입력해주세요")
    private String password;

    @NotEmpty(message = "이메일을 입력해주세요")
    private String email;
}