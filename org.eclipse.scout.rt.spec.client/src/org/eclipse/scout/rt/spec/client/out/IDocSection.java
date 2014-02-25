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
package org.eclipse.scout.rt.spec.client.out;

/**
 *
 */
public interface IDocSection {

  /**
   * @return a title or heading of the section
   */
  String getTitle();

  IDocSectionHeading getHeading();

  IDocTable getTable();

  IDocSection[] getSubSections();

  boolean hasSubSections();

  boolean hasTableCellTexts();

  boolean isDisplayed();

}
