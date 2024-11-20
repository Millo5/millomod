package net.millo.millomod.mod.util;

import com.google.gson.JsonObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

public class LocationItem {
    protected JsonObject data;
    private JsonObject loc;
    private double x, y, z, pitch, yaw;
    public final String id;

    private static final JsonObject defaultObject;
    static {
        JsonObject obj = new JsonObject();
        obj.addProperty("id","loc");
        JsonObject data = new JsonObject();
        data.addProperty("isBlock",false);
        JsonObject loc = new JsonObject();
        Arrays.stream(new String[]{"x", "y", "z", "pitch", "yaw"})
                .forEach(i -> loc.addProperty(i, 0));
        data.add("loc",loc);
        obj.add("data",data);
        defaultObject = obj;
    }

    public LocationItem(Vec3d pos) {
        this();
        setX(pos.x);
        setY(pos.y);
        setZ(pos.z);
    }

    public LocationItem() {
        id = defaultObject.get("id").getAsString();
        data = defaultObject.get("data").getAsJsonObject();

        this.loc = this.data.getAsJsonObject("loc");
        this.x = loc.get("x").getAsDouble();
        this.y = loc.get("y").getAsDouble();
        this.z = loc.get("z").getAsDouble();
        this.pitch = loc.get("pitch").getAsDouble();
        this.yaw = loc.get("yaw").getAsDouble();

        setX(x);
        setY(y);
        setZ(z);
        setPitch(pitch);
        setYaw(yaw);
    }

    public ItemStack toStack() {
        NbtCompound pbv = new NbtCompound();
        JsonObject varItem = new JsonObject();
        varItem.addProperty("id",id);
        varItem.add("data",data);
        pbv.put("hypercube:varitem", NbtString.of(varItem.toString()));
        ItemStack item = Items.PAPER.getDefaultStack();
        NbtComponent nbtComp = NbtComponent.of(pbv);
        item.set(DataComponentTypes.CUSTOM_DATA, nbtComp);
        return item;
    }

    public void setX(double x) {
        loc.addProperty("x", x);
        data.add("loc",loc);
        this.x = x;
    }

    public void setY(double y) {
        loc.addProperty("y",y);
        data.add("loc",loc);
        this.y = y;
    }

    public void setZ(double z) {
        loc.addProperty("z",z);
        data.add("loc",loc);
        this.z = z;
    }
    public void setPitch(double pitch) {
        loc.addProperty("pitch",pitch);
        data.add("loc",loc);
        this.pitch = pitch;
    }
    public void setYaw(double yaw) {
        loc.addProperty("yaw",yaw);
        data.add("loc",loc);
        this.yaw = yaw;
    }

    public LocationItem setRotation(double pitch, double yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
        return this;
    }


}
