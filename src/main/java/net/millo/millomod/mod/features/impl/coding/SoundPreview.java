package net.millo.millomod.mod.features.impl.coding;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.hypercube.actiondump.ActionDump;
import net.millo.millomod.mod.hypercube.actiondump.Sound;
import net.millo.millomod.mod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Arrays;
import java.util.Optional;

public class SoundPreview extends Feature  {
    @Override
    public String getKey() {
        return "sound_preview";
    }

    public void previewSound(ItemStack item) {
        String varitem = ItemUtil.getPBVString(item, "hypercube:varitem");
        if (varitem == null) return;

        JsonElement json = JsonParser.parseString(varitem);

        JsonObject obj = json.getAsJsonObject();
        String id = obj.get("id").getAsString();
        if (!id.equals("snd")) return;

        JsonObject data = obj.getAsJsonObject("data");
        double pitch = data.get("pitch").getAsDouble();
        double vol = data.get("vol").getAsDouble();
        String sound = data.get("sound").getAsString();

        ActionDump actionDump = ActionDump.getActionDump();
        if (actionDump == null) return;

        Optional<Sound> adSound = Arrays.stream(actionDump.sounds).filter(s -> s.icon.name.equals(sound)).findFirst();
        if (adSound.isEmpty()) return;
        try {
            Object fieldValue = SoundEvents.class.getDeclaredField(adSound.get().sound).get(null);
            if (fieldValue instanceof SoundEvent soundEvent) {
                SoundHandler.playSound(soundEvent, vol, pitch);
            } else if (fieldValue instanceof RegistryEntry.Reference<?> soundEventReference) {
                SoundHandler.playSound((SoundEvent) soundEventReference.value(), vol, pitch);
            } else if (fieldValue instanceof RegistryEntry<?> soundEventRegistry) {
                SoundHandler.playSound((SoundEvent) soundEventRegistry.value(), vol, pitch);
            }
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }


    }

}
