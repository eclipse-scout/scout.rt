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
package org.eclipse.scout.rt.client.ui.form.fields.imagefield;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;

public class ImageFieldEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  private int m_type;
  private List<IMenu> m_popupMenus;
  private BoundsSpec m_rect;

  public static final int TYPE_ZOOM_RECTANGLE = 10;
  public static final int TYPE_AUTO_FIT = 20;

  public ImageFieldEvent(IImageField source, int type) {
    super(source);
    m_type = type;
  }

  public ImageFieldEvent(IImageField source, int type, BoundsSpec r) {
    super(source);
    m_type = type;
    m_rect = r;
  }

  @Override
  public int getType() {
    return m_type;
  }

  public IImageField getImageField() {
    return (IImageField) getSource();
  }

  public void addPopupMenu(IMenu menu) {
    if (menu != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.add(menu);
    }
  }

  /**
   * used by TYPE_POPUP to add actions
   */
  public void addPopupMenus(List<? extends IMenu> menus) {
    if (menus != null) {
      if (m_popupMenus == null) {
        m_popupMenus = new ArrayList<IMenu>();
      }
      m_popupMenus.addAll(CollectionUtility.arrayListWithoutNullElements(menus));
    }
  }

  /**
   * used by TYPE_POPUP to add actions
   */
  public List<IMenu> getPopupMenus() {
    return CollectionUtility.arrayList(m_popupMenus);
  }

  /**
   * used by TYPE_POPUP to add actions
   */
  public int getPopupMenuCount() {
    return CollectionUtility.size(m_popupMenus);
  }

  /**
   * used by TYPE_ZOOM_RECTANGLE
   */
  public BoundsSpec getZoomRectangle() {
    return m_rect;
  }

}
