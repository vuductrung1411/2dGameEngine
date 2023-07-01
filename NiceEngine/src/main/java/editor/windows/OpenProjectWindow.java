package editor.windows;

import editor.MessageBox;
import editor.NiceImGui;
import editor.uihelper.ButtonColor;
import imgui.ImGui;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import system.MouseListener;
import system.Window;
import util.FileUtils;
import util.ProjectUtils;
import util.Settings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static editor.uihelper.NiceShortCall.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class OpenProjectWindow {
    private static boolean isOpen = false;
    private static List<String> projects = new ArrayList<>();
    private static int projectIconTexId = -1;
    private static boolean allowToCancel = false;
    private static String projectNeedToRename = "";

    public static void open(boolean allowToCancel){
        isOpen = true;
        getListProject();
        projectIconTexId = FileUtils.getIcon(FileUtils.ICON_NAME.PROJECT).getTexId();
        OpenProjectWindow.allowToCancel = allowToCancel;
    }

    public static void imgui(){
        if (!isOpen) return;

        ImGui.openPopup("Open project");
        ImGui.setNextWindowSize(Window.getWidth() * 0.3f, Window.getHeight() * 0.7f);

        if (ImGui.beginPopupModal("Open project")){
            ImGui.text("Select a project below or");
            ImGui.sameLine();
            NiceImGui.drawButton("Create new project", new ButtonColor(COLOR_DarkGreen, COLOR_Green, COLOR_DarkGreen), new Vector2f(200, 25));

            Vector4f textColor = Settings.NAME_COLOR;
            ImGui.textColored(textColor.x, textColor.y, textColor.z, textColor.w, "Project list");

            ImGui.beginChild("ProjectList", ImGui.getContentRegionMaxX() * 0.99f, ImGui.getContentRegionMaxY() * 0.8f, true);
            for (int i = 0; i < projects.size(); i++){
                String p = projects.get(i);
                boolean needToBreakFlag = false;

                ImGui.pushID("project" + p);
                ImGui.image(projectIconTexId, 25f, 25f);
                ImGui.sameLine();


                if (p.equals(projectNeedToRename)){
                    boolean changeCurrentProjectFlag = p.equals(ProjectUtils.CURRENT_PROJECT);

                    String[] newName = NiceImGui.inputTextNoLabel(p);
                    if (newName[0].equals("true")) {
                        if (!newName[1].equals(p)) {
                            if (projects.contains(newName[1])){
                                JOptionPane.showMessageDialog(null, "The project name '" + newName[1] + "' is existed!",
                                        "ERROR", JOptionPane.ERROR_MESSAGE);
                            } else {
                                File srcFile = new File("data\\" + p);
                                File desFile = new File("data\\" + newName[1]);
                                boolean rename = srcFile.renameTo(desFile);
                                if (!rename) {
                                    MessageBox.setShowMsb(true);
                                    MessageBox.setTypeOfMsb(MessageBox.TypeOfMsb.CREATE_FILE_SUCCESS);
                                    MessageBox.setMsbText("Rename failed");
                                } else {
                                    if (changeCurrentProjectFlag) {
                                        Window.get().changeCurrentProject(newName[1], false, false);
                                    }
                                }

                                needToBreakFlag = true;
                                getListProject();
                            }
                        }
                    }

                    if (ImGui.isItemHovered()){
                        ImGui.beginTooltip();
                        ImGui.textColored(textColor.x, textColor.y, textColor.z, textColor.w, "Enter to confirm rename");
                        ImGui.endTooltip();
                    }
                } else {
                    ImGui.button(p);
                    if (p.equals(ProjectUtils.CURRENT_PROJECT)){
                        ImGui.sameLine();
                        ImGui.textUnformatted("(This is current project)");
                    }
                    if (ImGui.isItemHovered()){
                        ImGui.beginTooltip();
                        ImGui.textColored(textColor.x, textColor.y, textColor.z, textColor.w, "Double-click to open this project!");
                        ImGui.text("Right-click to more option");
                        ImGui.endTooltip();

                        if (ImGui.isMouseDoubleClicked(GLFW.GLFW_MOUSE_BUTTON_LEFT)){
                            Window.get().changeCurrentProject(p, true, true);
                            isOpen = false;
                        }

                        if (ImGui.isMouseClicked(GLFW_MOUSE_BUTTON_RIGHT)){
                            ImGui.openPopup("Item popup");
                        }
                    }

                    if (ImGui.beginPopup("Item popup")){
                        if (ImGui.menuItem("Rename project")){
                            projectNeedToRename = p;
                        }
                        if (ImGui.menuItem("Open in Explorer")) {
                            try {
                                File file = new File("data\\" + p);
                                String command = "explorer.exe \"" + file.getAbsolutePath() + "\"";

                                Runtime.getRuntime().exec(command);
                            } catch (IOException e) {
                                MessageBox.setContext(true, MessageBox.TypeOfMsb.ERROR, "Error! Can't open project");
                            }
                        }
                        if (ImGui.menuItem("Delete this project")){
                            int response = JOptionPane.showConfirmDialog(null,
                                    "DELETE project '" + p + "'?\nProject will be removed permanent\nYou cannot undo this action!",
                                    "DELETE PROJECT", JOptionPane.YES_NO_OPTION);
                            if (response == JOptionPane.YES_OPTION) {
                                if (p.equals(ProjectUtils.CURRENT_PROJECT)){
                                    Window.get().changeCurrentProject("", false, true);
                                }

                                try {
                                    File file = new File("data\\" + p);
                                    if (file.exists()) {
                                        String[] command = {"cmd", "/c", "rmdir", "/s", "/q", file.getAbsolutePath()};
                                        Process process = Runtime.getRuntime().exec(command);

                                        int exitCode = process.waitFor();
                                        if (exitCode == 0) {
                                            System.out.println("Project '" + p + "' is removed");
                                        } else {
                                            System.out.println("Error when remove project '" + p + "'");
                                        }
                                    } else {
                                        System.out.println("Cannot find project '" + p + "'");
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                needToBreakFlag = true;
                                getListProject();
                            }
                        }

                        ImGui.endPopup();
                    }
                }

                ImGui.popID();

                if (needToBreakFlag) break;
            }

            //check click, check if click outside rename input, use hover any
            if (ImGui.isMouseClicked(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                if (!ImGui.isAnyItemHovered()) {
                    projectNeedToRename = "";
                }
            }
            ImGui.endChild();
            if (allowToCancel) {
                if (NiceImGui.drawButton("Cancel", new ButtonColor(COLOR_DarkRed, COLOR_Red, COLOR_DarkRed), new Vector2f(100, 30))) {
                    isOpen = false;
                }
            }

            ImGui.endPopup();
        }
    }
    
    private static void getListProject(){
        projects.clear();
        File directory = new File("data");
        File[] filesList = directory.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                if (file.isDirectory()){
                    projects.add(file.getName());
                }
            }
        }
        if (projects.contains(ProjectUtils.CURRENT_PROJECT)){
            int currProjectIndex = projects.indexOf(ProjectUtils.CURRENT_PROJECT);
            projects.set(currProjectIndex, projects.get(0));
            projects.set(0, ProjectUtils.CURRENT_PROJECT);
        }
    }
}