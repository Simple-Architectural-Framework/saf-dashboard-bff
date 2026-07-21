package com.ican.saf.ui.dashboard.bff.web.repository;

import com.ican.saf.ui.dashboard.bff.web.model.AppsUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2Repository extends JpaRepository<AppsUrl, Long> {
    Optional<AppsUrl> findByAppName(String appName);
}
