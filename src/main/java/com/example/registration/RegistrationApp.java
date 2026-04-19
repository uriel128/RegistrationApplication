package com.example.registration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistrationApp extends Application {
  // Regex patterns for all form fields.
  private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z'\\- ]{1,24}$");
  private static final Pattern FARMINGDALE_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@farmingdale\\.edu$",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern DOB_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])/\\d{4}$");
  private static final Pattern ZIP_PATTERN = Pattern.compile("^\\d{5}$");

  private final BooleanProperty firstNameValid = new SimpleBooleanProperty(false);
  private final BooleanProperty lastNameValid = new SimpleBooleanProperty(false);
  private final BooleanProperty emailValid = new SimpleBooleanProperty(false);
  private final BooleanProperty dobValid = new SimpleBooleanProperty(false);
  private final BooleanProperty zipValid = new SimpleBooleanProperty(false);

  @Override
  public void start(Stage primaryStage) {
    TextField firstNameField = new TextField();
    TextField lastNameField = new TextField();
    TextField emailField = new TextField();
    TextField dobField = new TextField();
    TextField zipField = new TextField();

    firstNameField.setPromptText("Ex: Jordan");
    lastNameField.setPromptText("Ex: Smith");
    emailField.setPromptText("Ex: jsmith@farmingdale.edu");
    dobField.setPromptText("MM/DD/YYYY");
    zipField.setPromptText("Ex: 11735");

    Label firstNameStatus = new Label(" ");
    Label lastNameStatus = new Label(" ");
    Label emailStatus = new Label(" ");
    Label dobStatus = new Label(" ");
    Label zipStatus = new Label(" ");

    Button addButton = new Button("Add");
    // The Add button remains disabled until every field has valid data.
    addButton.disableProperty().bind(firstNameValid.not()
        .or(lastNameValid.not())
        .or(emailValid.not())
        .or(dobValid.not())
        .or(zipValid.not()));

    // Keep validity state in sync while typing.
    firstNameField.textProperty()
        .addListener((obs, oldValue, newValue) -> firstNameValid.set(validatePattern(newValue, NAME_PATTERN)));
    lastNameField.textProperty()
        .addListener((obs, oldValue, newValue) -> lastNameValid.set(validatePattern(newValue, NAME_PATTERN)));
    emailField.textProperty()
        .addListener((obs, oldValue, newValue) -> emailValid.set(validatePattern(newValue, FARMINGDALE_EMAIL_PATTERN)));
    dobField.textProperty().addListener((obs, oldValue, newValue) -> dobValid.set(validateDateOfBirth(newValue)));
    zipField.textProperty()
        .addListener((obs, oldValue, newValue) -> zipValid.set(validatePattern(newValue, ZIP_PATTERN)));

    attachFocusValidation(firstNameField, firstNameStatus, firstNameValid,
        "First name is valid.", "First name must be 2-25 characters.");
    attachFocusValidation(lastNameField, lastNameStatus, lastNameValid,
        "Last name is valid.", "Last name must be 2-25 characters.");
    attachFocusValidation(emailField, emailStatus, emailValid,
        "Email is valid.", "Email must end with @farmingdale.edu.");
    attachFocusValidation(dobField, dobStatus, dobValid,
        "Date of birth is valid.", "Use MM/DD/YYYY and a real date.");
    attachFocusValidation(zipField, zipStatus, zipValid,
        "Zip code is valid.", "Zip code must be exactly 5 digits.");

    addButton.setOnAction(event -> {
      if (firstNameValid.get() && lastNameValid.get() && emailValid.get() && dobValid.get() && zipValid.get()) {
        // Navigate to a separate scene only when all validation checks pass.
        Scene successScene = createSuccessScene(
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            emailField.getText().trim());
        primaryStage.setScene(successScene);
      }
    });

    GridPane formGrid = new GridPane();
    formGrid.setHgap(12);
    formGrid.setVgap(8);

    formGrid.add(new Label("First Name"), 0, 0);
    formGrid.add(firstNameField, 0, 1);
    formGrid.add(firstNameStatus, 0, 2);

    formGrid.add(new Label("Last Name"), 1, 0);
    formGrid.add(lastNameField, 1, 1);
    formGrid.add(lastNameStatus, 1, 2);

    formGrid.add(new Label("Email"), 0, 3);
    formGrid.add(emailField, 0, 4, 2, 1);
    formGrid.add(emailStatus, 0, 5, 2, 1);

    formGrid.add(new Label("Date of Birth"), 0, 6);
    formGrid.add(dobField, 0, 7);
    formGrid.add(dobStatus, 0, 8);

    formGrid.add(new Label("Zip Code"), 1, 6);
    formGrid.add(zipField, 1, 7);
    formGrid.add(zipStatus, 1, 8);

    VBox card = new VBox(8);
    card.getStyleClass().add("form-card");
    card.setMaxWidth(620);

    Label title = new Label("Student Registration");
    title.getStyleClass().add("title");
    Label subtitle = new Label("All fields are required. Validation appears after focus changes.");
    subtitle.getStyleClass().add("subtitle");

    VBox.setMargin(addButton, new Insets(10, 0, 0, 0));
    card.getChildren().addAll(title, subtitle, formGrid, addButton);

    VBox root = new VBox(card);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(30));

    Scene scene = new Scene(root, 760, 540);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

    primaryStage.setTitle("JavaFX Registration Form");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void attachFocusValidation(
      TextField field,
      Label statusLabel,
      BooleanProperty isValid,
      String validMessage,
      String invalidMessage) {
    field.focusedProperty().addListener((obs, hadFocus, hasFocus) -> {
      if (!hasFocus) {
        updateFieldFeedback(field, statusLabel, isValid.get(), validMessage, invalidMessage);
      }
    });
  }

  private void updateFieldFeedback(
      TextField field,
      Label statusLabel,
      boolean valid,
      String validMessage,
      String invalidMessage) {
    field.getStyleClass().removeAll("field-valid", "field-invalid");
    statusLabel.getStyleClass().removeAll("status-valid", "status-invalid");

    if (valid) {
      field.getStyleClass().add("field-valid");
      statusLabel.getStyleClass().add("status-valid");
      statusLabel.setText(validMessage);
    } else {
      field.getStyleClass().add("field-invalid");
      statusLabel.getStyleClass().add("status-invalid");
      statusLabel.setText(invalidMessage);
    }
  }

  private boolean validatePattern(String value, Pattern pattern) {
    if (value == null) {
      return false;
    }
    return pattern.matcher(value.trim()).matches();
  }

  /**
   * Parses date-of-birth input in MM/DD/YYYY format.
   *
   * @param value the raw input value from the date-of-birth field
   * @return parsed {@link LocalDate} if the value is valid
   * @throws DateTimeParseException when the value does not represent a valid
   *                                calendar date
   */
  private LocalDate parseDateOfBirth(String value) throws DateTimeParseException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);
    return LocalDate.parse(value, formatter);
  }

  private boolean validateDateOfBirth(String value) {
    if (!validatePattern(value, DOB_PATTERN)) {
      return false;
    }

    try {
      parseDateOfBirth(value.trim());
      return true;
    } catch (DateTimeParseException ex) {
      return false;
    }
  }

  private Scene createSuccessScene(String firstName, String lastName, String email) {
    Label title = new Label("Registration Complete");
    title.getStyleClass().add("success-title");

    Label summary = new Label("Welcome " + firstName + " " + lastName + "!\n"
        + "Confirmation sent to: " + email);
    summary.getStyleClass().add("success-text");
    summary.setAlignment(Pos.CENTER);

    VBox root = new VBox(16, title, summary);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(36));

    Scene scene = new Scene(root, 760, 540);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    return scene;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
