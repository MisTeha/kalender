package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.Arrays;
import java.util.List;

public class UserGroupSelectorController {
    @FXML
    private TextField groupNameField;
    @FXML
    private TextField usernamesField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button createButton;

    private String groupName;
    private List<String> usernames;
    private boolean cancelled = true;

    @FXML
    private void cancel() {
        cancelled = true;
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    @FXML
    private void createGroup() {
        groupName = groupNameField.getText();
        usernames = Arrays.stream(usernamesField.getText().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        cancelled = false;
        ((Stage) createButton.getScene().getWindow()).close();
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
