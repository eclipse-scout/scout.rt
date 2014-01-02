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
 * A basic documentation element containing a section with a table for descriptions.
 */
public class SectionWithTable implements IDocSection {
  private final String m_id;
  private final String m_title;
  private final IDocTable m_table;
  private final IDocSection[] m_subSections;

  public SectionWithTable(String id, String title, IDocSection... subSections) {
    this(id, title, null, subSections);
  }

  public SectionWithTable(String id, String title, IDocTable table, IDocSection... subSections) {
    m_id = id;
    m_title = title;
    m_table = table;
    m_subSections = subSections;
  }

  @Override
  public String getTitle() {
    return m_title;
  }

  @Override
  public String getId() {
    return m_id;
  }

  @Override
  public IDocSection[] getSubSections() {
    return m_subSections;
  }

  @Override
  public IDocTable getTable() {
    return m_table;
  }

}
