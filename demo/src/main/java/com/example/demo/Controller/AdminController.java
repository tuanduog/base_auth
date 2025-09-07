package com.example.demo.Controller;

import java.io.File;
import java.io.FileOutputStream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job excelImportJob;

    @GetMapping("/hello")
    public ResponseEntity<?> sayHello() {
        return ResponseEntity.ok("Hello admin");
    }

    @PostMapping("/import-accounts")
    public ResponseEntity<?> importAccount(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        try {
            // Lưu file tạm để Job đọc
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(file.getBytes());
            }

            // Truyền filePath qua JobParameter
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", convFile.getAbsolutePath())
                    .addLong("time", System.currentTimeMillis()) // tránh JobInstance bị trùng
                    .toJobParameters();

            jobLauncher.run(excelImportJob, jobParameters);

            return ResponseEntity.ok("Job import started. Check logs for progress.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error while importing: " + e.getMessage());
        }
    }
}
