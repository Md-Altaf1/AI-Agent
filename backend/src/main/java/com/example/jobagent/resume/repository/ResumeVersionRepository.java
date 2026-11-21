package com.example.jobagent.resume.repository;

import com.example.jobagent.resume.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    List<ResumeVersion> findByResumeIdOrderByVersionNoDesc(Long resumeId);

    Optional<ResumeVersion> findFirstByResumeIdOrderByVersionNoDesc(Long resumeId);

    Optional<ResumeVersion> findByResumeIdAndSha256(Long resumeId, String sha256);

    @Query("""
        SELECT rv.resume.id, MAX(rv.versionNo)
        FROM ResumeVersion rv
        WHERE rv.resume.id IN :resumeIds
        GROUP BY rv.resume.id
        """)
    List<Object[]> findLatestVersionNumbersByResumeIds(
            @Param("resumeIds") Collection<Long> resumeIds
    );

    @Query("SELECT COALESCE(MAX(rv.versionNo), 0) FROM ResumeVersion rv WHERE rv.resume.id = :resumeId")
    Integer findMaxVersionNoByResumeId(@Param("resumeId") Long resumeId);

    Optional<ResumeVersion> findByIdAndResumeId(
            Long id,
            Long resumeId
    );
}