package editor;

import components.*;
import components.scripts.test.TargetDebugging;
import components.scripts.test.TestComponent;
import editor.uihelper.NiceImGui;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.reflections.Reflections;
import system.GameObject;
import org.joml.Vector4f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import renderer.PickingTexture;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InspectorWindow {

    //region Fields
    List<GameObject> activeGameObjects;
    private List<Vector4f> activeGameObjectOriginalColor;
    private GameObject activeGameObject = null;
    private PickingTexture pickingTexture;

    boolean firstTime = true;

    Set<Class<? extends Component>> classes;
    //endregion

    //region Contructors
    public InspectorWindow(PickingTexture pickingTexture) {
        this.activeGameObjects = new ArrayList<>();
        this.pickingTexture = pickingTexture;
        this.activeGameObjectOriginalColor = new ArrayList<>();

        Reflections reflections = new Reflections("physics2d.components");
        classes = reflections.getSubTypesOf(Component.class);
        reflections = new Reflections("components");
        classes.addAll(reflections.getSubTypesOf(Component.class));
    }
    //endregion

    String searchText = "";
    boolean showAddComponentMenu = false;

    //region Methods
    public void imgui() {
        ImGui.begin("Inspector");

        if (activeGameObjects.size() == 1 && activeGameObjects.get(0) != null) {
            activeGameObject = activeGameObjects.get(0);
        }

        if (activeGameObject == null) {
            ImGui.end();
            return;
        }

        activeGameObject.imgui();

        if (ImGui.button("Add component")) {
            showAddComponentMenu = true;
            searchText = "";
            ImGui.openPopup("AddComponentMenu");
        }

        if (showAddComponentMenu) {
            if (ImGui.beginPopup("AddComponentMenu")) {
                //searchText = NiceImGui.inputText("Search:", searchText, "AddingComponent" + activeGameObject.hashCode());

                searchText = NiceImGui.inputTextWithHintAndNoLabel("Search", searchText, "AddingComponent" + activeGameObject.hashCode());

                ImGui.beginChild("ComponentList", 500, 350, true, ImGuiWindowFlags.HorizontalScrollbar);
                for (Class<? extends Component> aClass : classes) {
                    String className = aClass.getSimpleName();
                    if (searchText.isEmpty() || className.toLowerCase().contains(searchText.toLowerCase())) {
                        if (ImGui.menuItem(className)) {
                            showAddComponentMenu = false;
                            try {
                                Component component = aClass.getDeclaredConstructor().newInstance(); // Tạo mới một đối tượng Component từ lớp aClass
                                if (activeGameObject.getComponent(aClass) == null) {
                                    activeGameObject.addComponent(component);
                                }
                            } catch (InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException | NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                            ImGui.closeCurrentPopup();
                        }
                    }
                }
                ImGui.endChild();
                ImGui.endPopup();
            }
        }

        ImGui.end();
    }

    public GameObject getActiveGameObject() {
        return activeGameObjects.size() == 1 ? this.activeGameObjects.get(0) : null;
    }

    public void clearSelected() {
        if (activeGameObjectOriginalColor.size() > 0) {
            int i = 0;
            for (GameObject go : activeGameObjects) {
                SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
                if (spr != null) {
                    spr.setColor(activeGameObjectOriginalColor.get(i));
                }
                i++;
            }
        }

        this.activeGameObjects.clear();
        this.activeGameObjectOriginalColor.clear();
    }

    public List<GameObject> getActiveGameObjects() {
        return this.activeGameObjects;
    }

    public void setActiveGameObject(GameObject go) {
        if (go != null) {
            clearSelected();
            this.activeGameObjects.add(go);
        }
    }

    public void addActiveGameObject(GameObject go) {
        SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
        if (spr != null) {
            this.activeGameObjectOriginalColor.add(new Vector4f(spr.getColor()));
            spr.setColor(new Vector4f(0.8f, 0.8f, 0.0f, 0.8f));
        } else {
            this.activeGameObjectOriginalColor.add(new Vector4f());
        }
        this.activeGameObjects.add(go);
    }

    public PickingTexture getPickingTexture() {
        return this.pickingTexture;
    }
    //endregion
}
