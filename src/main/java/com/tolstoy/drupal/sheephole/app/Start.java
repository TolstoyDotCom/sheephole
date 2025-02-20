/*
 * Copyright 2025 Chris Kelly
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.tolstoy.drupal.sheephole.app;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;
import org.dizitart.jbus.Subscribe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import atlantafx.base.theme.PrimerLight;
import org.controlsfx.control.textfield.TextFields;
import org.semver4j.Semver;

import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.storage.StorageEmbeddedDerby;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.app.utils.ResourceBundleWithFormatting;
import com.tolstoy.drupal.sheephole.app.installation.AppDirectories;
import com.tolstoy.drupal.sheephole.api.installation.IAppDirectories;
import com.tolstoy.drupal.sheephole.api.installation.IInstallationInstruction;
import com.tolstoy.drupal.sheephole.api.preferences.IPreferences;
import com.tolstoy.drupal.sheephole.api.preferences.IPreferencesFactory;
import com.tolstoy.drupal.sheephole.app.preferences.PreferencesFactory;
import com.tolstoy.drupal.sheephole.api.installation.InstallationInstructionType;
import com.tolstoy.drupal.sheephole.api.installation.IOperationResult;
import com.tolstoy.drupal.sheephole.api.installation.IInstallable;
import com.tolstoy.drupal.sheephole.api.installation.OperationResultType;
import com.tolstoy.drupal.sheephole.api.installation.ISiteProfile;
import com.tolstoy.drupal.sheephole.api.installation.PlatformType;
import com.tolstoy.drupal.sheephole.app.installation.BasicInstallableVersion;
import com.tolstoy.drupal.sheephole.app.installation.Installable;
import com.tolstoy.drupal.sheephole.app.installation.InstallationInstruction;
import com.tolstoy.drupal.sheephole.app.installation.SiteProfile;

public class Start extends Application {
	private static final Logger logger = LogManager.getLogger( Start.class );

	private static final int SERVER_PORT = 41295;

	private BusinessLogic businessLogic;
	private BasicServer basicServer;
	private JBus jbus;
	private Stage stage;
	private Scene scene;
	private MenuBar menuBar;
	private StatusBar statusBar;
	private BorderPane mainPane;
	private TextArea loggingTextArea;

	@Override
	public void start( Stage stage ) throws Exception {
		this.stage = stage;
		this.menuBar = createMenuBar();

		this.statusBar = new StatusBar();

		this.loggingTextArea.setEditable( false );
		this.loggingTextArea.setWrapText( true );
		this.loggingTextArea.setPrefHeight( 100 );

		BorderPane tempBorderPane = new BorderPane();
		tempBorderPane.setTop( this.loggingTextArea );
		tempBorderPane.setBottom( statusBar );

		this.mainPane = new BorderPane();
		this.mainPane.setPrefHeight( 100 );
		this.mainPane.setTop( menuBar );
		this.mainPane.setBottom( tempBorderPane );

		this.scene = new Scene( mainPane );

		this.stage.setScene( this.scene );
		this.stage.setTitle( "Sheephole" );

		this.stage.setMinWidth( 500 );
		this.stage.setMinHeight( 400 );

		Application.setUserAgentStylesheet( new PrimerLight().getUserAgentStylesheet() );

		this.stage.show();

		this.stage.setOnCloseRequest( new EventHandler<WindowEvent>() {
			@Override
			public void handle( WindowEvent e ) {
				Platform.exit();
				System.exit( 0 );
			}
		});

		this.jbus = new JBus();
		this.jbus.registerWeak( this );

		this.businessLogic = new BusinessLogic( this.jbus );

		try {
			BasicServer basicServer = new BasicServer( SERVER_PORT, this.jbus );
			basicServer.start();
			logger.info( "Server started" );
		}
		catch (Exception e) {
			logger.error( "Cannot start server, that functionality will not be available" );
			logger.catching( e );
		}
	}

	protected void onClickExit() {
		Platform.exit();
		System.exit( 0 );
	}

	protected void onClickCreateProfile() {
		setStatus( "" );

		GridPane grid = new GridPane();

		int row = 0;
		int colSpan = 2;
		int rowSpan = 1;

		grid.setAlignment( Pos.CENTER );
		grid.setHgap( 10 );
		grid.setVgap( 10 );
		grid.setPadding( new Insets( 25, 25, 25, 25 ) );
		Text title = new Text( "Create profile" );
		title.setFont( Font.font( "Tahoma", FontWeight.NORMAL, 20 ) );
		grid.add( title, 0, row++, 2, 1 );

		grid.add( new Label( "Title:" ), 0, row );
		TextField titleTextField = new TextField();
		grid.add( titleTextField, 1, row++, colSpan, rowSpan );

		grid.add( new Label( "Username:" ), 0, row );
		TextField usernameTextField = new TextField();
		grid.add( usernameTextField, 1, row++, colSpan, rowSpan );

		grid.add( new Label( "Password:" ), 0, row );
		TextField passwordTextField = new PasswordField();
		grid.add( passwordTextField, 1, row++, colSpan, rowSpan );
		passwordTextField.setPromptText( "This is not saved to the database." );

		grid.add( new Label( "URI:" ), 0, row );
		TextField uriTextField = new TextField();
		grid.add( uriTextField, 1, row++, colSpan, rowSpan );

		grid.add( new Label( "Directory:" ), 0, row );
		TextField directoryTextField = new TextField();
		grid.add( directoryTextField, 1, row++, colSpan, rowSpan );

		Button btnCancel = new Button( "Cancel" );
		Button btnCreate = new Button( "Create" );
		HBox hbBtn = new HBox( 10 );
		hbBtn.setAlignment( Pos.BOTTOM_RIGHT );
		hbBtn.getChildren().add( btnCancel );
		hbBtn.getChildren().add( btnCreate );
		grid.add( hbBtn, 1, row++ );

		final Text actiontarget = new Text();
		grid.add( actiontarget, 1, row++ );

		btnCancel.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				clearContentPane();
			}
		});

		btnCreate.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				IOperationResult res = businessLogic.createProfile( titleTextField.getText(),
																	usernameTextField.getText(),
																	passwordTextField.getText(),
																	uriTextField.getText(),
																	directoryTextField.getText() );
				setStatus( "" + res );
			}
		});

		setContentPane( grid );
	}

	protected void onClickListProfiles() {
		setStatus( "" );

		TableView tableView = new TableView();
		tableView.setEditable( true );

		buildTable( tableView );

		IOperationResult res = businessLogic.getProfiles();
		if ( res.getType() != OperationResultType.SUCCESS ) {
			clearContentPane();
			setStatus( "" + res );
			return;
		}

		ObservableList<ISiteProfile> profileList = FXCollections.observableArrayList( profile -> new Observable[] { profile.getToBeDeletedProperty() } );
		profileList.addAll( (List<SiteProfile>) res.getData() );

		tableView.setItems( profileList );

		profileList.addListener((ListChangeListener.Change<? extends ISiteProfile> change) -> {});

		Button btnSave = new Button( "Save" );
		Button btnDelete = new Button( "Delete selected" );

		BorderPane bottom = new BorderPane();
		bottom.setLeft( btnDelete );
		bottom.setRight( btnSave );

		VBox inner = new VBox( tableView, bottom );
		setContentPane( inner );

		btnSave.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				IOperationResult saveResult = businessLogic.saveProfiles( profileList );
				if ( saveResult.getType() == OperationResultType.SUCCESS ) {
					profileList.clear();
					profileList.addAll( (List<SiteProfile>) saveResult.getData() );
				}
				setStatus( "" + saveResult );
				logger.info( "AFTER SAVE, res=" + saveResult + ", list=" + saveResult.getData() );
			}
		});

		btnDelete.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				IOperationResult deleteResult, loadResult;

				List<ISiteProfile> deleteList = new ArrayList<ISiteProfile>();
				for ( ISiteProfile profile : profileList ) {
					if ( profile.getToBeDeleted() ) {
						deleteList.add( profile );
					}
				}

				logger.info( "TO DELETE: " + deleteList );

				if ( deleteList.size() < 1 ) {
					setStatus( "No items selected" );
					return;
				}

				deleteResult = businessLogic.deleteProfiles( deleteList );
				String msg = "" + deleteResult;

				loadResult = businessLogic.getProfiles();
				if ( loadResult.getType() != OperationResultType.SUCCESS ) {
					clearContentPane();
					msg = "PLEASE RESTART THE APP: DELETE: " + msg + ", LOAD: " + loadResult;
					logger.info( msg );
					setStatus( msg );
					return;
				}

				profileList.clear();
				profileList.addAll( (List<SiteProfile>) loadResult.getData() );
				setStatus( msg );
			}
		});
	}

	protected void onClickComposerInstall() {
		IOperationResult res;
		int row = 0;
		int colSpan = 2;
		int rowSpan = 1;

		var moduleAutocompleteLastSelected = new Object(){ IInstallable value = null; };

		res = businessLogic.getProfiles();
		if ( res.getType() != OperationResultType.SUCCESS ) {
			clearContentPane();
			setStatus( "" + res );
			return;
		}

		List<SiteProfile> profiles = (List<SiteProfile>) res.getData();
		if ( profiles.size() < 1 ) {
			clearContentPane();
			setStatus( "You need to create a profile first" );
			return;
		}

		GridPane grid = new GridPane();
		grid.setAlignment( Pos.CENTER );
		grid.setHgap( 10 );
		grid.setVgap( 10 );
		grid.setPadding( new Insets( 25, 25, 25, 25 ) );
		Text title = new Text( "Install a module" );
		title.setFont( Font.font( "Tahoma", FontWeight.NORMAL, 20 ) );
		grid.add( title, 0, row++, 2, 1 );

		ObservableList<MenuOption> profileList = profilesToMenuOptions( profiles );

		final ChoiceBox<MenuOption> profileChoiceBox = new ChoiceBox<>( profileList );
		profileChoiceBox.getSelectionModel().select( 0 );

		grid.add( new Label( "Profile:" ), 0, row );
		grid.add( profileChoiceBox, 1, row++, colSpan, rowSpan );

		grid.add( new Label( "Module name:" ), 0, row );
		TextField moduleAutocomplete = new TextField();
		grid.add( moduleAutocomplete, 1, row++, colSpan, rowSpan );

		grid.add( new Label( "Password:" ), 0, row );
		TextField passwordTextField = new PasswordField();
		grid.add( passwordTextField, 1, row++, colSpan, rowSpan );
		passwordTextField.setPromptText( "This is not saved to the database." );
		fillOutPassword( profiles, profileChoiceBox.getSelectionModel().getSelectedItem(), passwordTextField );

		AutoCompletionBinding<IInstallable> binding = TextFields.bindAutoCompletion( moduleAutocomplete, input -> {
			if ( input.getUserText().length() < 3 ) {
				return Collections.emptyList();
			}

			String match = input.getUserText().toLowerCase();

			MenuOption selected = profileChoiceBox.getSelectionModel().getSelectedItem();
			List<IInstallable> installables = profileIdToInstallables( selected != null ? selected.getId() : 0 );

			return installables.stream().filter( installable -> installable.isMatchFor( match ) ).collect( Collectors.toList() );
		});

		profileChoiceBox.setOnAction( event -> {
			moduleAutocomplete.clear();
			fillOutPassword( profiles, profileChoiceBox.getSelectionModel().getSelectedItem(), passwordTextField );
		});

		Button btnCancel = new Button( "Cancel" );
		Button btnCreate = new Button( "Install" );
		HBox hbBtn = new HBox( 10 );
		hbBtn.setAlignment( Pos.BOTTOM_RIGHT );
		hbBtn.getChildren().add( btnCancel );
		hbBtn.getChildren().add( btnCreate );
		grid.add( hbBtn, 1, 5 );

		final Text actiontarget = new Text();
		grid.add(actiontarget, 1, 6);

		btnCancel.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				clearContentPane();
			}
		});

		btnCreate.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				handleInstallationEvent( Arrays.asList( moduleAutocompleteLastSelected.value ), passwordTextField.getText(), profileChoiceBox.getSelectionModel().getSelectedItem() );
			}
		});

		binding.setOnAutoCompleted( e -> { moduleAutocompleteLastSelected.value = e.getCompletion(); } );

		setContentPane( grid );
	}

	protected void onClickComposerInstall( List<IInstallable> installables ) {
		IOperationResult res;
		int row = 0;
		int colSpan = 2;
		int rowSpan = 1;

		IInstallable installable = installables.get( 0 );

		res = businessLogic.getProfiles();
		if ( res.getType() != OperationResultType.SUCCESS ) {
			clearContentPane();
			setStatus( "" + res );
			return;
		}

		List<SiteProfile> profiles = (List<SiteProfile>) res.getData();
		if ( profiles.size() < 1 ) {
			clearContentPane();
			setStatus( "You need to create a profile first" );
			return;
		}

		GridPane grid = new GridPane();
		grid.setAlignment( Pos.CENTER );
		grid.setHgap( 10 );
		grid.setVgap( 10 );
		grid.setPadding( new Insets( 25, 25, 25, 25 ) );
		Text title = new Text( "Install a module" );
		title.setFont( Font.font( "Tahoma", FontWeight.NORMAL, 20 ) );
		grid.add( title, 0, row++, 2, 1 );

		ObservableList<MenuOption> profileList = profilesToMenuOptions( profiles );

		final ChoiceBox<MenuOption> profileChoiceBox = new ChoiceBox<>( profileList );
		profileChoiceBox.getSelectionModel().select( 0 );

		grid.add( new Label( "Profile:" ), 0, row );
		grid.add( profileChoiceBox, 1, row++, colSpan, rowSpan );

		grid.add( new Label( "Module name:" ), 0, row );
		Text moduleTitle = new Text( installable.getTitle() );
		grid.add( moduleTitle, 1, row++, colSpan, rowSpan );

		grid.add( new Label( "Password:" ), 0, row );
		TextField passwordTextField = new PasswordField();
		grid.add( passwordTextField, 1, row++, colSpan, rowSpan );
		passwordTextField.setPromptText( "This is not saved to the database." );
		fillOutPassword( profiles, profileChoiceBox.getSelectionModel().getSelectedItem(), passwordTextField );

		profileChoiceBox.setOnAction( event -> {
			fillOutPassword( profiles, profileChoiceBox.getSelectionModel().getSelectedItem(), passwordTextField );
		});

		Button btnCancel = new Button( "Cancel" );
		Button btnCreate = new Button( "Install" );
		HBox hbBtn = new HBox( 10 );
		hbBtn.setAlignment( Pos.BOTTOM_RIGHT );
		hbBtn.getChildren().add( btnCancel );
		hbBtn.getChildren().add( btnCreate );
		grid.add( hbBtn, 1, 5 );

		btnCancel.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				clearContentPane();
			}
		});

		btnCreate.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle( ActionEvent e ) {
				handleInstallationEvent( installables, passwordTextField.getText(), profileChoiceBox.getSelectionModel().getSelectedItem() );
			}
		});

		setContentPane( grid );
	}

	protected void onClickSetup() {
		List<IInstallable> installables = new ArrayList<IInstallable>( 2 );

		for ( int majorVersion = 10; majorVersion <= 11; majorVersion++ ) {
			List<IInstallationInstruction> installationInstructions = new ArrayList<IInstallationInstruction>();
			installationInstructions.add( new InstallationInstruction( InstallationInstructionType.COMPOSER_NAMESPACE, "drupal/sheephole_helper:1.0.x-dev@dev" ) );

			IInstallable installable = new Installable( "Sheephole helper",
														"https://www.drupal.org/project/sheephole_helper",
														"sheephole_helper",
														"This Project Browser add-on lets you install Drupal modules the right way (using composer) without having to learn SSH or the command line. And without creating an unsafe configuration.",
														PlatformType.DRUPAL,
														new BasicInstallableVersion( new Semver( majorVersion + ".0.0" ) ),
														installationInstructions );
			installables.add( installable );
		}

		onClickComposerInstall( installables );
	}

	protected void onClickHelpAbout() {
		setStatus( "" );
		FlowPane pane = new FlowPane();

		Font bigFont = Font.font( "Tahoma", FontWeight.NORMAL, 20 );
		Font smallFont = Font.font( "Tahoma", FontWeight.NORMAL, 14 );

		pane.setAlignment( Pos.TOP_CENTER );
		pane.setHgap( 10 );
		pane.setVgap( 10 );
		pane.setPadding( new Insets( 25, 25, 25, 25 ) );

		Text title = new Text( "Sheephole" );
		title.setFont( bigFont );
		title.setTextAlignment( TextAlignment.CENTER );

		Text noun = new Text( " (noun)" );
		noun.setFont( smallFont );

		Hyperlink siteLink = new Hyperlink( "WisdomTree.dev" );
		siteLink.setFont( smallFont );
		siteLink.setOnAction( e -> openBrowser( "https://wisdomtree.dev/" ) );

		Text def1 = new Text( " - a small, boulder-strewn mountain range near Joshua Tree.\n\n" );
		def1.setFont( smallFont );

		Text def2 = new Text( " - an application to install Drupal modules the right way (using composer) without having to use the command line.\n" );
		def2.setFont( smallFont );

		Text author = new Text( "Author: Chris Kelly of " );
		author.setFont( smallFont );

		Text donateText = new Text( "Do you find this software useful? If so, " );
		donateText.setFont( smallFont );

		Hyperlink donateLink = new Hyperlink( "please donate generously." );
		donateLink.setFont( smallFont );
		siteLink.setOnAction( e -> openBrowser( "https://www.paypal.com/donate/?hosted_button_id=4U3VYC5LNWRM4" ) );

		TextFlow flow = new TextFlow(
			title, noun,
			new Text( "\n\n" ),
			def1,
			def2,
			new Text( "\n\n" ),
			author, siteLink,
			new Text( "\n\n" ),
			donateText, donateLink
		);

		flow.setPrefWidth( 400 );

		pane.getChildren().add( flow );

		setContentPane( pane );
	}

	protected void handleInstallationEvent( List<IInstallable> installables, String password, MenuOption selected ) {
		IOperationResult res;

		if ( installables == null || installables.isEmpty() ) {
			setStatus( "No item selected" );
			return;
		}

		if ( password == null || password.length() < 1 ) {
			setStatus( "No password provided" );
			return;
		}

		if ( selected == null ) {
			setStatus( "No profile selected" );
			return;
		}

		res = businessLogic.loadProfileById( selected.getId() );
		if ( res.getType() != OperationResultType.SUCCESS ) {
			setStatus( "No such profile found" );
			return;
		}

		SiteProfile profile = (SiteProfile) res.getData();
		profile.setPassword( password );

		IInstallable matchingInstallable = null;
		for ( IInstallable installable : installables ) {
			if ( installable.getInstallableVersion().isCompatible( profile.getVersion() ) ) {
				matchingInstallable = installable;
				break;
			}
		}

		if ( matchingInstallable == null ) {
			setStatus( "No installation candidate found" );
			return;
		}

		res = businessLogic.installInstallable( matchingInstallable, profile, password );

		setStatus( "" + res );
	}

	protected void fillOutPassword( List<SiteProfile> profiles, MenuOption selected, TextField textField ) {
		for ( ISiteProfile profile : profiles ) {
			String pwd = profile.getPassword();
			if ( profile.getId() == selected.getId() ) {
				String password = profile.getPassword();
				if ( password != null && password.length() > 0 ) {
					textField.setText( password );
				}
			}
		}
	}

	protected MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();

		Menu menuFile = new Menu( "File" );
		menuBar.getMenus().add( menuFile );

		MenuItem menuItemExit = new MenuItem( "Exit" );
		menuItemExit.setOnAction( e -> onClickExit() );
		menuFile.getItems().add( menuItemExit );


		Menu menuProfile = new Menu( "Profile" );
		menuBar.getMenus().add( menuProfile );

		MenuItem menuItemCreateProfile = new MenuItem( "Create profile" );
		menuItemCreateProfile.setOnAction( e -> onClickCreateProfile() );
		menuProfile.getItems().add( menuItemCreateProfile );

		MenuItem menuItemListProfiles = new MenuItem( "List profiles" );
		menuItemListProfiles.setOnAction( e -> onClickListProfiles() );
		menuProfile.getItems().add( menuItemListProfiles );


		Menu menuCommands = new Menu( "Commands" );
		menuBar.getMenus().add( menuCommands );

		MenuItem menuItemComposer = new MenuItem( "Install module" );
		menuItemComposer.setOnAction( e -> onClickComposerInstall() );
		menuCommands.getItems().add( menuItemComposer );

		MenuItem menuItemSetup = new MenuItem( "Setup" );
		menuItemSetup.setOnAction( e -> onClickSetup() );
		menuCommands.getItems().add( menuItemSetup );


		Menu menuHelp = new Menu( "Help" );
		menuBar.getMenus().add( menuHelp );

		MenuItem menuItemAbout = new MenuItem( "About" );
		menuItemAbout.setOnAction( e -> onClickHelpAbout() );
		menuHelp.getItems().add( menuItemAbout );

		return menuBar;
	}

	protected void buildTable( TableView tableView ) {
		TableColumn<SiteProfile, Boolean> col0 = new TableColumn<>( "Delete?" );
		col0.setEditable( true );
		col0.setCellFactory( CheckBoxTableCell.<SiteProfile>forTableColumn( col0 ) );
		col0.setOnEditCommit( e -> { e.getRowValue().setToBeDeleted( e.getNewValue() ); } );
		tableView.getColumns().add( col0 );

		col0.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<SiteProfile,Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call( TableColumn.CellDataFeatures<SiteProfile, Boolean> cellData ) {
				return cellData.getValue().getToBeDeletedProperty();
			}
		});

		TableColumn<SiteProfile, String> col1 = new TableColumn<>( "Title" );
		col1.setEditable( true );
		col1.setCellValueFactory( new PropertyValueFactory<>( "title" ) );
		col1.setCellFactory( TextFieldTableCell.<SiteProfile>forTableColumn() );
		col1.setOnEditCommit( e -> e.getRowValue().setTitle( e.getNewValue() ) );
		tableView.getColumns().add( col1 );

		TableColumn<SiteProfile, String> col2 = new TableColumn<>( "Username" );
		col2.setEditable( true );
		col2.setCellValueFactory( new PropertyValueFactory<>( "userName" ) );
		col2.setCellFactory( TextFieldTableCell.<SiteProfile>forTableColumn() );
		col2.setOnEditCommit( e -> e.getRowValue().setUserName( e.getNewValue() ) );
		tableView.getColumns().add( col2 );

		TableColumn<SiteProfile, String> col3 = new TableColumn<>( "URI" );
		col3.setEditable( true );
		col3.setCellValueFactory( new PropertyValueFactory<>( "uri" ) );
		col3.setCellFactory( TextFieldTableCell.<SiteProfile>forTableColumn() );
		col3.setOnEditCommit( e -> e.getRowValue().setUri( e.getNewValue() ) );
		tableView.getColumns().add( col3 );

		TableColumn<SiteProfile, String> col4 = new TableColumn<>( "Directory" );
		col4.setEditable( true );
		col4.setCellValueFactory( new PropertyValueFactory<>( "directory" ) );
		col4.setCellFactory( TextFieldTableCell.<SiteProfile>forTableColumn() );
		col4.setOnEditCommit( e -> e.getRowValue().setDirectory( e.getNewValue() ) );
		tableView.getColumns().add( col4 );

		TableColumn<SiteProfile, String> col5 = new TableColumn<>( "Version" );
		col5.setEditable( false );
		col5.setCellValueFactory( new PropertyValueFactory<>( "platformVersion" ) );
		col5.setCellFactory( TextFieldTableCell.<SiteProfile>forTableColumn() );
		col5.setOnEditCommit( e -> e.getRowValue().setDirectory( e.getNewValue() ) );
		tableView.getColumns().add( col5 );

		col5.setCellValueFactory( new Callback<TableColumn.CellDataFeatures<SiteProfile, String>, ObservableValue<String>>() {
			public ObservableValue<String> call( TableColumn.CellDataFeatures<SiteProfile, String> p ) {
				return new ReadOnlyObjectWrapper( "" + p.getValue().getVersion() );
			}
		});
	}

	protected void setStatus( String s ) {
		statusBar.setText( s );
	}

	protected void setContentPane( Node node ) {
		mainPane.setCenter( node );
		stage.sizeToScene();
	}

	protected void clearContentPane() {
		mainPane.setCenter( new VBox() );
	}

	protected void openBrowser( String url ) {
		if ( Desktop.isDesktopSupported() ) {
			new Thread( () -> {
				try {
					Desktop.getDesktop().browse( new URI( url ) );
				}
				catch ( Exception e ) {
					logger.info( "Trying to open " + url + ", caught " + e.getMessage() );
					setStatus( "Please type " + url + " into your browser" );
				}
			}).start();
		}
	}

	protected ObservableList<MenuOption> profilesToMenuOptions( List<SiteProfile> profiles ) {
		ObservableList<MenuOption> ret = FXCollections.observableArrayList();
		for ( SiteProfile profile : profiles ) {
			ret.add( new MenuOption( profile.getId(), profile.getTitle() ) );
		}
		return ret;
	}

	protected List<IInstallable> profileIdToInstallables( long id ) {
		IOperationResult res = businessLogic.loadProfileById( id );
		if ( res.getType() != OperationResultType.SUCCESS ) {
			return Collections.emptyList();
		}

		SiteProfile profile = (SiteProfile) res.getData();

		return businessLogic.getInstallables( profile.getPlatformType(), profile.getVersion() );
	}

	@Subscribe
	private void listen( InstallInstallablesEvent event ) {
		Platform.runLater( () -> onClickComposerInstall( event.getInstallables() ) );
	}

	public Start() throws Exception {
		this.loggingTextArea = new TextArea();
		TextAreaLogAppender.setTextArea( this.loggingTextArea );
	}

	public static void main( String[] args ) {
		//System.setProperty( "log4j2.debug", "true" );
		launch( args );
	}

	private static class MenuOption {
		private final long id;
		private final String title;

		public MenuOption( long id, String title ) {
			this.id = id;
			this.title = title;
		}

		public long getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public boolean isMatchFor( String substring ) {
			return title.toLowerCase().contains( substring );
		}

		@Override
		public String toString() {
			return title;
		}
	}
}
