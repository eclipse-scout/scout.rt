/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuEvent;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuListener;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.json.JSONArray;

/**
 * Helper to handle {@link IContextMenu}. The context menu is never sent to client, only the actual menu items. That is
 * the reason why this is not an adapter.
 */
public class JsonContextMenu<CONTEXT_MENU extends IContextMenu> {

  private ContextMenuListener m_contextMenuListener;
  private Set<IJsonAdapter<?>> m_jsonMenuAdapters = new HashSet<IJsonAdapter<?>>();

  private final IUiSession m_uiSession;
  private final CONTEXT_MENU m_model;
  private IJsonAdapter<?> m_parent;
  private IFilter<IMenu> m_filter;

  public JsonContextMenu(CONTEXT_MENU model, IJsonAdapter<?> parent) {
    this(model, parent, new DisplayableActionFilter<IMenu>());
  }

  public JsonContextMenu(CONTEXT_MENU model, IJsonAdapter<?> parent, IFilter<IMenu> filter) {
    if (model == null) {
      throw new IllegalArgumentException("model must not be null");
    }
    m_model = model;
    m_parent = parent;
    m_uiSession = parent.getUiSession();
    m_filter = filter;
  }

  public CONTEXT_MENU getModel() {
    return m_model;
  }

  public IUiSession getUiSession() {
    return m_uiSession;
  }

  public IJsonAdapter<?> getParent() {
    return m_parent;
  }

  public IFilter<IMenu> getFilter() {
    return m_filter;
  }

  public void init() {
    attachModel();
    attachChildAdapters();
  }

  public void dispose() {
    detachModel();
  }

  protected void attachModel() {
    if (m_contextMenuListener != null) {
      throw new IllegalStateException();
    }
    m_contextMenuListener = new P_ContextMenuListener();
    getModel().addContextMenuListener(m_contextMenuListener);
  }

  protected void detachModel() {
    if (m_contextMenuListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeContextMenuListener(m_contextMenuListener);
    m_contextMenuListener = null;
  }

  public JSONArray childActionsToJson() {
    return JsonAdapterUtility.getAdapterIdsForModel(getUiSession(), getModel().getChildActions(), getParent(), getFilter());
  }

  public void attachChildAdapters() {
    m_jsonMenuAdapters.addAll(getParent().attachAdapters(getModel().getChildActions(), getFilter()));
  }

  public void handleModelContextMenuChanged(ContextMenuEvent event) {
    if (ContextMenuEvent.TYPE_STRUCTURE_CHANGED == event.getType()) {
      handleModelContextMenuStructureChanged(event);
    }
  }

  public void handleModelContextMenuStructureChanged(ContextMenuEvent event) {
    Set<IJsonAdapter> jsonMenuAdapters = new HashSet<IJsonAdapter>(m_jsonMenuAdapters);
    for (IJsonAdapter<?> adapter : jsonMenuAdapters) {
      // Dispose adapter only if's model is not part of the new models
      if (!getModel().getChildActions().contains((adapter.getModel()))) {
        adapter.dispose();
        m_jsonMenuAdapters.remove(adapter);
      }
    }
    List<IJsonAdapter<?>> menuAdapters = getParent().attachAdapters(getModel().getChildActions(), getFilter());
    m_jsonMenuAdapters.addAll(menuAdapters);

    IJsonAdapter<?> parent = getParent();
    if (parent.getModel() != event.getSource().getContainer()) {
      // Not sure if this is really possible
      throw new IllegalStateException("The model of the parent is different than the menu container. Parent: " + parent + ". Container: " + event.getSource().getContainer().getClass());
    }
    if (!(parent instanceof IJsonContextMenuOwner)) {
      throw new IllegalStateException("Parent is not a context menu owner, context menu changed event cannot be handled. Parent: " + parent);
    }
    @SuppressWarnings("unchecked")
    FilteredJsonAdapterIds<?> filteredAdapters = new FilteredJsonAdapterIds(menuAdapters, getFilter());
    ((IJsonContextMenuOwner) parent).handleModelContextMenuChanged(filteredAdapters);
  }

  protected class P_ContextMenuListener implements ContextMenuListener {

    @Override
    public void contextMenuChanged(ContextMenuEvent event) {
      handleModelContextMenuChanged(event);
    }
  }
}
