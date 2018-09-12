/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"serial", "squid:S2057"})
public class TileGridEvent extends EventObject implements IModelEvent {

  private static final Logger LOG = LoggerFactory.getLogger(TileGridEvent.class);
  public static final int TYPE_TILE_ACTION = 100;
  public static final int TYPE_TILE_CLICK = 200;

  private final int m_type;
  private ITile m_tile;
  private MouseButton m_mouseButton;

  public TileGridEvent(ITileGrid source, int type) {
    this(source, type, null);
  }

  public TileGridEvent(ITileGrid source, int type, ITile tile) {
    super(source);
    m_type = type;
    m_tile = tile;
  }

  public ITileGrid getTileGrid() {
    return (ITileGrid) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public ITile getTile() {
    return m_tile;
  }

  public void setTile(ITile tile) {
    m_tile = tile;
  }

  public void setMouseButton(MouseButton mouseButton) {
    m_mouseButton = mouseButton;
  }

  public MouseButton getMouseButton() {
    return m_mouseButton;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getClass().getSimpleName()).append("[");
    buf.append(getTypeName());
    // tiles
    if (m_tile != null && getTileGrid() != null) {
      buf.append(" ");
      buf.append("tile ").append(m_tile);
    }
    buf.append("]");
    return buf.toString();
  }

  /**
   * decode type
   */
  protected String getTypeName() {
    try {
      Field[] f = getClass().getDeclaredFields();
      for (Field aF : f) {
        if (Modifier.isPublic(aF.getModifiers()) && Modifier.isStatic(aF.getModifiers()) && aF.getName().startsWith("TYPE_")
            && ((Number) aF.get(null)).intValue() == m_type) {
          return (aF.getName());
        }
      }
    }
    catch (IllegalAccessException e) {
      LOG.error("Error Reading fields", e);
    }
    return "#" + m_type;
  }
}
