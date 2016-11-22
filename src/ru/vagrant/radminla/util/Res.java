package ru.vagrant.radminla.util;

import java.util.Arrays;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;

public class Res {

	private static final String defaultLanguage = "en";
	
	private static final String[][] loc = new String[][] {
		{defaultLanguage, "ru"}, //Language list
		{"m0", "Browse", "�������"},
		{"f1", "Choose file or create new", "�������� ���� ��� �������� �����"},
		{"s0", " Status: ", " ������: "},
		{"s1", "Failed to write into %1$s", "�� ������� �������� � %1$s"},
		{"s2", "%1$d/%2$d lines parsed", "%1$d/%2$d ����� ����������������"},
		{"s3", "Awaiting action", "�������� ��������"},
		{"s4", "Sorting userlist", "���������� �����"},
		{"s5", "Writing to %1$s", "������ � %1$s"},
		{"s6", "Searching for files", "����� ������"},
		{"dC", "connection", "����"},
		{"dS", "server launch", "������ �������"},
		{"d0", "remote", "����������"},
		{"d1", "redirect", "�������������"},
		{"d2", "file", "������ �����"},
		{"d3", "shutdown", "������ �� ����������"},
		{"d4", "message", "���������"},
		{"d5", "view", "����� ������"},
		{"d6", "telnet", "telnet"},
		{"d7", "chat", "���"},
		{"d8", "connection failed: incorrect password", "��������� �����������: ������������ ������"},
		{"d8s", "incorrect password", "������������ ������"},
		{"d9", "exit", "�����"},
		{"c0", "From", "��"},
		{"c1", "To", "��"},
		{"c2", "Filter", "������"},
		{"c3", "Showing ", "�������� ��������: "},
		{"c4", "Showing ", "�������� �������: "},
		{"c5", " users", ""},
		{"c6", " events", ""},
		{"l0", "All events", "��� �������"},
		{"t0", "Save to...", "��������� �..."},
		{"t2", "Start tracking", "������ ��������"},
		{"t3", "Tracking to:\n", "������� ��:\n"},
		{"t4", "Users:", "������������:"},
		{"t5", "Stop tracking", "���������� ��������"},
		{"t6", "Choose users and event types and press \"Start tracking\"", "�������� ������������� � ���� ������� � ������� \"������ ��������\""},
		{"t7", "All event types", "��� ���� �������"},
		{"t8", "Please select at least one event to track", "�������� ���� �� ���� ������� ��� ������������"},
		{"t9", "Please select at least one user to track", "�������� ���� �� ������ ������������ ��� ������������"},
		{"t10", "entered", "�������������"},
		{"t11", "left", "�����"},
		{"t12", "Create new log every ", "������ ��� ������ "},
		{"t13", " hours", " ����"},
		{"t14", "Creating new log every %1$s hours\n", "�������� ������ ���� ������ %1$s ����\n"},
		{"t15", "How often a new log should be created? (0 - always write into single file)", "��� ����� ��������� ����� ��� ����? (0 - �� ���������)"},
		{"b0", "Select all", "������� ���"},
		{"b1", "Deselect all", "����� ��� ���������"},
		{"b2", "Select all visible", "������� ��� �������"},
		{"b3", "Deselect all visible", "����� ��������� � �������"}
	};
	
	private static final StringProperty locale = new SimpleStringProperty(defaultLanguage);

	public static StringProperty languageProperty() { return locale; }
	
	public static String[] getAvailLangs() { return loc[0]; }
	
	public static void setLang(String language) {
		if (!Arrays.asList(loc[0]).contains(language)) language = defaultLanguage;
		locale.set(language);
	}
	
	public static String get(String key) {
		int off = 0;
		if (locale.get() != "en") {
			for (int i = 1; i < loc[0].length; i++)
				if (loc[0][i].equals(locale.get())) { off = i; break; }
		}
		for (int i = 1; i < loc.length; i++)
			if (loc[i][0].equals(key) && loc[i].length > off+1) return loc[i][off+1];
		return key;
	}
	
	public static void bind(ComboBoxBase<?> n, String key) {
		n.promptTextProperty().bind(Bindings.createStringBinding(() -> get(key), languageProperty()));
	}
	
	public static void bind(Labeled n, String key) {
		n.textProperty().bind(Bindings.createStringBinding(() -> get(key), languageProperty()));
	}

	public static void bind(TextInputControl n, String key) {
		n.textProperty().bind(Bindings.createStringBinding(() -> get(key), languageProperty()));
	}

}
