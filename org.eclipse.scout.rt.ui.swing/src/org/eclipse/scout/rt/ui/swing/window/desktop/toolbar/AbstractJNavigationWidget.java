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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import java.awt.Point;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public abstract class AbstractJNavigationWidget extends JPanel {

  private static final long serialVersionUID = 1L;

  private AbstractAction m_backAction;
  private AbstractAction m_forwardAction;
  private AbstractAction m_refreshAction;
  private AbstractAction m_stopAction;
  private AbstractAction m_historyAction;

  private final ISwingEnvironment m_env;

  public AbstractJNavigationWidget(ISwingEnvironment env) {
    m_env = env;
  }

  public abstract void rebuild();

  public AbstractAction getBackAction() {
    return m_backAction;
  }

  public void setBackAction(AbstractAction backAction) {
    m_backAction = backAction;
  }

  public AbstractAction getForwardAction() {
    return m_forwardAction;
  }

  public void setForwardAction(AbstractAction forwardAction) {
    m_forwardAction = forwardAction;
  }

  public AbstractAction getRefreshAction() {
    return m_refreshAction;
  }

  public void setRefreshAction(AbstractAction refreshAction) {
    m_refreshAction = refreshAction;
  }

  public AbstractAction getStopAction() {
    return m_stopAction;
  }

  public void setStopAction(AbstractAction stopAction) {
    m_stopAction = stopAction;
  }

  public AbstractAction getHistoryAction() {
    return m_historyAction;
  }

  public void setHistoryAction(AbstractAction historyAction) {
    m_historyAction = historyAction;
  }

  public abstract Point getHistoryMenuLocation();

  protected ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }
}
