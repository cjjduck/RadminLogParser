package ru.vagrant.radminla.core;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import ru.vagrant.radminla.gui.GUI;
import ru.vagrant.radminla.util.Res;

public class Core {

	private static File file;
	private static ArrayList<Parser> fileList;
	private static ArrayList<Task<Integer>> taskList;
	private static ArrayList<String> linesToWrite;
	private static SimpleDoubleProperty progress;
	private static SimpleStringProperty description;

	public static void init() {
		fileList = new ArrayList<>();
		taskList = new ArrayList<>();
		linesToWrite = new ArrayList<>();
		progress = new SimpleDoubleProperty();
		description = new SimpleStringProperty();
		getAvailLogs(getAvailAddresses());
	}
		
		private static ArrayList<String> getAvailAddresses() {
			Thread[] t = new Thread[255];
			ArrayList<String> reachable = new ArrayList<>();
			for (int i = 1; i <= 255; i++) {
				final byte b = (byte) i;
				t[i-1] = new Thread(()->{
					try {
						InetAddress ia = InetAddress.getByAddress(new byte[] { (byte)192, (byte)168, 0, b });
						if (ia.isReachable(2500)) reachable.add(ia.getHostAddress());
					} catch (IOException e) { e.printStackTrace(); }
				});
				t[i-1].setDaemon(true);
				t[i-1].start();
			}
			/* Wait for all threads to finish */
			for (int i = 0; i < t.length; i++) {
				try { t[i].join(); } catch (InterruptedException e) { e.printStackTrace(); } 
			}
			return reachable;
		}
		
		private static void getAvailLogs(ArrayList<String> reachable) {
			ExecutorService es = Executors.newFixedThreadPool(reachable.size()*2);
			List<Callable<Object>> todo = new ArrayList<Callable<Object>>(reachable.size()*2);
			for (int i = 0; i < reachable.size(); i++) {
				final int index = i;
				todo.add(Executors.callable(()->{
					String s = "\\\\"+reachable.get(index)+"\\c$\\WINDOWS\\System32\\rserver30\\Radm_log.htm";
					if (Files.exists(Paths.get(s), LinkOption.NOFOLLOW_LINKS)) {
						add(s, reachable.get(index));
					}
				}));
				todo.add(Executors.callable(()->{
					String s = "\\\\"+reachable.get(index)+"\\c$\\WINDOWS\\SysWOW64\\rserver30\\Radm_log.htm";
					if (Files.exists(Paths.get(s), LinkOption.NOFOLLOW_LINKS)) {
						add(s, reachable.get(index));
					}
				}));
			}
			try {
				es.invokeAll(todo);
			} catch (InterruptedException e) { e.printStackTrace(); }
			es.shutdown();
			if (fileList.isEmpty()) GUI.finishLoad();
		}
		
		private static void add(String s, String name) {
			Parser p = new Parser();
			fileList.add(p);
			try {
				p.openFile(s, InetAddress.getByName(name).getHostName());
			} catch (UnknownHostException e) { e.printStackTrace();	}
		}
	
	public static void update() {
		GUI.toggleProgressBar(progress, description);
		for (Parser p : fileList)
			p.update();
		GUI.toggleProgressBar();
		write();
	}
	
	public static void add(Task<Integer> task) {
		taskList.add(task);
		task.stateProperty().addListener(e->{
			for (Task<Integer> t : taskList) {
				if (t.getState().equals(State.READY)
					|| t.getState().equals(State.RUNNING) 
					|| t.getState().equals(State.SCHEDULED)) 
					return;
			}
			GUI.finishLoad();
			taskList.clear();
			GUI.toggleProgressBar();
		});
		task.progressProperty().addListener(e->calculateProgress());
	}

		private static void calculateProgress() {
			double d = 0;
			long completed = 0, total = 0;
			for (Task<Integer> t : taskList) {
				d += t.progressProperty().get();
				String[] s = t.messageProperty().get().split(">");
				if (s.length != 2) continue;
				completed += Long.parseLong(s[0]);
				total += Long.parseLong(s[1]);
			}
			d /= taskList.size();
			description.set(String.format(Res.get("s2"), completed, total));
			progress.set(d);
		}
	
	public static SimpleDoubleProperty getProgress() { return progress; }
	
/*
 ___________  ___  _____  _   _______ _   _ _____ 
|_   _| ___ \/ _ \/  __ \| | / /_   _| \ | |  __ \
  | | | |_/ / /_\ \ /  \/| |/ /  | | |  \| | |  \/
  | | |    /|  _  | |    |    \  | | | . ` | | __ 
  | | | |\ \| | | | \__/\| |\  \_| |_| |\  | |_\ \
  \_/ \_| \_\_| |_/\____/\_| \_/\___/\_| \_/\____/
 */
	
	public static void addLine(String line, String host) { linesToWrite.add(host+">"+line); }
	
	public static boolean write() {
		if (linesToWrite.isEmpty()) {		
			GUI.setStatus("s3");
			return true; 
		}
		if (file == null) 
			file = assignFile();
		GUI.setStatus(String.format("s5", file.getName()));
		List<String> lines;
		try {
			lines = Files.readAllLines(file.toPath(), StandardCharsets.ISO_8859_1);
			for (String s : linesToWrite) {
				String[] data = s.split(">");
				if (!lines.contains("["+data[0]+"]:")) {
					if (!lines.isEmpty())
						lines.add("\n");
					lines.add("["+data[0]+"]:");
					lines.add("\t"+data[1]);
				}
				else {
					int i = lines.indexOf("["+data[0]+"]:");
					while (!lines.get(i).equals("\n")) {
						i++;
						if (i >= lines.size()) break;
					}
					lines.add(i, "\t"+data[1]);
				}
			}
			Files.write(file.toPath(), lines, StandardCharsets.ISO_8859_1);
			linesToWrite.clear();
		} catch (IOException e) { 
			GUI.setStatus(String.format(Res.get("s1"), file.getName()));
			e.printStackTrace(); 
			return false;
		}
		GUI.setStatus("s3");
		return true;
	}
	
	public static File assignFile() {
		String s = "RadminLogParser_"+new SimpleDateFormat("dd-MM-yyyy_HH-mm").format(new Date());
		File f = new File(s+".txt");
		int i = 0;
		while (f.exists())
			f = new File(s+"_"+i+++".txt");
		try { f.createNewFile(); } catch (IOException e) { GUI.setStatus(String.format(Res.get("s1"), f.getName()));}
		return f;
	}
	
}
