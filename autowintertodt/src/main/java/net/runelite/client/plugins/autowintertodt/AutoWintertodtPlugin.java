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
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.autowintertodt.config.WintertodtNotifyDamage;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;

import static net.runelite.api.AnimationID.*;
import static net.runelite.api.ItemID.BRUMA_KINDLING;
import static net.runelite.api.ItemID.BRUMA_ROOT;
import static net.runelite.client.plugins.autowintertodt.config.WintertodtNotifyDamage.ALWAYS;
import static net.runelite.client.plugins.autowintertodt.config.WintertodtNotifyDamage.INTERRUPT;

@Extension
@PluginDescriptor(
        name = "Auto Wintertodt Plugin",
        description = "Play the Wintertodt boss plugin",
        tags = {"minigame", "firemaking", "boss"}
)
@Slf4j
public class AutoWintertodtPlugin extends Plugin {
    private static final int WINTERTODT_REGION = 6462;

    @Inject
    private Notifier notifier;

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
    private int inventoryScore;

    @Getter(AccessLevel.PACKAGE)
    private int totalPotentialinventoryScore;

    @Getter(AccessLevel.PACKAGE)
    private int numLogs;

    @Getter(AccessLevel.PACKAGE)
    private int numKindling;

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
        reset();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        reset();
    }

    private void reset() {
        inventoryScore = 0;
        totalPotentialinventoryScore = 0;
        numLogs = 0;
        numKindling = 0;
        currentActivity = AutoWintertodtActivity.IDLE;
        lastActionTime = null;
    }

    private boolean isInWintertodtRegion() {
        if (client.getLocalPlayer() != null) {
            return client.getLocalPlayer().getWorldLocation().getRegionID() == WINTERTODT_REGION;
        }

        return false;
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (!isInWintertodtRegion()) {
            if (isInWintertodt) {
                log.debug("Left Wintertodt!");
                reset();
            }

            isInWintertodt = false;
            return;
        }

        if (!isInWintertodt) {
            reset();
            log.debug("Entered Wintertodt!");
        }
        isInWintertodt = true;

        checkActionTimeout();
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        int timerValue = client.getVar(Varbits.WINTERTODT_TIMER);
        if (timerValue != previousTimerValue) {
            int timeToNotify = config.roundNotification();
            if (timeToNotify > 0) {
                int timeInSeconds = timerValue * 30 / 50;
                int prevTimeInSeconds = previousTimerValue * 30 / 50;

                log.debug("Seconds left until round start: {}", timeInSeconds);

                if (prevTimeInSeconds > timeToNotify && timeInSeconds <= timeToNotify) {
                    notifier.notify("Wintertodt round is about to start");
                }
            }

            previousTimerValue = timerValue;
        }
    }

    private void checkActionTimeout() {
        if (currentActivity == AutoWintertodtActivity.IDLE) {
            return;
        }

        int currentAnimation = client.getLocalPlayer() != null ? client.getLocalPlayer().getAnimation() : -1;
        if (currentAnimation != IDLE || lastActionTime == null) {
            return;
        }

        Duration actionTimeout = Duration.ofSeconds(3);
        Duration sinceAction = Duration.between(lastActionTime, Instant.now());

        if (sinceAction.compareTo(actionTimeout) >= 0) {
            log.debug("Activity timeout!");
            currentActivity = AutoWintertodtActivity.IDLE;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (!isInWintertodt) {
            return;
        }

        ChatMessageType chatMessageType = chatMessage.getType();

        if (chatMessageType != ChatMessageType.GAMEMESSAGE && chatMessageType != ChatMessageType.SPAM) {
            return;
        }

        MessageNode messageNode = chatMessage.getMessageNode();
        final AutoWintertodtInterruptType interruptType;

        if (messageNode.getValue().startsWith("You carefully fletch the root")) {
            setActivity(AutoWintertodtActivity.FLETCHING);
            return;
        }

        if (messageNode.getValue().startsWith("The cold of")) {
            interruptType = AutoWintertodtInterruptType.COLD;
        } else if (messageNode.getValue().startsWith("The freezing cold attack")) {
            interruptType = AutoWintertodtInterruptType.SNOWFALL;
        } else if (messageNode.getValue().startsWith("The brazier is broken and shrapnel")) {
            interruptType = AutoWintertodtInterruptType.BRAZIER;
        } else if (messageNode.getValue().startsWith("You have run out of bruma roots")) {
            interruptType = AutoWintertodtInterruptType.OUT_OF_ROOTS;
        } else if (messageNode.getValue().startsWith("Your inventory is too full")) {
            interruptType = AutoWintertodtInterruptType.INVENTORY_FULL;
        } else if (messageNode.getValue().startsWith("You fix the brazier")) {
            interruptType = AutoWintertodtInterruptType.FIXED_BRAZIER;
        } else if (messageNode.getValue().startsWith("You light the brazier")) {
            interruptType = AutoWintertodtInterruptType.LIT_BRAZIER;
        } else if (messageNode.getValue().startsWith("The brazier has gone out.")) {
            interruptType = AutoWintertodtInterruptType.BRAZIER_WENT_OUT;
        } else {
            return;
        }

        boolean wasInterrupted = false;
        boolean neverNotify = false;

        switch (interruptType) {
            case COLD:
            case BRAZIER:
            case SNOWFALL:

                // Recolor message for damage notification
                messageNode.setRuneLiteFormatMessage(ColorUtil.wrapWithColorTag(messageNode.getValue(), config.damageNotificationColor()));
                client.refreshChat();

                // all actions except woodcutting and idle are interrupted from damage
                if (currentActivity != AutoWintertodtActivity.WOODCUTTING && currentActivity != AutoWintertodtActivity.IDLE) {
                    wasInterrupted = true;
                }

                break;
            case INVENTORY_FULL:
            case OUT_OF_ROOTS:
            case BRAZIER_WENT_OUT:
                wasInterrupted = true;
                break;
            case LIT_BRAZIER:
            case FIXED_BRAZIER:
                wasInterrupted = true;
                neverNotify = true;
                break;
        }

        if (!neverNotify) {
            boolean shouldNotify = false;
            switch (interruptType) {
                case COLD:
                    WintertodtNotifyDamage notify = config.notifyCold();
                    shouldNotify = notify == ALWAYS || (notify == INTERRUPT && wasInterrupted);
                    break;
                case SNOWFALL:
                    notify = config.notifySnowfall();
                    shouldNotify = notify == ALWAYS || (notify == INTERRUPT && wasInterrupted);
                    break;
                case BRAZIER:
                    notify = config.notifyBrazierDamage();
                    shouldNotify = notify == ALWAYS || (notify == INTERRUPT && wasInterrupted);
                    break;
                case INVENTORY_FULL:
                    shouldNotify = config.notifyFullInv();
                    break;
                case OUT_OF_ROOTS:
                    shouldNotify = config.notifyEmptyInv();
                    break;
                case BRAZIER_WENT_OUT:
                    shouldNotify = config.notifyBrazierOut();
                    break;
            }

            if (shouldNotify) {
                notifyInterrupted(interruptType, wasInterrupted);
            }
        }

        if (wasInterrupted) {
            currentActivity = AutoWintertodtActivity.IDLE;
        }
    }

    private void notifyInterrupted(AutoWintertodtInterruptType interruptType, boolean wasActivityInterrupted) {
        final StringBuilder str = new StringBuilder();

        str.append("Wintertodt: ");

        if (wasActivityInterrupted) {
            str.append(currentActivity.getActionString());
            str.append(" interrupted! ");
        }

        str.append(interruptType.getInterruptSourceString());

        String notification = str.toString();
        log.debug("Sending notification: {}", notification);
        notifier.notify(notification);
    }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event) {
        if (!isInWintertodt) {
            return;
        }

        final Player local = client.getLocalPlayer();

        if (event.getActor() != local) {
            return;
        }

        final int animId = local.getAnimation();
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

            case FLETCHING_BOW_CUTTING:
                setActivity(AutoWintertodtActivity.FLETCHING);
                break;

            case LOOKING_INTO:
                setActivity(AutoWintertodtActivity.FEEDING_BRAZIER);
                break;

            case FIREMAKING:
                setActivity(AutoWintertodtActivity.LIGHTING_BRAZIER);
                break;

            case CONSTRUCTION:
            case CONSTRUCTION_IMCANDO:
                setActivity(AutoWintertodtActivity.FIXING_BRAZIER);
                break;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        final ItemContainer container = event.getItemContainer();

        if (!isInWintertodt || container != client.getItemContainer(InventoryID.INVENTORY)) {
            return;
        }

        final Item[] inv = container.getItems();

        inventoryScore = 0;
        totalPotentialinventoryScore = 0;
        numLogs = 0;
        numKindling = 0;

        for (Item item : inv) {
            inventoryScore += getPoints(item.getId());
            totalPotentialinventoryScore += getPotentialPoints(item.getId());

            switch (item.getId()) {
                case BRUMA_ROOT:
                    ++numLogs;
                    break;
                case BRUMA_KINDLING:
                    ++numKindling;
                    break;
            }
        }

        //If we're currently fletching but there are no more logs, go ahead and abort fletching immediately
        if (numLogs == 0 && currentActivity == AutoWintertodtActivity.FLETCHING) {
            currentActivity = AutoWintertodtActivity.IDLE;
        }
        //Otherwise, if we're currently feeding the brazier but we've run out of both logs and kindling, abort the feeding activity
        else if (numLogs == 0 && numKindling == 0 && currentActivity == AutoWintertodtActivity.FEEDING_BRAZIER) {
            currentActivity = AutoWintertodtActivity.IDLE;
        }
    }

    private void setActivity(AutoWintertodtActivity action) {
        currentActivity = action;
        lastActionTime = Instant.now();
    }

    private static int getPoints(int id) {
        switch (id) {
            case BRUMA_ROOT:
                return 10;
            case BRUMA_KINDLING:
                return 25;
            default:
                return 0;
        }
    }

    private static int getPotentialPoints(int id) {
        switch (id) {
            case BRUMA_ROOT:
            case BRUMA_KINDLING:
                return 25;
            default:
                return 0;
        }
    }
}
