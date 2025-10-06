package com.bludots.views.dashboard;

import com.bludots.entities.TomcatInstanceEntity;
import com.bludots.services.TomcatInstanceService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = com.bludots.views.MainLayout.class)
@AnonymousAllowed
public class DashboardView extends VerticalLayout {

    private final TomcatInstanceService service;
    private final Grid<TomcatInstanceEntity> grid;
    private List<TomcatInstanceEntity> allInstances;
    private final TextField searchField;
    private final Span resultsCount;


    @Autowired
    public DashboardView(TomcatInstanceService service) {
        this.service = service;

        setSpacing(true);
        setPadding(true);
        add(new H1("Tomcat Instances Dashboard"));

        // 🔍 Search Field
        searchField = new TextField();
        searchField.setPlaceholder("🔍 Search by Client Name or Status...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        // 🧭 Dropdown Filter (Status)
        Select<String> statusFilter = new Select<>();
        statusFilter.setItems("All", "Running", "Stopped", "Deploying");
        statusFilter.setValue("All");
        statusFilter.setWidth("150px");

        // 🔄 Refresh + ➕ Add buttons
        Button refreshButton = new Button("🔄 Refresh", e -> refreshData());
        Button addButton = new Button("➕ Add Instance", e -> openAddDialog());
        addButton.getStyle().set("background-color", "#4CAF50").set("color", "white");

        // Event listeners
        searchField.addValueChangeListener(e ->
                filterData(searchField.getValue(), statusFilter.getValue()));
        statusFilter.addValueChangeListener(e ->
                filterData(searchField.getValue(), statusFilter.getValue()));

        // 📋 Toolbar layout
        HorizontalLayout topBar = new HorizontalLayout(searchField, statusFilter, refreshButton, addButton);
        topBar.setSpacing(true);
        topBar.setAlignItems(Alignment.END);
        add(topBar);

        // 📊 Label dat aantal resultaten toont
        resultsCount = new Span();
        resultsCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#555")
                .set("margin-left", "5px");

        add(resultsCount);


        // 📊 Grid
        grid = new Grid<>(TomcatInstanceEntity.class, false);
        grid.addColumn(TomcatInstanceEntity::getName).setHeader("Client");
        grid.addComponentColumn(this::createStatusBadge).setHeader("Status");
        grid.addColumn(TomcatInstanceEntity::getIpAddress).setHeader("IP Address");
        grid.addComponentColumn(this::createActions).setHeader("Actions");
        add(grid);

        refreshData();
    }

    private void refreshData() {
        allInstances = service.getAll();
        grid.setItems(allInstances);
        updateResultsCount(allInstances.size(), allInstances.size());
    }


    // 🔎 Filter Logic
    private void filterData(String textFilter, String statusFilter) {
        if (allInstances == null) return;

        String search = (textFilter == null ? "" : textFilter.trim().toLowerCase());
        String status = (statusFilter == null ? "All" : statusFilter);

        List<TomcatInstanceEntity> filtered = allInstances.stream()
                .filter(i ->
                        (i.getName().toLowerCase().contains(search)
                                || i.getStatus().toLowerCase().contains(search))
                                && ("All".equals(status) || i.getStatus().equalsIgnoreCase(status))
                )
                .collect(Collectors.toList());

        grid.setItems(filtered);
        updateResultsCount(filtered.size(), allInstances.size());
    }


    // 🟢 Add Instance Popup
    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Tomcat Instance");

        TextField nameField = new TextField("Client name");
        TextField statusField = new TextField("Status (Running / Stopped / Deploying)");
        TextField ipField = new TextField("IP Address");

        nameField.setRequiredIndicatorVisible(true);
        statusField.setRequiredIndicatorVisible(true);
        ipField.setRequiredIndicatorVisible(true);

        nameField.setHelperText("Enter Firstname and Lastname");
        statusField.setHelperText("Allowed values: Running, Stopped, Deploying");
        ipField.setHelperText("e.g. 10.0.0.12");

        Button saveButton = new Button("Save", e -> {
            String name = nameField.getValue().trim();
            String status = statusField.getValue().trim();
            String ip = ipField.getValue().trim();

            // 🧩 Validation
            if (name.isEmpty() || status.isEmpty() || ip.isEmpty()) {
                Notification.show("⚠️ Please fill in all fields!");
                return;
            }
            if (!status.matches("(?i)Running|Stopped|Deploying")) {
                Notification.show("⚠️ Invalid status! Use: Running, Stopped or Deploying.");
                return;
            }
            if (!ip.matches("^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$")) {
                Notification.show("⚠️ Invalid IP address!");
                return;
            }

            TomcatInstanceEntity instance = new TomcatInstanceEntity(name, status, ip);
            service.save(instance);
            Notification.show("✅ Instance added successfully!");
            dialog.close();
            refreshData();
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#9E9E9E").set("color", "white");
        saveButton.getStyle().set("background-color", "#4CAF50").set("color", "white");

        FormLayout form = new FormLayout(nameField, statusField, ipField);
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        dialog.add(form, buttons);
        dialog.open();
    }

    private HorizontalLayout createActions(TomcatInstanceEntity instance) {
        // 🟡 Edit
        Button edit = new Button("Edit", e -> openEditDialog(instance));
        edit.getStyle().set("background-color", "#FFC107").set("color", "black");

        // 🔴 Delete (confirmation)
        Button delete = new Button("Delete", e -> openDeleteConfirmation(instance));
        delete.getStyle().set("background-color", "#F44336").set("color", "white");

        return new HorizontalLayout(edit, delete);
    }

    // ⚙️ Edit Dialog
    private void openEditDialog(TomcatInstanceEntity instance) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Instance");

        TextField nameEdit = new TextField("Client Name", instance.getName());
        TextField statusEdit = new TextField("Status", instance.getStatus());
        TextField ipEdit = new TextField("IP Address", instance.getIpAddress());

        Button save = new Button("Save", e -> {
            String name = nameEdit.getValue().trim();
            String status = statusEdit.getValue().trim();
            String ip = ipEdit.getValue().trim();

            if (name.isEmpty() || status.isEmpty() || ip.isEmpty()) {
                Notification.show("⚠️ Please fill in all fields!");
                return;
            }
            if (!status.matches("(?i)Running|Stopped|Deploying")) {
                Notification.show("⚠️ Invalid status!");
                return;
            }
            if (!ip.matches("^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$")) {
                Notification.show("⚠️ Invalid IP address!");
                return;
            }

            instance.setName(name);
            instance.setStatus(status);
            instance.setIpAddress(ip);
            service.save(instance);
            Notification.show("✅ Instance updated successfully!");
            dialog.close();
            refreshData();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        save.getStyle().set("background-color", "#4CAF50").set("color", "white");
        cancel.getStyle().set("background-color", "#9E9E9E").set("color", "white");

        FormLayout editForm = new FormLayout(nameEdit, statusEdit, ipEdit);
        HorizontalLayout actions = new HorizontalLayout(save, cancel);

        dialog.add(editForm, actions);
        dialog.open();
    }

    // 🗑️ Delete Confirmation Dialog
    private void openDeleteConfirmation(TomcatInstanceEntity instance) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Deletion");

        Span message = new Span("Are you sure you want to delete \"" + instance.getName() + "\"?");
        Button confirm = new Button("Yes, Delete", e -> {
            service.delete(instance.getId());
            Notification.show("🗑️ Instance deleted successfully.");
            confirmDialog.close();
            refreshData();
        });
        Button cancel = new Button("Cancel", e -> confirmDialog.close());

        confirm.getStyle().set("background-color", "#F44336").set("color", "white");
        cancel.getStyle().set("background-color", "#9E9E9E").set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(confirm, cancel);
        confirmDialog.add(message, buttons);
        confirmDialog.open();
    }

    // 📈 Telt resultaten
    private void updateResultsCount(int visible, int total) {
        resultsCount.setText("Showing " + visible + " of " + total + " instances");
    }


    // 🎨 Status Badges
    private Span createStatusBadge(TomcatInstanceEntity instance) {
        Span badge = new Span(instance.getStatus());
        badge.getStyle()
                .set("padding", "5px 10px")
                .set("border-radius", "10px")
                .set("color", "white")
                .set("font-weight", "bold");

        switch (instance.getStatus()) {
            case "Running" -> badge.getStyle().set("background-color", "#4CAF50");
            case "Stopped" -> badge.getStyle().set("background-color", "#F44336");
            case "Deploying" -> badge.getStyle().set("background-color", "#FFC107").set("color", "black");
            default -> badge.getStyle().set("background-color", "#9E9E9E");
        }
        return badge;
    }
}
