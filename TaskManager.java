import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TaskManager extends JFrame {
    private java.util.List<String> tasks = new ArrayList<>();
    private java.util.List<String> groups = new ArrayList<>();
    private Map<String, java.util.List<String>> groupMap = new HashMap<>();

    private static final String TASK_FILE = "tasks.txt";
    private static final String GROUP_FILE = "groups.txt";
    private static final String GROUP_MAP_FILE = "groupmap.txt";

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
        JButton btnGroup = new JButton("Group");
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
            tasks.add(field.getText());
            saveData();
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

        // Enumerate groups and their tasks
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

        // Show ungrouped tasks
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
            // If there are no groups at all, show all tasks enumerated
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
                tasks.remove(index);
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
            if (!groups.contains(g)) groups.add(g);
            groupMap.putIfAbsent(g, new ArrayList<>());
            saveData();
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
                groupMap.get(g).add(task);
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

        for (String g : groups) {
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
        try (PrintWriter pw = new PrintWriter(new FileWriter(TASK_FILE))) {
            for (String t : tasks) pw.println(t);
        } catch (Exception ignored) {}

        try (PrintWriter pw = new PrintWriter(new FileWriter(GROUP_FILE))) {
            for (String g : groups) pw.println(g);
        } catch (Exception ignored) {}

        try (PrintWriter pw = new PrintWriter(new FileWriter(GROUP_MAP_FILE))) {
            for (String g : groupMap.keySet()) {
                for (String t : groupMap.get(g)) pw.println(g + "::" + t);
            }
        } catch (Exception ignored) {}
    }

    private void loadData() {
        try (Scanner sc = new Scanner(new File(TASK_FILE))) {
            while (sc.hasNextLine()) tasks.add(sc.nextLine());
        } catch (Exception ignored) {}

        try (Scanner sc = new Scanner(new File(GROUP_FILE))) {
            while (sc.hasNextLine()) {
                String g = sc.nextLine();
                groups.add(g);
                groupMap.put(g, new ArrayList<>());
            }
        } catch (Exception ignored) {}

        try (Scanner sc = new Scanner(new File(GROUP_MAP_FILE))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split("::");
                if (parts.length == 2) {
                    groupMap.get(parts[0]).add(parts[1]);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        new TaskManager();
    }
}
