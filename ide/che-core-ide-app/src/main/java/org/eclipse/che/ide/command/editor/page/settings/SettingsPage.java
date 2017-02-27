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
package org.eclipse.che.ide.command.editor.page.settings;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.BaseCommandGoal;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * {@link CommandEditorPage} which allows to edit basic command's settings, e.g.:
 * <ul>
 * <li>goal;</li>
 * <li>applicable context.</li>
 * </ul>
 *
 * @author Artem Zatsarynnyi
 */
public class SettingsPage extends AbstractCommandEditorPage implements SettingsPageView.ActionDelegate,
                                                                       ResourceChangedHandler {

    private final SettingsPageView              view;
    private final AppContext                    appContext;
    private final PredefinedCommandGoalRegistry goalRegistry;
    private final CommandManager                commandManager;

    private final Map<Project, Boolean> projectsState;

    /** Initial value of the command's goal. */
    private String       goalInitial;
    /** Initial value of the workspace flag. */
    private boolean      workspaceInitial;
    /** Initial value of the applicable projects list. */
    private List<String> applicableProjectsInitial;

    @Inject
    public SettingsPage(SettingsPageView view,
                        AppContext appContext,
                        PredefinedCommandGoalRegistry predefinedCommandGoalRegistry,
                        CommandManager commandManager,
                        EditorMessages messages,
                        EventBus eventBus) {
        super(messages.pageSettingsTitle());

        this.view = view;
        this.appContext = appContext;
        this.goalRegistry = predefinedCommandGoalRegistry;
        this.commandManager = commandManager;

        eventBus.addHandler(ResourceChangedEvent.getType(), this);

        projectsState = new HashMap<>();

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        final String goalId = editedCommand.getGoal();
        final CommandGoal commandGoal = goalRegistry.getGoalForId(goalId);
        final ApplicableContext context = editedCommand.getApplicableContext();

        goalInitial = commandGoal.getId();
        workspaceInitial = context.isWorkspaceApplicable();
        applicableProjectsInitial = new ArrayList<>(context.getApplicableProjects());

        final Set<CommandGoal> goals = new HashSet<>();
        goals.addAll(goalRegistry.getAllGoals());
        goals.addAll(getCustomGoals());

        view.setAvailableGoals(goals);
        view.setGoal(commandGoal.getId());
        view.setWorkspace(editedCommand.getApplicableContext().isWorkspaceApplicable());

        refreshProjects();
    }

    /** Refresh 'Projects' section in the view. */
    private void refreshProjects() {
        projectsState.clear();

        final ApplicableContext context = editedCommand.getApplicableContext();

        for (Project project : appContext.getProjects()) {
            final boolean applicable = context.getApplicableProjects().contains(project.getPath());

            projectsState.put(project, applicable);
        }

        view.setProjects(projectsState);
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        final CommandGoal commandGoal = goalRegistry.getGoalForId(editedCommand.getGoal());
        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        return !(goalInitial.equals(commandGoal.getId()) &&
                 workspaceInitial == applicableContext.isWorkspaceApplicable() &&
                 applicableProjectsInitial.equals(applicableContext.getApplicableProjects()));
    }

    @Override
    public void onGoalChanged(String goalId) {
        editedCommand.setGoal(goalId);

        notifyDirtyStateChanged();
    }

    @Override
    public void onWorkspaceChanged(boolean value) {
        editedCommand.getApplicableContext().setWorkspaceApplicable(value);

        notifyDirtyStateChanged();
    }

    @Override
    public void onApplicableProjectChanged(Project project, boolean value) {
        projectsState.put(project, value);

        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        if (value) {
            applicableContext.addProject(project.getPath());
        } else {
            applicableContext.removeProject(project.getPath());
        }

        notifyDirtyStateChanged();
    }

    /** Returns all custom (non-predefined) command goals. */
    private Set<CommandGoal> getCustomGoals() {
        final Set<CommandGoal> list = new HashSet<>();

        for (ContextualCommand command : commandManager.getCommands()) {
            final String goalId = command.getGoal();

            final Optional<CommandGoal> goalOptional = goalRegistry.getGoalById(goalId);
            if (!goalOptional.isPresent() && !isNullOrEmpty(goalId)) {
                list.add(new BaseCommandGoal(goalId));
            }
        }

        return list;
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();
        final Resource resource = delta.getResource();

        if (resource.isProject()) {
            // defer refreshing the projects section since appContext#getProjects may return old data
            Scheduler.get().scheduleDeferred(this::refreshProjects);
        }
    }
}
