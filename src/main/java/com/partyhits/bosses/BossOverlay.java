package com.partyhits.bosses;

import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class BossOverlay extends Overlay
{
    private final BossHandler bossHandler;
    private double lastRenderedHp = 100.0;

    @Inject
    public BossOverlay(BossHandler bossHandler)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.bossHandler = bossHandler;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (bossHandler.isBossActive() && bossHandler.getBossNpc() != null)
        {
            NPC maiden = bossHandler.getBossNpc();
            double bossHp = bossHandler.getPredictedHpPercent();
            if (bossHp < 0)
                bossHp = 0;

            double threshold = bossHandler.updateThresh();
            if (Math.abs(bossHp - lastRenderedHp) >= threshold)
            {
                lastRenderedHp = bossHp;
            }
            String hpText = String.format("%.1f", lastRenderedHp);

            Point pt = maiden.getCanvasTextLocation(graphics, hpText, bossHandler.vertOffset() * 5);
            if (pt != null)
            {
				graphics.setFont(new Font(bossHandler.hpFont().getName(), Font.BOLD, bossHandler.fontSize()));
				int x = pt.getX() + bossHandler.horOffset();
				int y = pt.getY();

				graphics.setColor(new Color(0,0,0, bossHandler.textColor().getAlpha()));
				graphics.drawString(hpText, x + 1, y + 1);

				graphics.setColor(bossHandler.textColor());
				graphics.drawString(hpText, x, y);
            }
        }
        return null;
    }
}
