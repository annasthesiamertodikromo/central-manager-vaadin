package com.bludots.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TomcatInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String status;
    private String ipAddress;

    public TomcatInstanceEntity() {}

    public TomcatInstanceEntity(String name, String status, String ipAddress) {
        this.name = name;
        this.status = status;
        this.ipAddress = ipAddress;
    }

    //getters en setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getIpAddress() { return ipAddress; }

    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
