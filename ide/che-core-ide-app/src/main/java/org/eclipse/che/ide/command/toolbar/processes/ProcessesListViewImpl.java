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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Singleton;

import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropdownList;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/** Implementation of {@link ProcessesListView} that displays processes in a dropdown list. */
@Singleton
public class ProcessesListViewImpl implements ProcessesListView {

    private final FlowPanel    rootPanel;
    private final DropdownList dropdownList;

    private final Map<Process, BaseListItem<Process>> listItems;
    private final Map<Process, ProcessItemRenderer>   renderers;
    private final CommandResources                    resources;

    private ActionDelegate delegate;

    @Inject
    public ProcessesListViewImpl(CommandResources resources) {
        this.resources = resources;

        listItems = new HashMap<>();
        renderers = new HashMap<>();

        final Label label = new Label("EXEC");
        label.addStyleName(resources.commandToolbarCss().processesListLabel());

        dropdownList = new DropdownList(getEmptyListWidget());
        dropdownList.setWidth("400px");
        dropdownList.setDropdownPanelWidth("400px");
        dropdownList.setSelectionHandler(item -> listItems.entrySet()
                                                          .stream()
                                                          .filter(entry -> item.equals(entry.getValue()))
                                                          .forEach(entry -> delegate.onProcessChosen(entry.getKey())));

        rootPanel = new FlowPanel();
        rootPanel.add(label);
        rootPanel.add(dropdownList);
    }

    private Widget getEmptyListWidget() {
        final Label commandNameLabel = new InlineHTML("Ready");
        commandNameLabel.addStyleName(resources.commandToolbarCss().processWidgetText());
        commandNameLabel.addStyleName(resources.commandToolbarCss().processWidgetCommandNameLabel());

        final Label machineNameLabel = new InlineHTML("&nbsp; - start command");
        machineNameLabel.addStyleName(resources.commandToolbarCss().processWidgetText());
        machineNameLabel.addStyleName(resources.commandToolbarCss().processWidgetMachineNameLabel());

        final FlowPanel emptyListWidget = new FlowPanel();
        emptyListWidget.addStyleName(resources.commandToolbarCss().processWidgetText());
        emptyListWidget.add(commandNameLabel);
        emptyListWidget.add(machineNameLabel);

        return emptyListWidget;
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @Override
    public void clearList() {
        dropdownList.clear();
    }

    @Override
    public void notifyProcessStopped(Process process) {
        final ProcessItemRenderer renderer = renderers.get(process);

        if (renderer != null) {
            renderer.notifyProcessStopped();
        }
    }

    @Override
    public void addProcess(Process process) {
        final BaseListItem<Process> listItem = new BaseListItem<>(process);
        final ProcessItemRenderer renderer = new ProcessItemRenderer(listItem,
                                                                     p -> delegate.onStopProcess(p),
                                                                     p -> delegate.onReRunProcess(p));

        listItems.put(process, listItem);
        renderers.put(process, renderer);

        dropdownList.addItem(listItem, renderer);
    }

    @Override
    public void removeProcess(Process process) {
        final BaseListItem<Process> listItem = listItems.get(process);

        if (listItem != null) {
            listItems.remove(process);
            renderers.remove(process);

            dropdownList.removeItem(listItem);
        }
    }
}
