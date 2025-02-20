package com.partyhits.maiden;

import com.partyhits.XpToDamage;
import com.partyhits.util.Hit;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
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
    private double realHpPercent;
    @Getter
    private boolean maidenActive;
    private Map<Integer, Integer> queuedDamage = new HashMap<>();

    public void init(NPC maiden)
    {
        overlayManager.add(maidenOverlay);
        queuedDamage.clear();
        maidenActive = true;
        maidenNpc = maiden;
        predictedHpPercent = 100.0;
        realHpPercent = 100.0;
        int partySize = xpToDamage.getToBPartySize();
        switch (partySize)
        {
            case 1:
                maxHp = 500;
                break; // for testing in solo entry mode, todo remove later
            case 2:
                maxHp = 950;
                break; // for testing in duo entry mode, todo remove later
            case 3:
                maxHp = 2625;
                break;
            case 4:
                maxHp = 3062;
                break;
            case 5:
                maxHp = 3500;
                break;
        }
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

        updateHpPercentage();
        int queuedDmgTotal = 0;
        for (int damage : queuedDamage.keySet())
        {
            queuedDmgTotal += damage;
        }

        double queuedDamagePercentage = (queuedDmgTotal / (double) maxHp) * 100;
        predictedHpPercent = realHpPercent - queuedDamagePercentage;
    }

    private void updateHpPercentage() {
        if (maidenNpc.getHealthRatio() / maidenNpc.getHealthScale() != 1)
            realHpPercent = ((double) maidenNpc.getHealthRatio() / (double) maidenNpc.getHealthScale() * 100);

    }

    public void queueDamage(Hit hit)
    {
        queuedDamage.put(hit.getDamage(), hit.getTickDelay());
        updatePredictedHp();
    }
}
