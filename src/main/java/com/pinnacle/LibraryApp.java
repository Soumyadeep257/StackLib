package com.pinnacle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LibraryApp extends Application {
    private final DatabaseManager db = new DatabaseManager();
    private BorderPane mainLayout;

    @Override
    public void start(Stage primaryStage) {
        db.initializeDatabase(); 

        // --- SIDEBAR NAVIGATION ---
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: #191923; -fx-padding: 30px 20px;");

        Label logo = new Label("StackLib");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold; -fx-padding: 0 0 40 0;");

        Button btnDash = createNavButton("📊 Dashboard");
        Button btnBooks = createNavButton("📚 Manage Books");
        Button btnMembers = createNavButton("👥 Manage Members");
        Button btnIssue = createNavButton("📤 Issue Book");
        Button btnReturn = createNavButton("📥 Return Book");
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnLogout = createNavButton("🚪 Logout");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-font-size: 15px; -fx-alignment: center-left; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> Platform.exit());

        sidebar.getChildren().addAll(logo, btnDash, btnBooks, btnMembers, btnIssue, btnReturn, spacer, btnLogout);

        // --- MAIN CONTENT AREA ---
        mainLayout = new BorderPane();
        mainLayout.setLeft(sidebar);
        mainLayout.setStyle("-fx-base: #1e1e2f; -fx-background-color: #1e1e2f; -fx-control-inner-background: #2a2a40;");
        
        // Navigation Logic
        btnDash.setOnAction(e -> mainLayout.setCenter(createDashboardView()));
        btnBooks.setOnAction(e -> mainLayout.setCenter(createBookManagementView()));
        btnMembers.setOnAction(e -> mainLayout.setCenter(createMemberManagementView()));
        btnIssue.setOnAction(e -> mainLayout.setCenter(createTransactionView("Issue Book", true)));
        btnReturn.setOnAction(e -> mainLayout.setCenter(createTransactionView("Return Book", false)));
        
        mainLayout.setCenter(createDashboardView());

        Scene scene = new Scene(mainLayout, 1100, 700);
        primaryStage.setTitle("StackLib");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- VIEW 1: DASHBOARD ---
    private VBox createDashboardView() {
        VBox view = new VBox(30);
        view.setStyle("-fx-padding: 40px;");

        Label title = new Label("Dashboard Overview");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox cardsBox = new HBox(30);
        cardsBox.getChildren().addAll(
            createStatCard("Total Books", String.valueOf(db.getCount("books")), "#00adb5"),
            createStatCard("Total Members", String.valueOf(db.getCount("members")), "#7b61ff"),
            createStatCard("Active Issues", String.valueOf(db.getCount("issues")), "#ff9f43")
        );

        view.getChildren().addAll(title, cardsBox);
        return view;
    }

    // --- VIEW 2: MANAGE BOOKS ---
    @SuppressWarnings("unchecked")
    private VBox createBookManagementView() {
        VBox view = new VBox(20);
        view.setStyle("-fx-padding: 30px;");

        Label title = new Label("Manage Library Assets");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox form = createFormCard();
        TextField txtId = createTextField("Book ID"); txtId.setPrefWidth(80); // NEW ID FIELD
        TextField txtName = createTextField("Book Name");
        TextField txtAuthor = createTextField("Author");
        TextField txtCat = createTextField("Category");
        TextField txtQty = createTextField("Qty"); txtQty.setPrefWidth(70);
        Button btnAdd = createActionButton("Add Book", "#00adb5");

        form.getChildren().addAll(txtId, txtName, txtAuthor, txtCat, txtQty, btnAdd);

        TableView<Book> table = new TableView<>();
        TableColumn<Book, Integer> colId = new TableColumn<>("ID"); colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Book, String> colName = new TableColumn<>("Name"); colName.setCellValueFactory(new PropertyValueFactory<>("name")); colName.setPrefWidth(250);
        TableColumn<Book, String> colAuthor = new TableColumn<>("Author"); colAuthor.setCellValueFactory(new PropertyValueFactory<>("author")); colAuthor.setPrefWidth(150);
        TableColumn<Book, Integer> colQty = new TableColumn<>("Quantity"); colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        table.getColumns().addAll(colId, colName, colAuthor, colQty);
        table.setItems(FXCollections.observableArrayList(db.getAllBooks()));

        // DELETE BUTTON
        Button btnDelete = createActionButton("Delete Selected Book", "#ff4757");
        HBox bottomControls = new HBox(btnDelete);
        bottomControls.setAlignment(Pos.CENTER_RIGHT);

        // LOGIC
        btnAdd.setOnAction(e -> {
            try {
                db.addBook(Integer.parseInt(txtId.getText()), txtName.getText(), txtAuthor.getText(), txtCat.getText(), Integer.parseInt(txtQty.getText()));
                table.setItems(FXCollections.observableArrayList(db.getAllBooks())); 
                txtId.clear(); txtName.clear(); txtAuthor.clear(); txtCat.clear(); txtQty.clear();
            } catch (Exception ex) { showAlert("Error", "Invalid data. Make sure ID and Qty are numbers, and ID is unique."); }
        });

        btnDelete.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                db.deleteBook(selected.getId());
                table.setItems(FXCollections.observableArrayList(db.getAllBooks()));
            } else {
                showAlert("Warning", "Please click on a book in the table to delete it.");
            }
        });

        view.getChildren().addAll(title, form, table, bottomControls);
        return view;
    }

    // --- VIEW 3: MANAGE MEMBERS ---
    @SuppressWarnings("unchecked")
    private VBox createMemberManagementView() {
        VBox view = new VBox(20);
        view.setStyle("-fx-padding: 30px;");

        Label title = new Label("Student Directory");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox form = createFormCard();
        TextField txtId = createTextField("Student ID");
        TextField txtName = createTextField("Full Name");
        TextField txtDept = createTextField("Department");
        TextField txtPhone = createTextField("Phone");
        Button btnAdd = createActionButton("Add Member", "#7b61ff");

        form.getChildren().addAll(txtId, txtName, txtDept, txtPhone, btnAdd);

        TableView<Member> table = new TableView<>();
        TableColumn<Member, Integer> colId = new TableColumn<>("Student ID"); colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        TableColumn<Member, String> colName = new TableColumn<>("Name"); colName.setCellValueFactory(new PropertyValueFactory<>("name")); colName.setPrefWidth(200);
        TableColumn<Member, String> colDept = new TableColumn<>("Department"); colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        TableColumn<Member, String> colPhone = new TableColumn<>("Phone"); colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        table.getColumns().addAll(colId, colName, colDept, colPhone);
        table.setItems(FXCollections.observableArrayList(db.getAllMembers()));

        // DELETE BUTTON
        Button btnDelete = createActionButton("Delete Selected Member", "#ff4757");
        HBox bottomControls = new HBox(btnDelete);
        bottomControls.setAlignment(Pos.CENTER_RIGHT);

        // LOGIC
        btnAdd.setOnAction(e -> {
            try {
                db.addMember(Integer.parseInt(txtId.getText()), txtName.getText(), txtDept.getText(), txtPhone.getText());
                table.setItems(FXCollections.observableArrayList(db.getAllMembers())); 
                txtId.clear(); txtName.clear(); txtDept.clear(); txtPhone.clear();
            } catch (Exception ex) { showAlert("Error", "Student ID must be a number and unique."); }
        });

        btnDelete.setOnAction(e -> {
            Member selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                db.deleteMember(selected.getStudentId());
                table.setItems(FXCollections.observableArrayList(db.getAllMembers()));
            } else {
                showAlert("Warning", "Please click on a member in the table to delete them.");
            }
        });

        view.getChildren().addAll(title, form, table, bottomControls);
        return view;
    }

    // --- VIEW 4 & 5: ISSUE / RETURN BOOK ---
    private VBox createTransactionView(String actionName, boolean isIssue) {
        VBox view = new VBox(20);
        view.setStyle("-fx-padding: 50px; -fx-alignment: top-center;");

        Label title = new Label(actionName);
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox form = new VBox(20);
        form.setPrefWidth(400); form.setMaxWidth(400);
        form.setStyle("-fx-background-color: #2a2a40; -fx-padding: 40px; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 8);");

        TextField txtStudentId = createTextField("Student ID");
        TextField txtBookId = createTextField("Book ID");
        Button btnSubmit = createActionButton(actionName, isIssue ? "#ff9f43" : "#00adb5");
        btnSubmit.setPrefWidth(320);

        form.getChildren().addAll(txtStudentId, txtBookId, btnSubmit);

        btnSubmit.setOnAction(e -> {
            try {
                int sId = Integer.parseInt(txtStudentId.getText());
                int bId = Integer.parseInt(txtBookId.getText());
                
                // Retrieve the detailed result message from the database
                String result = isIssue ? db.issueBook(sId, bId) : db.returnBook(sId, bId);
                
                if (result.equals("SUCCESS")) {
                    showAlert("Success", actionName + " completed successfully!");
                    txtStudentId.clear(); txtBookId.clear();
                } else {
                    // Display the EXACT reason it failed in the alert box
                    showAlert("Failed", result);
                }
            } catch (Exception ex) { showAlert("Error", "Please enter valid numeric IDs."); }
        });

        view.getChildren().addAll(title, form);
        return view;
    }

    // --- UI HELPER METHODS ---
    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0ab; -fx-font-size: 15px; -fx-font-weight: bold; -fx-alignment: center-left; -fx-cursor: hand;");
        btn.setPrefWidth(200); btn.setPadding(new Insets(12));
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2a2a40; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-alignment: center-left; -fx-background-radius: 8px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0ab; -fx-font-size: 15px; -fx-font-weight: bold; -fx-alignment: center-left;"));
        return btn;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25)); card.setPrefSize(250, 120);
        card.setStyle("-fx-background-color: #2a2a40; -fx-background-radius: 12px; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 4px; -fx-border-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 8);");
        Label lblTitle = new Label(title); lblTitle.setStyle("-fx-text-fill: #a0a0ab; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label lblValue = new Label(value); lblValue.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }

    private HBox createFormCard() {
        HBox form = new HBox(15);
        form.setStyle("-fx-background-color: #2a2a40; -fx-padding: 20px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        return form;
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #1e1e2f; -fx-text-fill: white; -fx-prompt-text-fill: #666; -fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #3f3f5a; -fx-border-radius: 6px;");
        return tf;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 6px; -fx-cursor: hand;");
        return btn;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.showAndWait();
    }
}