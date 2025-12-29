package com.codewithpot.store.competency;

import com.codewithpot.store.common.entity.shoply.CompetencyEntity;
import com.codewithpot.store.common.entity.shoply.CriteriaEntity;
import com.codewithpot.store.common.entity.shoply.SourceScoringEntity;
import com.codewithpot.store.common.entity.shoply.ThemeEntity;
import com.codewithpot.store.common.repository.shoply.CompetencyRepository;
import com.codewithpot.store.common.repository.shoply.CriteriaRepository;
import com.codewithpot.store.common.repository.shoply.SourceScoringRepository;
import com.codewithpot.store.common.repository.shoply.ThemeRepository;
import com.codewithpot.store.competency.dto.CompetencyConfig;
import com.codewithpot.store.competency.dto.Criteria;
import com.codewithpot.store.competency.dto.SourceScoring;
import com.codewithpot.store.competency.dto.Theme;
import com.codewithpot.store.json.model.UserModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "Competency")
public class CompetencyController {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private CriteriaRepository criteriaRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SourceScoringRepository sourceScoringRepository;

//    @GetMapping("/competency")
//    public ResponseEntity<List<CompetencyConfig>> competencyConfig() throws IOException {
//        List<CompetencyConfig> result = new ArrayList<>();
//
//        // Multi file Reader
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource[] resources = resolver.getResources("classpath:json/competencies/*.json");
//
//        for (Resource resource : resources) {
//            try (InputStream is = resource.getInputStream()) {
//
//                CompetencyConfig config = mapper.readValue(is, CompetencyConfig.class);
//
//                String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
//                log.info("------------------------------------------");
//                log.info("FILE: {}", resource.getFilename());
//                log.info("DATA FROM JSON (Pretty):\n{}", prettyJson);
//                log.info("------------------------------------------");
//
//                result.add(config);
//            }
//        }
//
//        return ResponseEntity.ok(result);
//    }

//    @PostMapping(path = "/competency-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<CompetencyConfig> competencyConfigRequest(@RequestPart(value = "file") MultipartFile file) throws IOException {
//        try (InputStream inputStream = file.getInputStream()) {
//            CompetencyConfig compFile = mapper.readValue(inputStream, CompetencyConfig.class);
//
//            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(compFile);
//            log.info("------------------------------------------");
//            log.info("DATA FROM JSON (Pretty):\n{}", prettyJson);
//            log.info("------------------------------------------");
//
//            compFile.getCriteria().forEach((key, value) -> {
//                System.out.println("key = " + key);
//                System.out.println("value = " + value);
//                System.out.println("SourceScoring = " + value.getSourceScoring());
//                System.out.println("SourceScoring.IDP = " + value.getSourceScoring().getIdp());
//                System.out.println("SourceScoring.AtsJourney = " + value.getSourceScoring().getAtsJourney());
//            });
//
//            return ResponseEntity.ok(compFile);
//        }
//    }

    @PostMapping(path = "/competency-save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> competencyConfigSave(@RequestPart("file") MultipartFile file) throws IOException {

        CompetencyConfig compFile;
        try (InputStream is = file.getInputStream()) {
            compFile = mapper.readValue(is, CompetencyConfig.class);
        }

        log.info("===== COMPETENCY CONFIG =====");
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(compFile));

        CompetencyEntity competency = new CompetencyEntity();
        competency.setDisplayName(compFile.getDisplayName());
        competency.setIcon(compFile.getIcon());
        competency.setSortOrder(compFile.getOrder());
        competency.setCompetencyLevel(compFile.getCompetencyLevel());

        ThemeEntity theme = new ThemeEntity();
        theme.setPrimary(compFile.getTheme().getPrimary());
        theme.setDark(compFile.getTheme().getDark());
        theme.setLight(compFile.getTheme().getLight());
        theme.setLighter(compFile.getTheme().getLighter());
        theme.setText(compFile.getTheme().getText());
        theme.setBg(compFile.getTheme().getBg());
        theme.setPreBg(compFile.getTheme().getPreBg());
        theme.setBorder(compFile.getTheme().getBorder());
        theme.setStatBg(compFile.getTheme().getStatBg());
        theme.setCompetency(competency);
        competency.setTheme(theme);

        List<CriteriaEntity> criteriaEntities = new ArrayList<>();

        compFile.getCriteria().forEach((code, value) -> {

            CriteriaEntity criteria = new CriteriaEntity();
            criteria.setCode(code);
            criteria.setTitle(value.getTitle());
            criteria.setThai(value.getThai());
            criteria.setEnglish(value.getEnglish());
            criteria.setMaximumScore(value.getMaximumScore());
            criteria.setTotalEvaluationsAllowed(value.getTotalEvaluationsAllowed());
            criteria.setCompetency(competency);

            List<SourceScoringEntity> scoringList = new ArrayList<>();

            value.getSourceScoring().forEach((sourceName, scoring) -> {
                SourceScoringEntity ss = new SourceScoringEntity();
                ss.setSourceName(sourceName);
                ss.setMaximumScorePerEvaluation(scoring.getMaximumScorePerEvaluation());
                ss.setAcceptedMinScore(scoring.getAcceptedMinScore());
                ss.setCriteria(criteria);

                scoringList.add(ss);
            });

            criteria.setSourceScoring(scoringList);
            criteriaEntities.add(criteria);
        });
        competency.setCriteria(criteriaEntities);

        competencyRepository.save(competency);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/competencies/{id}")
    public ResponseEntity<CompetencyConfig> getById(@PathVariable int id) {

        CompetencyEntity competencyEntity = competencyRepository.findById(id).orElse(null);
        if (competencyEntity == null) {
            throw CompetencyException.dataNotFound();
        }

        Theme theme = mapTheme(competencyEntity.getTheme());
        Map<String, Criteria> criteriaMap = new LinkedHashMap<>();
        for (CriteriaEntity c : competencyEntity.getCriteria()) {
            criteriaMap.put(c.getCode(), mapCriteria(c));
        }

        CompetencyConfig response = new CompetencyConfig();
        response.setOrder(competencyEntity.getSortOrder());
        response.setIcon(competencyEntity.getIcon());
        response.setTheme(theme);
        response.setCriteria(criteriaMap);
        response.setCompetencyLevel(competencyEntity.getCompetencyLevel());
        response.setDisplayName(competencyEntity.getDisplayName());

        return ResponseEntity.ok(response);
    }

    private Theme mapTheme(ThemeEntity entity) {
        Theme theme = new Theme();
        theme.setPrimary(entity.getPrimary());
        theme.setDark(entity.getDark());
        theme.setLight(entity.getLight());
        theme.setLighter(entity.getLighter());
        theme.setText(entity.getText());
        theme.setBg(entity.getBg());
        theme.setPreBg(entity.getPreBg());
        theme.setBorder(entity.getBorder());
        theme.setStatBg(entity.getStatBg());
        return theme;
    }

    private Criteria mapCriteria(CriteriaEntity entity) {
        Criteria criteria = new Criteria();
        criteria.setTitle(entity.getTitle());
        criteria.setThai(entity.getThai());
        criteria.setEnglish(entity.getEnglish());
        criteria.setMaximumScore(entity.getMaximumScore());
        criteria.setTotalEvaluationsAllowed(entity.getTotalEvaluationsAllowed());

        Map<String, SourceScoring> scoringMap = new LinkedHashMap<>();
        for (SourceScoringEntity ss : entity.getSourceScoring()) {
            SourceScoring scoring = new SourceScoring();
            scoring.setMaximumScorePerEvaluation(ss.getMaximumScorePerEvaluation());
            scoring.setAcceptedMinScore(ss.getAcceptedMinScore());
            scoringMap.put(ss.getSourceName(), scoring);
        }

        criteria.setSourceScoring(scoringMap);

        return criteria;
    }

//    @GetMapping("/get-competencies")
//    public ResponseEntity<List<CompetencyEntity>> getAllCompetencies() {
//        List<CompetencyEntity> list = competencyRepository.findAll();
//        return ResponseEntity.ok(list);
//    }
//
//    @GetMapping("/get-criteria")
//    public ResponseEntity<List<CriteriaEntity>> getAllCriteria() {
//        List<CriteriaEntity> list = criteriaRepository.findAll();
//        return ResponseEntity.ok(list);
//    }
}
