package com.bludots.views.dashboard;

import com.bludots.entities.TomcatInstanceEntity;
import com.bludots.services.TomcatInstanceService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
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
    private TextField searchField;
    private final Span resultsCount;

    @Autowired
    public DashboardView(TomcatInstanceService service) {
        this.service = service;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // HEADER
        HorizontalLayout header = createHeader();
        add(header);

        // Filterbar
        HorizontalLayout filterBar = createFilterBar();
        add(filterBar);

        // Result count
        resultsCount = new Span();
        resultsCount.getStyle()
                .set("font-size", "14px")
                .set("color", "#666")
                .set("margin", "8px 0 0 12px");
        add(resultsCount);

        // GRID
        grid = new Grid<>(TomcatInstanceEntity.class, false);
        grid.addColumn(TomcatInstanceEntity::getName).setHeader("Client").setAutoWidth(true);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Status").setAutoWidth(true);
        grid.addColumn(TomcatInstanceEntity::getIpAddress).setHeader("IP Address").setAutoWidth(true);
        grid.addComponentColumn(this::createActionMenu).setHeader("Actions").setAutoWidth(true);

        Scroller gridScroller = new Scroller(grid);
        gridScroller.setSizeFull();
        gridScroller.setScrollDirection(Scroller.ScrollDirection.BOTH);

        add(gridScroller);
        setFlexGrow(1, gridScroller);

        refreshData();
    }

    // HEADER (Logo + Titel + Menu)
    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
                .set("background-color", "#2E3A59")
                .set("color", "white")
                .set("padding", "10px 20px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.15)");

        Image logo = new Image("https://cdn-icons-png.flaticon.com/512/882/882702.png", "BluDots Logo");
        logo.setWidth("40px");
        logo.setHeight("40px");

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setSpacing(false);
        titleLayout.setPadding(false);
        titleLayout.add(
                new H1("BluDots Central Manager"),
                new H3("Tomcat Instances Dashboard")
        );
        titleLayout.getStyle().set("color", "white");

        // Profile Menu
        MenuBar profileMenu = new MenuBar();
        MenuItem userItem = profileMenu.addItem("👤 Admin");
        SubMenu submenu = userItem.getSubMenu();
        submenu.addItem("⚙️ Settings", e -> Notification.show("Settings feature coming soon."));
        submenu.addItem("🚪 Logout", e -> Notification.show("Logout not implemented yet."));

        header.add(logo, titleLayout);
        header.expand(titleLayout);
        header.add(profileMenu);
        return header;
    }


    // FILTERBAR
    private HorizontalLayout createFilterBar() {
        searchField = new TextField();
        searchField.setPlaceholder("🔍 Search by Client or Status...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("280px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        Select<String> statusFilter = new Select<>();
        statusFilter.setItems("All", "Running", "Stopped", "Deploying");
        statusFilter.setValue("All");
        statusFilter.setWidth("150px");

        Button refreshButton = new Button("🔄 Refresh", e -> refreshData());
        Button addButton = new Button("➕ Add Instance", e -> openAddDialog());
        addButton.getStyle().set("background-color", "#4CAF50").set("color", "white");

        HorizontalLayout bar = new HorizontalLayout(searchField, statusFilter, refreshButton, addButton);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.END);
        bar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bar.getStyle()
                .set("padding", "10px 20px")
                .set("border-bottom", "1px solid #ddd")
                .set("background-color", "#FAFAFA");

        // Listeners
        searchField.addValueChangeListener(e -> filterData(searchField.getValue(), statusFilter.getValue()));
        statusFilter.addValueChangeListener(e -> filterData(searchField.getValue(), statusFilter.getValue()));

        return bar;
    }

    private void refreshData() {
        allInstances = service.getAll();
        grid.setItems(allInstances);
        updateResultsCount(allInstances.size(), allInstances.size());
    }

    // ----------------- Filter via service -----------------
    private void filterData(String textFilter, String statusFilter) {
        // Haal gefilterde data op via service
        List<TomcatInstanceEntity> filtered = service.searchAndFilter(textFilter, statusFilter);

        // Toon gefilterde resultaten
        grid.setItems(filtered);

        // Update teller
        updateResultsCount(filtered.size(), allInstances == null ? filtered.size() : allInstances.size());
    }


    // ACTIES via 3-dot Menu
    private MenuBar createActionMenu(TomcatInstanceEntity instance) {
        MenuBar menuBar = new MenuBar();
        MenuItem main = menuBar.addItem("⋮");
        SubMenu sub = main.getSubMenu();

        sub.addItem("▶️ Start", e -> asyncStart(instance, e.getSource()));
        sub.addItem("⏹️ Stop", e -> asyncStop(instance, e.getSource()));
        sub.addItem("🔁 Redeploy", e -> asyncRedeploy(instance, e.getSource()));
        sub.addItem("✏️ Edit", e -> openEditDialog(instance));
        sub.addItem("🗑️ Delete", e -> openDeleteConfirmation(instance));

        return menuBar;
    }

    // ADD DIALOG
    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Tomcat Instance");

        TextField nameField = new TextField("Client Name");
        TextField statusField = new TextField("Status (Running / Stopped / Deploying)");
        TextField ipField = new TextField("IP Address");

        nameField.setRequiredIndicatorVisible(true);
        statusField.setRequiredIndicatorVisible(true);
        ipField.setRequiredIndicatorVisible(true);

        nameField.setHelperText("Enter Firstname and Lastname");
        statusField.setHelperText("Allowed values: Running, Stopped, Deploying");
        ipField.setHelperText("e.g. 10.0.0.12");

        Button save = new Button("Save", e -> {
            String name = nameField.getValue().trim();
            String status = statusField.getValue().trim();
            String ip = ipField.getValue().trim();

            if (name.isEmpty() || status.isEmpty() || ip.isEmpty()) {
                Notification.show("⚠️ Please fill all fields!");
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

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.getStyle().set("background-color", "#9E9E9E").set("color", "white");
        save.getStyle().set("background-color", "#4CAF50").set("color", "white");

        dialog.add(new FormLayout(nameField, statusField, ipField), new HorizontalLayout(save, cancel));
        dialog.open();
    }

    // Edit
    private void openEditDialog(TomcatInstanceEntity instance) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Instance");

        TextField nameField = new TextField("Client Name");
        TextField statusField = new TextField("Status (Running / Stopped / Deploying)");
        TextField ipField = new TextField("IP Address");

        // Prefill values
        nameField.setValue(instance.getName());
        statusField.setValue(instance.getStatus());
        ipField.setValue(instance.getIpAddress());

        // Same UI requirements as Add
        nameField.setRequiredIndicatorVisible(true);
        statusField.setRequiredIndicatorVisible(true);
        ipField.setRequiredIndicatorVisible(true);

        nameField.setHelperText("Enter Firstname and Lastname");
        statusField.setHelperText("Allowed values: Running, Stopped, Deploying");
        ipField.setHelperText("e.g. 10.0.0.12");

        Button save = new Button("Save", e -> {
            String name = nameField.getValue().trim();
            String status = statusField.getValue().trim();
            String ip = ipField.getValue().trim();

            if (name.isEmpty() || status.isEmpty() || ip.isEmpty()) {
                Notification.show("⚠️ Please fill all fields!");
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

            // Apply changes only if valid
            instance.setName(name);
            instance.setStatus(status);
            instance.setIpAddress(ip);
            service.save(instance);

            Notification.show("✅ Updated successfully!");
            dialog.close();
            refreshData();
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.getStyle().set("background-color", "#9E9E9E").set("color", "white");
        save.getStyle().set("background-color", "#4CAF50").set("color", "white");

        dialog.add(new FormLayout(nameField, statusField, ipField), new HorizontalLayout(save, cancel));
        dialog.open();
    }

    // Delete
    private void openDeleteConfirmation(TomcatInstanceEntity instance) {
        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Confirm Deletion");
        Span msg = new Span("Are you sure you want to delete \"" + instance.getName() + "\"?");
        Button yes = new Button("Yes, Delete", e -> {
            service.delete(instance.getId());
            Notification.show("🗑️ Instance deleted successfully.");
            confirm.close();
            refreshData();
        });
        Button cancel = new Button("Cancel", e -> confirm.close());
        confirm.add(msg, new HorizontalLayout(yes, cancel));
        confirm.open();
    }

    // ASYNC Start (knop disablen)
    private void asyncStart(TomcatInstanceEntity instance, MenuItem item) {
        item.setEnabled(false);
        service.startInstanceAsync(instance, UI.getCurrent())
                .thenRun(() -> UI.getCurrent().access(() -> {
                    item.setEnabled(true);
                    refreshData();
                }));
    }

    // ASYNC Stop
    private void asyncStop(TomcatInstanceEntity instance, MenuItem item) {
        item.setEnabled(false);
        service.stopInstanceAsync(instance, UI.getCurrent())
                .thenRun(() -> UI.getCurrent().access(() -> {
                    item.setEnabled(true);
                    refreshData();
                }));
    }

    // ASYNC Redeploy
    private void asyncRedeploy(TomcatInstanceEntity instance, MenuItem item) {
        item.setEnabled(false);
        service.redeployInstanceAsync(instance, UI.getCurrent())
                .thenRun(() -> UI.getCurrent().access(() -> {
                    item.setEnabled(true);
                    refreshData();
                }));
    }

    private void updateResultsCount(int visible, int total) {
        resultsCount.setText("Showing " + visible + " of " + total + " instances");
    }

    // Status Badge
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
