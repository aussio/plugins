/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, TomC <https://github.com/tomcylke>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.oneclick;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetPressed;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.oneclick.comparables.ClickCompare;
import net.runelite.client.plugins.oneclick.comparables.misc.Blank;
import net.runelite.client.plugins.oneclick.comparables.misc.Custom;
import net.runelite.client.plugins.oneclick.comparables.misc.Healer;
import net.runelite.client.plugins.oneclick.comparables.skilling.Spell;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
		name = "One Click",
		description = "OP One Click methods.",
		tags = "ganom"
)
@Getter
@Slf4j
public class OneClickPlugin extends Plugin
{
	private static final String MAGIC_IMBUE_EXPIRED_MESSAGE = "Your Magic Imbue charge has ended.";
	private static final String MAGIC_IMBUE_MESSAGE = "You are charged to combine runes!";

	@Inject
	private Client client;

	@Inject
	private OneClickConfig config;

	private final Map<Integer, String> targetMap = new HashMap<>();

	private ClickCompare comparable = new Blank();
	private boolean enableImbue;
	private boolean imbue;

	static final int BA_CALL_LISTEN = 7;
	public static final int BA_HEALER_GROUP_ID = 488;
	private int widgetTick = -1;
	private MenuEntry widgetEntry = null;

	@Setter
	private boolean tick;

	@Provides
	OneClickConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OneClickConfig.class);
	}

	@Override
	protected void startUp()
	{
		updateConfig();
	}

	@Override
	protected void shutDown()
	{

	}

	@Subscribe
	public void onWidgetPressed(WidgetPressed event)
	{
		for (MenuEntry menuEntry : client.getMenuEntries())
		{
			if (menuEntry.isForceLeftClick())
			{
				widgetEntry = menuEntry;
				widgetTick = client.getTickCount() + 2;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && imbue)
		{
			imbue = false;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"oneclick".equals(event.getGroup()))
		{
			return;
		}
		updateConfig();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		switch (event.getMessage())
		{
			case MAGIC_IMBUE_MESSAGE:
				imbue = true;
				break;
			case MAGIC_IMBUE_EXPIRED_MESSAGE:
				imbue = false;
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getTickCount() == widgetTick && widgetEntry != null)
		{
			widgetEntry = null;
		}

		tick = false;

		if (comparable instanceof Healer)
		{
			Widget widget = client.getWidget(BA_HEALER_GROUP_ID, BA_CALL_LISTEN);

			if (widget != null && widget.getText() != null)
			{
				((Healer) comparable).setRoleText(widget.getText().trim());
			}
			else
			{
				((Healer) comparable).setRoleText("");
			}
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (comparable instanceof Spell)
		{
			((Spell) comparable).onMenuOpened(event);
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!event.isForceLeftClick())
		{
			targetMap.put(event.getIdentifier(), event.getTarget());
		}

/*		if (config.deprioritizeWalk())
		{
			switch (config.getType())
			{
				case SEED_SET:
				case BA_HEALER:
					if (event.getOpcode() == MenuAction.WALK.getId())
					{
						MenuEntry menuEntry = client.getLeftClickMenuEntry();
						menuEntry.setOpcode(MenuAction.WALK.getId() + MENU_ACTION_DEPRIORITIZE_OFFSET);
						client.setLeftClickMenuEntry(menuEntry);
					}
					break;
				default:
					break;
			}
		}*/

		if (comparable == null)
		{
			throw new AssertionError("This should not be possible.");
		}

		if (comparable.isEntryValid(event))
		{
			comparable.modifyEntry(event);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (tick)
		{
			event.consume();
			return;
		}


		if (event.getMenuTarget() == null)
		{
			return;
		}

		if (comparable == null)
		{
			throw new AssertionError("This should not be possible.");
		}

		if (widgetEntry != null && widgetEntry.getType() == event.getMenuAction() && widgetEntry.getIdentifier() == event.getId())
		{
			event.setMenuOption(widgetEntry.getOption());
			event.setMenuTarget(widgetEntry.getTarget());
			event.setMenuAction(widgetEntry.getType());
			event.setId(widgetEntry.getIdentifier());
			event.setParam0(widgetEntry.getParam0());
			event.setParam1(widgetEntry.getParam1());
			widgetEntry = null;
		}

		if (comparable.isClickValid(event))
		{
			comparable.modifyClick(event);
		}
	}

	private void updateConfig()
	{
		enableImbue = config.isUsingImbue();
		Types type = config.getType();
		switch (type)
		{
			case SPELL:
				comparable = config.getSpells().getComparable();
				comparable.setClient(client);
				comparable.setPlugin(this);
				if (comparable instanceof Spell)
				{
					((Spell) comparable).setSpellSelection(config.getSpells());
				}
				break;
			case CUSTOM:
				comparable = type.getComparable();
				comparable.setClient(client);
				comparable.setPlugin(this);
				((Custom) comparable).updateMap(config.swaps());
				break;
			default:
				comparable = type.getComparable();
				comparable.setClient(client);
				comparable.setPlugin(this);
				break;
		}
	}
}
