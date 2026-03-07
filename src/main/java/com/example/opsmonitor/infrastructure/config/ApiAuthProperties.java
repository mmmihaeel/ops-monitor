package com.example.opsmonitor.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ops.monitor.api-auth")
public class ApiAuthProperties {

  private String viewerUsername = "ops_viewer";
  private String viewerPassword = "ops_viewer_password";
  private String operatorUsername = "ops_operator";
  private String operatorPassword = "ops_operator_password";
  private String adminUsername = "ops_admin";
  private String adminPassword = "ops_admin_password";

  public String getViewerUsername() {
    return viewerUsername;
  }

  public void setViewerUsername(String viewerUsername) {
    this.viewerUsername = viewerUsername;
  }

  public String getViewerPassword() {
    return viewerPassword;
  }

  public void setViewerPassword(String viewerPassword) {
    this.viewerPassword = viewerPassword;
  }

  public String getOperatorUsername() {
    return operatorUsername;
  }

  public void setOperatorUsername(String operatorUsername) {
    this.operatorUsername = operatorUsername;
  }

  public String getOperatorPassword() {
    return operatorPassword;
  }

  public void setOperatorPassword(String operatorPassword) {
    this.operatorPassword = operatorPassword;
  }

  public String getAdminUsername() {
    return adminUsername;
  }

  public void setAdminUsername(String adminUsername) {
    this.adminUsername = adminUsername;
  }

  public String getAdminPassword() {
    return adminPassword;
  }

  public void setAdminPassword(String adminPassword) {
    this.adminPassword = adminPassword;
  }
}
