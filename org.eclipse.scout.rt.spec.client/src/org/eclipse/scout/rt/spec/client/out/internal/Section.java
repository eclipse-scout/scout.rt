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
package org.eclipse.scout.rt.spec.client.out.internal;

import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.IDocTable;

/**
 * A basic documentation element containing a table for descriptions and/or subsections.
 */
public class Section implements IDocSection {
  protected final IDocTable m_table;
  protected final IDocSection[] m_subSections;
  protected String m_title;
  protected String m_introduction;

  public Section(String title, IDocSection... subSections) {
    this(title, null, null, subSections);
  }

  public Section(String title, IDocTable table, IDocSection... subSections) {
    this(title, null, table, subSections);
  }

  public Section(String title, String introduction, IDocTable table, IDocSection... subSections) {
    m_title = title;
    m_introduction = introduction;
    m_table = table;
    m_subSections = subSections;
  }

  @Override
  public String getTitle() {
    return m_title;
  }

  @Override
  public String getIntroduction() {
    return m_introduction;
  }

  @Override
  public IDocSection[] getSubSections() {
    if (m_subSections == null) {
      return new IDocSection[]{};
    }
    return m_subSections.clone();
  }

  @Override
  public IDocTable getTable() {
    return m_table;
  }

  @Override
  public boolean hasSubSections() {
    return getSubSections() != null && getSubSections().length > 0;
  }

  @Override
  public boolean hasTableCellTexts() {
    return getTable() != null && getTable().getCellTexts() != null && getTable().getCellTexts().length > 0;
  }

  @Override
  public boolean isDisplayed() {
    return hasSubSections() || hasTableCellTexts();
  }

}
