package application;

import dao.BookDAO;
import dao.MemberDAO;
import dao.TransactionDAO;
import model.Book;
import model.Member;
import model.Transaction;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    private final BookDAO        bookDAO        = new BookDAO();
    private final MemberDAO      memberDAO      = new MemberDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private TableView<Book> bookTable = new TableView<>();
    
    // Dashboard labels (refreshed after every action)
    private Label totalBooksLbl   = new Label("0");
    private Label totalMembersLbl = new Label("0");
    private Label issuedLbl       = new Label("0");
    private Label overdueLbl      = new Label("0");
    
    private void loadBooks() {
        bookTable.setItems(FXCollections.observableArrayList(bookDAO.getAllBooks()));
    }

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
                new Tab("Dashboard",    buildDashboardTab()),
                new Tab("Books",        buildBooksTab()),
                new Tab("Members",      buildMembersTab()),
                new Tab("Issue / Return", buildTransactionTab()),
                new Tab("History",      buildHistoryTab())
        );

        refreshDashboard();

        Scene scene = new Scene(tabs, 950, 680);
        scene.getStylesheets().add(getClass().getResource("/style.css") != null
                ? getClass().getResource("/style.css").toExternalForm() : "");
        stage.setTitle("Library Management System");
        stage.setScene(scene);
        stage.show();
        
        
    }

    
    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD TAB
    // ═══════════════════════════════════════════════════════════════════════
    
    private Pane buildDashboardTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));

        Label heading = new Label("Library Dashboard");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        HBox cards = new HBox(20);
        cards.getChildren().addAll(
                statCard("Total Books",      totalBooksLbl,   "#4A90D9"),
                statCard("Total Members",    totalMembersLbl, "#27AE60"),
                statCard("Currently Issued", issuedLbl,       "#E67E22"),
                statCard("Overdue",          overdueLbl,      "#E74C3C")
        );

        root.getChildren().addAll(heading, cards);
        return root;
    }

    private VBox statCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color:" + color + "; -fx-background-radius:10;");

        Label t = new Label(title);
        t.setTextFill(Color.WHITE);
        t.setFont(Font.font("Arial", 13));

        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        card.getChildren().addAll(t, valueLabel);
        return card;
    }

    private void refreshDashboard() {
        totalBooksLbl.setText(String.valueOf(bookDAO.getTotalBooks()));
        totalMembersLbl.setText(String.valueOf(memberDAO.getTotalMembers()));
        issuedLbl.setText(String.valueOf(transactionDAO.getTotalIssued()));
        overdueLbl.setText(String.valueOf(transactionDAO.getOverdueCount()));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BOOKS TAB
    // ═══════════════════════════════════════════════════════════════════════

    private Pane buildBooksTab() {
        // --- form inputs ---
        TextField titleF    = new TextField(); titleF.setPromptText("Title");
        TextField authorF   = new TextField(); authorF.setPromptText("Author");
        TextField categoryF = new TextField(); categoryF.setPromptText("Category");
        TextField quantityF = new TextField(); quantityF.setPromptText("Quantity");
        TextField searchF   = new TextField(); searchF.setPromptText("Search by title / author / category");

        Button addBtn    = btn("Add Book",    "#27AE60");
        Button updateBtn = btn("Update",      "#4A90D9");
        Button deleteBtn = btn("Delete",      "#E74C3C");
        Button clearBtn  = btn("Clear",       "#888888");

        // --- table ---
        TableView<Book> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(
                col("ID",       "book_id",       60),
                col("Title",    "title",         200),
                col("Author",   "author",        150),
                col("Category", "category",      120),
                col("Qty",      "quantity",      60),
                col("Status",   "status",        110)
        );
        table.setItems(FXCollections.observableArrayList(bookDAO.getAllBooks()));

        // populate form on row select
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                titleF.setText(sel.getTitle());
                authorF.setText(sel.getAuthor());
                categoryF.setText(sel.getCategory());
                quantityF.setText(String.valueOf(sel.getQuantity()));
            }
        });

        // live search
        searchF.textProperty().addListener((obs, o, n) ->
                table.setItems(FXCollections.observableArrayList(bookDAO.searchBook(n))));

        // actions
        addBtn.setOnAction(e -> {
            try {
                bookDAO.addBook(titleF.getText(), authorF.getText(), categoryF.getText(),
                        Integer.parseInt(quantityF.getText()));
                table.setItems(FXCollections.observableArrayList(bookDAO.getAllBooks()));
                clearFields(titleF, authorF, categoryF, quantityF);
                refreshDashboard();
            } catch (NumberFormatException ex) { alert("Quantity must be a number."); }
        });

        updateBtn.setOnAction(e -> {
            Book sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a book to update."); return; }
            try {
                bookDAO.updateBook(sel.getBook_id(), titleF.getText(), authorF.getText(),
                        categoryF.getText(), Integer.parseInt(quantityF.getText()));
                table.setItems(FXCollections.observableArrayList(bookDAO.getAllBooks()));
                refreshDashboard();
            } catch (NumberFormatException ex) { alert("Quantity must be a number."); }
        });

        deleteBtn.setOnAction(e -> {
            Book sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a book to delete."); return; }
            bookDAO.deleteBook(sel.getBook_id());
            table.setItems(FXCollections.observableArrayList(bookDAO.getAllBooks()));
            refreshDashboard();
        });

        clearBtn.setOnAction(e -> clearFields(titleF, authorF, categoryF, quantityF));

        // --- layout ---
        HBox formRow = new HBox(10, titleF, authorF, categoryF, quantityF);
        HBox.setHgrow(titleF, Priority.ALWAYS);
        HBox.setHgrow(authorF, Priority.ALWAYS);

        HBox btnRow = new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                label("Book Management"), searchF, formRow, btnRow, table
        );
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MEMBERS TAB
    // ═══════════════════════════════════════════════════════════════════════

    private Pane buildMembersTab() {
        TextField nameF   = new TextField(); nameF.setPromptText("Full Name");
        TextField emailF  = new TextField(); emailF.setPromptText("Email");
        TextField phoneF  = new TextField(); phoneF.setPromptText("Phone");
        TextField searchF = new TextField(); searchF.setPromptText("Search by name / email");

        Button addBtn    = btn("Add Member", "#27AE60");
        Button deleteBtn = btn("Delete",     "#E74C3C");

        TableView<Member> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                col("ID",        "member_id", 60),
                col("Name",      "name",      180),
                col("Email",     "email",     200),
                col("Phone",     "phone",     130),
                col("Joined",    "joinDate",  120)
        );
        table.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                nameF.setText(sel.getName());
                emailF.setText(sel.getEmail());
                phoneF.setText(sel.getPhone());
            }
        });

        searchF.textProperty().addListener((obs, o, n) ->
                table.setItems(FXCollections.observableArrayList(memberDAO.searchMember(n))));

        addBtn.setOnAction(e -> {
            if (nameF.getText().isBlank()) { alert("Name is required."); return; }
            memberDAO.addMember(nameF.getText(), emailF.getText(), phoneF.getText());
            table.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));
            clearFields(nameF, emailF, phoneF);
            refreshDashboard();
        });

        deleteBtn.setOnAction(e -> {
            Member sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a member."); return; }
            memberDAO.deleteMember(sel.getMember_id());
            table.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));
            refreshDashboard();
        });

        HBox formRow = new HBox(10, nameF, emailF, phoneF);
        HBox.setHgrow(nameF, Priority.ALWAYS);
        HBox.setHgrow(emailF, Priority.ALWAYS);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                label("Member Management"), searchF, formRow,
                new HBox(10, addBtn, deleteBtn), table
        );
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ISSUE / RETURN TAB
    // ═══════════════════════════════════════════════════════════════════════

    private Pane buildTransactionTab() {
        // Issue section
        TextField memberIdF = new TextField(); memberIdF.setPromptText("Member ID");
        TextField bookIdF   = new TextField(); bookIdF.setPromptText("Book ID");
        Button issueBtn = btn("Issue Book", "#4A90D9");

        // Return section
        TextField txIdF  = new TextField(); txIdF.setPromptText("Transaction ID");
        Button returnBtn = btn("Return Book", "#27AE60");

        Label fineLabel = new Label();
        fineLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Active transactions table
        TableView<Transaction> activeTable = new TableView<>();
        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activeTable.getColumns().addAll(
                col("Tx ID",      "transaction_id", 60),
                col("Member",     "memberName",      160),
                col("Book",       "bookTitle",       200),
                col("Issued",     "issueDate",       110),
                col("Due",        "dueDate",         110),
                col("Status",     "status",          90)
        );
        activeTable.setItems(FXCollections.observableArrayList(transactionDAO.getActiveTransactions()));

        // populate return field on select
        activeTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) txIdF.setText(String.valueOf(sel.getTransaction_id()));
        });

        issueBtn.setOnAction(e -> {
            try {
                int mid = Integer.parseInt(memberIdF.getText().trim());
                int bid = Integer.parseInt(bookIdF.getText().trim());
                boolean ok = transactionDAO.issueBook(mid, bid);
                if (ok) {
                    info("Book issued successfully! Due in 14 days.");
                    loadBooks();
                    memberIdF.clear(); bookIdF.clear();
                } else {
                    alert("Could not issue: book may be out of stock, member/book ID invalid, or member already has this book.");
                }
                activeTable.setItems(FXCollections.observableArrayList(transactionDAO.getActiveTransactions()));
                refreshDashboard();
            } catch (NumberFormatException ex) { alert("Enter valid numeric IDs."); }
        });

        returnBtn.setOnAction(e -> {
            try {
                int txId = Integer.parseInt(txIdF.getText().trim());
                double fine = transactionDAO.returnBook(txId);
                if (fine < 0) {
                    alert("Transaction not found or already returned.");
                } else if (fine > 0) {
                    fineLabel.setTextFill(Color.web("#E74C3C"));
                    fineLabel.setText(String.format("Book returned. Fine due: ₹%.2f", fine));
                    info(String.format("Book returned. Overdue fine: ₹%.2f", fine));
                    loadBooks();
                } else {
                    fineLabel.setTextFill(Color.web("#27AE60"));
                    fineLabel.setText("Book returned on time. No fine.");
                    info("Book returned on time.");
                    loadBooks();
                }
                txIdF.clear();
                activeTable.setItems(FXCollections.observableArrayList(transactionDAO.getActiveTransactions()));
                refreshDashboard();
            } catch (NumberFormatException ex) { alert("Enter a valid Transaction ID."); }
        });

        // Layout
        TitledPane issuePane = new TitledPane("Issue a Book",
                hbox(10, memberIdF, bookIdF, issueBtn));
        issuePane.setCollapsible(false);

        TitledPane returnPane = new TitledPane("Return a Book",
                new VBox(10, hbox(10, txIdF, returnBtn), fineLabel));
        returnPane.setCollapsible(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                label("Issue / Return"),
                issuePane,
                returnPane,
                label("Active Issues"),
                activeTable
        );
        VBox.setVgrow(activeTable, Priority.ALWAYS);
        return root;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HISTORY TAB
    // ═══════════════════════════════════════════════════════════════════════

    private Pane buildHistoryTab() {
        TextField memberIdF = new TextField(); memberIdF.setPromptText("Filter by Member ID (blank = all)");
        Button loadBtn = btn("Load", "#4A90D9");

        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                col("Tx ID",      "transaction_id", 60),
                col("Member",     "memberName",      150),
                col("Book",       "bookTitle",       190),
                col("Issued",     "issueDate",       100),
                col("Due",        "dueDate",         100),
                col("Returned",   "returnDate",      100),
                col("Fine (₹)",   "fine",            80),
                col("Status",     "status",          90)
        );
        table.setItems(FXCollections.observableArrayList(transactionDAO.getAllTransactions()));

        loadBtn.setOnAction(e -> {
            String txt = memberIdF.getText().trim();
            if (txt.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(transactionDAO.getAllTransactions()));
            } else {
                try {
                    int mid = Integer.parseInt(txt);
                    table.setItems(FXCollections.observableArrayList(
                            transactionDAO.getTransactionsByMember(mid)));
                } catch (NumberFormatException ex) { alert("Enter a valid Member ID."); }
            }
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                label("Transaction History"),
                hbox(10, memberIdF, loadBtn),
                table
        );
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITY HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, ?> col(String header, String prop, double width) {
        TableColumn<T, Object> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(width);
        return c;
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; " +
                   "-fx-background-radius:6; -fx-font-weight:bold;");
        return b;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        return l;
    }

    private HBox hbox(int spacing, javafx.scene.Node... nodes) {
        HBox h = new HBox(spacing, nodes);
        h.setAlignment(Pos.CENTER_LEFT);
        for (javafx.scene.Node n : nodes)
            if (n instanceof TextField) HBox.setHgrow(n, Priority.ALWAYS);
        return h;
    }

    private void clearFields(TextField... fields) {
        for (TextField f : fields) f.clear();
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}