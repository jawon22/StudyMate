package mate.StudyMate.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MemberStatus status;

    @Column(name= "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 탈퇴시 저장

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "oauth_id")
    private String oauthId;


    // 회원 생성 메서드
    public static Member createMember(String name, String password, String email, String phoneNumber) {
        Member member = new Member();
        member.name = name;
        member.password = password;
        member.email = email;
        member.role = Role.USER;
        member.status = MemberStatus.ACTIVE;
        member.createdAt = LocalDateTime.now();
        member.emailVerified = false;
        member.phoneNumber = phoneNumber;

        return member;
    }

    // 회원 정보 업데이트
    public void updateMember(String name, String profileImage, String phoneNumber) {
        this.name = name;
        this.profileImage = profileImage;
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }


    // 회원 탈퇴 ( 계정 비활성화)
    public void deleteMember() {
        this.status = MemberStatus.INACTIVE;
        this.deletedAt = LocalDateTime.now();
    }
}