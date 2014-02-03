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
import org.eclipse.scout.rt.spec.client.out.IDocSectionHeading;
import org.eclipse.scout.rt.spec.client.out.IDocTable;

/**
 * A basic documentation element containing a section with a table for descriptions.
 */
public class SectionWithTable implements IDocSection {
  private final IDocTable m_table;
  private final IDocSection[] m_subSections;
  private final DocSectionHeading m_docSectionHeading;

  public SectionWithTable(String id, String title, IDocSection... subSections) {
    this(id, title, null, subSections);
  }

  public SectionWithTable(String id, String title, IDocTable table, IDocSection... subSections) {
    m_docSectionHeading = new DocSectionHeading(id, title);
    m_table = table;
    m_subSections = subSections;
  }

  @Override
  public String getTitle() {
    return m_docSectionHeading.getName();
  }

  @Override
  public String getId() {
    return m_docSectionHeading.getId();
  }

  @Override
  public IDocSectionHeading getHeading() {
    return m_docSectionHeading;
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
