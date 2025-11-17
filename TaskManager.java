import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class TaskManager extends JFrame {
	private java.util.List<String> tasks = new ArrayList<>();
	private java.util.List<String> groups = new ArrayList<>();
	private Map<String, java.util.List<String>> groupMap = new HashMap<>();

	private static final Path STORAGE_DIR = determineStorageDir();
	private static final Path TASK_FILE = STORAGE_DIR.resolve("tasks.txt");
	private static final Path GROUP_FILE = STORAGE_DIR.resolve("groups.txt");
	private static final Path GROUP_MAP_FILE = STORAGE_DIR.resolve("groupmap.txt");

	public TaskManager() {
		loadData();
		setupUI();
	}

	private void setupUI() {
		setTitle("Task Manager");
		setSize(400, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 1));

		JButton btnNewTask = new JButton("New Task");
		JButton btnTasks = new JButton("Tasks");
		JButton btnMarkDone = new JButton("Mark task done");
		JButton btnNewGroup = new JButton("New group");
		JButton btnAddtoGroup = new JButton("Add to group");
		JButton btnDeleteGroup = new JButton("Delete group");

		btnNewTask.addActionListener(e -> newTaskWindow());
		btnTasks.addActionListener(e -> showTasksWindow());
		btnMarkDone.addActionListener(e -> markTaskDoneWindow());
		btnNewGroup.addActionListener(e -> newGroupWindow());
		btnAddtoGroup.addActionListener(e -> addToGroupWindow());
		btnDeleteGroup.addActionListener(e -> deleteGroupWindow());

		panel.add(btnNewTask);
		panel.add(btnTasks);
		panel.add(btnMarkDone);
		panel.add(btnNewGroup);
		panel.add(btnAddtoGroup);
		panel.add(btnDeleteGroup);

		add(panel);
		setVisible(true);
	}

	private void newTaskWindow() {
		JFrame win = new JFrame("New Task");
		win.setSize(300, 150);
		win.setLocationRelativeTo(null);
		JPanel p = new JPanel();
		JTextField field = new JTextField(15);
		JButton save = new JButton("Save");
		JButton back = new JButton("Back");

		save.addActionListener(e -> {
			String text = field.getText();
			if (text != null && !text.isBlank()) {
				tasks.add(text);
				saveData();
			}
			win.dispose();
		});

		back.addActionListener(e -> win.dispose());

		p.add(field);
		p.add(save);
		p.add(back);
		win.add(p);
		win.setVisible(true);
	}

	private void showTasksWindow() {
		JFrame win = new JFrame("Tasks by Group");
		win.setSize(400, 400);
		win.setLocationRelativeTo(null);
		JPanel p = new JPanel(new BorderLayout());

		JTextArea area = new JTextArea();
		area.setEditable(false);

		int groupIndex = 1;
		for (String g : groups) {
			area.append(groupIndex + ". " + g + ":\n");
			java.util.List<String> list = groupMap.getOrDefault(g, new ArrayList<>());
			if (list.isEmpty()) {
				area.append("    (no tasks)\n");
			} else {
				int taskIndex = 1;
				for (String t : list) {
					area.append("    " + taskIndex + ". " + t + "\n");
					taskIndex++;
				}
			}
			area.append("\n");
			groupIndex++;
		}

		java.util.List<String> ungrouped = new ArrayList<>();
		for (String t : tasks) {
			boolean inGroup = false;
			for (java.util.List<String> lst : groupMap.values()) {
				if (lst != null && lst.contains(t)) { inGroup = true; break; }
			}
			if (!inGroup) ungrouped.add(t);
		}

		if (!ungrouped.isEmpty()) {
			area.append("Ungrouped:\n");
			int uIndex = 1;
			for (String t : ungrouped) {
				area.append("    " + uIndex + ". " + t + "\n");
				uIndex++;
			}
		} else if (groups.isEmpty()) {
			if (!tasks.isEmpty()) {
				area.append("All Tasks:\n");
				int tIndex = 1;
				for (String t : tasks) {
					area.append("    " + tIndex + ". " + t + "\n");
					tIndex++;
				}
			} else {
				area.append("(no tasks)\n");
			}
		}

		JButton back = new JButton("Back");
		back.addActionListener(e -> win.dispose());

		p.add(new JScrollPane(area), BorderLayout.CENTER);
		p.add(back, BorderLayout.SOUTH);

		win.add(p);
		win.setVisible(true);
	}

	private void markTaskDoneWindow() {
		JFrame win = new JFrame("Mark task done");
		win.setSize(300, 300);
		win.setLocationRelativeTo(null);

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		for (int i = 0; i < tasks.size(); i++) {
			String task = tasks.get(i);
			JButton b = new JButton((i+1) + ". " + task);
			int index = i;
			b.addActionListener(e -> {
				if (index >= 0 && index < tasks.size()) {
					tasks.remove(index);
					saveData();
				}
				win.dispose();
			});
			p.add(b);
		}

		JButton back = new JButton("Back");
		back.addActionListener(e -> win.dispose());
		p.add(back);

		win.add(new JScrollPane(p));
		win.setVisible(true);
	}

	private void newGroupWindow() {
		JFrame win = new JFrame("New Group");
		win.setSize(300, 150);
		win.setLocationRelativeTo(null);
		JPanel p = new JPanel();
		JTextField field = new JTextField(15);
		JButton save = new JButton("Save");
		JButton back = new JButton("Back");

		save.addActionListener(e -> {
			String g = field.getText();
			if (g != null && !g.isBlank() && !groups.contains(g)) {
				groups.add(g);
				groupMap.putIfAbsent(g, new ArrayList<>());
				saveData();
			}
			win.dispose();
		});

		back.addActionListener(e -> win.dispose());

		p.add(field);
		p.add(save);
		p.add(back);
		win.add(p);
		win.setVisible(true);
	}

	private void addToGroupWindow() {
		JFrame win = new JFrame("Select Task");
		win.setSize(300, 300);
		win.setLocationRelativeTo(null);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		for (String t : tasks) {
			JButton b = new JButton(t);
			b.addActionListener(e -> selectGroupWindow(t));
			p.add(b);
		}

		JButton back = new JButton("Back");
		back.addActionListener(e -> win.dispose());
		p.add(back);

		win.add(new JScrollPane(p));
		win.setVisible(true);
	}

	private void selectGroupWindow(String task) {
		JFrame win = new JFrame("Select Group");
		win.setSize(300, 300);
		win.setLocationRelativeTo(null);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		for (String g : groups) {
			JButton b = new JButton(g);
			b.addActionListener(e -> {
				groupMap.computeIfAbsent(g, k -> new ArrayList<>()).add(task);
				saveData();
				win.dispose();
			});
			p.add(b);
		}

		JButton back = new JButton("Back");
		back.addActionListener(e -> win.dispose());
		p.add(back);

		win.add(new JScrollPane(p));
		win.setVisible(true);
	}

	private void deleteGroupWindow() {
		JFrame win = new JFrame("Delete Group");
		win.setSize(300, 300);
		win.setLocationRelativeTo(null);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		for (String g : new ArrayList<>(groups)) {
			JButton b = new JButton(g);
			b.addActionListener(e -> {
				groups.remove(g);
				groupMap.remove(g);
				saveData();
				win.dispose();
			});
			p.add(b);
		}

		JButton back = new JButton("Back");
		back.addActionListener(e -> win.dispose());
		p.add(back);

		win.add(new JScrollPane(p));
		win.setVisible(true);
	}

	private void saveData() {
		try {
			Files.createDirectories(STORAGE_DIR);
		} catch (Exception ignored) {}

		try (BufferedWriter bw = Files.newBufferedWriter(TASK_FILE, StandardCharsets.UTF_8)) {
			for (String t : tasks) { bw.write(t); bw.newLine(); }
		} catch (Exception ignored) {}

		try (BufferedWriter bw = Files.newBufferedWriter(GROUP_FILE, StandardCharsets.UTF_8)) {
			for (String g : groups) { bw.write(g); bw.newLine(); }
		} catch (Exception ignored) {}

		try (BufferedWriter bw = Files.newBufferedWriter(GROUP_MAP_FILE, StandardCharsets.UTF_8)) {
			for (Map.Entry<String, java.util.List<String>> e : groupMap.entrySet()) {
				for (String t : e.getValue()) {
					bw.write(e.getKey() + "::" + t);
					bw.newLine();
				}
			}
		} catch (Exception ignored) {}
	}

	private void loadData() {
		if (Files.exists(TASK_FILE)) {
			try (Scanner sc = new Scanner(TASK_FILE.toFile(), StandardCharsets.UTF_8.name())) {
				while (sc.hasNextLine()) tasks.add(sc.nextLine());
			} catch (Exception ignored) {}
		}
		if (Files.exists(GROUP_FILE)) {
			try (Scanner sc = new Scanner(GROUP_FILE.toFile(), StandardCharsets.UTF_8.name())) {
				while (sc.hasNextLine()) {
					String g = sc.nextLine();
					if (!groups.contains(g)) {
						groups.add(g);
						groupMap.put(g, new ArrayList<>());
					}
				}
			} catch (Exception ignored) {}
		}

		if (Files.exists(GROUP_MAP_FILE)) {
			try (Scanner sc = new Scanner(GROUP_MAP_FILE.toFile(), StandardCharsets.UTF_8.name())) {
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					String[] parts = line.split("::", 2);
					if (parts.length == 2) {
						groupMap.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(parts[1]);
						if (!groups.contains(parts[0])) groups.add(parts[0]);
					}
				}
			} catch (Exception ignored) {}
		}
	}

	private static Path determineStorageDir() {
		String env = System.getenv("GOOGLE_DRIVE_PATH");
		if (env != null && !env.isBlank()) {
			Path p = Paths.get(env);
			try {
				if (Files.exists(p) && Files.isDirectory(p)) return p;
			} catch (Exception ignored) {}
		}

		String user = System.getProperty("user.home");
		Path[] candidates = new Path[] {
			Paths.get("G:", "My Drive"),
			Paths.get(user, "Google Drive"),
			Paths.get(user, "Google Drive", "My Drive"),
			Paths.get(user, "Drive", "My Drive"),
			Paths.get(user, "My Drive")
		};
		for (Path c : candidates) {
			try {
				if (Files.exists(c) && Files.isDirectory(c)) return c;
			} catch (Exception ignored) {}
		}

		return Paths.get("").toAbsolutePath();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new TaskManager());
	}
}
