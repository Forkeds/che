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
package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.command.toolbar.commands.ExecuteCommandPresenter;
import org.eclipse.che.ide.command.toolbar.previewurl.PreviewUrlListPresenter;
import org.eclipse.che.ide.command.toolbar.processes.ProcessesListPresenter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Presenter for run and debug buttons
 */
@Singleton
public class CommandToolbarPresenter implements Presenter, CommandToolbarView.ActionDelegate {

    private final ProcessesListPresenter  processesListPresenter;
    private final PreviewUrlListPresenter previewUrlListPresenter;
    private final ExecuteCommandPresenter executeCommandPresenter;
    private final CommandToolbarView      view;

    @Inject
    public CommandToolbarPresenter(CommandToolbarView view,
                                   ProcessesListPresenter processesListPresenter,
                                   PreviewUrlListPresenter previewUrlListPresenter,
                                   ExecuteCommandPresenter executeCommandPresenter) {
        this.view = view;
        this.processesListPresenter = processesListPresenter;
        this.previewUrlListPresenter = previewUrlListPresenter;
        this.executeCommandPresenter = executeCommandPresenter;

        view.setDelegate(this);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        executeCommandPresenter.go(view.getCommandsPanelContainer());
        processesListPresenter.go(view.getProcessesListContainer());
        previewUrlListPresenter.go(view.getPreviewUrlsListContainer());
    }
}
