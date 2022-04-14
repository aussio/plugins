/*
 * Copyright (c) 2018, terminatusx <jbfleischman@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.autowintertodt;

import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

class AutoWintertodtOverlay extends OverlayPanel {
    private final AutoWintertodtPlugin plugin;
    private final AutoWintertodtConfig wintertodtConfig;

    @Inject
    private AutoWintertodtOverlay(AutoWintertodtPlugin plugin, AutoWintertodtConfig wintertodtConfig) {
        super(plugin);
        this.plugin = plugin;
        this.wintertodtConfig = wintertodtConfig;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Wintertodt overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isInWintertodt() || !wintertodtConfig.showOverlay()) {
            return null;
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(plugin.getCurrentActivity().getActionString())
                .color(plugin.getCurrentActivity() == AutoWintertodtActivity.IDLE ? Color.RED : Color.GREEN)
                .build());

        String inventoryString = plugin.getNumLogs() > 0 ? plugin.getInventoryScore() + " (" + plugin.getTotalPotentialinventoryScore() + ") pts" : plugin.getInventoryScore() + " pts";
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Inventory:")
                .leftColor(Color.WHITE)
                .right(inventoryString)
                .rightColor(plugin.getInventoryScore() > 0 ? Color.GREEN : Color.RED)
                .build());

        String kindlingString = plugin.getNumLogs() > 0 ? plugin.getNumKindling() + " (" + (plugin.getNumLogs() + plugin.getNumKindling()) + ")" : Integer.toString(plugin.getNumKindling());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Kindling:")
                .leftColor(Color.WHITE)
                .right(kindlingString)
                .rightColor(plugin.getNumKindling() + plugin.getNumLogs() > 0 ? Color.GREEN : Color.RED)
                .build());

        return super.render(graphics);
    }
}