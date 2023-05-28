package system;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Component;
import components.ComponentDeserializer;
import components.SpriteRenderer;
import editor.NiceImGui;
import imgui.ImGui;
import imgui.type.ImBoolean;
import util.AssetPool;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GameObject {
    //region Fields
    private static int ID_COUNTER = 0;
    private int uid = -1;
    public String name = "";
    public String tag = "";
    private List<Component> components;
    public transient Transform transform;
    private boolean doSerialization = true;
    private boolean isDead = false;
    //endregion

    //region Constructors
    public GameObject(String name) {
        this.name = name;
        this.components = new ArrayList<>();

        this.uid = ID_COUNTER++;
    }
    //endregion

    //region Methods
    public GameObject copy() {
        // TODO: come up with cleaner solution
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
                .enableComplexMapKeySerialization()
                .create();
        String objAsJson = gson.toJson(this);
        GameObject obj = gson.fromJson(objAsJson, GameObject.class);

        obj.generateUid();

        for (Component c : obj.getAllComponents()) {
            c.generateId();
        }

        SpriteRenderer sprite = obj.getComponent(SpriteRenderer.class);
        if (sprite != null && sprite.getTexture() != null) {
            sprite.setTexture(AssetPool.getTexture(sprite.getTexture().getFilePath()));
        }

        return obj;
    }

    public void destroy() {
        isDead = true;
        for (int i = 0; i < components.size(); i++) {
            components.get(i).destroy();
        }
    }

    public static void init(int maxId) {
        ID_COUNTER = maxId;
    }

    /**
     * Update is called once per frame
     *
     * @param dt : The interval in seconds from the last frame to the current one
     */
    public void update(float dt) {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).update(dt);
        }
    }

    /**
     * Start is called before the first frame update
     */
    public void start() {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).start();
        }
    }

    public void imgui() {
        this.name = NiceImGui.inputText("Name", this.name, "Name of " + this.hashCode());
        this.tag = NiceImGui.inputText("Tag", this.tag, "Tag of " + this.hashCode());

        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);

            ImBoolean removeComponentButton = new ImBoolean(true);

            if (ImGui.collapsingHeader(c.getClass().getSimpleName(), removeComponentButton)) {
                c.imgui();
            }

            if (!removeComponentButton.get()) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Remove component '" + c.getClass().getSimpleName() + "' from game object '" + this.name + "'?",
                        "Remove component",
                        JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    components.remove(i);
                    i--;
                }
            }

        }
    }

    public void editorUpdate(float dt) {
        for (int i = 0; i < this.components.size(); i++) {
            components.get(i).editorUpdate(dt);
        }
    }
    //endregion

    //region Properties
    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "Error: Casting component";
                }
            }
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component c) {
        c.generateId();
        this.components.add(c);
        c.gameObject = this;
    }

    public boolean isDead() {
        return isDead;
    }

    public int getUid() {
        return this.uid;
    }

    public List<Component> getAllComponents() {
        return this.components;
    }

    public void setNoSerialize() {
        this.doSerialization = false;
    }

    public void generateUid() {
        this.uid = ID_COUNTER++;
    }

    public boolean doSerialization() {
        return this.doSerialization;
    }

    public boolean compareTag(String tag) {
        return this.tag.equals(tag);
    }
    //endregion
}
