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
package org.eclipse.che.plugin.git.ide.remove;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;

/**
 * The view of {@link RemoveFromIndexPresenter}.
 *
 * @author Andrey Plotnikov
 */
public interface RemoveFromIndexView extends View<RemoveFromIndexView.ActionDelegate> {

    /** Is needed to delegate some function into CloneRepository view. */
    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Remove button. */
        void onRemoveClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();
    }

    /**
     * Set content into message field.
     *
     * @param message
     *         content of message
     */
    void setMessage(@NotNull String message);

    /** @return <code>true</code> if files need to remove only from index, and <code>false</code> otherwise */
    boolean isRemoved();

    /**
     * Set state for files.
     *
     * @param isRemoved
     *         <code>true</code> to remove file only from index, <code>false</code> to remove files
     */
    void setRemoved(boolean isRemoved);

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}