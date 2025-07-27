package com.partyhits;

import com.partyhits.util.FontTypes;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;
import net.runelite.client.config.Range;

@ConfigGroup("partyhits")
public interface PartyHitsConfig extends Config
{
	@ConfigSection(
			name = "General",
			position = 0,
			description = "General Settings",
			closedByDefault = false
	)
	String generalSettings = "generalSettings";

	@ConfigSection(
			name = "Font",
			position = 1,
			description = "Font Options",
			closedByDefault = true
	)
	String fontSettings = "fontSettings";

	@ConfigSection(
			name = "Maiden",
			position = 2,
			description = "Maiden Settings",
			closedByDefault = true
	)
	String maidenSettings = "maidenSettings";

	@ConfigSection(
			name = "Verzik",
			position = 3,
			description = "Verzik Settings",
			closedByDefault = true
	)
	String verzikSettings = "verzikSettings";
	@ConfigSection(
			name = "Sotetseg",
			position = 4,
			description = "Sotetseg Settings",
			closedByDefault = true
	)
	String soteSettings = "soteSettings";

	@ConfigItem(
			position = 0,
			keyName = "Duration",
			name = "Hitsplat duration",
			description = "How long should the hitsplat stay for (in frames).",
			section = generalSettings
	)
	default int duration()
	{
		return 150;
	}
	@ConfigItem(
			position = 1,
			keyName = "Maiden Only",
			name = "Maiden Only",
			description = "Only show hits in the maiden room.",
			section = generalSettings
	)
	default boolean maidenOnly()
	{
		return false;
	}
	@ConfigItem(
			position = 2,
			keyName = "Show Self",
			name = "Show Self",
			description = "Show your own hits.",
			section = generalSettings
	)
	default boolean ownHits()
	{
		return false;
	}
	@ConfigItem(
			position = 3,
			keyName = "Show party hits",
			name = "Show party hits",
			description = "Show the hits of your party members.",
			section = generalSettings
	)
	default boolean partyHits()
	{
		return true;
	}
	@ConfigItem(
			position = 4,
			keyName = "Maiden Live HP",
			name = "Maiden Live HP",
			description = "Shows maidens current hp.",
			section = maidenSettings
	)
	default boolean maidenHP()
	{
		return false;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 5,
			keyName = "Height Offset",
			name = "Height Offset",
			description = "Make the hitsplat higher above the player.",
			section = fontSettings
	)
	default int offset()
	{
		return 20;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 6,
			keyName = "Horizontal Offset",
			name = "Horizontal Offset",
			description = "Adjust the horizontal offset of the text.",
			section = fontSettings
	)
	default int horOffset()
	{
		return 0;
	}
	@ConfigItem(
			position = 7,
			keyName = "Font",
			name = "Font",
			description = "Change the font of the text.",
			section = fontSettings
	)
	default FontTypes font()
	{
		return FontTypes.ARIAL;
	}
	@ConfigItem(
			position = 8,
			keyName = "Font Size",
			name = "Font Size",
			description = "Change the size of the text.",
			section = fontSettings
	)
	default int size()
	{
		return 15;
	}
	@ConfigItem(
			position = 9,
			keyName = "Text Color",
			name = "Text Color",
			description = "Change the color of the text.",
			section = fontSettings
	)
	default Color color()
	{
		return Color.WHITE;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 10,
			keyName = "Maiden Height Offset",
			name = "Maiden Height Offset",
			description = "Make the Maiden HP higher.",
			section = maidenSettings
	)
	default int maidenOffset()
	{
		return 30;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 11,
			keyName = "Maiden Horizontal Offset",
			name = "Maiden Horizontal Offset",
			description = "Adjust the horizontal offset of the text on maiden.",
			section = maidenSettings
	)
	default int maidenHorOffset()
	{
		return -5;
	}
	@ConfigItem(
			position = 12,
			keyName = "Maiden Font",
			name = "Maiden Font",
			description = "Change the font of the HP on maiden.",
			section = maidenSettings
	)
	default FontTypes maidenFont()
	{
		return FontTypes.ARIAL;
	}
	@ConfigItem(
			position = 13,
			keyName = "Maiden Font Size",
			name = "Maiden Font Size",
			description = "Change the size of the HP on maiden.",
			section = maidenSettings
	)
	default int maidenSize()
	{
		return 15;
	}
	@ConfigItem(
			position = 14,
			keyName = "Maiden Text Color",
			name = "Maiden Text Color",
			description = "Change the color of the text on maiden.",
			section = maidenSettings
	)
	default Color maidenColor()
	{
		return Color.GREEN;
	}
	@ConfigItem(
			position = 15,
			keyName = "Sync hits",
			name = "Sync hits",
			description = "Sync your hits with your teammates (delays your hits updating slightly)",
			section = maidenSettings
	)
	default boolean syncHits()
	{
		return false;
	}
	@ConfigItem(
			position = 16,
			keyName = "Update Threshold",
			name = "Update Threshold",
			description = "Only update Maidens HP if change is more than x% from old hp",
			section = maidenSettings
	)
	default double updateThreshold()
	{
		return 0.2;
	}
	@ConfigItem(
			position = 17,
			keyName = "Update on tick only",
			name = "Update on tick only",
			description = "Only update maidens health every tick, instead of on xp drop",
			section = maidenSettings
	)
	default boolean onTickOnly()
	{
		return false;
	}
	@ConfigItem(
			position = 18,
			keyName = "Verzik Live HP P2",
			name = "Verzik Live HP P2",
			description = "Shows Verziks current hp on p2.",
			section = verzikSettings
	)
	default boolean verzikHpP2()
	{
		return false;
	}
	@ConfigItem(
			position = 19,
			keyName = "Verzik Live HP P3",
			name = "Verzik Live HP P3",
			description = "Shows Verziks current hp on p3.",
			section = verzikSettings
	)
	default boolean verzikHpP3()
	{
		return false;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 20,
			keyName = "Verzik Height Offset",
			name = "Verzik Height Offset",
			description = "Make the Verzik HP higher.",
			section = verzikSettings
	)
	default int verzikOffset()
	{
		return 30;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 21,
			keyName = "Verzik Horizontal Offset",
			name = "Verzik Horizontal Offset",
			description = "Adjust the horizontal offset of the text on Verzik.",
			section = verzikSettings
	)
	default int verzikHorOffset()
	{
		return -5;
	}
	@ConfigItem(
			position = 22,
			keyName = "Verzik Font",
			name = "Verzik Font",
			description = "Change the font of the HP on verzik.",
			section = verzikSettings
	)
	default FontTypes verzikFont()
	{
		return FontTypes.ARIAL;
	}
	@ConfigItem(
			position = 23,
			keyName = "Verzik Font Size",
			name = "Verzik Font Size",
			description = "Change the size of the HP on Verzik.",
			section = verzikSettings
	)
	default int verzikSize()
	{
		return 15;
	}
	@ConfigItem(
			position = 24,
			keyName = "Verzik Text Color",
			name = "Verzik Text Color",
			description = "Change the color of the text on Verzik.",
			section = verzikSettings
	)
	default Color verzikColor()
	{
		return Color.GREEN;
	}
	@ConfigItem(
			position = 25,
			keyName = "Sync hits",
			name = "Sync hits",
			description = "Sync your hits with your teammates (delays your hits updating slightly)",
			section = verzikSettings
	)
	default boolean syncVerzikHits()
	{
		return false;
	}
	@ConfigItem(
			position = 26,
			keyName = "Update Threshold",
			name = "Update Threshold",
			description = "Only update Verziks HP if change is more than x% from old hp",
			section = verzikSettings
	)
	default double updateVerzikThreshold()
	{
		return 0.2;
	}
	@ConfigItem(
			position = 27,
			keyName = "Update on tick only",
			name = "Update on tick only",
			description = "Only update Verziks health every tick, instead of on xp drop",
			section = verzikSettings
	)
	default boolean onTickOnlyVerzik()
	{
		return false;
	}
	@ConfigItem(
			position = 28,
			keyName = "Sote Live HP",
			name = "Sote Live HP",
			description = "Shows Sotetsegs current hp%",
			section = soteSettings
	)
	default boolean soteHp()
	{
		return false;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 29,
			keyName = "Sote Height Offset",
			name = "Sote Height Offset",
			description = "Make the Sote HP higher.",
			section = soteSettings
	)
	default int soteOffset()
	{
		return 30;
	}
	@Range(min = -100, max = 100)
	@ConfigItem(
			position = 30,
			keyName = "Sote Horizontal Offset",
			name = "Sote Horizontal Offset",
			description = "Adjust the horizontal offset of the text on Sote.",
			section = soteSettings
	)
	default int soteHorOffset()
	{
		return -5;
	}
	@ConfigItem(
			position = 31,
			keyName = "Sote Font",
			name = "Sote Font",
			description = "Change the font of the HP on Sote.",
			section = soteSettings
	)
	default FontTypes soteFont()
	{
		return FontTypes.ARIAL;
	}
	@ConfigItem(
			position = 32,
			keyName = "Sote Font Size",
			name = "Sote Font Size",
			description = "Change the size of the HP on Sote.",
			section = soteSettings
	)
	default int soteSize()
	{
		return 15;
	}
	@ConfigItem(
			position = 33,
			keyName = "Sote Text Color",
			name = "Sote Text Color",
			description = "Change the color of the text on Sote.",
			section = soteSettings
	)
	default Color soteColor()
	{
		return Color.GREEN;
	}
	@ConfigItem(
			position = 34,
			keyName = "Sync hits",
			name = "Sync hits",
			description = "Sync your hits with your teammates (delays your hits updating slightly)",
			section = soteSettings
	)
	default boolean syncSoteHits()
	{
		return false;
	}
	@ConfigItem(
			position = 35,
			keyName = "Update Threshold",
			name = "Update Threshold",
			description = "Only update Sotes HP if change is more than x% from old hp",
			section = soteSettings
	)
	default double updateSoteThreshold()
	{
		return 0.2;
	}
	@ConfigItem(
			position = 36,
			keyName = "Update on tick only",
			name = "Update on tick only",
			description = "Only update Sotes health every tick, instead of on xp drop",
			section = soteSettings
	)
	default boolean onTickOnlySote()
	{
		return false;
	}
}
