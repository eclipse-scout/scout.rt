/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.menu;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuEvent;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuListener;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.json.JSONObject;

// FIXME AWE: (menu) diese klasse soll kein JsonAdapter mehr sein. Parent der menus ist dann die Table

public class JsonContextMenu<T extends IContextMenu> extends AbstractJsonPropertyObserver<T> {

  private ContextMenuListener m_contextMenuListener;
  private List<IMenu> m_menus;

  public JsonContextMenu(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
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

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    m_menus = getModel().getChildActions();
    attachAdapters(m_menus);
  }

  @Override
  public String getObjectType() {
    return "ContextMenu";
  }

  public void handleModelContextMenuChanged(ContextMenuEvent event) {
    if (ContextMenuEvent.TYPE_STRUCTURE_CHANGED == event.getEventType()) {
      handleModelContextMenuStructureChanged(event);
    }
  }

  public void handleModelContextMenuStructureChanged(ContextMenuEvent event) {
    //Dispose the removed menus
    m_menus.removeAll(getModel().getChildActions());
    for (Object model : m_menus) {
      IJsonAdapter<Object> jsonAdapter = getJsonSession().getJsonAdapter(model, this, false);
      jsonAdapter.dispose();
    }
    m_menus = getModel().getChildActions();

    IJsonAdapter<?> owner = getAdapter(event.getSource().getOwner());
    List<IJsonAdapter<?>> menuAdapters = attachAdapters(m_menus);
    if (owner instanceof IContextMenuOwner) {
      ((IContextMenuOwner) owner).handleModelContextMenuChanged(menuAdapters);
    }
  }

  public Collection<IJsonAdapter<?>> getJsonChildActions() {
    Collection<IJsonAdapter<?>> adapters = getAdapters(getModel().getChildActions());
    return adapters;
  }

  protected class P_ContextMenuListener implements ContextMenuListener {

    @Override
    public void contextMenuChanged(ContextMenuEvent event) {
      handleModelContextMenuChanged(event);
    }

  }

}
