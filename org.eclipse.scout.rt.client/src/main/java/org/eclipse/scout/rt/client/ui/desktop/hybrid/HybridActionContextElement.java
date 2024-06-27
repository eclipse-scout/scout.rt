/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.util.Assertions;

public class HybridActionContextElement {

  private final IWidget m_widget;
  private final Object m_element; // optional

  public static HybridActionContextElement of(IWidget widget) {
    return of(widget, null);
  }

  public static HybridActionContextElement of(IWidget widget, Object element) {
    return new HybridActionContextElement(widget, element);
  }

  protected HybridActionContextElement(IWidget widget, Object element) {
    m_widget = Assertions.assertNotNull(widget);
    m_element = element;
  }

  public IWidget getWidget() {
    return m_widget;
  }

  public Object getElement() {
    return m_element;
  }
}
