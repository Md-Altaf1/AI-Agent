package com.example.jobagent.scheduler;

import com.example.jobagent.auth.repository.UserRepository;
import com.example.jobagent.job.service.JobIngestionService;
import com.example.jobagent.matching.service.JobMatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobMatchingScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobMatchingScheduler.class);

    private final JobIngestionService jobIngestionService;
    private final UserRepository userRepository;
    private final JobMatchingService jobMatchingService;

    public JobMatchingScheduler(
            JobIngestionService jobIngestionService,
            UserRepository userRepository,
            JobMatchingService jobMatchingService) {
        this.jobIngestionService = jobIngestionService;
        this.userRepository = userRepository;
        this.jobMatchingService = jobMatchingService;
    }

    // Default: runs every 6 hours (00:00, 06:00, 12:00, 18:00)
    @Scheduled(cron = "${jobagent.scheduler.matching-cron:0 0 */6 * * *}")
    public void runAutoJobMatching() {
        log.info("Starting scheduled job ingestion and matching pipeline...");

        // Step 1: Ingest fresh jobs from external connectors
        try {
            var summary = jobIngestionService.ingestAllSources();
            log.info(
                    "Job ingestion completed: inserted={}, updated={}, unchanged={}, failedConnectors={}",
                    summary.insertedCount(),
                    summary.updatedCount(),
                    summary.unchangedCount(),
                    summary.failedConnectors()
            );
        } catch (Exception ex) {
            // Do not abort matching for existing jobs if external ingestion fails
            log.error("Scheduled job ingestion failed: {}", ex.getMessage());
        }

        // Step 2: Evaluate all jobs for every registered user
        var users = userRepository.findAll();
        int successCount = 0;

        for (var user : users) {
            try {
                int evaluated = jobMatchingService.evaluateAllJobsForUser(user.getEmail());
                log.info("Evaluated {} job-resume pairs for user {}", evaluated, user.getEmail());
                successCount++;
            } catch (Exception ex) {
                log.error("Auto-matching failed for userId={}: {}", user.getId(), ex.getMessage());
            }
        }

        log.info("Scheduled pipeline completed. Processed {}/{} users.", successCount, users.size());
    }
}