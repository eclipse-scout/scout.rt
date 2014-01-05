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
 * Simple data object for a documentation table.
 */
public class DocTable implements IDocTable {
  private final String[] m_headerTexts;
  private final String[][] m_cellTexts;

  public DocTable(String[] headerTexts, String[][] cellTexts) {
    m_headerTexts = headerTexts.clone();
    m_cellTexts = cellTexts.clone();
  }

  @Override
  public String[][] getCellTexts() {
    return m_cellTexts.clone();
  }

  @Override
  public String[] getHeaderTexts() {
    return m_headerTexts.clone();
  }

}
