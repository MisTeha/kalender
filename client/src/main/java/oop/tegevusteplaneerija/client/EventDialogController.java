package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oop.tegevusteplaneerija.common.CalendarEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

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

    private CalendarEvent result;

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
        ZonedDateTime startDate = start.atTime(startH.getValue(), startM.getValue(), startS.getValue()).atZone(ZoneId.systemDefault());

        LocalDate end = endDate.getValue();
        ZonedDateTime endDate = end.atTime(endH.getValue(), endM.getValue(), endS.getValue()).atZone(ZoneId.systemDefault());

        var event = new CalendarEvent(title, desc, startDate, endDate);
        result = event;

        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public CalendarEvent getResult() {
        return result;
    }
}
