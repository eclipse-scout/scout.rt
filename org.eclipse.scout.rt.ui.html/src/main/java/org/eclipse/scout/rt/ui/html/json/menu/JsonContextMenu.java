/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuEvent;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuListener;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
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
  private final Set<IJsonAdapter<?>> m_jsonMenuAdapters = new HashSet<>();

  private final IUiSession m_uiSession;
  private final CONTEXT_MENU m_model;
  private final IJsonAdapter<?> m_parent;
  private final Predicate<IMenu> m_filter;
  private final Consumer<FilteredJsonAdapterIds<?>> m_structureChangeHandler;

  public JsonContextMenu(CONTEXT_MENU model, IJsonAdapter<?> parent) {
    this(model, parent, new DisplayableActionFilter<>());
  }

  public JsonContextMenu(CONTEXT_MENU model, IJsonAdapter<?> parent, Predicate<IMenu> filter) {
    this(model, parent, filter, null);
  }

  public JsonContextMenu(CONTEXT_MENU model, IJsonAdapter<?> parent, Predicate<IMenu> filter, Consumer<FilteredJsonAdapterIds<?>> structureChangedHandler) {
    if (model == null) {
      throw new IllegalArgumentException("model must not be null");
    }
    m_model = model;
    m_parent = parent;
    m_uiSession = parent.getUiSession();
    m_filter = filter;
    if (structureChangedHandler == null) {
      structureChangedHandler = new P_StructureChangedHandler();
    }
    m_structureChangeHandler = structureChangedHandler;
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

  public Predicate<IMenu> getFilter() {
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
    Set<IJsonAdapter> jsonMenuAdapters = new HashSet<>(m_jsonMenuAdapters);
    for (IJsonAdapter<?> adapter : jsonMenuAdapters) {
      // Dispose adapter only if its model is not part of the new models
      if (!getModel().getChildActions().contains((adapter.getModel()))) {
        adapter.dispose();
        m_jsonMenuAdapters.remove(adapter);
      }
    }
    List<IJsonAdapter<?>> menuAdapters = getParent().attachAdapters(getModel().getChildActions(), getFilter());
    m_jsonMenuAdapters.addAll(menuAdapters);

    @SuppressWarnings("unchecked")
    FilteredJsonAdapterIds<?> filteredAdapters = new FilteredJsonAdapterIds(menuAdapters, getFilter());
    m_structureChangeHandler.accept(filteredAdapters);
  }

  protected class P_ContextMenuListener implements ContextMenuListener {

    @Override
    public void contextMenuChanged(ContextMenuEvent event) {
      handleModelContextMenuChanged(event);
    }
  }

  protected class P_StructureChangedHandler implements Consumer<FilteredJsonAdapterIds<?>> {

    @Override
    public void accept(FilteredJsonAdapterIds<?> filteredAdapters) {
      IJsonAdapter<?> parent = getParent();

      if (!(parent instanceof IJsonContextMenuOwner)) {
        throw new IllegalStateException("Parent is not a context menu owner, context menu changed event cannot be handled. Parent: " + parent);
      }
      ((IJsonContextMenuOwner) parent).handleModelContextMenuChanged(filteredAdapters);
    }
  }
}
