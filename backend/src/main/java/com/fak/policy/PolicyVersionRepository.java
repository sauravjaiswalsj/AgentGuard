package com.fak.policy;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, Long> {
    Optional<PolicyVersion> findFirstByStatus(String status);
    List<PolicyVersion> findAllByOrderByCreatedAtDesc();
}
