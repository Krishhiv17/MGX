package com.mgx.supportedcountry.repository;

import com.mgx.supportedcountry.model.SupportedCountry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportedCountryRepository extends JpaRepository<SupportedCountry, String> {}
