package oop.tegevusteplaneerija.client;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;
import oop.tegevusteplaneerija.common.mudel.Grupp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class EventDialogController {
    @FXML
    private TextField titleField;

    @FXML
    private TextField descField;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    private Spinner<Integer> startH, startM, startS, endH, endM, endS;

    @FXML
    private ComboBox<Grupp> groupComboBox;

    private CalendarEvent result;

    public void setGroups(List<Grupp> groups, Grupp selected) {
        groupComboBox.setItems(FXCollections.observableArrayList(groups));
        if (selected != null)
            groupComboBox.getSelectionModel().select(selected);
    }

    @FXML
    public void initialize() {
        groupComboBox.setCellFactory(new Callback<>() {
            @Override
            public javafx.scene.control.ListCell<Grupp> call(javafx.scene.control.ListView<Grupp> lv) {
                return new javafx.scene.control.ListCell<>() {
                    @Override
                    protected void updateItem(Grupp item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : item.getNimi());
                    }
                };
            }
        });
        groupComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Grupp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNimi());
            }
        });
    }

    @FXML
    void cancel() {
        result = null;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void ok() {
        String title = titleField.getText();
        String desc = descField.getText();
        LocalDate start = startDate.getValue();
        ZonedDateTime startDate = start.atTime(startH.getValue(), startM.getValue(), startS.getValue())
                .atZone(ZoneId.systemDefault());
        LocalDate end = endDate.getValue();
        ZonedDateTime endDate = end.atTime(endH.getValue(), endM.getValue(), endS.getValue())
                .atZone(ZoneId.systemDefault());
        Grupp selected = groupComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) {
            result = null;
        } else {
            result = new CalendarEvent(title, desc, startDate, endDate, selected);
        }
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public CalendarEvent getResult() {
        return result;
    }
}
