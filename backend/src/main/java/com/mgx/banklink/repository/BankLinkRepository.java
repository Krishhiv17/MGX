package com.mgx.banklink.repository;

import com.mgx.banklink.model.BankLink;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankLinkRepository extends JpaRepository<BankLink, UUID> {
  Optional<BankLink> findByUserId(UUID userId);

  Optional<BankLink> findByPhoneNumberAndBankRef(String phoneNumber, String bankRef);
}
