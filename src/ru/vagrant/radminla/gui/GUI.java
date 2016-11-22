package ru.vagrant.radminla.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.vagrant.radminla.core.Core;
import ru.vagrant.radminla.util.Const;
import ru.vagrant.radminla.util.OptionWriter;
import ru.vagrant.radminla.util.Res;

public class GUI extends Application {

/*
 ______ _____ _____ _    ______  _____ 
|  ___|_   _|  ___| |   |  _  \/  ___|
| |_    | | | |__ | |   | | | |\ `--. 
|  _|   | | |  __|| |   | | | | `--. \
| |    _| |_| |___| |___| |/ / /\__/ /
\_|    \___/\____/\_____/___/  \____/ 
 */
	
	private static Stage stage;
	private static VBox alltogetherContainer;
	private static HBox statusBar;
	private static HBox overlayContainer; 
	private static MainWindow mainContainer;
	
	private static ProgressBar progress;
	private static Label status;
	
/*
 _____ _____ _   _  _____ ___________ _   _ _____ _____ ___________ 
/  __ \  _  | \ | |/  ___|_   _| ___ \ | | /  __ \_   _|  _  | ___ \
| /  \/ | | |  \| |\ `--.  | | | |_/ / | | | /  \/ | | | | | | |_/ /
| |   | | | | . ` | `--. \ | | |    /| | | | |     | | | | | |    / 
| \__/\ \_/ / |\  |/\__/ / | | | |\ \| |_| | \__/\ | | \ \_/ / |\ \ 
 \____/\___/\_| \_/\____/  \_/ \_| \_|\___/ \____/ \_/  \___/\_| \_|
 */	

	@Override
	public void start(Stage stage) throws Exception {
		GUI.stage = stage;
		addGUI();
		finish();
		OptionWriter.load();
		Core.init();
	}
	
		private void addGUI() {
			mainContainer = new MainWindow();
			initOverlay();
			initStatus();
			mainContainer.minHeightProperty().bind(alltogetherContainer.heightProperty().subtract(overlayContainer.heightProperty()));
			mainContainer.maxHeightProperty().bind(alltogetherContainer.heightProperty().subtract(overlayContainer.heightProperty()));
		}
		
		private void finish() {
			stage.setScene(alltogetherContainer.getScene());
			stage.setTitle(Const.title);
			stage.show();
			stage.setMinWidth(750); stage.setMinHeight(400);		
			stage.setWidth(stage.getMinWidth()); stage.setHeight(stage.getMinHeight());
			stage.centerOnScreen();
		}

/*
______  ___   _   _  _____ _____ 
| ___ \/ _ \ | \ | ||  ___/  ___|
| |_/ / /_\ \|  \| || |__ \ `--. 
|  __/|  _  || . ` ||  __| `--. \
| |   | | | || |\  || |___/\__/ /
\_|   \_| |_/\_| \_/\____/\____/ 
*/

/*
Altogether (VBox)
	-> Any
		-> Template (StackPane)
		OR
		-> Main (VBox)
	-> Overlay (HBox)
		-> Status (HBox)
		-> Language button (ChoiceBox)
 */
		
		private void initOverlay() {
			/* Add progressbar */
			progress = new ProgressBar();
			progress.setId("#progress");
			progress.setPrefHeight(Double.MAX_VALUE);
			
			/* Add language menu */
			ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.observableArrayList(Res.getAvailLangs()));
			cb.getSelectionModel().select(0);
			cb.valueProperty().addListener((obs, oldS, newS)->Res.setLang(newS));

			/* Init overlay containers */
			statusBar = new HBox(5);
			overlayContainer = new HBox();
			alltogetherContainer = new VBox();
			new Scene(alltogetherContainer);
			alltogetherContainer.getChildren().addAll(mainContainer, overlayContainer);
			
			/* Customize containers */
			statusBar.setEffect(null);
			statusBar.setEffect(null);
			overlayContainer.getChildren().addAll(statusBar, cb);
			alltogetherContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(153,218,255,0.1) 0%, rgba(1,125,98,0.18) 26%, rgba(64,119,128,0.24) 66%, rgba(70,104,128,0.3) 100%);");
			overlayContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(70,104,128,0.18) 0%, rgba(64,119,128,0.1) 26%, rgba(1,125,98,0.07) 66%, rgba(153,218,255,0.03) 100%);");
			
			/* Bind size */
			overlayContainer.prefHeightProperty().bind(cb.heightProperty());
			statusBar.prefWidthProperty().bind(overlayContainer.widthProperty().subtract(cb.widthProperty()));
		}

		private void initStatus() {
			Label l0 = new Label();
			l0.setId("status");
			Res.bind(l0, "s0");
			l0.setStyle("-fx-font-size: "+Const.size+";");
			status = new Label();
			status.setStyle(l0.getStyle());
			setStatus("s6");
			statusBar.getChildren().addAll(l0, status);
		}
		
		public static void setStatus(String s) {
			status.textProperty().unbind(); 
			Res.bind(status, s);
		}
		
		public static void toggleProgressBar(SimpleDoubleProperty d, SimpleStringProperty s) {
			progress.progressProperty().unbind();
			progress.progressProperty().bind(d);
			status.textProperty().unbind();
			status.textProperty().bind(s);
			if (!statusBar.getChildren().contains(progress))
				statusBar.getChildren().add(1, progress);
		}
		
		public static void toggleProgressBar() {
			Platform.runLater(()->statusBar.getChildren().remove(progress));
		}
		
		public static void setMainGUI() { 
			mainContainer = new MainWindow();
			mainContainer.minHeightProperty().bind(alltogetherContainer.heightProperty().subtract(overlayContainer.heightProperty()));
			mainContainer.maxHeightProperty().bind(alltogetherContainer.heightProperty().subtract(overlayContainer.heightProperty()));
			alltogetherContainer.getChildren().add(0, mainContainer);	
		}
		
/*
  ___  ______ _____ 
 / _ \ | ___ \_   _|
/ /_\ \| |_/ / | |  
|  _  ||  __/  | |  
| | | || |    _| |_ 
\_| |_/\_|    \___/ 
 */
		
		public static void addEvent(String time, String name, String host, int state, boolean b) {
			mainContainer.addEvent(time, name, host, state, b);
		}
		
		/* Adding to Application task queue */
		public static void finishLoad() {
			Platform.runLater(()->{
				setStatus("s4");
				mainContainer.render();
				setStatus("s3");	
			});
		}
		
		public static void setReplace(String s0, String s1) { mainContainer.setReplace(s0, s1); }
		
		public static Stage getStage() { return stage; }
				
}