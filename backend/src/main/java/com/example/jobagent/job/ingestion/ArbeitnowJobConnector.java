package com.example.jobagent.job.ingestion;

import com.example.jobagent.job.entity.EmploymentType;
import com.example.jobagent.job.entity.WorkMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
public class ArbeitnowJobConnector implements JobSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(ArbeitnowJobConnector.class);

    private final RestClient restClient;
    private final String apiUrl;

    public ArbeitnowJobConnector(
            @Qualifier("jobSourceRestClient") RestClient restClient,
            @Value("${jobagent.ingestion.arbeitnow.url:https://www.arbeitnow.com/api/job-board-api}") String apiUrl) {
        this.restClient = restClient;
        this.apiUrl = apiUrl;
    }

    @Override
    public String sourceCode() {
        return "ARBEITNOW";
    }

    @Override
    public List<FetchedJobPosting> fetchJobs() {
        ArbeitnowApiResponse response = restClient.get()
                .uri(apiUrl)
                .retrieve()
                .body(ArbeitnowApiResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            log.warn("No job items returned from Arbeitnow API at {}", apiUrl);
            return Collections.emptyList();
        }

        return response.data().stream()
                .filter(item -> item.title() != null && !item.title().isBlank())
                .filter(item -> item.url() != null && !item.url().isBlank())
                .map(this::toFetchedJobPosting)
                .toList();
    }

    private FetchedJobPosting toFetchedJobPosting(ArbeitnowJobItem item) {
        String company = (item.companyName() != null && !item.companyName().isBlank())
                ? item.companyName().trim()
                : "Unknown Company";

        String location = (item.location() != null && !item.location().isBlank())
                ? item.location().trim()
                : (Boolean.TRUE.equals(item.remote()) ? "Remote" : "Unspecified");

        String city = extractCity(location);
        WorkMode workMode = Boolean.TRUE.equals(item.remote())
                ? WorkMode.REMOTE
                : inferWorkMode(location);

        EmploymentType employmentType = inferEmploymentType(item.jobTypes());

        Instant postedAt = (item.createdAt() != null && item.createdAt() > 0)
                ? Instant.ofEpochSecond(item.createdAt())
                : Instant.now();

        List<String> tags = (item.tags() != null)
                ? item.tags().stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .toList()
                : List.of();

        String cleanDescription = (item.description() != null && !item.description().isBlank())
                ? item.description()
                : item.title();

        return new FetchedJobPosting(
                item.slug(),
                item.title().trim(),
                company,
                location,
                city,
                employmentType,
                workMode,
                cleanDescription,
                item.url().trim(),
                postedAt,
                tags
        );
    }

    private String extractCity(String location) {
        String[] parts = location.split(",");
        return parts.length > 0 ? parts[0].trim() : location;
    }

    private WorkMode inferWorkMode(String location) {
        String lower = location.toLowerCase(Locale.ROOT);
        if (lower.contains("remote")) {
            return WorkMode.REMOTE;
        }
        if (lower.contains("hybrid")) {
            return WorkMode.HYBRID;
        }
        return WorkMode.ONSITE;
    }

    private EmploymentType inferEmploymentType(List<String> jobTypes) {
        if (jobTypes == null || jobTypes.isEmpty()) {
            return EmploymentType.UNKNOWN;
        }
        String joined = String.join(" ", jobTypes).toLowerCase(Locale.ROOT);
        if (joined.contains("intern")) {
            return EmploymentType.INTERNSHIP;
        }
        if (joined.contains("contract") || joined.contains("freelance")) {
            return EmploymentType.CONTRACT;
        }
        if (joined.contains("part")) {
            return EmploymentType.PART_TIME;
        }
        if (joined.contains("full")) {
            return EmploymentType.FULL_TIME;
        }
        return EmploymentType.UNKNOWN;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ArbeitnowApiResponse(List<ArbeitnowJobItem> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ArbeitnowJobItem(
            String slug,
            @JsonProperty("company_name") String companyName,
            String title,
            String description,
            String url,
            Boolean remote,
            String location,
            @JsonProperty("job_types") List<String> jobTypes,
            List<String> tags,
            @JsonProperty("created_at") Long createdAt
    ) {
    }
}