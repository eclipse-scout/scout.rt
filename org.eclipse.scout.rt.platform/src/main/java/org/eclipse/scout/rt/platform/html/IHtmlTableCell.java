/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html;

/**
 * Marker Interface for table cell
 */
public interface IHtmlTableCell extends IHtmlElement {

  /**
   * Set the colspan.
   */
  IHtmlTableCell colspan(int colspan);

  /**
   * Add a css class
   */
  @Override
  IHtmlTableCell cssClass(CharSequence cssClass);

  /**
   * Add a css style
   */
  @Override
  IHtmlTableCell style(CharSequence style);

  /**
   * Add an application local link
   *
   * @param path
   *          path to identify what is the link referring to.
   */
  @Override
  IHtmlTableCell appLink(CharSequence path);

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlTableCell addAttribute(String name, CharSequence value);
}
