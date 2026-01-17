package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.ClientLevelData.class)
public class ClientLevelDataMixin {
    @ModifyReturnValue(
            method = "getDayTime",
            at = @At("RETURN")
    )
    private long getDayTime(long original) {
        return Config.INSTANCE.getTimeChanger() ? Config.INSTANCE.getTime() : original;
    }
}