/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public class PageFormMap {
  private Map<String, List<IPageForm>> m_pageFormMaps;

  public PageFormMap() {
    m_pageFormMaps = new HashMap<String, List<IPageForm>>();
  }

  public IPageForm get(IPage<?> page) {
    return get(page, false);
  }

  public IPageForm get(IPage<?> page, boolean onlyVisible) {
    if (page == null) {
      return null;
    }

    for (List<IPageForm> pageFormList : m_pageFormMaps.values()) {
      if (pageFormList == null) {
        continue;
      }

      for (IPageForm pageForm : pageFormList) {
        if (page.equals(pageForm.getPage())) {
          IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
          if (!onlyVisible || desktop.isShowing(pageForm)) {
            return pageForm;
          }
        }
      }
    }

    return null;
  }

  public IPageForm get(String displayViewId, IPage<?> page) {
    if (displayViewId == null || page == null) {
      return null;
    }

    List<IPageForm> list = m_pageFormMaps.get(displayViewId);
    if (list == null) {
      return null;
    }

    for (IPageForm pageForm : list) {
      if (displayViewId.equals(pageForm.getDisplayViewId()) && page.equals(pageForm.getPage())) {
        return pageForm;
      }
    }

    return null;
  }

  public void put(IPageForm pageForm) {
    String displayViewId = pageForm.getDisplayViewId();
    List<IPageForm> list = m_pageFormMaps.get(displayViewId);
    if (list == null) {
      list = new LinkedList<IPageForm>();
    }

    list.add(pageForm);
    m_pageFormMaps.put(displayViewId, list);
  }

  public void remove(IPage<?> page) {
    for (List<IPageForm> pageFormList : m_pageFormMaps.values()) {
      if (pageFormList == null) {
        continue;
      }

      for (IPageForm pageForm : new LinkedList<IPageForm>(pageFormList)) {
        if (page.equals(pageForm.getPage())) {
          pageFormList.remove(pageForm);
        }
      }
    }
  }

  public void remove(IPageForm pageForm) {
    for (List<IPageForm> pageFormList : m_pageFormMaps.values()) {
      if (pageFormList == null) {
        continue;
      }
      pageFormList.remove(pageForm);
    }
  }

  public void clear() {
    m_pageFormMaps.clear();
  }

}
