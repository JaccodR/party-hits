package com.partyhits.maiden;

import com.partyhits.XpToDamage;
import com.partyhits.util.Hit;
import lombok.Getter;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.annotations.HitsplatType;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MaidenHandler
{
    @Inject
    private XpToDamage xpToDamage;
    @Inject
    private MaidenOverlay maidenOverlay;
    @Inject
    private OverlayManager overlayManager;
    @Getter
    private NPC maidenNpc;
    private int maxHp;
    @Getter
    private double predictedHpPercent;
    @Getter
    private boolean maidenActive;
    private double trackedHp;
    private Map<Integer, Integer> queuedDamage = new HashMap<>();

    public void init(NPC maiden)
    {
        overlayManager.add(maidenOverlay);
        queuedDamage.clear();
        maidenActive = true;
        maidenNpc = maiden;
        predictedHpPercent = 100.0;

        int partySize = xpToDamage.getToBPartySize();
        boolean maidenEM = maidenNpc.getId() == NpcID.THE_MAIDEN_OF_SUGADINTI_10814;

        maxHp = getMaidenMaxHp(partySize, maidenEM);
        trackedHp = maxHp;
    }

    public void deactivate()
    {
        overlayManager.remove(maidenOverlay);
        maidenActive = false;
        queuedDamage.clear();
        maidenNpc = null;
    }

    @Subscribe
    protected void onGameTick(GameTick event)
    {
        if (maidenActive)
        {
            updatePredictedHp();
            reduceQueuedDamage();
        }
    }

    @Subscribe
    protected void onHitsplatApplied(HitsplatApplied event)
    {
        if (event.getActor() == maidenNpc)
        {
            Hitsplat hitsplat = event.getHitsplat();
            int dmg = hitsplat.getAmount();
            if (hitsplat.getHitsplatType() == HitsplatID.HEAL)
            {
                trackedHp += dmg;
            }
            else
            {
                trackedHp -= dmg;
            }
        }
    }

    private void reduceQueuedDamage()
    {
        Map<Integer, Integer> newMap = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : queuedDamage.entrySet())
        {
            int dmg = entry.getKey();
            int tickDelay = entry.getValue();

            tickDelay--;
            if (tickDelay > 0)
            {
                newMap.put(dmg, tickDelay);
            }
        }
        queuedDamage = newMap;
    }

    public void updatePredictedHp()
    {
        if (maidenNpc == null || maidenNpc.isDead() || maidenNpc.getHealthScale() == 0)
        {
            return;
        }

        int queuedDmgTotal = 0;
        for (int damage : queuedDamage.keySet())
        {
            queuedDmgTotal += damage;
        }

        double queuedDamagePercentage = (queuedDmgTotal / (double) maxHp) * 100;
        double trackedHpPercentage = (trackedHp / (double) maxHp) * 100;
        predictedHpPercent = trackedHpPercentage - queuedDamagePercentage;
    }

    private int getMaidenMaxHp(int partySize, boolean maidenEM)
    {
        if (maidenEM)
        {
            switch (partySize)
            {
                case 1: return 500;
                case 2: return 950;
                case 3: return 1350;
                case 4: return 1700;
                default: return 2000;
            }
        }
        else
        {
            switch (partySize)
            {
                case 4: return 3062;
                case 5: return 3500;
                default: return 2625;
            }
        }
    }

    public void queueDamage(Hit hit)
    {
        queuedDamage.put(hit.getDamage(), hit.getTickDelay());
        updatePredictedHp();
    }
}
