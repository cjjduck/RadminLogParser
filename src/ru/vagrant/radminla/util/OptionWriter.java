package ru.vagrant.radminla.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import ru.vagrant.radminla.Main;
import ru.vagrant.radminla.gui.GUI;

public class OptionWriter {

	public static void write(String line) {
		try {
			File f = new File(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()+".txt");
			if (!f.exists())
				f.createNewFile();
			List<String> l = Files.readAllLines(Paths.get(f.toURI()));
			l.add(line);
			Files.write(Paths.get(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()+".txt"), l);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void load() {
		try {
			File f = new File(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()+".txt");
			if (!f.exists())
				return;
			List<String> l = Files.readAllLines(Paths.get(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()+".txt"));
			for (String s : l) {
				if (s.contains("=")) {
					String[] s_ = s.split("=");
					GUI.setReplace(s_[0], s_[1]);
				}
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
	
}
