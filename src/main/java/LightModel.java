
@lombok.Data
public class LightModel {
    public int id;
    public String name;
    public boolean isOn;
    public LightModel(int id, String name, boolean isOn) {
        this.id = id;
        this.name = name;
        this.isOn = isOn;
    }

}
