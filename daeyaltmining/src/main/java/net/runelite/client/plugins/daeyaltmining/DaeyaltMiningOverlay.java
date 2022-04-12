package net.runelite.client.plugins.daeyaltmining;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

public class DaeyaltMiningOverlay extends Overlay {
    private static final Color OBJECT_BORDER_COLOR = Color.RED;

    private final Client client;
    private final DaeyaltMiningPlugin plugin;
    private final DaeyaltMiningConfig config;

    @Inject
    public DaeyaltMiningOverlay(Client client, DaeyaltMiningConfig config, DaeyaltMiningPlugin plugin) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.config = config;
        this.client = client;
        this.plugin = plugin;
    }

    public static void renderClickBox(Graphics2D graphics, Point mousePosition, Shape objectClickbox, Color configColor) {
        if (objectClickbox.contains(mousePosition.getX(), mousePosition.getY())) {
            graphics.setColor(configColor.darker());
        } else {
            graphics.setColor(configColor);
        }

        graphics.draw(objectClickbox);
        graphics.setColor(new Color(configColor.getRed(), configColor.getGreen(), configColor.getBlue(), 50));
        graphics.fill(objectClickbox);
    }

    public Point mouse() {
        return client.getMouseCanvasPosition();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        GameObject current = plugin.current_daeyalt_essence;
        if (current != null) {
            Shape clickbox = current.getClickbox();
            if (clickbox != null) {
                renderClickBox(graphics, mouse(), clickbox, plugin.animating ? config.DaeyaltMiningColorInteracting() : config.DaeyaltMiningColor());
                if (config.DaeyaltMiningTimeTillChange()) {
                    if (plugin.count != -1) {
                        String count = "~" + Math.abs(plugin.count - 101);
                        if (plugin.count == 102) {
                            count = "SOON";
                        }
                        final Point canvasPoint = current.getCanvasTextLocation(
                                graphics, count, current.getRenderable().getModel().getCenterZ()
                        );
                        OverlayUtil.renderTextLocation(graphics, canvasPoint, count, Color.WHITE);
                    }
                }
            }
        }
        return null;
    }
}