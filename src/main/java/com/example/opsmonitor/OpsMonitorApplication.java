package com.example.opsmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpsMonitorApplication {

  public static void main(String[] args) {
    SpringApplication.run(OpsMonitorApplication.class, args);
  }
}
