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
package org.eclipse.scout.rt.ui.swing.splash;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public class SplashProgressMonitor extends NullProgressMonitor {
  private ISwingEnvironment m_env;
  private String m_name1;
  private String m_name2;
  private String m_displayText;
  private ISplashWindow m_splash;

  public SplashProgressMonitor(ISwingEnvironment env) {
    m_env = env;
  }

  @Override
  public void beginTask(String name, int totalWork) {
    m_name1 = name;
    setNameInternal();
  }

  @Override
  public void subTask(String name) {
    m_name2 = name;
    setNameInternal();
  }

  @Override
  public void setTaskName(String name) {
    m_name1 = name;
    m_name2 = null;
    setNameInternal();
  }

  private void setNameInternal() {
    if (m_name2 != null) {
      m_displayText = m_name2;
    }
    else if (m_name1 != null) {
      m_displayText = m_name1;
    }
    else {
      m_displayText = "...";
    }
    SwingUtilities.invokeLater(
        new Runnable() {
          public void run() {
            if (m_splash != null) {
              m_splash.setStatusText(m_displayText);
            }
          }
        }
        );
  }

  @Override
  public void done() {
    hideSplash();
  }

  public void showSplash() {
    SwingUtilities.invokeLater(
        new Runnable() {
          public void run() {
            showSplashInSwingThread();
          }
        }
        );
  }

  private void showSplashInSwingThread() {
    if (m_splash == null) {
      if (!(m_env.getRootFrame() instanceof JFrame) && (m_env.getRootFrame() instanceof RootPaneContainer)) {
        m_splash = new EmbeddedSplashWindow((RootPaneContainer) m_env.getRootFrame());
      }
      else {
        m_splash = new SplashWindow(m_env.getRootFrame());
      }
      m_splash.setStatusText(m_displayText);
      m_splash.showSplash();
    }
  }

  public void hideSplash() {
    SwingUtilities.invokeLater(
        new Runnable() {
          public void run() {
            hideSplashInSwingThread();
          }
        }
        );
  }

  private void hideSplashInSwingThread() {
    if (m_splash != null) {
      m_splash.disposeSplash();
      m_splash = null;
    }
  }

  /**
   * Must be called in swing thread
   */
  public void addCustomPanel(JComponent panel) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("must be called in swing thread");
    }
    showSplashInSwingThread();
    m_splash.addCustomPanel(panel);
  }

  /**
   * Must be called in swing thread
   */
  public void removeCustomPanel(JComponent panel) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("must be called in swing thread");
    }
    if (m_splash != null) {
      m_splash.removeCustomPanel(panel);
    }
  }

}
