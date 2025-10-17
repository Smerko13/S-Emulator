package components.mainDashboard.users;

import api.dto.ExecutionDetailsDTO;
import api.dto.VariableDTO;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExecutionStatusDialog {

    public static void show(ExecutionDetailsDTO details, Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Execution Status - Run #" + details.runId);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Header with execution info
        Label headerLabel = new Label(String.format(
            "Execution #%d: %s - %s (Architecture: %s, Level: %s, Cycles: %d)",
            details.runId,
            details.executionType,
            details.programFunctionName,
            details.architectureType,
            details.executionLevel,
            details.cpuCyclesUsed
        ));
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        headerLabel.setWrapText(true);
        root.setTop(headerLabel);
        BorderPane.setMargin(headerLabel, new Insets(0, 0, 10, 0));

        // Table to display variables
        TableView<VariableRow> table = new TableView<>();

        TableColumn<VariableRow, String> nameColumn = new TableColumn<>("Variable Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);

        TableColumn<VariableRow, Integer> valueColumn = new TableColumn<>("Final Value");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setPrefWidth(100);

        TableColumn<VariableRow, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(100);

        table.getColumns().addAll(nameColumn, valueColumn, typeColumn);

        // Populate table with variables
        if (details.finalVariables != null) {
            for (VariableDTO var : details.finalVariables) {
                table.getItems().add(new VariableRow(var.getName(), var.getValue(), var.getType()));
            }
        }

        root.setCenter(table);

        // Bottom buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Execution #%d: %s - %s%n", details.runId, details.executionType, details.programFunctionName));
            sb.append(String.format("Architecture: %s, Level: %s, Cycles: %d%n%n", details.architectureType, details.executionLevel, details.cpuCyclesUsed));
            sb.append("Variables:%n");
            if (details.finalVariables != null) {
                for (VariableDTO var : details.finalVariables) {
                    sb.append(String.format("  %s = %d (%s)%n", var.getName(), var.getValue(), var.getType()));
                }
            }

            ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());
            Clipboard.getSystemClipboard().setContent(content);

            // Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Execution details copied to clipboard!", ButtonType.OK);
            alert.initOwner(dialog);
            alert.showAndWait();
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(copyButton, closeButton);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 500, 400);
        dialog.setScene(scene);
        dialog.show();
    }

    // Inner class for table rows
    public static class VariableRow {
        private final String name;
        private final int value;
        private final String type;

        public VariableRow(String name, int value, String type) {
            this.name = name;
            this.value = value;
            this.type = type != null ? type : "Unknown";
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }
}

