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
package org.eclipse.scout.rt.shared.services.common.node;

import java.io.Serializable;

public class NodeStatusTableHolder implements Serializable {

  private static final long serialVersionUID = 1L;

  private String[] m_tableHeaders;
  private Object[][] m_tableData;
  private String m_processingNode;

  public String[] getTableHeaders() {
    return m_tableHeaders;
  }

  public void setTableHeaders(String[] tableHeaders) {
    m_tableHeaders = tableHeaders;
  }

  public Object[][] getTableData() {
    return m_tableData;
  }

  public void setTableData(Object[][] tableData) {
    m_tableData = tableData;
  }

  public String getProcessingNode() {
    return m_processingNode;
  }

  public void setProcessingNode(String processingNode) {
    m_processingNode = processingNode;
  }
}
