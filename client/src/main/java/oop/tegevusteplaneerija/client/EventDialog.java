package oop.tegevusteplaneerija.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import oop.tegevusteplaneerija.common.mudel.CalendarEvent;

import java.io.IOException;

public class EventDialog {
    private Stage dialog;
    private FXMLLoader fxml;

    public EventDialog(ActionEvent e, Stage owner) throws IOException {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Add New Event");

        fxml = new FXMLLoader(EventDialog.class.getClassLoader().getResource("EventDialog.fxml"));
        Scene scene = new Scene(fxml.load());

        dialog.setScene(scene);
    }

    public CalendarEvent waitForResult() {
        dialog.showAndWait();

        EventDialogController controller = fxml.getController();
        return controller.getResult();
    }

    public FXMLLoader getFxmlLoader() {
        return fxml;
    }
}
