package com.partyhits.maiden;

import com.partyhits.XpToDamage;
import com.partyhits.util.Hit;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


public class MaidenHandler
{
    @Inject
    private XpToDamage xpToDamage;
    @Inject
    private Client client;
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
    private List<Pair<Integer, Integer>> queuedDamage = new ArrayList<>();

    public void init(NPC maiden)
    {
        overlayManager.add(maidenOverlay);
        queuedDamage.clear();
        maidenActive = true;
        maidenNpc = maiden;
        predictedHpPercent = 100.0;
        realHpPercent = 100.0;

        int partySize = xpToDamage.getToBPartySize();
        boolean maidenEM = maidenNpc.getId() == NpcID.THE_MAIDEN_OF_SUGADINTI_10814;

        maxHp = getMaidenMaxHp(partySize, maidenEM);
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
            /*
            for (Pair<Integer, Integer> entry : queuedDamage)
            {
                System.out.println("Current ticks queue:");
                System.out.println("Damage queue entry: " + entry.getLeft() + " expire in: " + entry.getRight());
            }
             */

            updateHpPercentage();
            updatePredictedHp(1);
            reduceQueuedDamage();
        }
    }



    private void reduceQueuedDamage()
    {
        List<Pair<Integer, Integer>> newQueuedDamage = new ArrayList<>();

        for (Pair<Integer, Integer> entry : queuedDamage)
        {
            int dmg = entry.getLeft();
            int tickDelay = entry.getRight() - 1;

            if (tickDelay > 0)
            {
                newQueuedDamage.add(Pair.of(dmg, tickDelay));
            }
        }
        queuedDamage = newQueuedDamage;
    }

    public void updatePredictedHp(int tick)
    {
        if (maidenNpc == null || maidenNpc.isDead() || maidenNpc.getHealthScale() == 0)
        {
            return;
        }

        int queuedDmgTotal = 0;
        for (Pair<Integer, Integer> entry : queuedDamage)
        {
            if (entry.getRight() >= tick)
                queuedDmgTotal += entry.getLeft();
        }

        double queuedDamagePercentage = (queuedDmgTotal / (double) maxHp) * 100;
        predictedHpPercent = realHpPercent - queuedDamagePercentage;
        //System.out.println("Real hp: " + realHpPercent + " Queued dmg% " + queuedDamagePercentage + " predicted% " + predictedHpPercent);
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

    private void updateHpPercentage()
    {
        if (maidenNpc.getHealthRatio() / maidenNpc.getHealthScale() != 1)
            realHpPercent = ((double) maidenNpc.getHealthRatio() / (double) maidenNpc.getHealthScale() * 100);
    }

    public void queueDamage(Hit hit, boolean ownHit)
    {
        if (hit.getTickDelay() > 0)
        {
            if (ownHit)
            {
                queuedDamage.add(Pair.of(hit.getDamage(), hit.getTickDelay()));
                updateHpPercentage();
                updatePredictedHp(1);
            }
            else
            {
                queuedDamage.add(Pair.of(hit.getDamage(), hit.getTickDelay() - 1));
                updateHpPercentage();
                updatePredictedHp(0);
            }
        }
    }
}
