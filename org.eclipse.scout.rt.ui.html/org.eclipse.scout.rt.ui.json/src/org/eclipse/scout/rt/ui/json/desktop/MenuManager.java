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
package org.eclipse.scout.rt.ui.json.desktop;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.menu.JsonMenu;
import org.json.JSONArray;

public class MenuManager {
  private IJsonSession m_jsonSession;
  private List<JsonMenu> m_jsonSelectionMenus;
  private List<JsonMenu> m_jsonEmptySpaceMenus;

  public MenuManager(IJsonSession session) {
    m_jsonSession = session;
    m_jsonSelectionMenus = new LinkedList<>();
    m_jsonEmptySpaceMenus = new LinkedList<>();
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  public void replaceEmptySpaceMenus(List<IMenu> menus) {
    disposeSelectionMenus();
    createSelectionMenus(menus);
  }

  public void createEmptySpaceMenus(List<IMenu> menus) {
    for (IMenu menu : menus) {
      JsonMenu jsonMenu = (JsonMenu) getJsonSession().getOrCreateJsonRenderer(menu);
      m_jsonEmptySpaceMenus.add(jsonMenu);
    }
  }

  public void disposeEmptySpaceMenus() {
    for (JsonMenu menu : m_jsonEmptySpaceMenus) {
      menu.dispose();
    }
    m_jsonEmptySpaceMenus.clear();
  }

  public boolean replaceSelectionMenus(List<IMenu> menus) {
    List<IMenu> existingMenus = new ArrayList<>(m_jsonSelectionMenus.size());
    for (JsonMenu menu : m_jsonSelectionMenus) {
      existingMenus.add(menu.getModelObject());
    }
    if (CollectionUtility.equalsCollection(existingMenus, menus, false)) {
      return false;
    }

    disposeSelectionMenus();
    createSelectionMenus(menus);
    return true;
  }

  public void createSelectionMenus(List<IMenu> menus) {
    for (IMenu menu : menus) {
      JsonMenu jsonMenu = (JsonMenu) getJsonSession().getOrCreateJsonRenderer(menu);
      m_jsonSelectionMenus.add(jsonMenu);
    }
  }

  public void disposeSelectionMenus() {
    for (JsonMenu menu : m_jsonSelectionMenus) {
      menu.dispose();
    }
    m_jsonSelectionMenus.clear();
  }

  public void dispose() {
    disposeEmptySpaceMenus();
    disposeSelectionMenus();
  }

  public JSONArray getJsonSelectionMenus() {
    JSONArray jsonArray = new JSONArray();
    for (JsonMenu menu : m_jsonSelectionMenus) {
      jsonArray.put(menu.toJson());
    }
    return jsonArray;
  }

  public JSONArray getJsonEmptySpaceMenus() {
    JSONArray jsonArray = new JSONArray();
    for (JsonMenu menu : m_jsonEmptySpaceMenus) {
      jsonArray.put(menu.toJson());
    }
    return jsonArray;
  }
}
