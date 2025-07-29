package com.partyhits.bosses;

import com.partyhits.util.FontTypes;
import net.runelite.api.NpcID;

import java.awt.*;
public class SoteHandler extends BossHandler
{
    @Override
    protected int getMaxHp()
    {
        int partySize = xpToDamage.getToBPartySize();
        boolean soteEM = getBossNpc().getId() == NpcID.SOTETSEG_10864;

        if (soteEM)
        {
            switch (partySize)
            {
                case 1: return 560;
                case 2: return 1064;
                case 3: return 1512;
                case 4: return 1904;
                default: return 2240;
            }
        }
        else
        {
            switch (partySize)
            {
                case 4: return 3500;
                case 5: return 4000;
                default: return 3000;
            }
        }
    }

    protected int vertOffset()
    {
        return config.soteOffset();
    }

    protected int horOffset()
    {
        return config.soteHorOffset();
    }

    protected FontTypes hpFont()
    {
        return config.soteFont();
    }

    protected int fontSize()
    {
        return config.soteSize();
    }

    protected Color textColor()
    {
        return config.soteColor();
    }

    protected double updateThresh()
    {
        return config.updateSoteThreshold();
    }
    protected boolean onTickOnly()
    {
        return config.onTickOnlySote();
    }
}

