package net.runelite.client.plugins.oneclick.comparables.skilling;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.oneclick.ClickItem;
import net.runelite.client.plugins.oneclick.Spells;
import net.runelite.client.plugins.oneclick.comparables.ClickCompare;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Predicate;

@Slf4j
public class Spell extends ClickCompare {
    private final Predicate<MenuOptionClicked> cast;
    private final Predicate<MenuOptionClicked> reset;
    private final Predicate<MenuOptionClicked> set;
    private final String spell;
    private ClickItem clickItem = null;
    @Setter
    private Spells spellSelection = Spells.NONE;

    public Spell(String spell) {
        this.spell = spell;
        this.cast = (event) -> event.getMenuAction() == MenuAction.WIDGET_TARGET &&
                event.getMenuOption().equals("Cast") &&
                event.getMenuTarget().contains("<col=00ff00>" + spell + "</col><col=ffffff> -> ");
        this.reset = (event) -> event.getMenuAction() == MenuAction.RUNELITE &&
                event.getId() == -1;
        this.set = (event) -> event.getMenuAction() == MenuAction.RUNELITE;
    }

    public void onMenuOpened(MenuOpened event) {
        if (client == null) {
            return;
        }

        final MenuEntry firstEntry = event.getFirstEntry();

        if (firstEntry == null) {
            return;
        }

        final int widgetId = firstEntry.getParam1();

        if (widgetId == WidgetInfo.INVENTORY.getId()) {
            final Widget spell = client.getWidget(spellSelection.getWidgetInfo());

            if (spell == null) {
                return;
            }

            switch (spellSelection) {
                case HIGH_ALCH:
                    if (spell.getSpriteId() != SpriteID.SPELL_HIGH_LEVEL_ALCHEMY ||
                            spell.getSpriteId() == SpriteID.SPELL_HIGH_LEVEL_ALCHEMY_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 55 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case SUPERHEAT:
                    if (spell.getSpriteId() != SpriteID.SPELL_SUPERHEAT_ITEM ||
                            spell.getSpriteId() == SpriteID.SPELL_SUPERHEAT_ITEM_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 43 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case ENCHANT_SAPPHIRE:
                    if (spell.getSpriteId() != SpriteID.SPELL_LVL_1_ENCHANT ||
                            spell.getSpriteId() == SpriteID.SPELL_LVL_1_ENCHANT_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 7 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case ENCHANT_EMERALD:
                    if (spell.getSpriteId() != SpriteID.SPELL_LVL_2_ENCHANT ||
                            spell.getSpriteId() == SpriteID.SPELL_LVL_2_ENCHANT_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 27 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case ENCHANT_RUBY:
                    if (spell.getSpriteId() != SpriteID.SPELL_LVL_3_ENCHANT ||
                            spell.getSpriteId() == SpriteID.SPELL_LVL_3_ENCHANT_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 49 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case ENCHANT_DIAMOND:
                    if (spell.getSpriteId() != SpriteID.SPELL_LVL_4_ENCHANT ||
                            spell.getSpriteId() == SpriteID.SPELL_LVL_4_ENCHANT_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 57 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case ENCHANT_DRAGONSTONE:
                    if (spell.getSpriteId() != SpriteID.SPELL_LVL_5_ENCHANT ||
                            spell.getSpriteId() == SpriteID.SPELL_LVL_5_ENCHANT_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 68 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case ENCHANT_ONYX:
                    if (spell.getSpriteId() != SpriteID.SPELL_LVL_6_ENCHANT ||
                            spell.getSpriteId() == SpriteID.SPELL_LVL_6_ENCHANT_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 87 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                case ENCHANT_ZENYTE:
                    if (spell.getSpriteId() != SpriteID.SPELL_LVL_7_ENCHANT ||
                            spell.getSpriteId() == SpriteID.SPELL_LVL_7_ENCHANT_DISABLED ||
                            client.getBoostedSkillLevel(Skill.MAGIC) < 93 ||
                            client.getVarbitValue(4070) != 0) {
                        clickItem = null;
                        return;
                    }
                    break;
                default:
                    clickItem = null;
                    break;

            }

            final int itemId = firstEntry.getIdentifier();

            if (itemId == -1) {
                return;
            }

/*			final MenuEntry[] menuList = new MenuEntry[event.getMenuEntries().length + 1];

			for (int i = event.getMenuEntries().length - 1; i >= 0; i--)
			{
				if (i == 0)
				{
					menuList[i] = event.getMenuEntries()[i];
				}
				else
				{
					menuList[i + 1] = event.getMenuEntries()[i];
				}
			}*/

            final boolean set = clickItem != null && clickItem.getId() == firstEntry.getIdentifier();
            String target = "";
            String option = set ? "Unset" : "Set";

            switch (spellSelection) {
                case HIGH_ALCH:
                    target = "<col=00ff00>High Alchemy Item <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case SUPERHEAT:
                    target = "<col=00ff00>Superheat Item <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case ENCHANT_SAPPHIRE:
                    target = "<col=00ff00>Lvl-1 Enchant <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case ENCHANT_EMERALD:
                    target = "<col=00ff00>Lvl-2 Enchant <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case ENCHANT_RUBY:
                    target = "<col=00ff00>Lvl-3 Enchant <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case ENCHANT_DIAMOND:
                    target = "<col=00ff00>Lvl-4 Enchant <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case ENCHANT_DRAGONSTONE:
                    target = "<col=00ff00>Lvl-5 Enchant <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case ENCHANT_ONYX:
                    target = "<col=00ff00>Lvl-6 Enchant <col=ffffff> -> " + firstEntry.getTarget();
                    break;
                case ENCHANT_ZENYTE:
                    target = "<col=00ff00>Lvl-7 Enchant <col=ffffff> -> " + firstEntry.getTarget();
                    break;
            }

            client.createMenuEntry(1)
                    .setOption(option)
                    .setTarget(target)
                    .setType(MenuAction.RUNELITE)
                    .setIdentifier(set ? -1 : firstEntry.getIdentifier())
                    .setParam0(0)
                    .setParam1(widgetId)
                    .setForceLeftClick(false);
        }
    }

    @Override
    public boolean isEntryValid(MenuEntryAdded event) {
        return event.getOpcode() == MenuAction.WIDGET_TARGET.getId() &&
                event.getOption().equals("Cast") &&
                event.getTarget().equals("<col=00ff00>" + spell + "</col>");
    }

    @Override
    public void modifyEntry(MenuEntryAdded event) {
        if (client == null || plugin == null || clickItem == null) {
            return;
        }

        client.createMenuEntry(-1)
                .setOption("Cast")
                .setTarget("<col=00ff00>" + spell + "</col>" + "<col=ffffff> -> " + clickItem.getName())
                .setType(event.getMenuAction())
                .setIdentifier(event.getIdentifier())
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1())
                .setForceLeftClick(true);
    }

    @Override
    public boolean isClickValid(MenuOptionClicked event) {
        return cast.test(event) || reset.test(event) || set.test(event);
    }

    @Override
    public void modifyClick(MenuOptionClicked event) {
        if (plugin == null || client == null) {
            return;
        }

        if (cast.test(event)) {
            if (clickItem == null) {
                return;
            }

            final Pair<Integer, Integer> pair = findItem(clickItem.getId());

            if (pair.getLeft() != -1) {
                event.setMenuAction(MenuAction.WIDGET_USE_ON_ITEM);
                event.setId(pair.getLeft());
                event.setActionParam(pair.getRight());
                event.setWidgetId(WidgetInfo.INVENTORY.getId());
                client.setSelectedSpellName("<col=00ff00>" + spell + "</col>" + "<col=ffffff>");
                client.setSelectedSpellWidget(spellSelection.getWidgetInfo().getId());
            }
        } else if (reset.test(event)) {
            clickItem = null;
        } else if (set.test(event)) {
            final String itemName = event.getMenuTarget().split("->")[1];
            clickItem = new ClickItem(itemName, event.getId());
        }
    }

    @Override
    public void backupEntryModify(MenuEntry e) {
        if (plugin == null || clickItem == null) {
            return;
        }

        e.setOption("Cast");
        e.setTarget("<col=00ff00>" + spell + "</col>" + "<col=ffffff> -> " + clickItem.getName());
        e.setForceLeftClick(true);
    }
}
