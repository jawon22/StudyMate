package mate.StudyMate.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotEmpty(message = "아이디 입력은 필수입니다.")
    private String name;

    @NotEmpty(message = "비밀번호 입력은 필수입니다.")
    private String password;
}
