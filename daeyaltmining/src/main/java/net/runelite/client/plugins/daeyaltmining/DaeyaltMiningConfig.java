package net.runelite.client.plugins.daeyaltmining;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("DaeyaltMining")

public interface DaeyaltMiningConfig extends Config {

    @Alpha
    @ConfigItem(
            name = "Overlay Color",
            description = "",
            position = 1,
            keyName = "DaeyaltMiningOverlayColor"
    )
    default Color DaeyaltMiningColor()
    {
        return Color.GREEN;
    }

    @Alpha
    @ConfigItem(
            name = "Overlay Color (Interacting)",
            description = "",
            position = 2,
            keyName = "DaeyaltMiningOverlayColorInteracting"
    )
    default Color DaeyaltMiningColorInteracting()
    {
        return Color.MAGENTA;
    }

    @ConfigItem(
            name = "Time till change",
            description = "",
            position = 3,
            keyName = "DaeyaltMiningOverlayTimeTillChange"
    )
    default boolean DaeyaltMiningTimeTillChange()
    {
        return true;
    }

}