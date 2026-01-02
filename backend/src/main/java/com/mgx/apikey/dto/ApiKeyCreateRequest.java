package com.mgx.apikey.dto;

import java.util.List;

public class ApiKeyCreateRequest {
  private String ownerName;
  private List<String> scopes;

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }
}
