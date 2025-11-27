package com.codewithpot.store.json;

import com.codewithpot.store.common.entity.shoply.EvaluationEntity;
import com.codewithpot.store.common.entity.shoply.MockDataEntity;
import com.codewithpot.store.common.repository.shoply.EvaluationRepository;
import com.codewithpot.store.common.repository.shoply.MockDataRepository;
import com.codewithpot.store.common.repository.shoply.UserRepository;
import com.codewithpot.store.json.model.MockData;
import com.codewithpot.store.json.model.Sample;
import com.codewithpot.store.json.model.UserModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@RestController
@Tag(name = "Json")
public class JsonController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EvaluationRepository evaluationRepository;
    private final MockDataRepository mockDataRepository;

    public JsonController(EvaluationRepository userRepo, MockDataRepository mockDataRepository) {
        this.evaluationRepository = userRepo;
        this.mockDataRepository = mockDataRepository;
    }

    @GetMapping("/get-user-json-file")
    public ResponseEntity<UserModel> getUserJsonFile() {
        ClassPathResource resource = new ClassPathResource("json/data.json");

        try (InputStream inputStream = resource.getInputStream()) {
            UserModel userModel = objectMapper.readValue(inputStream, UserModel.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userModel);

            log.info("------------------------------------------");
            log.info("DATA FROM JSON (Pretty):\n{}", prettyJson);
            log.info("------------------------------------------");

            return ResponseEntity.ok(userModel);

        } catch (IOException e) {
            log.error("Error reading JSON file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get-evaluation")
    public ResponseEntity<Sample> getEvaluation() {
        ClassPathResource resource = new ClassPathResource("json/evaluation.json");
        ClassPathResource resource2 = new ClassPathResource("json/x.json");

        try (InputStream inputStream = resource.getInputStream();
             InputStream inputStream2 = resource2.getInputStream();
        ) {

            Sample profile = objectMapper.readValue(inputStream, Sample.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(profile);

            log.info("------------------------------------------");
            log.info("DATA FROM JSON: " + prettyJson);
            log.info("------------------------------------------");

            List<EvaluationEntity> profiles = profile.getEvaluationSessions().stream().map(x -> new EvaluationEntity()
                    .setScore(x.getScore())
                    .setContext(x.getContext())
                    .setDate(x.getDate())
                    .setSourceType(x.getSourceType())
                    .setEvidenceCount(x.getEvidenceCount())
                    .setAccepted(x.getAccepted())).toList();

            evaluationRepository.saveAll(profiles);

            MockData mockData = objectMapper.readValue(inputStream2, MockData.class);
            String prettyJsonMockData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mockData);

            log.info("------------------------------------------");
            log.info("DATA2 FROM JSON: " + prettyJsonMockData);
            log.info("------------------------------------------");

            MockDataEntity mockDataEntity = new MockDataEntity()
                    .setAge(mockData.getAge())
                    .setAverageScore(mockData.getAverageScore())
                    .setIsOnline(mockData.getIsOnline())
                    .setFullName(mockData.getFullName())
                    .setEmailVerified(mockData.getEmailVerified());

            mockDataRepository.save(mockDataEntity);

            return ResponseEntity.ok(profile);
        } catch (IOException e) {
            log.error("Error reading JSON file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("update-user")
    public ResponseEntity<UserModel> updateUserJsonFile(@RequestParam String name) {

        File externalFile = new File("updated-data.json");

        try {
            UserModel userModel;
            InputStream inputStream;

            if (externalFile.exists()) {
                log.info("Reading from external file: " + externalFile.getAbsolutePath());
                inputStream = new FileInputStream(externalFile);
            } else {
                log.info("Reading from default classpath");
                inputStream = new ClassPathResource("json/data.json").getInputStream();
            }

            try (inputStream) {
                userModel = objectMapper.readValue(inputStream, UserModel.class);
            }

            userModel.setName(name);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile, userModel);

            log.info("Updated file successfully at: " + externalFile.getAbsolutePath());

            return ResponseEntity.ok(userModel);

        } catch (IOException e) {
            log.error("Error processing JSON file", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}