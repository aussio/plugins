/*
 * Copyright (c) 2018, terminatusx <jbfleischman@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2020, loldudester <HannahRyanster@gmail.com>
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

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;

import static net.runelite.api.AnimationID.*;

@Extension
@PluginDescriptor(
        name = "Woodcut Blisterwood Tree",
        description = "Woodcut the Blisterwood tree",
        tags = {"woodcutting"}
)
@Slf4j
public class AutoWintertodtPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AutoWintertodtOverlay overlay;

    @Inject
    private AutoWintertodtConfig config;

    @Getter(AccessLevel.PACKAGE)
    private AutoWintertodtActivity currentActivity = AutoWintertodtActivity.IDLE;

    @Getter(AccessLevel.PACKAGE)
    private boolean isInWintertodt;

    private Instant lastActionTime;

    private int previousTimerValue;

    @Provides
    AutoWintertodtConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoWintertodtConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        log.info("startUp");
        reset();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("shutDown");
        overlayManager.remove(overlay);
        reset();
    }

    private void reset() {
        currentActivity = AutoWintertodtActivity.IDLE;
        lastActionTime = null;
    }

    // private boolean isInWintertodtRegion() {
    //     if (client.getLocalPlayer() != null) {
    //         return client.getLocalPlayer().getWorldLocation().getRegionID() == WINTERTODT_REGION;
    //     }

    //     return false;
    // }

    @Subscribe
    private void onGameTick(GameTick gameTick) {
        // checkActionTimeout();
        handleState();

    }

    private void handleState() {
        switch (currentActivity) {
            case IDLE:
//                log.info(currentActivity.getActionString());
                break;
            case WOODCUTTING:
//                log.info(currentActivity.getActionString());
                break;
            case INVENTORY_FULL:
//                log.info(currentActivity.getActionString());
                break;
            case DROPPING:
//                log.info(currentActivity.getActionString());
                break;
            default:
//                log.info(currentActivity.getActionString());
        }
    }

    // private void checkActionTimeout() {
    //     if (currentActivity == AutoWintertodtActivity.IDLE) {
    //         return;
    //     }

    //     // I wonder when you'd not find the player? Weird...
    //     int currentAnimation = client.getLocalPlayer() != null ? client.getLocalPlayer().getAnimation() : -1;
    //     if (currentAnimation != IDLE || lastActionTime == null) {
    //         return;
    //     }

    //     Duration actionTimeout = Duration.ofSeconds(3);
    //     Duration sinceAction = Duration.between(lastActionTime, Instant.now());

    //     if (sinceAction.compareTo(actionTimeout) >= 0) {
    //         log.info("Activity timeout!");
    //         currentActivity = AutoWintertodtActivity.IDLE;
    //     }
    // }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event) {
        // if (!isInWintertodt) {
        //     return;
        // }

        final Player local = client.getLocalPlayer();

        if (event.getActor() != local) {
            return;
        }

        final int animId = local.getAnimation();

        log.info("Animation changed: {}", animId);

        switch (animId) {
            case WOODCUTTING_BRONZE:
            case WOODCUTTING_IRON:
            case WOODCUTTING_STEEL:
            case WOODCUTTING_BLACK:
            case WOODCUTTING_MITHRIL:
            case WOODCUTTING_ADAMANT:
            case WOODCUTTING_RUNE:
            case WOODCUTTING_GILDED:
            case WOODCUTTING_DRAGON:
            case WOODCUTTING_DRAGON_OR:
            case WOODCUTTING_INFERNAL:
            case WOODCUTTING_3A_AXE:
            case WOODCUTTING_CRYSTAL:
            case WOODCUTTING_TRAILBLAZER:
                setActivity(AutoWintertodtActivity.WOODCUTTING);
                break;
            case IDLE:
                setActivity(AutoWintertodtActivity.IDLE);
                break;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        final ItemContainer container = event.getItemContainer();

        if (container != client.getItemContainer(InventoryID.INVENTORY)) {
            log.info("Container changed that wasn't invent. ID: {}", container.getId());
            return;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            int inv_size = inventoryWidget.getWidgetItems().size();
            log.info("inv_size: {}", inv_size);
            if (inv_size == 28) {
                setActivity(AutoWintertodtActivity.INVENTORY_FULL);
            }
        } else {
            log.info("Couldn't get inventoryWidget");
        }

    }

    private void setActivity(AutoWintertodtActivity action) {
        if (action != currentActivity) {
            log.info("New Action: {}", action);
        }
        currentActivity = action;
        lastActionTime = Instant.now();
    }
}
