package ktb.leafresh.backend.domain.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "refresh_token")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @Column(name = "rt_key", length = 255, nullable = false)
    private String rtKey;

    @Column(name = "rt_value", length = 512, nullable = false)
    private String rtValue;
}
