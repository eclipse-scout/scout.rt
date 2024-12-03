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
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Represents a widget and optionally a non-{@link IWidget} element owned by it.
 */
public class HybridActionContextElement {

  private final IWidget m_widget;
  private final Object m_element; // optional

  /**
   * @param widget
   *     may not be {@code null}
   * @throws AssertionException
   *     if widget is {@code null}
   */
  public static HybridActionContextElement of(IWidget widget) {
    return of(widget, null);
  }

  /**
   * @param widget
   *     may not be {@code null}
   * @param element
   *     may be {@code null}
   * @throws AssertionException
   *     if widget is {@code null}
   */
  public static HybridActionContextElement of(IWidget widget, Object element) {
    return new HybridActionContextElement(widget, element);
  }

  protected HybridActionContextElement(IWidget widget, Object element) {
    m_widget = Assertions.assertNotNull(widget);
    m_element = element;
  }

  /**
   * @return never {@code null}
   */
  public IWidget getWidget() {
    return m_widget;
  }

  /**
   * @return the typed {@link #getWidget()}, never {@code null}
   * @throws AssertionException
   *     if the widget is not an instance of the given type
   */
  public <T extends IWidget> T getWidget(Class<T> widgetType) {
    return Assertions.assertInstance(m_widget, widgetType);
  }

  /**
   * @return never {@code null}
   * @throws AssertionException
   *     if the element is {@code null}. Use {@link #optElement()} to return {@code null} instead.
   */
  public Object getElement() {
    return getElement(Object.class);
  }

  /**
   * @return the typed {@link #getElement()}, never {@code null}
   * @throws AssertionException
   *     if the widget is not an instance of the given type. An exception is also thrown if the element is {@code null}.
   *     Use {@link #optElement(Class)} to return {@code null} instead.
   */
  public <T> T getElement(Class<T> elementType) {
    return Assertions.assertInstance(Assertions.assertNotNull(m_element), elementType);
  }

  /**
   * @return may be {@code null}
   */
  public Object optElement() {
    return optElement(Object.class);
  }

  /**
   * @return the typed {@link #getElement()} or {@code null}
   * @throws AssertionException
   *     if the widget is not an instance of the given type.
   */
  public <T> T optElement(Class<T> elementType) {
    if (m_element == null) {
      return null;
    }
    return Assertions.assertInstance(m_element, elementType);
  }
}
