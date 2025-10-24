package com.bludots.views.logs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.bludots.views.MainLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Log Viewer")
@Route(value = "logs", layout = MainLayout.class)
@AnonymousAllowed
@CssImport(value = "./themes/central-manager/styles.css")
public class LogViewerView extends VerticalLayout {

    private final VerticalLayout logContainer;

    public LogViewerView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Title Bar
        H1 title = new H1("🧾 System Log Viewer");
        title.getStyle()
                .set("color", "#2E3A59")
                .set("font-size", "1.6em")
                .set("margin-bottom", "0");

        Button refreshButton = new Button("🔄 Refresh Logs", e -> loadLogs());
        refreshButton.getStyle().set("background-color", "#4CAF50").set("color", "white");

        VerticalLayout header = new VerticalLayout(title, refreshButton);
        header.setAlignItems(FlexComponent.Alignment.START);
        header.getStyle().set("margin-bottom", "20px");
        add(header);

        // Log container
        logContainer = new VerticalLayout();
        logContainer.setSpacing(true);
        logContainer.setPadding(true);
        logContainer.getStyle()
                .set("background-color", "#F9FAFB")
                .set("border-radius", "10px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        add(logContainer);
        setFlexGrow(1, logContainer);

        loadLogs(); // initial load
    }

    private void loadLogs() {
        logContainer.removeAll();

        // Mock log data (later replaced by real DB or AWS logs)
        List<String> logs = List.of(
                "[INFO] Server started successfully at " + now(),
                "[INFO] User 'Admin' logged in",
                "[WARN] High memory usage detected (85%)",
                "[ERROR] Deployment failed for instance Tomcat#3",
                "[INFO] Instance Tomcat#1 restarted",
                "[INFO] System running normally"
        );

        for (String log : logs) {
            logContainer.add(createLogCard(log));
        }

        Notification.show("✅ Logs refreshed.", 2000, Notification.Position.TOP_CENTER);
    }

    private Div createLogCard(String logText) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "12px 16px")
                .set("border-radius", "8px")
                .set("margin-bottom", "10px")
                .set("font-family", "monospace")
                .set("font-size", "14px");

        Span text = new Span(logText);

        if (logText.contains("[ERROR]")) {
            card.getStyle().set("background-color", "#ffebee").set("color", "#c62828");
        } else if (logText.contains("[WARN]")) {
            card.getStyle().set("background-color", "#fff8e1").set("color", "#f9a825");
        } else {
            card.getStyle().set("background-color", "#e8f5e9").set("color", "#2e7d32");
        }

        card.add(text);
        return card;
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
