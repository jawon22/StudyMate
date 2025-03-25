package mate.StudyMate.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddMemberForm {

    @NotEmpty(message = "아이디는 필수입니다.")
    private String name;

    @NotEmpty(message = "비밀번호는 필수입니다.")
    private String password;

    @NotEmpty(message = "이메일은 필수입니다.")
    @Email(message = "올바른 형식의 이메일을 작성해주세요.")
    private String email;

    @NotEmpty(message = "휴대번호는 필수입니다.")
    private String phoneNumber;

    @NotEmpty(message = "이메일 인증 코드를 입력해주세요")
    private String inputCode; // 이메일 인증 코드
}