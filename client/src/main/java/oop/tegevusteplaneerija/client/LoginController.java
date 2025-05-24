package oop.tegevusteplaneerija.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import oop.tegevusteplaneerija.client.util.ClientDBManager;
import oop.tegevusteplaneerija.common.mudel.Kasutaja;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label errorLabel;

    private ClientDBManager dbManager;
    private LoginCallback callback;

    public void setDbManager(ClientDBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setLoginCallback(LoginCallback callback) {
        this.callback = callback;
    }

    @FXML
    private void initialize() {
        errorLabel.setText("");
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username.");
            return;
        }
        try {
            Kasutaja user = dbManager.loginKasutaja(username);
            if (user != null) {
                errorLabel.setText("");
                if (callback != null)
                    callback.onLogin(user);
            } else {
                errorLabel.setText("User does not exist.");
            }
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username.");
            return;
        }
        try {
            Kasutaja user = dbManager.loginKasutaja(username);
            if (user != null) {
                errorLabel.setText("Username already taken.");
                return;
            }
            int id = dbManager.lisaKasutaja(username);
            if (id > 0) {
                Kasutaja newUser = dbManager.leiaKasutaja(username);
                errorLabel.setText("");
                if (callback != null)
                    callback.onLogin(newUser);
            } else {
                errorLabel.setText("Registration failed.");
            }
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }

    public interface LoginCallback {
        void onLogin(Kasutaja user);
    }
}
