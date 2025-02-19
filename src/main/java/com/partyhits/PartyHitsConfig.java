package com.partyhits;

import com.partyhits.util.FontTypes;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("partyhits")
public interface PartyHitsConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "Duration",
			name = "Hitsplat duration",
			description = "How long should the hitsplat stay for (in frames)."
	)
	default int duration()
	{
		return 150;
	}
	@ConfigItem(
			position = 1,
			keyName = "Offset",
			name = "Hitsplat Offset",
			description = "Make the hitsplat higher above the player."
	)
	default int offset()
	{
		return 5;
	}
	@ConfigItem(
			position = 2,
			keyName = "Font",
			name = "Font",
			description = "Change the font of the text."
	)
	default FontTypes font()
	{
		return FontTypes.ARIAL;
	}
	@ConfigItem(
			position = 3,
			keyName = "Font Size",
			name = "Font Size",
			description = "Change the size of the text."
	)
	default int size()
	{
		return 15;
	}
	@ConfigItem(
			position = 4,
			keyName = "Text Color",
			name = "Text Color",
			description = "Change the color of the text."
	)
	default Color color()
	{
		return Color.WHITE;
	}
	@ConfigItem(
			position = 5,
			keyName = "Maiden only",
			name = "Maiden only",
			description = "Only show in the maiden room."
	)
	default boolean maidenOnly()
	{
		return false;
	}
}
