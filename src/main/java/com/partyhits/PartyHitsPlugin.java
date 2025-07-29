package com.partyhits;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.partyhits.bosses.MaidenHandler;
import com.partyhits.bosses.SoteHandler;
import com.partyhits.bosses.VerzikHandler;
import com.partyhits.util.Hit;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "ToB Predicted Hit",
		description = "Shows the hits of your party members in ToB",
		tags = {"party", "hits", "tob", "maiden", "verzik"}
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
	@Inject
	private MaidenHandler maidenHandler;
	@Inject
	private VerzikHandler verzikHandler;
	@Inject
	private SoteHandler soteHandler;
	@Inject
	private EventBus eventBus;
	private static final int[] previousExp = new int[Skill.values().length];
	private final List<Integer> hitBuffer = new ArrayList<>();
	private boolean resetXpTrackerLingerTimerFlag = false;
	private boolean inTob = false;
	private final int MAIDEN_REGIONID = 12613;
	private final int MAX_XP = 20000000;
	private static final Set<Integer> VERZIK_P2_IDS = new HashSet<>(Arrays.asList(
			NpcID.VERZIK_VITUR_8372, NpcID.VERZIK_VITUR_10850, NpcID.VERZIK_VITUR_10833
	));
	private static final Set<Integer> VERZIK_P3_IDS = new HashSet<>(Arrays.asList(
			NpcID.VERZIK_VITUR_8374, NpcID.VERZIK_VITUR_10852, NpcID.VERZIK_VITUR_10835
	));
	private static final Set<Integer> RANGED_BOWS = new HashSet<>(Arrays.asList(
			ItemID.TWISTED_BOW, ItemID.BOW_OF_FAERDHINEN, ItemID.BOW_OF_FAERDHINEN_C,
			ItemID.ARMADYL_CROSSBOW, ItemID.RUNE_CROSSBOW, ItemID.DRAGON_CROSSBOW
	));
	private static final Set<Integer> RANGED_THROWN = new HashSet<>(Arrays.asList(
			ItemID.CHINCHOMPA_10033, ItemID.RED_CHINCHOMPA_10034, ItemID.BLACK_CHINCHOMPA,
			ItemID.BLAZING_BLOWPIPE, ItemID.TOXIC_BLOWPIPE
	));
	private static final Set<Integer> POWERED_STAVES = new HashSet<>(Arrays.asList(
			ItemID.SANGUINESTI_STAFF,
			ItemID.TRIDENT_OF_THE_SEAS_FULL,
			ItemID.TRIDENT_OF_THE_SEAS,
			ItemID.TRIDENT_OF_THE_SWAMP,
			ItemID.TRIDENT_OF_THE_SWAMP_E,
			ItemID.HOLY_SANGUINESTI_STAFF,
			ItemID.ACCURSED_SCEPTRE,
			ItemID.WARPED_SCEPTRE
	));
	private static final Set<Integer> SHADOW = new HashSet<>(Arrays.asList(
			ItemID.TUMEKENS_SHADOW,
			ItemID.CORRUPTED_TUMEKENS_SHADOW
		));

	@Override
	protected void startUp()
	{
		overlayManager.add(partyHitsOverlay);
		wsClient.registerMessage(Hit.class);
		eventBus.register(maidenHandler);
		eventBus.register(verzikHandler);
		eventBus.register(soteHandler);
		inTob = false;
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
		if (maidenHandler.isBossActive())
			maidenHandler.deactivate();
		if (verzikHandler.isBossActive())
			verzikHandler.deactivate();
		if (soteHandler.isBossActive())
			soteHandler.deactivate();

		overlayManager.remove(partyHitsOverlay);
		eventBus.unregister(maidenHandler);
		eventBus.unregister(verzikHandler);
		eventBus.unregister(soteHandler);
		wsClient.unregisterMessage(Hit.class);
	}

	@Subscribe
	protected void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == Varbits.THEATRE_OF_BLOOD)
		{
			int tobVar = event.getValue();
			inTob = tobVar == 2 || tobVar == 3;

			if (!inTob && maidenHandler.isBossActive())
				maidenHandler.deactivate();
			if (!inTob && verzikHandler.isBossActive())
				verzikHandler.deactivate();
			if (!inTob && soteHandler.isBossActive())
				soteHandler.deactivate();
		}
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
	protected void onNpcSpawned(NpcSpawned event)
	{
		if (!inTob)
			return;

		NPC npc = event.getNpc();
		int npcId = npc.getId();

		switch (npcId)
		{
			case NpcID.THE_MAIDEN_OF_SUGADINTI: // regular mode
			case NpcID.THE_MAIDEN_OF_SUGADINTI_10822: // hard mode
			case NpcID.THE_MAIDEN_OF_SUGADINTI_10814: // entry mode
				if (config.maidenHP())
					maidenHandler.init(npc);
				break;
		}

		if (VERZIK_P3_IDS.contains(npcId))
		{
			if (config.verzikHpP3())
				verzikHandler.init(npc);
		}

		if (Objects.equals(npc.getName(), "Sotetseg"))
		{
			if (config.soteHp() && !soteHandler.isBossActive())
			{
				soteHandler.init(npc);
			}
		}
	}

	@Subscribe
	protected void onNpcChanged(NpcChanged event)
	{
		if (!inTob)
			return;

		NPC npc = event.getNpc();
		int npcId = npc.getId();
		if (VERZIK_P2_IDS.contains(npcId))
		{
			if (config.verzikHpP2())
				verzikHandler.init(npc);
		}
		if (npcId == NpcID.SOTETSEG_10865 || npcId == NpcID.SOTETSEG_8388 || npcId == NpcID.SOTETSEG_10868)
		{
			if (config.soteHp() && !soteHandler.isBossActive())
			{
				soteHandler.init(npc);
			}
		}
	}

	@Subscribe
	protected void onNpcDespawned(NpcDespawned event)
	{
		if (!inTob)
			return;

		String npcName = event.getNpc().getName();
		if (Objects.equals(npcName, "The Maiden of Sugadinti"))
		{
			if (maidenHandler.isBossActive())
				maidenHandler.deactivate();
		}
		else if (Objects.equals(npcName, "Verzik Vitur"))
		{
			if (verzikHandler.isBossActive())
				verzikHandler.deactivate();
		}
		else if (Objects.equals(npcName, "Sotetseg"))
		{
			if (soteHandler.isBossActive())
				soteHandler.deactivate(); //todo only despawn if boss is dead; not in between phases
		}
	}

	@Subscribe
	protected void onGameTick(GameTick event)
	{
		if (!inTob)
			return;

		if (!hitBuffer.isEmpty())
		{
			int totalXp = 0;
			for (int xp : hitBuffer)
			{
				totalXp += xp;
			}
			processXP(totalXp);
			hitBuffer.clear();
		}
	}

	@Subscribe
	protected void onStatChanged(StatChanged event)
	{
		int currentXp = event.getXp();
		int previousXp = previousExp[event.getSkill().ordinal()];
		int xpDiff = currentXp - previousXp;
		if (previousXp > 0 && xpDiff > 0)
		{
			if (event.getSkill() == Skill.HITPOINTS)
				hitBuffer.add(xpDiff);
		}
		previousExp[event.getSkill().ordinal()] = event.getXp();
	}

	@Subscribe
	protected void onFakeXpDrop(FakeXpDrop event)
	{
		if (event.getXp() >= MAX_XP)
			return;

		if (event.getSkill() == Skill.HITPOINTS)
			hitBuffer.add(event.getXp());
	}

	private void processXP(int xpDrop)
	{
		if (!inTob)
			return;

		Player player = client.getLocalPlayer();
		if (player == null)
			return;

		Actor actor = player.getInteracting();
		if (!(actor instanceof NPC))
			return;

		int npcId = ((NPC) actor).getId();

		int dmg = xpToDamage.calculateHit(npcId, xpDrop);
		if (dmg > 0)
		{
			int projectileDelay = 0;

			if (Objects.equals(actor.getName(), "The Maiden of Sugadinti"))
			{
				projectileDelay = calcDelay(player, (NPC) actor, 6);
			}
			else if (VERZIK_P2_IDS.contains(npcId))
			{
				projectileDelay = calcDelay(player, (NPC) actor, 3);
			}
			else if (VERZIK_P3_IDS.contains(npcId))
			{
				projectileDelay = calcDelay(player, (NPC) actor, 7);
			}
			else if (Objects.equals(actor.getName(), "Sotetseg"))
			{
				projectileDelay = calcDelay(player, (NPC) actor, 5);
			}

			Hit hit = new Hit(dmg, player.getName(), projectileDelay);
			sendHit(hit);

			if (config.maidenHP() && !config.syncHits() && inMaidenRegion())
				maidenHandler.queueDamage(hit, true);
			if ((config.verzikHpP2() || config.verzikHpP3()) && !config.syncVerzikHits() && verzikHandler.isBossActive())
				verzikHandler.queueDamage(hit, true);
			if (config.soteHp() && !config.syncSoteHits() && soteHandler.isBossActive())
				soteHandler.queueDamage(hit, true);
		}
	}

	@Subscribe
	protected void onHit(Hit hit)
	{
		boolean isLocalPlayer = Objects.equals(hit.getPlayer(), client.getLocalPlayer().getName());

		if (config.maidenHP() && inMaidenRegion())
		{
			if (!isLocalPlayer || config.syncHits())
			{
				maidenHandler.queueDamage(hit, false);
			}
		}

		if ((config.verzikHpP2() || config.verzikHpP3()) && verzikHandler.isBossActive())
		{
			if (!isLocalPlayer || config.syncVerzikHits())
			{
				verzikHandler.queueDamage(hit, false);
			}
		}

		if (config.soteHp() && soteHandler.isBossActive())
		{
			if (!isLocalPlayer || config.syncSoteHits())
			{
				soteHandler.queueDamage(hit, false);
			}
		}

		if (config.partyHits() && !isLocalPlayer)
		{
			if (!config.maidenOnly() || inMaidenRegion())
			{
				partyHitsOverlay.addHit(hit, config.duration());
			}
		}
	}

	private int calcDelay(Player player, NPC npc, int size)
	{
		WorldPoint npcLoc = npc.getWorldLocation();
		int minDistance = 10;

		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				WorldPoint tileLocation = new WorldPoint(npcLoc.getX() + x, npcLoc.getY() + y, npcLoc.getPlane());
				int distance = player.getWorldLocation().distanceTo(tileLocation);

				if (distance < minDistance)
				{
					minDistance = distance;
				}
			}
		}
		return getTickDelay(minDistance);
	}

	private int getTickDelay(int distance)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
			return 0;

		PlayerComposition playerComposition = player.getPlayerComposition();
		if (playerComposition == null)
			return 0;

		int weaponUsed = playerComposition.getEquipmentId(KitType.WEAPON);
		if (RANGED_BOWS.contains(weaponUsed))
		{
			return (int) Math.floor((3 + distance) / 6.0) + 2;
		}
		else if (RANGED_THROWN.contains(weaponUsed))
		{
			return (int) Math.floor(distance / 6.0) + 2;
		}
		else if (POWERED_STAVES.contains(weaponUsed))
		{
			return (int) Math.floor((1 + distance) / 3.0) + 2;
		}
		else if (SHADOW.contains(weaponUsed))
		{
			return (int) Math.floor((1 + distance) / 3.0) + 3;
		}
		else if (weaponUsed == ItemID.ZARYTE_CROSSBOW)
		{
			return 3; // zcb spec has a set projectile delay of 3, later differentiate between auto & spec
		}
		else if (weaponUsed == ItemID.TONALZTICS_OF_RALOS)
		{
			return 2;
		}
		else if (weaponUsed == ItemID.DRAGON_CLAWS || weaponUsed == ItemID.BURNING_CLAWS || weaponUsed == ItemID.DUAL_MACUAHUITL)
		{
			return 0; // later fix these multi hitsplat weapons to work for maiden damage queue, for now just exclude them
		}
		return 1;
	}

	private void sendHit(Hit hit)
	{
		if (party.isInParty())
		{
			clientThread.invokeLater(() -> party.send(hit));

			if (config.maidenOnly() && !inMaidenRegion())
				return;

			if (config.ownHits() && Objects.equals(hit.getPlayer(), client.getLocalPlayer().getName()))
				partyHitsOverlay.addHit(hit, config.duration());

		}
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
