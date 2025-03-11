package mate.StudyMate.domain.oauth;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mate.StudyMate.domain.member.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "oauth_provider")
public class OauthProvider {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_provider_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_name", nullable = false, length = 50)
    private OauthProviderName ProviderName;


    // 생성자
    public OauthProvider(Member member, OauthProviderName providerName) {
        this.member = member;
        ProviderName = providerName;
    }
}