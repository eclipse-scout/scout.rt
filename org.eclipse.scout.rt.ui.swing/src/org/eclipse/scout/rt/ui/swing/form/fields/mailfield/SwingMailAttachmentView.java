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
package org.eclipse.scout.rt.ui.swing.form.fields.mailfield;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.FileNotFoundException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ext.JLabelEx;

public class SwingMailAttachmentView extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingMailAttachmentView.class);

  private final SwingMailAttachment m_attachment;

  public SwingMailAttachmentView(SwingMailAttachment attachment) {
    m_attachment = attachment;
    createContent();

    setToolTipText(m_attachment.getFile().getName());
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  protected void createContent() {
    JLabelEx iconLabel = new JLabelEx();
    sun.awt.shell.ShellFolder shellFolder;
    try {
      shellFolder = sun.awt.shell.ShellFolder.getShellFolder(m_attachment.getFile());
      Icon icon = new ImageIcon(shellFolder.getIcon(true));
      iconLabel.setIcon(icon);
    }
    catch (FileNotFoundException t) {
      LOG.warn(null, t);
    }
    iconLabel.setText(m_attachment.getFile().getName());
    iconLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    iconLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
    add(BorderLayout.CENTER, iconLabel);
  }

  public SwingMailAttachment getAttachment() {
    return m_attachment;
  }
}
