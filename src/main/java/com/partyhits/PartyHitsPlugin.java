package com.partyhits;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.partyhits.XpToDamage.XpToDamage;
import com.partyhits.util.AttackStyle;
import com.partyhits.util.Hit;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "ToB Party Hits",
		description = "Shows the hits of your party members above their head in ToB",
		tags = {"party", "hits", "tob"}
)
public class PartyHitsPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private PartyHitsConfig config;
	@Inject
	private WSClient wsClient;
	@Inject
	private PartyService party;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private PartyHitsOverlay partyHitsOverlay;
	@Inject
	private XpToDamage xpToDamage;
	private static final int[] previousExp = new int[Skill.values().length];
	private boolean resetXpTrackerLingerTimerFlag = false;
	private final int MAIDEN_REGIONID = 12613;
	private final int MAX_XP = 20000000;

	@Override
	protected void startUp()
	{
		overlayManager.add(partyHitsOverlay);
		wsClient.registerMessage(Hit.class);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
					{
						int[] xps = client.getSkillExperiences();
						System.arraycopy(xps, 0, previousExp, 0, previousExp.length);
					});
		}
		else
		{
			resetXpTracker();
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(partyHitsOverlay);
		wsClient.unregisterMessage(Hit.class);
	}

	@Subscribe
	protected void onStatChanged(StatChanged event)
	{
		int currentXp = event.getXp();
		int previousXp = previousExp[event.getSkill().ordinal()];
		int xpDiff = currentXp - previousXp;
		if (previousXp > 0 && xpDiff > 0)
		{
			processXP(event.getSkill(), xpDiff);
		}
		previousExp[event.getSkill().ordinal()] = event.getXp();
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING)
		{
			resetXpTracker();
			resetXpTrackerLingerTimerFlag = true;
		}
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN && resetXpTrackerLingerTimerFlag)
		{
			resetXpTrackerLingerTimerFlag = false;
		}
	}

	@Subscribe
	protected void onFakeXpDrop(FakeXpDrop event)
	{
		if (event.getXp() >= MAX_XP)
			return;

		processXP(event.getSkill(), event.getXp());
	}

	private void processXP(Skill skill, int xpDrop)
	{
		if (!inTob())
			return;

		if (config.maidenOnly())
			if (!inMaidenRegion())
				return;

		if (skill == Skill.HITPOINTS)
		{
			Player player = client.getLocalPlayer();
			if (player == null)
				return;

			int npcId = -1;
			Actor actor = player.getInteracting();
			if (actor instanceof NPC)
			{
				npcId = ((NPC) actor).getId();

				int dmg = xpToDamage.calculateHit(npcId, xpDrop);
				if (dmg > 0)
				{
					Hit hit = new Hit(dmg, AttackStyle.MELEE, client.getLocalPlayer().getName());
					sendHit(hit);
				}
			}
		}
	}

	@Subscribe
	protected void onHit(Hit hit)
	{
		partyHitsOverlay.addHit(hit, config.duration());
	}

	private void sendHit(Hit hit)
	{
		if (party.isInParty())
		{
			clientThread.invokeLater(() -> party.send(hit));
		}
	}

	private boolean inTob()
	{
		int tobVar = client.getVarbitValue(Varbits.THEATRE_OF_BLOOD);
		return tobVar == 2 || tobVar == 3;
	}

	private boolean inMaidenRegion()
	{
		return ArrayUtils.contains(client.getTopLevelWorldView().getMapRegions(), MAIDEN_REGIONID);
	}

	private void resetXpTracker()
	{
		Arrays.fill(previousExp, 0);
	}

	@Provides
	PartyHitsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyHitsConfig.class);
	}
}
