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
		{"m0", "Browse", "Открыть"},
		{"f1", "Choose file or create new", "Выберите файл или создайте новый"},
		{"s0", " Status: ", " Статус: "},
		{"s1", "Failed to write into %1$s", "Не удалось записать в %1$s"},
		{"s2", "%1$d/%2$d lines parsed", "%1$d/%2$d строк проанализировано"},
		{"s3", "Awaiting action", "Ожидание действия"},
		{"s4", "Sorting userlist", "Сортировка листа"},
		{"s5", "Writing to %1$s", "Запись в %1$s"},
		{"s6", "Searching for files", "Поиск файлов"},
		{"dC", "connection", "вход"},
		{"dS", "server launch", "запуск сервера"},
		{"d0", "remote", "управление"},
		{"d1", "redirect", "переадресация"},
		{"d2", "file", "запрос файла"},
		{"d3", "shutdown", "запрос на выключение"},
		{"d4", "message", "сообщение"},
		{"d5", "view", "показ экрана"},
		{"d6", "telnet", "telnet"},
		{"d7", "chat", "чат"},
		{"d8", "connection failed: incorrect password", "неудачное подключение: неправильный пароль"},
		{"d8s", "incorrect password", "неправильный пароль"},
		{"d9", "exit", "выход"},
		{"c0", "From", "От"},
		{"c1", "To", "До"},
		{"c2", "Filter", "Фильтр"},
		{"c3", "Showing ", "Показано клиентов: "},
		{"c4", "Showing ", "Показано событий: "},
		{"c5", " users", ""},
		{"c6", " events", ""},
		{"l0", "All events", "Все события"},
		{"t0", "Save to...", "Сохранить в..."},
		{"t2", "Start tracking", "Начать слежение"},
		{"t3", "Tracking to:\n", "Следить за:\n"},
		{"t4", "Users:", "Пользователи:"},
		{"t5", "Stop tracking", "Прекратить слежение"},
		{"t6", "Choose users and event types and press \"Start tracking\"", "Выберите пользователей и типы событий и нажмите \"Начать слежение\""},
		{"t7", "All event types", "Все виды событий"},
		{"t8", "Please select at least one event to track", "Выберите хотя бы одно событие для отслеживания"},
		{"t9", "Please select at least one user to track", "Выберите хотя бы одного пользователя для отслеживания"},
		{"t10", "entered", "присоединился"},
		{"t11", "left", "вышел"},
		{"t12", "Create new log every ", "Делить лог каждые "},
		{"t13", " hours", " часа"},
		{"t14", "Creating new log every %1$s hours\n", "Создание нового лога каждые %1$s часа\n"},
		{"t15", "How often a new log should be created? (0 - always write into single file)", "Как часто создавать новый лог файл? (0 - не создавать)"},
		{"b0", "Select all", "Выбрать все"},
		{"b1", "Deselect all", "Снять все выделение"},
		{"b2", "Select all visible", "Выбрать все видимые"},
		{"b3", "Deselect all visible", "Снять выделение с видимых"}
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
