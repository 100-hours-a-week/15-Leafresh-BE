package ktb.leafresh.backend.domain.member.domain.entity;

import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tree_levels")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreeLevel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "treeLevel", cascade = CascadeType.ALL)
  private List<Member> members = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'SPROUT'")
  private TreeLevelName name;

  @Column(nullable = false)
  private Integer minLeafPoint;

  @Column(nullable = false, length = 512)
  private String imageUrl;

  @Column(nullable = false)
  private String description;

  @PrePersist
  public void prePersist() {
    if (name == null) this.name = TreeLevelName.SPROUT;
  }
}
