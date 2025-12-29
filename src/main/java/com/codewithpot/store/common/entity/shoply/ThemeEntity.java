package com.codewithpot.store.common.entity.shoply;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "theme")
@Data
public class ThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "primary_color")
    private String primary;
    private String dark;
    private String light;
    private String lighter;
    private String text;
    private String bg;

    @Column(name = "pre_bg")
    private String preBg;

    private String border;

    @Column(name = "stat_bg")
    private String statBg;

    @OneToOne
    @JoinColumn(name = "competency_id", nullable = false)
    private CompetencyEntity competency;
}