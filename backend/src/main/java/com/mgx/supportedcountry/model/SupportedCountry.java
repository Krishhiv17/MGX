package com.mgx.supportedcountry.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mgx_supported_countries")
public class SupportedCountry {

  @Id
  @Column(name = "country_code", nullable = false)
  private String countryCode;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "status", nullable = false)
  private String status;

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
