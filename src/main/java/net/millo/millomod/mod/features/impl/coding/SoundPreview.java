package net.millo.millomod.mod.features.impl.coding;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.millo.millomod.MilloMod;
import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.hypercube.actiondump.ActionDump;
import net.millo.millomod.mod.hypercube.actiondump.Sound;
import net.millo.millomod.mod.hypercube.actiondump.SoundVariant;
import net.millo.millomod.mod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
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

        if (data.has("key")) return; // Custom sounds are not supported

        double pitch = data.get("pitch").getAsDouble();
        double vol = data.get("vol").getAsDouble();
        String sound = data.get("sound").getAsString();
        String variant;
        if (data.has("variant")) {
            variant = data.get("variant").getAsString();
        } else {
            variant = "";
        }

        ActionDump actionDump = ActionDump.getActionDump();
        if (actionDump == null) return;

        Optional<Sound> adSound = Arrays.stream(actionDump.sounds).filter(s -> s.icon.name.equals(sound)).findFirst();
        if (adSound.isEmpty()) return;
        try {
            Object fieldValue = SoundEvents.class.getDeclaredField(adSound.get().sound).get(null);

            SoundEvent soundEvent;
            switch (fieldValue) {
                case SoundEvent se -> soundEvent = se;
                case RegistryEntry.Reference<?> soundEventReference ->
                        soundEvent = (SoundEvent) soundEventReference.value();
                case RegistryEntry<?> soundEventRegistry -> soundEvent = (SoundEvent) soundEventRegistry.value();
                case null, default -> {
                    MilloMod.MC.player.sendMessage(Text.of("Failed to play sound: " + fieldValue.getClass().getName()), false);
                    return;
                }
            }

            if (!variant.isEmpty()) {
                Optional<SoundVariant> optionalVariant = Arrays.stream(adSound.get().variants).filter(v -> v.id.equals(variant)).findFirst();
                if (optionalVariant.isPresent()) {
                    long seed = optionalVariant.get().seed;
                    SoundHandler.playSoundVariant(adSound.get().soundId, seed, (float) vol, (float) pitch);
                    return;
                }
            }
            SoundHandler.playSound(soundEvent, vol, pitch);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            MilloMod.MC.player.sendMessage(Text.of("Failed to play sound: " + e.getMessage()), false);

            for (Field field : SoundEvents.class.getFields()) {
                MilloMod.MC.player.sendMessage(Text.of(field.toString()), false);
            }
        }
    }

}
