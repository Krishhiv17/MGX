package com.mgx.otp.repository;

import com.mgx.otp.model.OtpSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpSessionRepository extends JpaRepository<OtpSession, UUID> {}
