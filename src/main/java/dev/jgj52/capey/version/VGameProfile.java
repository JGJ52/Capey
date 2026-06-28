package dev.jgj52.capey.version;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class VGameProfile {
    public static UUID getUUID(GameProfile profile) {
        return profile.
                //? >= 1.21.9 {
                        id
                //? } else {
                /*getId
                 *///? }
                        ();
    }

    public static String getName(GameProfile profile) {
        return profile.
                //? >= 1.21.9 {
                name
                //? } else {
                /*getName
                 *///? }
                        ();
    }

    private final UUID uuid;
    private final String name;
    private final Component cname;
    public VGameProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.cname = Component.literal(name);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Component name() {
        return cname;
    }
}
