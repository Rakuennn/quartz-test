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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

//            InputStream inputStream = resource.getInputStream();
//            InputStream inputStream2 = resource2.getInputStream();

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
//        }finally {
//            inputStream.close();
//            inputStream2.close();
//        }
    }

    @PostMapping("update-user-same-path")
    public ResponseEntity<UserModel> updateUserJsonFile(@RequestParam String name) {

        File externalFile1 = new File("app/updated-data1.json");
        File externalFile2 = new File("app/updated-data2.json");

        try {
            File parentDir = externalFile1.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    log.info("Created directory: " + parentDir.getAbsolutePath());
                }
            }

            UserModel userModel;
            InputStream inputStream;

            if (externalFile1.exists()) {
                log.info("Reading from external file: " + externalFile1.getAbsolutePath());
                inputStream = new FileInputStream(externalFile1);
            } else {
                log.info("Reading from default classpath");
                inputStream = new ClassPathResource("json/data.json").getInputStream();
            }

            try (inputStream) {
                userModel = objectMapper.readValue(inputStream, UserModel.class);
            }

            userModel.setName(name);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile1, userModel);
            log.info("Updated file 1 successfully at: " + externalFile1.getAbsolutePath());

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile2, userModel);
            log.info("Updated file 2 successfully at: " + externalFile2.getAbsolutePath());

            return ResponseEntity.ok(userModel);

        } catch (IOException e) {
            log.error("Error processing JSON file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("update-user-dif-path")
    public ResponseEntity<UserModel> updateUserJsonFileDifPath(@RequestParam String name) {

        File externalFile1 = new File("app/app1/dif-data1.json");
        File externalFile2 = new File("app/app2/dif-data2.json");

        try {
            File parentDir = externalFile1.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    log.info("Created directory: " + parentDir.getAbsolutePath());
                }
            }

            File parentDir2 = externalFile2.getParentFile();
            if (parentDir2 != null && !parentDir2.exists()) {
                boolean created = parentDir2.mkdirs();
                if (created) {
                    log.info("Created directory: " + parentDir2.getAbsolutePath());
                }
            }


            UserModel userModel;
            InputStream inputStream;

            if (externalFile1.exists()) {
                log.info("Reading from external file: " + externalFile1.getAbsolutePath());
                inputStream = new FileInputStream(externalFile1);
            } else {
                log.info("Reading from default classpath");
                inputStream = new ClassPathResource("json/data.json").getInputStream();
            }

            try (inputStream) {
                userModel = objectMapper.readValue(inputStream, UserModel.class);
            }

            userModel.setName(name);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile1, userModel);
            log.info("Updated file 1 successfully at: " + externalFile1.getAbsolutePath());

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile2, userModel);
            log.info("Updated file 2 successfully at: " + externalFile2.getAbsolutePath());

            return ResponseEntity.ok(userModel);

        } catch (IOException e) {
            log.error("Error processing JSON file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(path = "add-file-same-path", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserModel> addUserJsonFile(@RequestPart(value = "file") MultipartFile file) {

        File externalFile1 = new File("app/updated-data1.json");
        File externalFile2 = new File("app/updated-data2.json");

        try {
            createParentDirectory(externalFile1);
            UserModel userModel;

            try (InputStream inputStream = file.getInputStream()) {
                userModel = objectMapper.readValue(inputStream, UserModel.class);
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile1, userModel);
            log.info("Saved uploaded file to: " + externalFile1.getAbsolutePath());

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile2, userModel);
            log.info("Saved uploaded file to: " + externalFile2.getAbsolutePath());

            return ResponseEntity.ok(userModel);

        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(path = "add-user-dif-path", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserModel> addUserJsonFileDifPath(@RequestPart(value = "file") MultipartFile file) {

        File externalFile1 = new File("app/app1/dif-data1.json");
        File externalFile2 = new File("app/app2/dif-data2.json");

        try {
            createParentDirectory(externalFile1);
            createParentDirectory(externalFile2);
            UserModel userModel;

            try (InputStream inputStream = file.getInputStream()) {
                userModel = objectMapper.readValue(inputStream, UserModel.class);
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile1, userModel);
            log.info("Saved uploaded file to: " + externalFile1.getAbsolutePath());

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile2, userModel);
            log.info("Saved uploaded file to: " + externalFile2.getAbsolutePath());

            return ResponseEntity.ok(userModel);

        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void createParentDirectory(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                log.info("Created directory: " + parentDir.getAbsolutePath());
            }
        }
    }
}