package com.codewithpot.store.common.entity.shoply;

import com.codewithpot.store.competency.dto.Theme;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Competency")
@Accessors(chain = true)
@Data
public class CompetencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "competency_id")
    private Integer competencyId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "competency_level")
    private Integer competencyLevel;

    @OneToOne(mappedBy = "competency", cascade = CascadeType.ALL)
    private ThemeEntity theme;

    @OneToMany(mappedBy = "competency", cascade = CascadeType.ALL)
    private List<CriteriaEntity> criteria = new ArrayList<>();
}
