/**
 *
 */
package is.moneytracker;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.mapping.List;
import org.hibernate.type.DateType;

import is.moneytracker.model.Category;
import is.moneytracker.model.Transaction;
import is.moneytracker.model.TransactionType;
import is.moneytracker.util.Message;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

/**
 * @author Van-Duyet Le
 *
 */
/**
 * @author Van-Duyet Le
 *
 */
public class CategoryController implements Initializable {
	public static final String TRANS_INCOME = "Thu nhập";
	public static final String TRANS_OUTCOME = "Chi tiêu";

	private final ObservableList<TransactionType> form_trans_type_options =
		FXCollections.observableArrayList(
			new TransactionType("income", TRANS_INCOME),
			new TransactionType("outcome", TRANS_OUTCOME)
    );

	// Reference main
	private MoneyTrackerMain mainApp;

	@FXML private AnchorPane anchor;
	@FXML private Button back_to_main_btn;

	@FXML private Label form_message_success;
	@FXML private ComboBox<TransactionType> cat_type;
	@FXML private TextField cat_name;
	@FXML private Button cat_submit;
	@FXML private Button editBtn;

	private FXMLLoader loader;

	@FXML private TableView<Category> mainTable;
	@FXML private ObservableList<Category> categoriesData = FXCollections.observableArrayList();

	// Table Column
	@FXML private TableColumn<Category, String> mainTableColumnStt;
	@FXML private TableColumn<Category, String> mainTableColumnType;
	@FXML private TableColumn<Category, String> mainTableColumnName;
	@FXML private TableColumn<Category, String> mainTableColumnAction;

	Category selectedItem;

	/**
	 * Constructor
	 */
	public CategoryController() {
		this.loader = new FXMLLoader(getClass().getResource("view/Category.fxml"));
		this.loader.setController(this);

		// setMainTableData();

		try {
			this.anchor = (AnchorPane) this.loader.load();

		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public CategoryController(MoneyTrackerMain mainApp) {
		this();
		this.setMainApp(mainApp);

		// Load data
		loadTableData();
	}

	public void loadTableData() {
		// Load data to table
		Session session = this.getMainApp().getSession();
		org.hibernate.Transaction tx = null;
		try {
			tx = session.beginTransaction();
			java.util.List<Category> trans = session.createQuery("FROM Category").list();

			categoriesData = FXCollections.observableArrayList(trans);
			this.mainTable.setItems(categoriesData);
			tx.commit();
		}
		catch (HibernateException e) {
			if (tx!=null) tx.rollback();
			Message.Error(e.toString());
			// e.printStackTrace();
		}
	}

	public void setMainTableData() {
		if (mainApp == null) return;
	}

	@Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		this.cat_type.setItems(this.form_trans_type_options);

		final StyleChangingRowFactory<Category> rowFactory = new StyleChangingRowFactory<>("highlightedRow");
		mainTable.setRowFactory(rowFactory);

		mainTableColumnStt.setCellValueFactory(new PropertyValueFactory<Category, String>("id"));
		mainTableColumnType.setCellValueFactory(new PropertyValueFactory<Category, String>("type"));
		mainTableColumnName.setCellValueFactory(new PropertyValueFactory<Category, String>("name"));

		CategoryController that = this;
	    mainTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
	        @Override
	        public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
	            //Check whether item is selected and set value of selected item to Label
	            if(mainTable.getSelectionModel().getSelectedItem() != null)
	            {
	               TableViewSelectionModel selectionModel = mainTable.getSelectionModel();
	               that.selectedItem = (Category) selectionModel.getSelectedItem();
	               System.out.println("Selected Value" + that.selectedItem.getId());
	             }
	             }
	         });

    }

	public void delCat() {
		if (this.selectedItem != null) {
			Session session = this.getMainApp().getSession();
	        org.hibernate.Transaction tx = null;
	        try {
	        	tx = session.beginTransaction();
				Category cat = session.load(Category.class, this.selectedItem.getId());
				session.delete(cat);
				tx.commit();

				Message.MessageBox("Xoá thành công");
				loadTableData();
	        } catch (HibernateException e) {
	            Message.Error(e.getMessage());
	            e.printStackTrace();
	         }

		} else {
			Message.Error("Vui lòng chọn dòng để xoá");
		}
	}

	public void editCat() {
		if (this.selectedItem != null) {

			// Create the custom dialog.
			Dialog<String> dialog = new Dialog<>();
			dialog.setTitle("Sửa");

			// Set the button types.
			ButtonType loginButtonType = new ButtonType("Lưu", ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

			// Create the config labels and fields.
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));

			ComboBox<TransactionType> trans_type = new ComboBox<TransactionType>();
			trans_type.setItems(form_trans_type_options);

			TextField cat_name = new TextField();
			cat_name.setText(this.selectedItem.getName());

			grid.add(new Label("Loại:"), 0, 0);
			grid.add(trans_type, 1, 0);
			grid.add(new Label("Loại giao dịch: "), 0, 1);
			grid.add(cat_name, 1, 1);

			dialog.getDialogPane().setContent(grid);

			Optional<String> result = dialog.showAndWait();


//			this.cat_name.setText(this.selectedItem.getName());



//			Session session = this.getMainApp().getSession();
//	        org.hibernate.Transaction tx = null;
//	        try {
//	        	tx = session.beginTransaction();
//				Category cat = session.load(Category.class, this.selectedItem.getId());
//				session.delete(cat);
//				tx.commit();
//
//				Message.MessageBox("Xoá thành công");
//				loadTableData();
//	        } catch (HibernateException e) {
//	            Message.Error(e.getMessage());
//	            e.printStackTrace();
//	         }

		} else {
			Message.Error("Vui lòng chọn dòng để xoá");
		}
	}


	public void submitNewCat() {
		Category saver = new Category();
		Session session = this.getMainApp().getSession();
        org.hibernate.Transaction tx = null;
        try {
			tx = session.beginTransaction();
			this.form_message_success.setVisible(false);

			saver.setName(this.cat_name.getText());
			if (this.cat_name.getText().equals("")) {
				Message.Error("Vui lòng nhập tên danh mục");
				return;
			}

			if (this.cat_type.getValue() == null) {
				Message.Error("Vui lòng chọn loại giao dịch");
				return;
			}
			saver.setType(this.cat_type.getValue().getId());
			System.out.println("Type:" + this.cat_type.getValue().getId());

			// TODO
			saver.setImage("");
			saver.setDescription("");

			// Save
			session.save(saver);
			tx.commit();

			// After save success
			this.form_message_success.setVisible(true);
			this.cat_name.setText(null);

			// Reload main table
			this.loadTableData();
        } catch (HibernateException e) {
           if (tx!=null) tx.rollback();

           Message.Error(e.getMessage());
           e.printStackTrace();
        }
	}

	/**
	 * Get Parent Scene
	 * @return Scene
	 */
	public Scene getParentScene() {
		return this.getMainApp().getMainScene();
	}

	public AnchorPane getAnchor() {
		return this.anchor;
	}

	/**
	 * Is called by main --> reference back to itself
	 */
	public void setMainApp(MoneyTrackerMain main) {
		this.mainApp = main;
	}
	public MoneyTrackerMain getMainApp() {
		return this.mainApp;
	}

	public void backToMain() {
		this.getMainApp().showMoneyTrackerOverview();
	}

	// ================================
	// Context menu
	// ================================
	/**
	 * Add record
	 */
	public void addAction() {

	}


	public void exitAction() throws Exception {
		this.getMainApp().closeMoneyTracker();
	}

	public void aboutAction() {
		System.out.println("About...");
		this.getMainApp().aboutMoneyTracker();
	}
}
