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
package org.eclipse.che.ide.command.editor.page.name;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

/**
 * {@link CommandEditorPage} which allows to edit command's name.
 *
 * @author Artem Zatsarynnyi
 */
public class NamePage extends AbstractCommandEditorPage implements NamePageView.ActionDelegate {

    private final NamePageView view;

    /** Initial value of the command's name. */
    private String commandNameInitial;

    @Inject
    public NamePage(NamePageView view, EditorMessages messages) {
        super(messages.pageInfoTitle());

        this.view = view;

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        commandNameInitial = editedCommand.getName();

        view.setCommandName(editedCommand.getName());
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        return !(commandNameInitial.equals(editedCommand.getName()));
    }

    @Override
    public void onNameChanged(String name) {
        editedCommand.setName(name);

        notifyDirtyStateChanged();
    }
}
