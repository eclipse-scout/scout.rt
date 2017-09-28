/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.svg.client.svgfield;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
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

  void addSvgFieldListener(ISvgFieldListener listener);

  void removeSvgFieldListener(ISvgFieldListener listener);

  ISvgFieldUIFacade getUIFacade();

  /**
   * @return the point of the selection. This is set by the ui facade when a click or hyperlink occurs. Use
   *         {@link org.eclipse.scout.rt.svg.client.SVGUtility#getElementsAt(SVGDocument, SVGPoint)} to find affected
   *         elements
   */
  SVGPoint getSelection();

  /**
   * set the selected point. Use {@link org.eclipse.scout.rt.svg.client.SVGUtility#getElementsAt(SVGDocument, SVGPoint)}
   * to find affected elements
   */
  void setSelection(SVGPoint point);

}
