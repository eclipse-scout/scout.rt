/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import java.util.EventObject;

public class WidgetEvent extends EventObject implements IModelEvent {

  private static final long serialVersionUID = 1L;

  public static final int TYPE_SCROLL_TO_TOP = 100;
  public static final int TYPE_FOCUS_IN = 200;
  public static final int TYPE_FOCUS_OUT = 300;
  public static final int TYPE_REVEAL = 400;

  private int m_type;
  private ScrollOptions m_scrollOptions;

  public WidgetEvent(Object source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public int getType() {
    return m_type;
  }

  public void setScrollOptions(ScrollOptions scrollOptions) {
    m_scrollOptions = scrollOptions;
  }

  public ScrollOptions getScrollOptions() {
    return m_scrollOptions;
  }
}
