package com.bludots.services;

import com.bludots.entities.TomcatInstanceEntity;
import com.bludots.repositories.TomcatInstanceRepository;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import com.vaadin.flow.component.notification.Notification;


import java.util.List;

@Service
public class TomcatInstanceService {

    private final TomcatInstanceRepository repository;

    public TomcatInstanceService(TomcatInstanceRepository repository) {
        this.repository = repository;
    }

    // Get all instances
    public List<TomcatInstanceEntity> getAll() {
        return repository.findAll();
    }

    // Get by Status
    public List<TomcatInstanceEntity> getByStatus(String status) {
        if (status == null || status.equalsIgnoreCase("All")) {
            return getAll();
        }
        return repository.findByStatusIgnoreCase(status);
    }

    // Search (Name or Status)
    public List<TomcatInstanceEntity> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        return repository.findByNameContainingIgnoreCaseOrStatusContainingIgnoreCase(keyword, keyword);
    }

    // Search + Filter Combined
    public List<TomcatInstanceEntity> searchAndFilter(String keyword, String status) {
        List<TomcatInstanceEntity> results = search(keyword);
        if (status != null && !status.equalsIgnoreCase("All")) {
            return results.stream()
                    .filter(i -> i.getStatus().equalsIgnoreCase(status))
                    .toList();
        }
        return results;
    }

    // Save or Update
    public TomcatInstanceEntity save(TomcatInstanceEntity instance) {
        return repository.save(instance);
    }

    // Delete
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // Get by ID (used later for details page)
    public TomcatInstanceEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Async
    public CompletableFuture<Void> startInstanceAsync(TomcatInstanceEntity instance, UI ui) {
        // Check: if already running, don't start again
        if ("Running".equalsIgnoreCase(instance.getStatus())) {
            ui.access(() -> Notification.show("⚠️ " + instance.getName() + " is already running."));
            return CompletableFuture.completedFuture(null);
        }

        instance.setStatus("Deploying");
        save(instance);

        ui.access(() -> ui.getPage().reload()); // update UI

        return CompletableFuture.runAsync(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            instance.setStatus("Running");
            save(instance);
            ui.access(() -> Notification.show("✅ " + instance.getName() + " is now running."));
        });
    }

    @Async
    public CompletableFuture<Void> stopInstanceAsync(TomcatInstanceEntity instance, UI ui) {
        instance.setStatus("Stopped");
        save(instance);
        ui.access(() -> Notification.show("🛑 " + instance.getName() + " stopped."));
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> redeployInstanceAsync(TomcatInstanceEntity instance, UI ui) {
        instance.setStatus("Deploying");
        save(instance);
        ui.access(() -> Notification.show("♻️ Redeploying " + instance.getName() + "..."));

        return CompletableFuture.runAsync(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            instance.setStatus("Running");
            save(instance);
            ui.access(() -> Notification.show("✅ " + instance.getName() + " redeployed successfully."));
        });
    }

    // Mock logs for now – later replaced with real server logs
    public List<String> getLogsForInstance(Long instanceId) {
        return List.of(
                "[INFO] Server starting...",
                "[INFO] Deploying application...",
                "[INFO] Application started successfully!",
                "[WARN] Low memory detected",
                "[ERROR] Example error log here"
        );
    }

}
