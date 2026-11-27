package com.example.jobagent.job.ingestion;

import java.util.List;

public interface JobSourceConnector {
    String sourceCode();
    List<FetchedJobPosting> fetchJobs();
}