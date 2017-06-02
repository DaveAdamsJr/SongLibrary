package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class Controller{
	@FXML private TextField addsongname, addartist, addalbum, addyear, editsongname, editartist, editalbum, edityear;
	@FXML private Button addbutton, addcxbutton, editbutton, editcxbutton, deletebutton;
	@FXML private ListView<String> lv;
	private ObservableList<String> ol = FXCollections.observableArrayList();
	private ArrayList<ArrayList<String>> database = new ArrayList<ArrayList<String>>();

	public void start(Stage primaryStage) {
		loadDBFromFile();
		refreshList();
		lv.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) ->	showSongInfo());
		if (ol.size()!=0) lv.getSelectionModel().select(0);
		addbutton.setOnAction((event) -> addSong());
		addcxbutton.setOnAction((event) -> cancelAdd());
		editbutton.setOnAction((event) -> editSongInfo());
		editcxbutton.setOnAction((event) -> showSongInfo());
		deletebutton.setOnAction((event) -> deleteSong());
	}

	private void loadDBFromFile() {
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader("db.data"));
			while ((line = br.readLine()) != null) database.add(new ArrayList<String>(Arrays.asList(line.split("\\|")).subList(0, 4)));
			br.close();
		} catch (FileNotFoundException e) { //no problem; file will be created at first save
		} catch (IOException e) { e.printStackTrace(); }
	}

	private void saveDBToFile() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("db.data", false));
			for(ArrayList<String> al : database) bw.write(al.get(0) + "|" + al.get(1) + "|" + al.get(2) + "|" + al.get(3) + System.lineSeparator());
			bw.close();
		} catch (IOException e) { e.printStackTrace(); }
	}

	private void refreshList() {
		ol.clear();
		for (int i = 0; i<database.size(); i++) ol.add(database.get(i).get(0));
		if (lv!=null) lv.setItems(ol);
	}

	private void showSongInfo() {
		if (lv.getSelectionModel().getSelectedIndex()==-1) return;
		ArrayList<String> song = database.get(lv.getSelectionModel().getSelectedIndex());
		editsongname.setText(song.get(0));
		editartist.setText(song.get(1));
		editalbum.setText(song.get(2));
		edityear.setText(song.get(3));
	}

	private void addSong() {
		int i = addSongtoDB(addsongname.getText().toLowerCase(), addartist.getText().toLowerCase(), addalbum.getText().toLowerCase(),
				addyear.getText().toLowerCase());
		cancelAdd();
		if (i==-1) {
			popupError("Duplicate Song", "A song with that name and artist already exists.");
			return;
		}
		refreshList();
		lv.getSelectionModel().clearAndSelect(i);
	}

	private void cancelAdd() {
		addsongname.setText("");
		addartist.setText("");
		addalbum.setText("");
		addyear.setText("");
	}

	private void editSongInfo() {
		if (lv.getSelectionModel().getSelectedIndex()==-1) {
			popupError("No song selected", "Select a song to edit its information.");
			return;
		}
		ArrayList<String> song = database.get(lv.getSelectionModel().getSelectedIndex());
		if ((song.get(0).compareTo(editsongname.getText().toLowerCase())==0)
				&&(song.get(1).compareTo(editartist.getText().toLowerCase())==0)) { //edits where song and artist are unchanged
			song.set(2, editalbum.getText().toLowerCase());
			song.set(3, edityear.getText().toLowerCase());
			saveDBToFile();
			refreshList();
			return;
		}
		if (findIndex(editsongname.getText().toLowerCase(), editartist.getText().toLowerCase())==-1) {
			popupError("Duplicate Song", "A song with that name and artist already exists.");
			showSongInfo();
			return;
		}
		database.remove(song);
		int i = addSongtoDB(editsongname.getText().toLowerCase(), editartist.getText().toLowerCase(), editalbum.getText().toLowerCase(),
				edityear.getText().toLowerCase());
		refreshList();
		lv.getSelectionModel().clearAndSelect(i);
	}

	private void deleteSong() {
		int i = lv.getSelectionModel().getSelectedIndex();
		database.remove(i);
		saveDBToFile();
		refreshList();
		if (ol.size()>0) {
			lv.getSelectionModel().clearAndSelect((i<ol.size()) ? i : i-1);
		} else {
			editsongname.setText("");
			editartist.setText("");
			editalbum.setText("");
			edityear.setText("");
		}
	}

	private int addSongtoDB(String name, String artist, String album, String year) {
		int i = findIndex(name, artist);
		if (i==-1) return -1; //duplicate song
		database.add(i, new ArrayList<String>(Arrays.asList(name, artist, album, year)));
		saveDBToFile();
		return i;
	}

	private int findIndex(String name, String artist) { //-1 for duplicates
		int i = database.size();
		for (ArrayList<String> song : database) {
			if ((name.compareTo(song.get(0))>0)||((name.compareTo(song.get(0))==0)&&(artist.compareTo(song.get(1))>0))) continue;
			if ((name.compareTo(song.get(0))==0)&&(artist.compareTo(song.get(1))==0)) return -1;
			i = database.indexOf(song);
			break;
		}
		return i;
	}

	private void popupError(String title, String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("SongLib - Error");
		alert.setHeaderText(title);
		alert.setContentText(msg);
		alert.showAndWait();
	}
}