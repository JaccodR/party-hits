package com.partyhits.maiden;

import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class MaidenOverlay extends Overlay
{
    @Inject
    private Client client;
    private final MaidenHandler maidenHandler;
    private double lastHp;
    @Setter
    private boolean shouldUpdate;
    private int updateDelay;

    @Inject
    public MaidenOverlay(MaidenHandler maidenHandler)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.maidenHandler = maidenHandler;
        lastHp = 100.0;
        shouldUpdate = true;
        updateDelay = 0;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (maidenHandler.isMaidenActive() && maidenHandler.getMaidenNpc() != null)
        {
            NPC maiden = maidenHandler.getMaidenNpc();
            double maidenHp = maidenHandler.getPredictedHpPercent();
            if (maidenHp < 0)
                maidenHp = 0;


            if (updateDelay != 0)
            {
                maidenHp = lastHp;
                updateDelay--;
            }

            String hpText = String.format("%.1f", maidenHp);
            lastHp = maidenHp;

            Point pt = maiden.getCanvasTextLocation(graphics, hpText,25); // add z offset
            if (pt != null)
            {
                graphics.setFont(new Font("Arial", Font.BOLD, 20)); // add font options

                graphics.setColor(Color.GREEN); // add different color options based on hp % thresholds

                graphics.drawString(hpText, pt.getX(), pt.getY()); // properly centre the text
            }
        }
        return null;
    }

    public void updateHpOverlay()
    {
        updateDelay = 5; // anti flicker solution for now...
    }
}
