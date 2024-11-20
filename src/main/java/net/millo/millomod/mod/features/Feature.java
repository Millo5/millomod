package net.millo.millomod.mod.features;

import net.millo.millomod.system.Config;

public abstract class Feature {

    protected boolean enabled = true;

    public abstract String getKey();


    public void onTick() {}

    public void onConfigUpdate(Config config) {
        enabled = alwaysActive() || (boolean) config.get(getKey()+".enabled");
    }
    public void defaultConfig(Config config) {
        if (!alwaysActive()) config.setIfNull(getKey()+".enabled", !disabledByDefault());
        if (this instanceof IRenderable) ((IRenderable) this).setHudConfig(config);
    }
    public void toggleEnabled() {
        enabled = !enabled;
        Config.getInstance().set(getKey()+".enabled", enabled);
    }

    public boolean disabledByDefault() { return false; }
    public boolean alwaysActive() { return false; }

    public boolean scrollInHotbar(double amount) { return false; }

    public boolean isEnabled() {
        return enabled;
    }
}
