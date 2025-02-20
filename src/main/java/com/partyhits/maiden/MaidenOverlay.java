package com.partyhits.maiden;

import com.partyhits.PartyHitsConfig;
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
    private final PartyHitsConfig config;
    private final MaidenHandler maidenHandler;

    @Inject
    public MaidenOverlay(MaidenHandler maidenHandler, PartyHitsConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.maidenHandler = maidenHandler;
        this.config = config;
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


            String hpText = String.format("%.1f", maidenHp);

            Point pt = maiden.getCanvasTextLocation(graphics, hpText,config.maidenOffset() * 5);
            if (pt != null)
            {
                graphics.setFont(new Font(config.maidenFont().getName(), Font.BOLD, config.maidenSize()));

                graphics.setColor(config.maidenColor());
                if (config.threshColor())
                {
                    if (maidenHp < 30.0)
                    {
                        graphics.setColor(config.maidenThirty());
                    }
                    else if (maidenHp < 50.0)
                    {
                        graphics.setColor(config.maidenFifty());
                    }
                    else if (maidenHp < 70.0)
                    {
                        graphics.setColor(config.maidenSeventy());
                    }
                }
                FontMetrics fontMetrics = graphics.getFontMetrics();
                int x = pt.getX() - (fontMetrics.stringWidth(hpText) / 2) + config.maidenHorOffset();
                int y = pt.getY() - (fontMetrics.getHeight() / 2) - fontMetrics.getDescent();

                graphics.drawString(hpText, x, y);
            }
        }
        return null;
    }
}
