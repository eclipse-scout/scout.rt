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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

public class PageFormConfig {
  private boolean m_nodePageSwitchEnabled;
  private boolean m_tablePageAllowed;
  private boolean m_detailFormVisible;
  private boolean m_keepSelection;
  private boolean m_tableStatusVisible;

  /**
   * If enabled, clicking on a page with nodes will lead to a selection of the parent node.
   */
  public boolean isNodePageSwitchEnabled() {
    return m_nodePageSwitchEnabled;
  }

  public void setNodePageSwitchEnabled(boolean nodePageSwitchEnabled) {
    m_nodePageSwitchEnabled = nodePageSwitchEnabled;
  }

  public boolean isTablePageAllowed() {
    return m_tablePageAllowed;
  }

  public void setTablePageAllowed(boolean tablePagesAllowed) {
    m_tablePageAllowed = tablePagesAllowed;
  }

  public boolean isDetailFormVisible() {
    return m_detailFormVisible;
  }

  public void setDetailFormVisible(boolean detailFormVisible) {
    m_detailFormVisible = detailFormVisible;
  }

  public boolean isKeepSelection() {
    return m_keepSelection;
  }

  public void setKeepSelection(boolean keepSelection) {
    m_keepSelection = keepSelection;
  }

  public boolean isTableStatusVisible() {
    return m_tableStatusVisible;
  }

  public void setTableStatusVisible(boolean tableStatusVisible) {
    m_tableStatusVisible = tableStatusVisible;
  }
}
