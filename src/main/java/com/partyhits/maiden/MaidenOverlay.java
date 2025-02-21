package com.partyhits.maiden;

import com.partyhits.PartyHitsConfig;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class MaidenOverlay extends Overlay
{
    private final PartyHitsConfig config;
    private final MaidenHandler maidenHandler;

    @Inject
    public MaidenOverlay(MaidenHandler maidenHandler, PartyHitsConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
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
				int x = pt.getX() + config.maidenHorOffset();
				int y = pt.getY();

				graphics.setColor(new Color(0,0,0, config.maidenColor().getAlpha()));
				graphics.drawString(hpText, x + 1, y + 1);

				graphics.setColor(config.maidenColor());
				graphics.drawString(hpText, x, y);
            }
        }
        return null;
    }
}
