/*
 * Copyright (c) 2018-2019, Shaun Dreclin <https://github.com/ShaunDreclin>
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
package net.runelite.client.plugins.daeyaltmining;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectID;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDescriptor(
        name = "Daeyalt Mining",
        description = "Better DaeyaltMining Something Something",
        tags = {"mining"}
)
public class DaeyaltMiningPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DaeyaltMiningOverlay overlay;

    public GameObject current_daeyalt_essence;
    public boolean animating = false;
    public Integer count = -1;
    public int firstoffset = 3;
    public boolean first = true;

    public final int DAEYALT_REGION = 14744;
    public final int IDLE_ANIMATION_ID = -1;

    @Override

    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        current_daeyalt_essence = null;
    }

    @Subscribe
    private void onGameStateChanged(final GameStateChanged event) {
        final GameState gameState = event.getGameState();
        switch (gameState) {
            case LOGGED_IN:
                current_daeyalt_essence = null;
                count = -1;
                firstoffset = 3;
                first = true;
                break;
        }
    }

    @Subscribe
    private void onGameTick(final GameTick event) {
        if (client.getLocalPlayer().getWorldLocation().getRegionID() == DAEYALT_REGION) {
            animating = client.getLocalPlayer().getAnimation() != IDLE_ANIMATION_ID;
            if (count < 102 && count != -1) {
                count++;
            }
            if (first) {
                firstoffset--;
                if (firstoffset == 0) {
                    first = false;
                }
            }
        }
    }

    @Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == ObjectID.DAEYALT_ESSENCE_39095) {
            current_daeyalt_essence = event.getGameObject();
            if (!first) {
                count = 0;
            }
        }
    }
}