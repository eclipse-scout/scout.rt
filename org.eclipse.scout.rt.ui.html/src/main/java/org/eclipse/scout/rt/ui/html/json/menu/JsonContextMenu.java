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
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.json.JSONArray;
import org.json.JSONObject;

// FIXME awe: (menu) diese klasse soll kein JsonAdapter mehr sein. Parent der menus ist dann die Table
public class JsonContextMenu<CONTEXT_MENU extends IContextMenu> extends AbstractJsonPropertyObserver<CONTEXT_MENU> {

  private ContextMenuListener m_contextMenuListener;
  private Set<IJsonAdapter<?>> m_jsonMenuAdapters = new HashSet<IJsonAdapter<?>>();

  public JsonContextMenu(CONTEXT_MENU model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    // ContextMenu is just a wrapper, not a real adapter meant to be sent to client
    return null;
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_contextMenuListener != null) {
      throw new IllegalStateException();
    }
    m_contextMenuListener = new P_ContextMenuListener();
    getModel().addContextMenuListener(m_contextMenuListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_contextMenuListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeContextMenuListener(m_contextMenuListener);
    m_contextMenuListener = null;
  }

  @Override
  public JSONObject toJson() {
    // ContextMenu is just a wrapper, not a real adapter meant to be sent to client
    return null;
  }

  public JSONArray childActionsToJson() {
    return JsonAdapterUtility.getAdapterIdsForModel(getUiSession(), getModel().getChildActions(), this, new DisplayableActionFilter<IMenu>());
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    m_jsonMenuAdapters.addAll(attachAdapters(getModel().getChildActions(), new DisplayableActionFilter<IMenu>()));
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
    List<IJsonAdapter<?>> menuAdapters = attachAdapters(getModel().getChildActions(), new DisplayableActionFilter<IMenu>());
    m_jsonMenuAdapters.addAll(menuAdapters);

    IJsonAdapter<?> parent = getParent();
    if (parent.getModel() != event.getSource().getOwner()) {
      // Not sure if this is really possible
      throw new IllegalStateException("The model of the parent is different than the menu owner. Parent: " + parent + ". Owner: " + event.getSource().getOwner().getClass());
    }
    if (!(parent instanceof IJsonContextMenuOwner)) {
      throw new IllegalStateException("Parent is not a context menu owner, context menu changed event cannot be handled. Parent: " + parent);
    }
    ((IJsonContextMenuOwner) parent).handleModelContextMenuChanged(menuAdapters);
  }

  protected class P_ContextMenuListener implements ContextMenuListener {

    @Override
    public void contextMenuChanged(ContextMenuEvent event) {
      handleModelContextMenuChanged(event);
    }
  }
}
