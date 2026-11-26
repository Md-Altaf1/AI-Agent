package com.example.jobagent.scheduler;

import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.matching.service.JobMatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobMatchingScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobMatchingScheduler.class);

    private final UserRepository userRepository;
    private final JobMatchingService jobMatchingService;

    public JobMatchingScheduler(UserRepository userRepository, JobMatchingService jobMatchingService) {
        this.userRepository = userRepository;
        this.jobMatchingService = jobMatchingService;
    }

    // Default: Har 6 ghante mein chalega (00:00, 06:00, 12:00, 18:00)
    @Scheduled(cron = "${jobagent.scheduler.matching-cron:0 0 */6 * * *}")
    public void runAutoJobMatching() {
        log.info("Starting scheduled automatic job matching...");

        var users = userRepository.findAll();
        int successCount = 0;

        for (var user : users) {
            try {
                int evaluated = jobMatchingService.evaluateAllJobsForUser(user.getEmail());
                log.info("User {} ke liye {} job-resume pairs evaluate hue.", user.getEmail(), evaluated);
                successCount++;
            } catch (Exception ex) {
                log.error("Auto-matching failed for userId={}: {}", user.getId(), ex.getMessage());
            }
        }

        log.info("Scheduled job matching completed. Processed {}/{} users.", successCount, users.size());
    }
}