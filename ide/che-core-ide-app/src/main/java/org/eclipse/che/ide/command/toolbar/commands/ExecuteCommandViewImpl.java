/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar.commands;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.goal.DebugGoal;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.toolbar.commands.button.CommandPopupItem;
import org.eclipse.che.ide.command.toolbar.commands.button.CommandsButton;
import org.eclipse.che.ide.command.toolbar.commands.button.CommandsDataProvider;
import org.eclipse.che.ide.command.toolbar.commands.button.MachinePopupItem;
import org.eclipse.che.ide.ui.menubutton.MenuPopupButton;
import org.eclipse.che.ide.ui.menubutton.PopupActionHandler;
import org.eclipse.che.ide.ui.menubutton.PopupItem;
import org.eclipse.che.ide.ui.menubutton.PopupItemDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Implementation of {@link ExecuteCommandView} uses {@link MenuPopupButton}s for displaying commands of each goal.
 * Allows to execute command by choosing one from the button's dropdown menu.
 */
@Singleton
public class ExecuteCommandViewImpl implements ExecuteCommandView {

    private final Map<CommandGoal, List<ContextualCommand>> commands;
    /** Stores created buttons by goals in order to reuse it. */
    private final Map<CommandGoal, CommandsButton>          buttonsCache;

    private final FlowPanel        buttonsPanel;
    private final CommandResources resources;
    private final AppContext       appContext;
    private final RunGoal          runGoal;
    private final DebugGoal        debugGoal;

    private ActionDelegate delegate;

    @Inject
    public ExecuteCommandViewImpl(CommandResources resources,
                                  AppContext appContext,
                                  RunGoal runGoal,
                                  DebugGoal debugGoal) {
        this.resources = resources;
        this.appContext = appContext;
        this.runGoal = runGoal;
        this.debugGoal = debugGoal;
        commands = new HashMap<>();
        buttonsCache = new HashMap<>();
        buttonsPanel = new FlowPanel();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return buttonsPanel;
    }

    @Override
    public void setCommands(Map<CommandGoal, List<ContextualCommand>> commands) {
        this.commands.clear();
        this.commands.putAll(commands);

        buttonsCache.clear();
        buttonsPanel.clear();

        createOrUpdateButtons();
    }

    /** Add buttons with commands to panel. */
    private void createOrUpdateButtons() {
        // for now, display commands of Run and Debug goals only
        List<CommandGoal> goals = new ArrayList<>();
        goals.add(runGoal);
        goals.add(debugGoal);

        goals.forEach(this::createOrUpdateButton);
    }

    /** Add button with commands of the given goal to panel. */
    private void createOrUpdateButton(CommandGoal goal) {
        final CommandsButton button = buttonsCache.getOrDefault(goal, createButton(goal));
        buttonsCache.put(goal, button);

        final List<ContextualCommand> commandsOfGoal = commands.getOrDefault(goal, emptyList());
        final CommandsDataProvider dataProvider = (CommandsDataProvider)button.getPopupItemDataProvider();

        dataProvider.setCommands(commandsOfGoal);

        updateButtonTooltip(button);

        buttonsPanel.add(button);
    }

    private void updateButtonTooltip(CommandsButton button) {
        final PopupItemDataProvider itemsProvider = button.getPopupItemDataProvider();
        final PopupItem defaultItem = itemsProvider.getDefaultItem();
        final List<PopupItem> items = itemsProvider.getItems();

        if (defaultItem != null) {
            button.setTitle("Execute " + defaultItem.getName());
        } else if (items.isEmpty()) {
            button.setTitle("No command defined for " + button.getGoal().getId() + ". Configure it in Commands panel.");
        } else {
            button.setTitle("Choose command of " + button.getGoal().getId() + " goal to execute");
        }
    }

    /** Creates and returns new {@link CommandsButton} for the specified {@code goal}. */
    private CommandsButton createButton(CommandGoal goal) {
        final CommandsDataProvider dataProvider = new CommandsDataProvider(appContext);

        final CommandsButton button = new CommandsButton(goal, getIconForGoal(goal), dataProvider, new PopupActionHandler() {
            @Override
            public void onItemSelected(PopupItem item) {
                if (item instanceof CommandPopupItem) {
                    final ContextualCommand command = ((CommandPopupItem)item).getCommand();

                    delegate.onCommandExecute(command, null);
                } else if (item instanceof MachinePopupItem) {
                    final MachinePopupItem machinePopupItem = (MachinePopupItem)item;

                    delegate.onCommandExecute(machinePopupItem.getCommand(), machinePopupItem.getMachine());
                }

                dataProvider.setDefaultItem(item);
            }
        });

        button.addStyleName(resources.commandToolbarCss().toolbarButton());

        return button;
    }

    /** Returns {@link FontAwesome}-icon for the given goal. */
    private SafeHtml getIconForGoal(CommandGoal goal) {
        final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();

        if (goal.equals(runGoal)) {
            safeHtmlBuilder.appendHtmlConstant(FontAwesome.PLAY);
        } else if (goal.equals(debugGoal)) {
            safeHtmlBuilder.appendHtmlConstant(FontAwesome.BUG);
        }

        return safeHtmlBuilder.toSafeHtml();
    }
}
