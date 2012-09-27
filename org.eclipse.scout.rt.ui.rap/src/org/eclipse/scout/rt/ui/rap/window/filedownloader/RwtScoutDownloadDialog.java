/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.filedownloader;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class RwtScoutDownloadDialog extends Dialog {
  private static final long serialVersionUID = 1L;

  private Browser m_browser;
  private String m_downloadURL;

  public RwtScoutDownloadDialog(Shell parent, String downloadURL) {
    super(parent);
    setURL(downloadURL);
  }

  private void setURL(String downloadURL) {
    if (m_browser != null && !m_browser.isDisposed()) {
      m_browser.setUrl(downloadURL);
    }
    this.m_downloadURL = downloadURL;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Control control = super.createDialogArea(parent);
    m_browser = new Browser(parent, SWT.NONE);
    if (m_downloadURL != null) {
      m_browser.setUrl(m_downloadURL);
    }
    return control;
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    return null;
  }

  @Override
  protected int getShellStyle() {
    return SWT.NO_TRIM;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setSize(1, 1);
    newShell.setMinimized(true);
  }
}
