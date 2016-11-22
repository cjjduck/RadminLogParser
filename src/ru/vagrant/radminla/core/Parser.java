package ru.vagrant.radminla.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javafx.concurrent.Task;
import ru.vagrant.radminla.gui.GUI;
import ru.vagrant.radminla.util.Const;

public class Parser {

	private int last;
	private String path;
	private String computer;
	
	public Task<Integer> processLog;
	
	public void openFile(String path, String host) {
		computer = host;
		last = 0;
		try {
			this.path = path;
			prepTask(Files.readAllLines(Paths.get(path)));
		} catch (IOException e) { e.printStackTrace(); }
		initTask();
	}
	
	public void update() {
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(new File(path)));
			int cnt = 0;
			while (reader.readLine() != null) {}
			cnt = reader.getLineNumber(); 
			reader.close();
			if (last < cnt) {
				prepTask(Files.readAllLines(Paths.get(path)));
				initTask();
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
	
		private void prepTask(List<String> l) {
			processLog = new Task<Integer>() {
				@Override
				protected Integer call() throws Exception {
					for (int i = last; i < l.size(); i++) {
						updateMessage((i-last)+">"+(l.size()-last));
						updateProgress(i, l.size());
						String s = l.get(i);
						if (s.charAt(0) != '&') continue;
						parse(s);
					}
					last = l.size();
					return 0;
				}
			};
			Core.add(processLog);
		}
	
		private void initTask() {
			Thread t = new Thread(processLog);
			t.setDaemon(true);
			t.start();
		}
		
		private void parse(String s) {
			s = s.substring(s.indexOf("RServer3 ")+9);
			s = s.replace("<br>", "");
			String time, name = "", state = null;
			time = s.substring(0, s.indexOf(" ", s.indexOf(" ")+1));
			s = s.substring(time.length()+1).replace("Connection from ", "");
			if (s.equals("Radmin Server 3 is started")) GUI.addEvent(time, computer, computer, -2, (last>0));
			else {
				if (s.contains(":")) { name = s.substring(0, s.indexOf(":")); state = s.substring(s.indexOf(":")+2); }
				else if (s.contains(")")) { name = s.substring(0, s.lastIndexOf(")")+1); state = s.substring(s.lastIndexOf(")")+2); }
				if (name.isEmpty()) return;
				GUI.addEvent(time, name, computer, Const.stateList.indexOf(state), (last>0));
			}
		}
		
}
