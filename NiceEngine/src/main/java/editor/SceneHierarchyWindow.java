package editor;

import editor.uihelper.ButtonColor;
import editor.uihelper.NiceImGui;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector2f;
import system.GameObject;
import system.Window;

import java.util.List;

import static editor.uihelper.NiceShortCall.*;

public class SceneHierarchyWindow {
    //region Singleton
    private SceneHierarchyWindow() {
    }

    private static SceneHierarchyWindow instance = null;

    public static SceneHierarchyWindow getInstance() {
        if (instance == null) {
            instance = new SceneHierarchyWindow();
        }

        return instance;
    }
    //endregion

    //region Fields
    private static String payloadDragDropType = "SceneHierarchy";
    private static GameObject selectedGameObject = null;
    //endregion

    //region Methods
    public void imgui() {
        ImGui.begin("Hierarchy");
        ImGui.alignTextToFramePadding();
        List<GameObject> gameObjects = Window.getScene().getGameObjects();

        int index = 0;
        for (GameObject obj : gameObjects) {
            if (!obj.doSerialization()) {
                continue;
            }

//            boolean treeNodeOpen = doTreeNode(obj, index);
//
//            if (treeNodeOpen) {
//                ImGui.treePop();
//            }
            ImGui.pushID(index);
            float w = ImGui.getContentRegionAvailX();
            float h = ImGui.getTextLineHeightWithSpacing();
            if (obj.equals(selectedGameObject)) {
                NiceImGui.NiceButtonWithLeftText(obj.name, new ButtonColor(COLOR_Blue, COLOR_DarkAqua, COLOR_Blue), new Vector2f(w, h));
            } else {
                if (NiceImGui.NiceButtonWithLeftText(obj.name, new ButtonColor(COLOR_DarkBlue, COLOR_DarkAqua, COLOR_Blue), new Vector2f(w, h))) {
                    Window.getImguiLayer().getPropertiesWindow().setActiveGameObject(obj);
                    selectedGameObject = obj;
                }
                ;
            }
            ImGui.popID();
            index++;
        }

        ImGui.end();
    }

    private boolean doTreeNode(GameObject obj, int index) {
        ImGui.pushID(index);
        boolean treeNodeOpen = ImGui.treeNodeEx(
                obj.name,
                ImGuiTreeNodeFlags.DefaultOpen |
                        ImGuiTreeNodeFlags.FramePadding |
                        ImGuiTreeNodeFlags.OpenOnArrow |
                        ImGuiTreeNodeFlags.SpanAvailWidth,
                obj.name
        );
        ImGui.popID();

        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayloadObject(payloadDragDropType, obj);
            ImGui.text(obj.name);
            ImGui.endDragDropSource();
        }

        if (ImGui.beginDragDropTarget()) {
            Object payloadObj = ImGui.acceptDragDropPayloadObject(payloadDragDropType);
            if (payloadObj != null) {
                if (payloadObj.getClass().isAssignableFrom(GameObject.class)) {
                    GameObject playerGameObject = (GameObject) payloadObj;
                    System.out.println("Payload accepted: '" + playerGameObject.name + "'");
                }
            }

            ImGui.endDragDropTarget();
        }

        return treeNodeOpen;
    }

    public static void setSelectedGameObject(GameObject go) {
        selectedGameObject = go;
    }
    //endregion
}
