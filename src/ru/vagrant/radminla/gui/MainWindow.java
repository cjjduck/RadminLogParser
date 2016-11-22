package ru.vagrant.radminla.gui;

import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.util.Callback;
import javafx.util.Duration;
import ru.vagrant.radminla.core.Core;
import ru.vagrant.radminla.util.Const;
import ru.vagrant.radminla.util.OptionWriter;
import ru.vagrant.radminla.util.Res;

public class MainWindow extends VBox {
	
/*
 ______ _____ _____ _    ______  _____ 
|  ___|_   _|  ___| |   |  _  \/  ___|
| |_    | | | |__ | |   | | | |\ `--. 
|  _|   | | |  __|| |   | | | | `--. \
| |    _| |_| |___| |___| |/ / /\__/ /
\_|    \___/\____/\_____/___/  \____/ 
 */
	
	/* Event entry example: [time>user(w/o >index or <S if server)>status>host name] */
	private ArrayList<ArrayList<String>> eventData;
	/* User entry example: [host<S] or [user>userData.get() index]*/
	private ArrayList<ArrayList<String>> userData;
	
	private Button acceptButton, declineButton;
	private ImageView editButton;
	private TextField editField;
	private Popup popup;
	private DataListCell selectedCell;
	
	private String filterString;
	private String[] filterStatus;
	private String filterDateFrom;
	private String filterDateTo;
	private ArrayList<ArrayList<String>> filterImpossibleUsers;
	private ArrayList<String> filterHiddenHost;
	private ArrayList<String> filterToggled;
	private ArrayList<String> filterToggled_;
	private HashMap<String, String> filterReplace;
	
	private SimpleIntegerProperty users, events;
	
	private double trackDivideTimer;
	private Timeline checkTimer, divideTimer;
	
	private ListView<String> userlist; 
	private ListView<String> eventlist; 
	
	private Insets padded, standart;
	
/*
 _____ _____ _   _  _____ ___________ _   _ _____ _____ ___________ 
/  __ \  _  | \ | |/  ___|_   _| ___ \ | | /  __ \_   _|  _  | ___ \
| /  \/ | | |  \| |\ `--.  | | | |_/ / | | | /  \/ | | | | | | |_/ /
| |   | | | | . ` | `--. \ | | |    /| | | | |     | | | | | |    / 
| \__/\ \_/ / |\  |/\__/ / | | | |\ \| |_| | \__/\ | | \ \_/ / |\ \ 
 \____/\___/\_| \_/\____/  \_/ \_| \_|\___/ \____/ \_/  \___/\_| \_|
 */
	
	public MainWindow() {
		/* Init paddings presets and stuff */
		super(5);
		checkTimer = new Timeline(new KeyFrame(Duration.seconds(30), e->Core.update()));
		checkTimer.setCycleCount(Animation.INDEFINITE);
		padded = new Insets(0,0,0,21);
		standart = new Insets(0,0,0,0);
		/* Init data lists */
		eventData = new ArrayList<>();
		userData = new ArrayList<>();
		/* Init filter fields*/
		filterString = "";
		filterDateTo = "";
		filterDateFrom = "";
		filterStatus = new String[2];
		filterStatus[0] = "S0123456789";
		filterImpossibleUsers = new ArrayList<>();
		filterHiddenHost = new ArrayList<>();
		filterToggled = new ArrayList<>();
		filterToggled_ = new ArrayList<>();
		filterReplace = new HashMap<>();
		/* Init ListViews and drag events */
		initUserlist();
		initEventList();
		/* Init edit engine */
		initEditPopup();
		/* Init properties */
		users = new SimpleIntegerProperty(0);
		events = new SimpleIntegerProperty(0);
		/* [finishing] Init fillings */
		getChildren().add(setBar());
		getChildren().add(setPane()); /* Do not combine those */
	}
	
		private void initUserlist() {
			userlist = new ListView<>();
			userlist.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
				@Override
				public ListCell<String> call(ListView<String> list) {
					DataListCell dlc = new DataListCell();
					dlc.setSelectedStateCallback(new Callback<String, ObservableValue<Boolean>>() {
						@Override
						public ObservableValue<Boolean> call(String param) {
							BooleanProperty obs = new SimpleBooleanProperty();
							if (filterToggled.contains(param)) obs.set(true);
							obs.addListener((o, oldS, newS)->{});
							return obs;
						}
					});
					return dlc;
				}
			});
			userlist.getSelectionModel().selectedItemProperty().addListener((o, oldS, newS)->showFilteredEvents());
		}
				
		private void initEventList() {
			eventlist = new ListView<>();
			eventlist.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
				@Override 
				public ListCell<String> call(ListView<String> list) {
					return new EventListCell();
				}
			});
		}
		
/*		
  ___________ _____ _____  ______ ___________ _   _______ 
 |  ___|  _  \_   _|_   _| | ___ \  _  | ___ \ | | | ___ \
 | |__ | | | | | |   | |   | |_/ / | | | |_/ / | | | |_/ /
 |  __|| | | | | |   | |   |  __/| | | |  __/| | | |  __/ 
 | |___| |/ / _| |_  | |   | |   \ \_/ / |   | |_| | |    
 \____/|___/  \___/  \_/   \_|    \___/\_|    \___/\_|    
*/	                                                          		

		private void initEditPopup() {
			setButtons();
			setEditField();
			HBox hb = new HBox();
			hb.getChildren().addAll(editField, acceptButton, declineButton);
			popup = new Popup();
			popup.getContent().add(hb);
			editButton = new ImageView(Const.edit);
			setEvents();
		}
	
			private void setButtons() {
				acceptButton = new Button("☑");
				acceptButton.setStyle("-fx-base: #80ff88;");
				acceptButton.setOnAction(e->apply());
				declineButton = new Button("☒");
				declineButton.setStyle("-fx-base: #f46e6e;");
				declineButton.setOnAction(e->popup.hide());
			}
		
			private void setEditField() {
				editField = new TextField();
			}
			
			private void setEvents() {
				GUI.getStage().xProperty().addListener(e->popup.setX(editButton.localToScene(0d, 0d).getX()+GUI.getStage().getX()));
				GUI.getStage().yProperty().addListener(e->popup.setY(editButton.localToScene(0d, 0d).getY()+GUI.getStage().getY()));
				editButton.xProperty().addListener(e->popup.setX(editButton.localToScene(0d, 0d).getX()+GUI.getStage().getX()));
				editButton.yProperty().addListener(e->popup.setY(editButton.localToScene(0d, 0d).getY()+GUI.getStage().getY()));
				editButton.setOnMouseClicked(e->{
					if (popup.isShowing()) popup.hide();
					else showPopup();
				});
				GUI.getStage().focusedProperty().addListener(e->popup.hide());
				userlist.getSelectionModel().selectedItemProperty().addListener(e->popup.hide());
			}
			
			private void showPopup() {
				popup.setX(editButton.localToScene(0d, 0d).getX()+GUI.getStage().getX());
				popup.setY(editButton.localToScene(0d, 0d).getY()+GUI.getStage().getY());
				popup.show(GUI.getStage());
				String s = userlist.getSelectionModel().getSelectedItem();
				if (s.endsWith("<S"))
					s.replace("<S", "");
				else
					s = s.substring(0, s.indexOf(">"));
				editField.setText(s);
				editField.selectAll();
				editField.requestFocus();
			}
		
			private void apply() {
				if (!editField.getText().isEmpty()) {
					filterReplace.put(userlist.getSelectionModel().getSelectedItem(), editField.getText());
					OptionWriter.write(userlist.getSelectionModel().getSelectedItem()+"="+editField.getText());
					if (selectedCell != null)
						selectedCell.setText(editField.getText());
				}
				popup.hide();
			}
			
			void setReplace(String key, String value) { filterReplace.put(key, value); }
	
/*
______ _____ _      _      ___________ 
|  ___|_   _| |    | |    |  ___| ___ \
| |_    | | | |    | |    | |__ | |_/ /
|  _|   | | | |    | |    |  __||    / 
| |    _| |_| |____| |____| |___| |\ \ 
\_|    \___/\_____/\_____/\____/\_| \_|
 */

		private VBox setBar() {
			HBox bar0 = setTopBar();
			bar0.setBackground(Background.EMPTY);
			
			FlowPane bar1 = setToggleButtons();
			bar0.setBackground(Background.EMPTY);
			
			VBox vb = new VBox(5);
			vb.setBackground(Background.EMPTY);;
			vb.getChildren().addAll(bar0, bar1);
			return vb;
		}
		
			/*______  _______  ______
			  |_____] |_____| |_____/
			  |_____] |     | |    \_
			 */
		
			private HBox setTopBar() {
				HBox hb = new HBox(5);
				hb.getChildren().addAll(
						setSearchField(), 
						setDatePickerField(0), 
						setDatePickerField(1));
				((Region)hb.lookup("#search")).prefWidthProperty().bind(
						hb.widthProperty()
						.subtract(((Region)hb.lookup("#date0")).minWidthProperty())
						.subtract(((Region)hb.lookup("#date1")).minWidthProperty()));
				return hb;
			}
														
				private TextField setSearchField() {
					TextField tf = new TextField();
					tf.setId("search");
					tf.promptTextProperty().bind(Bindings.createStringBinding(() -> Res.get("c2"), Res.languageProperty()));
					tf.textProperty().addListener((o, oldS, newS)->{
						filterString = newS;
						showFilteredUsers();
					});
					return tf;
				}
				
				private DatePicker setDatePickerField(int i) {
					DatePicker dp = new DatePicker();
					dp.setId("date"+i);
					Res.bind(dp, "c"+i);
					dp.setMinWidth(100);
					dp.setOnAction((e)->{
						if (dp.getValue() == null) {
							if (i == 0) filterDateFrom = "";
							else 		filterDateTo = "";
						}
						else {
							String s = dp.getValue().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
							if (i == 0)	filterDateFrom = s;
							else		filterDateTo = s;
						}
						showFilteredEvents();
					});
					return dp;
				}
			
			private FlowPane setToggleButtons() {
				FlowPane fb = new FlowPane(3, 3);
				fb.getChildren().add(setToggleButton("S"));
				for (int i = 0; i <= 9; i++) fb.getChildren().add(setToggleButton(""+i));
				return fb;
			}
			
				private ToggleButton setToggleButton(String s) {
					ToggleButton tb = new ToggleButton();
					tb.setMaxHeight(10);
					tb.setSelected(true);
					tb.setFont(Font.font(null, (s.equals("0")?FontWeight.BOLD:FontWeight.NORMAL), -1));
					Res.bind(tb, "d"+s+(s.equals("8")?"s":""));
					tb.selectedProperty().addListener((o, oldS, newS)->{
						if (newS && !filterStatus[0].contains(s)) filterStatus[0] += s;
						else filterStatus[0] = filterStatus[0].replace(s, "");
						showFilteredEvents();
					});
					return tb;
				}
	
		/*_____  _______ __   _ _______
 		 |_____] |_____| | \  | |______
 		 |       |     | |  \_| |______
 		*/
				
		private SplitPane setPane() {
			SplitPane sp = new SplitPane();
			sp.setBackground(Background.EMPTY);
			sp.getItems().addAll(userlistPane(), eventlistPane(), trackPane());
			sp.setDividerPositions(0.28, 0.70);
			sp.prefHeightProperty().addListener((o, oldS, newS)->{Platform.runLater(() -> sp.getParent().requestLayout());});
			sp.prefHeightProperty().bind(heightProperty().subtract(((VBox)getChildren().get(0)).heightProperty()));
			userlist.prefHeightProperty().bind(sp.heightProperty()
				.subtract(((HBox)((VBox)sp.getItems().get(0)).getChildren().get(0)).heightProperty())
				.subtract(((Label)((VBox)sp.getItems().get(0)).getChildren().get(2)).heightProperty()));
			eventlist.prefHeightProperty().bind(sp.heightProperty()
					.subtract(((Label)((VBox)sp.getItems().get(1)).getChildren().get(1)).heightProperty()));
			return sp;
		}
		
			private VBox userlistPane() {
				Button sel = new Button("", new ImageView(Const.selectAll));
				sel.setTooltip(getTooltip("b0"));
				sel.setOnAction((e)->{
					filterToggled.clear();
					for (ArrayList<String> l : userData) {
						filterToggled.addAll(l);
					}
					redraw();
				});
				Button desel = new Button("", new ImageView(Const.deselectAll));
				desel.setTooltip(getTooltip("b1"));
				desel.setOnAction((e)->{
					filterToggled.clear();
					redraw();
				});
				Button selV = new Button("", new ImageView(Const.selectAllVisible));
				selV.setTooltip(getTooltip("b2"));
				selV.setOnAction((e)->{
					filterToggled.clear();
					filterToggled.addAll(userlist.getItems());
					filterToggled.remove(0);
					redraw();
				});
				Button deselV = new Button("", new ImageView(Const.deselectAllVisible));
				deselV.setTooltip(getTooltip("b3"));
				deselV.setOnAction((e)->{
					filterToggled.removeAll(userlist.getItems());
					redraw();
				});
				HBox hb = new HBox();
				hb.getChildren().addAll(sel, desel, selV, deselV);
				VBox vb = new VBox();
				vb.getChildren().addAll(hb, userlist, userinfoPane());
				return vb;
			}
				
				private Tooltip getTooltip(String s) {
					Tooltip t = new Tooltip();
					t.textProperty().bind(Bindings.createStringBinding(()->Res.get(s), Res.languageProperty()));
					return t;
				}
				
				private Label userinfoPane() {
					Label l = new Label();
					l.textProperty().bind(
						Bindings.createStringBinding(()->Res.get("c3"), Res.languageProperty())
						.concat(users)
						.concat(Bindings.createStringBinding(()->Res.get("c5"), Res.languageProperty())));
					return l;
				}
				
			private VBox eventlistPane() {
				VBox vb = new VBox();
				vb.getChildren().addAll(eventlist, eventinfoPane());
				return vb;
			}
			
			private Label eventinfoPane() {
				Label l = new Label();
				l.textProperty().bind(
					Bindings.createStringBinding(()->Res.get("c4"), Res.languageProperty())
					.concat(events)
					.concat(Bindings.createStringBinding(()->Res.get("c6"), Res.languageProperty())));
				return l;
			}

		/* _______  ______ _______ _______ _     _
    		  |    |_____/ |_____| |       |____/ 
    		  |    |    \_ |     | |_____  |    \_
    	*/
		
		private VBox trackPane() {
			TextArea ta = setTextArea();
			HBox hb0 = setBreakLogBar();
			Button b = setTrackButton(ta);
			VBox vb = new VBox();
			vb.getChildren().addAll(ta, hb0, b);
			ta.prefHeightProperty().bind(vb.heightProperty().subtract(b.heightProperty()).subtract(hb0.heightProperty()));
			return vb;
		}
					
			private TextArea setTextArea() {
				TextArea ta = new TextArea();
				ta.setEditable(false);
				ta.setWrapText(true);
				Res.bind(ta, "t6");
				return ta;
			}
			
			private HBox setBreakLogBar() {
				HBox hb = new HBox();
				Label l0 = new Label(), l1 = new Label();
				l0.setMinWidth(Region.USE_PREF_SIZE);
				l1.setMinWidth(Region.USE_PREF_SIZE);
				Res.bind(l0, "t12");
				Res.bind(l1, "t13");
				TextField tf = new TextField("0");
				tf.textProperty().addListener((e, oldS, newS)->{
					if (newS.length() > 10) {
						Toolkit.getDefaultToolkit().beep();
						tf.textProperty().set(oldS);
						return;
					}
					else if (newS.length() < 1) {
						tf.textProperty().set("0");
						return;
					}
					boolean b = false;
					for (char c : newS.toCharArray()) {
						if (c == '.' && !b) {
							b = true;
							continue;
						}
						if (Character.isDigit(c) || (c == '.' && !b))
							continue;
						Toolkit.getDefaultToolkit().beep();
						tf.textProperty().set(oldS);
						return;
					}
					try { trackDivideTimer = Double.parseDouble(newS); }
					catch (NumberFormatException e_) {
						Toolkit.getDefaultToolkit().beep();
						tf.textProperty().set(oldS);
					}
				});
				hb.getChildren().addAll(l0, tf, l1);
				tf.setTooltip(setBreakLogBarTooltip());
				l0.setTooltip(setBreakLogBarTooltip());
				l1.setTooltip(setBreakLogBarTooltip());
				tf.prefWidthProperty().bind(hb.widthProperty().subtract(l0.prefWidthProperty()).subtract(l1.prefWidthProperty()));
				return hb;
			}
			
				private Tooltip setBreakLogBarTooltip() {
					Tooltip t = new Tooltip();
					t.textProperty().bind(Bindings.createStringBinding(()->Res.get("t15"), Res.languageProperty()));
					return t;
				}
			
			private Button setTrackButton(TextArea ta) {
				Button b = new Button();
				b.setMinWidth(Button.USE_PREF_SIZE);
				b.setMaxWidth(Double.MAX_VALUE);
				Res.bind(b, "t2");
				b.setOnAction((e)->{
					ta.textProperty().unbind();
					if (b.getText().equals(Res.get("t2"))) {
						if (filterStatus[0].isEmpty()) {
							ta.setWrapText(true);
							Res.bind(ta, "t8");
							return;
						}
						else if (filterToggled.size() == 0) {
							ta.setWrapText(true);
							Res.bind(ta, "t9");
							return;
						}
						if (trackDivideTimer < 0.0028) {
							trackDivideTimer = 0;
						}
						filterStatus[1] = filterStatus[0];
						filterToggled_.clear();
						filterToggled_.addAll(filterToggled);
						ta.setWrapText(false);
						ta.textProperty().bind(bindSet());
						b.textProperty().unbind();
						Res.bind(b, "t5");
						checkTimer.play();
						if (trackDivideTimer > 0) {
							divideTimer = new Timeline(new KeyFrame(Duration.seconds((int)(trackDivideTimer*60*60)), e_->Core.assignFile()));
							divideTimer.play();
						}
					} else {
						checkTimer.stop();
						divideTimer.stop();
						ta.textProperty().unbind();
						ta.setWrapText(true);
						Res.bind(ta, "t6");
						Res.bind(b, "t2");
					}
				});
				return b;
			}
			
				private StringExpression bindSet() {
					/* save to */
					StringExpression se = new SimpleStringProperty("");
					/* save every n hrs */
					if (trackDivideTimer > 0)
						se = se.concat(propertyTemplateHours());
					/* status */
					se = se.concat(propertyTemplateEvents());
					normalizeStatus();
					if (filterStatus[0].equals("S0123456789")) {
						SimpleStringProperty state = new SimpleStringProperty();
						state.bind(Bindings.createStringBinding(()->Res.get("t7"), Res.languageProperty()));
						se = se.concat("\t").concat(state).concat("\n");
					}
					else {
						for (char c : filterStatus[0].toCharArray()) {
							SimpleStringProperty state = new SimpleStringProperty();
							state.bind(Bindings.createStringBinding(()->Res.get("d"+c+(c=='8'?"s":"")), Res.languageProperty()));
							se = se.concat("\t").concat(state).concat("\n");
						}
					}
					/* users: */
					se = se.concat(propertyTemplateUsers());
					/* users */
					String users = "";
					if (filterToggled.contains("All events")) {
						SimpleStringProperty ssp2 = new SimpleStringProperty();
						ssp2.bind(Bindings.createStringBinding(()->Res.get("l0"), Res.languageProperty()));
						se = se.concat("\n\t").concat(ssp2);
					}
					else {
						for (String s : filterToggled) {
							if (!s.contains(">"))
								users += "\n\t"+s;
							else
								users += "\n\t"+s.split(">")[0];
						}
						se = se.concat(users);
					}
					return se;
				}
				
					private SimpleStringProperty propertyTemplateHours() {
						SimpleStringProperty ssp = new SimpleStringProperty();
						ssp.bind(Bindings.createStringBinding(()->
							String.format(Res.get("t14"), new DecimalFormat("#.##").format(trackDivideTimer)), 
							Res.languageProperty()));
						return ssp;
					}
					
					private SimpleStringProperty propertyTemplateEvents() {
						SimpleStringProperty ssp = new SimpleStringProperty();
						ssp.bind(Bindings.createStringBinding(()->Res.get("t3"), Res.languageProperty()));
						return ssp;
					}
					
					private SimpleStringProperty propertyTemplateUsers() {
						SimpleStringProperty ssp = new SimpleStringProperty();
						ssp.bind(Bindings.createStringBinding(()->Res.get("t4"), Res.languageProperty()));
						return ssp;
					}
				
					private void normalizeStatus() {
						for (int i = 9; i >= 0; i--)
							if (filterStatus[0].contains(""+i))
								filterStatus[0] = i+filterStatus[0].replace(""+i, "");
						if (filterStatus[0].contains("S"))
							filterStatus[0] = "S"+filterStatus[0].replace("S", "");
					}
		
/*
 _____ _   _ _____     _   _   ___   _   _______ _      _____ 
|  ___| | | |_   _|   | | | | / _ \ | \ | |  _  \ |    |  ___|
| |__ | | | | | |     | |_| |/ /_\ \|  \| | | | | |    | |__  
|  __|| | | | | |     |  _  ||  _  || . ` | | | | |    |  __| 
| |___\ \_/ / | |  _  | | | || | | || |\  | |/ /| |____| |___ 
\____/ \___/  \_/ (_) \_| |_/\_| |_/\_| \_/___/ \_____/\____/ 
 */
				
	void addEvent(String time, String name, String host, int state, boolean b) {
		if (state == -1) return; /* No such state */
		int i = getIndexByHost(host);
		int j = getNode(name, i);
		if (j == -1) return;
		String s = time+">"+j+">"+(state==-2?"S":state)+">"+host;
		eventData.get(i).add(s);
		/* Log line */
		if (b && checkTimer.getStatus().equals(Status.RUNNING) && !name.equals(host) 
			&& (filterToggled_.contains(name) || filterToggled_.contains("All events")) 
			&& filterStatus[1].contains(""+state))
			Core.addLine("["+time+"] "+(filterReplace.containsKey(name)?filterReplace.get(name):name)+" "+
			(state<=7?Res.get("t10")+" ("+Res.get("d"+state)+")":(state==8?Res.get("d8"):Res.get("t11"))), 
			(filterReplace.containsKey(host)?filterReplace.get(host):host));
	}
	
		private int getIndexByHost(String host) {
			for (int i = 0; i < userData.size(); i++) {
				if (userData.get(i).size() > 0 && userData.get(i).get(0).equals(host+"<S"))
					return i;
			}
			userData.add(new ArrayList<String>(Arrays.asList(host+"<S")));
			eventData.add(new ArrayList<String>());
			return userData.size()-1;
		}
	
		private int getNode(String name, int index) {
			name = name.replace("(", "").replace(")", "");
			String[] s = name.split(" ");
			
			switch(s.length) {
			case 1: //ip
				for (int i = 0; i < userData.get(index).size(); i++)
					if (userData.get(index).get(i).contains(s[0]))
						return i;
				userData.get(index).add(s[0]+">"+index);
				return userData.size()-1;
			case 2: //dns (ip) OR ip (name)
				if (s[0].length() - s[0].replace(".", "").length() == 3 &&
					s[0].chars().allMatch(c->Character.isDigit((char)c) || c == 46/* dot */)) { //ip (name)
					for (int i = 0; i < userData.get(index).size(); i++) {
						if (userData.get(index).get(i).contains(s[0])) {
							if (!userData.get(index).get(i).contains(s[1]))
								userData.get(index).set(i, s[0]+" ("+s[1]+")>"+index);
							return i;
						}
					}
					userData.get(index).add(s[0]+" ("+s[1]+")>"+index);
				} else { //dns (ip)
					for (int i = 0; i < userData.get(index).size(); i++) {
						if (userData.get(index).get(i).contains(s[1])) return i; 
					}
					userData.get(index).add(s[1]+">"+index);
				}
				return userData.get(index).size()-1;
			case 3: //dns (ip) (name)
				for (int i = 0; i < userData.get(index).size(); i++) {
					if (userData.get(index).get(i).contains(s[1])) {
						if (!userData.get(index).get(i).contains(s[2]))
							userData.get(index).set(i, s[1]+" ("+s[2]+")>"+index);
						return i;
					}
				}
				userData.get(index).add(s[1]+" ("+s[2]+")>"+index);
				return userData.get(index).size()-1;
			}
			return -1;
		}
		
	void render() {
		for (int i = 0; i < eventData.size(); i++) {
			int size = eventData.get(i).size();
			for (int j = 0; j < size; j++) {
				String[] s = eventData.get(i).get(j).split(">");
				try { 
					int index = Integer.parseInt(s[1]);
					String[] host = userData.get(i).get(index).split(">");
					if (host.length != 2) host[0] = userData.get(i).get(index);
					eventData.get(i).set(j, 
							s[0]
							+">"+(host.length != 2?userData.get(i).get(index):host[0])
							+">"+s[2]
							+">"+s[3]);
				}
				catch (NumberFormatException e) { continue; }
			}
		}
		clearHiddenUsersFilter();
		showFilteredEvents();
	}
		
/*
______ _____ _    _____ ___________ 
|  ___|_   _| |  |_   _|  ___| ___ \
| |_    | | | |    | | | |__ | |_/ /
|  _|   | | | |    | | |  __||    / 
| |    _| |_| |____| | | |___| |\ \ 
\_|    \___/\_____/\_/ \____/\_| \_|
*/	

	private void redraw() {
		userlist.setItems(null);
		showFilteredUsers();
		showFilteredEvents();
	}
	
	private void showFilteredUsers() {
		ArrayList<String> filtered = new ArrayList<>();
		filtered.add("All events");
		for (int i = 0; i < userData.size(); i++) {
			if (filterHiddenHost.size() > 0 && filterHiddenHost.contains(userData.get(i).get(0))) { /* Host is hidden */
				filtered.add(userData.get(i).get(0)); /* But we're showing host name cell anyway */
				continue;
			}
			for (int j = 0; j < userData.get(i).size(); j++) {
				/* Always add server */
				if (j == 0) { filtered.add(userData.get(i).get(0)); continue; }
				/* Check if something is typed in the search field */
				if (!filterString.isEmpty() && !userData.get(i).get(j).split(">")[0].contains(filterString) 
					&& j != 0) 
					continue; /* Not in filter string */
				/* Check if user have events to show */
				else if (!filterImpossibleUsers.get(i).contains(userData.get(i).get(j).replace(">"+i, "")))
					continue;
				filtered.add(userData.get(i).get(j));
			}
		}
		users.set(filtered.size()-1-userData.size()); /* minus All events and hosts; they're showing 100% of time */
		userlist.setItems(FXCollections.observableArrayList(filtered));
	}
		
	private void showFilteredEvents() {
		boolean b = false;
		String selectedUser = userlist.getSelectionModel().getSelectedItem();
		if (filterToggled.contains("All events") 
			|| (selectedUser != null && selectedUser.equals("All events"))) 
			b = true; /* All events selected or checkboxed */
		ArrayList<String> filtered = new ArrayList<>();
		clearHiddenUsersFilter();
		for (int i = 0; i < eventData.size(); i++) {
			for (String s : eventData.get(i)) {
				String[] data = s.split(">");
				if (!filterStatus[0].contains(data[2])) continue;
				if (!filterDateFrom.isEmpty() && filterDateFrom.compareTo(data[0]) >= 0) continue;
				if (!filterDateTo.isEmpty() && filterDateTo.compareTo(data[0]) <= 0) continue;
				if (!filterImpossibleUsers.get(i).contains(data[1]))
					filterImpossibleUsers.get(i).add(data[1]);
				if (b) { filtered.add(s); continue; } /* Override selections and checkboxes if All events 
														is selected or checkboxed */
				if (filtered.contains(s)) continue; /* TODO check if it really necessary */
				if (selectedUser != null) { /* Something is selected */
					String[] user = selectedUser.split(">");
					if (user.length == 2) { /* NOT //Server */
						if (user[0].equals(data[1]) && user[1].equals(""+i)) {
							filtered.add(s);
							continue;
						}
					} 
					else if (selectedUser.equals(data[1])) { /* //Server */
						filtered.add(s);
						continue;
					}
				}
				if (filterToggled.size() > 0) { /* Something is checked */
					if (data[1].equals(data[3]+"<S")) { /* //Server */
						if (filterToggled.contains(data[3]+"<S")) filtered.add(s);
					}
					/* Not //Server */
					else if (filterToggled.contains(data[1]+">"+i))
						filtered.add(s);
				}
			}
		}
		events.set(filtered.size());
		Collections.sort(filtered);
		eventlist.setItems(FXCollections.observableArrayList(filtered));
		showFilteredUsers();
	}
	
		private void clearHiddenUsersFilter() {
			filterImpossibleUsers.clear();
			userData.forEach(e->filterImpossibleUsers.add(new ArrayList<String>()));
		}
		
	private void filter(String s) {
		if (filterToggled.contains(s)) filterToggled.remove(s);
		else if (!filterToggled.contains(s)) filterToggled.add(s); 
		showFilteredEvents();
	}
		
/*
 _____  _____ _      _      _____ 
/  __ \|  ___| |    | |    /  ___|
| /  \/| |__ | |    | |    \ `--. 
| |    |  __|| |    | |     `--. \
| \__/\| |___| |____| |____/\__/ /
 \____/\____/\_____/\_____/\____/ 
*/
	
	private class DataListCell extends CheckBoxListCell<String> {
		@Override
		public void updateSelected(boolean selected) {
			super.updateSelected(selected);
			if (getItem() == null || getItem().equals("All events")) return;
			if (selected) {
				selectedCell = this;
				if (getGraphic() instanceof HBox) {
					HBox hb = (HBox) getGraphic();
					hb.getChildren().add(editButton);
					setGraphic(hb);
				}
				else {
					HBox hb = new HBox();
					hb.getChildren().addAll(getGraphic(), editButton);
					setGraphic(hb);
				}
			}
			else
				((HBox)getGraphic()).getChildren().remove(editButton);
		}
		
		@Override
		public void updateItem(String item, boolean empty) {
			setFont(Font.getDefault()); 
			textProperty().unbind(); 
			setText("");
			setPadding(standart);
			super.updateItem(item, (item==null));
			if (item != null) {
				((CheckBox)getGraphic()).setOnAction(null);
				if (filterToggled.contains(item))
					((CheckBox)getGraphic()).setSelected(true);
				((CheckBox)getGraphic()).setOnAction((e)->{
					filter(item);
				});
				if (item.endsWith("<S") || item.equals("All events")) {
					if (item.endsWith("<S")) {
						setText(item.replace("<S", ""));
						CheckBox cb = new CheckBox();
						cb.setIndeterminate(true);
						cb.setSelected(true);
						cb.setOnAction((e)->{
							for (ArrayList<String> l : userData) {
								if (l.get(0).equals(item)) {
									if (filterHiddenHost.contains(item))
										filterHiddenHost.remove(item);
									else
										filterHiddenHost.add(item);
									cb.setSelected(true);
									cb.setIndeterminate(true);
									showFilteredUsers();
									return;
								}
							}
						});
						HBox hb = new HBox();
						hb.getChildren().addAll(cb, getGraphic());
						setGraphic(hb);
					} else {
						SimpleStringProperty p = new SimpleStringProperty();
						p.bind(Bindings.createStringBinding(() -> Res.get("l0"), Res.languageProperty()));
						textProperty().bind(Bindings.concat(p));
					}
					setFont(Font.font(null, FontWeight.BOLD, Font.getDefault().getSize()));
				}
				else {
					setPadding(padded);
					setText(item.substring(0, item.indexOf(">")));
				}
				if (filterReplace.containsKey(item)) {
					setText(filterReplace.get(item));
				}
			}
		}
	}
	
	private class EventListCell extends ListCell<String> {
		@Override
		public void updateItem(String item, boolean empty) {
			if (item != null) {
				String[] s = item.split(">");
				String time = s[0]; 
				String host = s[3];
				String user = s[1]; 
				SimpleStringProperty state = new SimpleStringProperty();
				state.bind(Bindings.createStringBinding(() -> 
					Res.get("d"+
							(s[1].endsWith("<S")
								?"S"
								:s[2]+
									(s[2].equals("8")
										?"s"
										:""
									)
								)
							), 
							Res.languageProperty()));
				String user_;
				String suffix = getSuffix(host);
				if (filterReplace.containsKey(user+suffix))
					user_ = filterReplace.get(user+suffix);
				else user_ = user;
				if (filterReplace.containsKey(host+"<S"))
					host = filterReplace.get(host+"<S");
				if (user.endsWith("<S"))
					textProperty().bind(Bindings.concat("["+time+"] "+host.replace("<S", "")+": ", state));
				else
					textProperty().bind(Bindings.concat("["+time+"] "+host.replace("<S", "")+": ", user_, " -> ", state));
				empty = false;
			}
			else {
				textProperty().unbind();
				setText("");
				empty = true;
			}
			super.updateItem(item, empty);
		}
	}
	
		private String getSuffix(String host) {
			for (int i = 0; i < userData.size(); i++) {
				if (userData.get(i).get(0).equals(host+"<S"))
					return ">"+i;
			}
			return "";
		}
		
}
