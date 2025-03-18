package mate.StudyMate.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddMemberForm {

    @NotEmpty(message = "아이디는 필수")
    private String name;

    @NotEmpty(message = "비밀번호는 필수")
    private String password;

    @NotEmpty(message = "이메일은 필수")
    private String email;

    @NotEmpty(message = "휴대번호는 필수")
    private String phoneNumber;
}