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
package org.eclipse.che.ide.command.toolbar.previewurl;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropDownList;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/** Implementation of {@link PreviewUrlListView} that displays preview URLs in a drop down list. */
@Singleton
public class PreviewUrlListViewImpl implements PreviewUrlListView {

    /** Mapping of URL to list item. */
    private final Map<String, BaseListItem<String>> listItems;
    private final DropDownList                      dropDownList;

    private ActionDelegate delegate;

    @Inject
    public PreviewUrlListViewImpl() {
        listItems = new HashMap<>();

        dropDownList = new DropDownList(PreviewUrlItemRenderer.HEADER_WIDGET);
        dropDownList.setWidth("90px");
        dropDownList.setSelectionHandler(item -> {
            for (Entry<String, BaseListItem<String>> entry : listItems.entrySet()) {
                if (item.equals(entry.getValue())) {
                    delegate.onUrlChosen(entry.getKey());
                    return;
                }
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return dropDownList;
    }

    @Override
    public void addUrl(String url) {
        if (listItems.containsKey(url)) {
            return; // no sense to add the equals URLs even if it belong to different commands
        }

        final BaseListItem<String> listItem = new BaseListItem<>(url);
        final PreviewUrlItemRenderer renderer = new PreviewUrlItemRenderer(listItem);

        listItems.put(url, listItem);
        dropDownList.addItem(listItem, renderer);
    }

    @Override
    public void removeUrl(String url) {
        final BaseListItem<String> listItem = listItems.remove(url);

        if (listItem != null) {
            dropDownList.removeItem(listItem);
        }
    }

    @Override
    public void clearList() {
        listItems.clear();
        dropDownList.clear();
    }
}
