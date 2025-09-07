package com.example.demo.Config;

import com.example.demo.Enum.UserRole;
import com.example.demo.Model.Users;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Utils.ExcelStreamingReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public BatchConfig(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.userRepository = userRepository;
    }

    /**
     * ItemReader cho Excel file
     */
    @Bean
    @StepScope
    public ExcelStreamingReader excelReader(@Value("#{jobParameters['filePath']}") String filePath) throws Exception {
        InputStream inputStream = new FileInputStream(filePath);
        return new ExcelStreamingReader(inputStream);
    }

    /**
     * Processor: xử lý từng Users trước khi ghi
     */
    @Bean
    public ItemProcessor<Users, Users> excelProcessor() {
        return user -> {
            if (user.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return user;
        };
    }

    /**
     * Writer: ghi danh sách user xuống DB hoặc log
     */
    @Bean
    public ItemWriter<Users> excelWriter() {
        return users -> {
            System.out.println("Writing users: " + users.size());
            for (Users u : users) {
                if (u.getRole() == null) {
                    u.setRole(UserRole.STUDENT);
                }
                // if (u.getPassword() != null) { // && !u.getPassword().startsWith("$2a$")
                // u.setPassword(passwordEncoder.encode(u.getPassword()));
                // }
            }
            userRepository.saveAll(users);
        };
    }

    /**
     * Step: xử lý từng chunk user
     */
    @Bean
    public Step excelImportStep(ExcelStreamingReader reader,
            ItemProcessor<Users, Users> processor,
            ItemWriter<Users> writer) {
        return new StepBuilder("excelImportStep", jobRepository)
                .<Users, Users>chunk(50, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    /**
     * Job: gồm step import
     */
    @Bean
    public Job excelImportJob(Step excelImportStep) {
        return new JobBuilder("excelImportJob", jobRepository)
                .start(excelImportStep)
                .build();
    }
}
