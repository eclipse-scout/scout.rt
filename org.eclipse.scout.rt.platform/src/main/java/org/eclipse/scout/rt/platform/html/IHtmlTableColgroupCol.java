/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.html;

/**
 * Marker Interface for table colgroup col.
 */
public interface IHtmlTableColgroupCol extends IHtmlElement {

  /**
   * Add a css class
   */
  @Override
  IHtmlTableColgroupCol cssClass(CharSequence cssClass);

  /**
   * Add a css style
   */
  @Override
  IHtmlTableColgroupCol style(CharSequence style);

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlTableColgroupCol addAttribute(String name, CharSequence value);
}
