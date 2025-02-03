/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.svg.client.svgfield;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPoint;

/**
 * The field supports for official SVG standard documents rendering and interaction.
 */
public interface ISvgField extends IFormField, IAppLinkCapable {
  /**
   * type {@link SVGDocument}
   */
  String PROP_SVG_DOCUMENT = "svgDocument";
  /**
   * type {@link SVGPoint}
   */
  String PROP_SELECTION = "selection";

  SVGDocument getSvgDocument();

  void setSvgDocument(SVGDocument doc);

  IFastListenerList<ISvgFieldListener> svgFieldListeners();

  default void addSvgFieldListener(ISvgFieldListener listener) {
    svgFieldListeners().add(listener);
  }

  default void removeSvgFieldListener(ISvgFieldListener listener) {
    svgFieldListeners().remove(listener);
  }

  ISvgFieldUIFacade getUIFacade();

  /**
   * @return the point of the selection. This is set by the ui facade when a click or hyperlink occurs. Use
   * {@link org.eclipse.scout.rt.svg.client.SVGUtility#getElementsAt(SVGDocument, SVGPoint)} to find affected
   * elements
   */
  SVGPoint getSelection();

  /**
   * set the selected point. Use {@link org.eclipse.scout.rt.svg.client.SVGUtility#getElementsAt(SVGDocument, SVGPoint)}
   * to find affected elements
   */
  void setSelection(SVGPoint point);
}
