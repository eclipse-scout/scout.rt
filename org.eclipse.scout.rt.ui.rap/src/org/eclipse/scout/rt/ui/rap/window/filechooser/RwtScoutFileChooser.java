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
package org.eclipse.scout.rt.ui.rap.window.filechooser;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>SwtScoutFileChooser</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutFileChooser {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFileChooser.class);

  private final IFileChooser m_scoutFileChooser;
  private final Shell m_parentShell;

  public RwtScoutFileChooser(Shell parentShell, IFileChooser fileChooser) {
    m_parentShell = parentShell;
    m_scoutFileChooser = fileChooser;
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) m_parentShell.getDisplay().getData(IRwtEnvironment.class.getName());
  }

  public void showFileChooser() {
    File[] files = null;
    try {
      if (getScoutFileChooser().isFolderMode()) {
        files = showDirecoryDialog();
      }
      else {
        files = showFileDialog();
      }
    }
    finally {
      final File[] finalFiles = files;
      Runnable job = new Runnable() {
        @Override
        public void run() {
          m_scoutFileChooser.setFiles(finalFiles);
        }
      };
      getUiEnvironment().invokeScoutLater(job, 0);
    }
  }

  protected File[] showFileDialog() {
    int style = SWT.NONE;
    if (getScoutFileChooser().isTypeLoad()) {
      style |= SWT.OPEN;
    }
    else {
      style |= SWT.SAVE;
    }
    if (getScoutFileChooser().isMultiSelect()) {
      style |= SWT.MULTI;
    }
    String[] extensions = new String[]{"*.*"};
    if (getScoutFileChooser().getFileExtensions() != null) {
      ArrayList<String> extensionList = new ArrayList<String>();
      for (String ext : getScoutFileChooser().getFileExtensions()) {
        extensionList.add("*." + ext);
      }
      extensions = extensionList.toArray(new String[extensionList.size()]);
    }
    FileDialog dialog = new FileDialog(getParentShell(), style);
    dialog.setFilterExtensions(extensions);
    if (getScoutFileChooser().getDirectory() != null) {
      dialog.setFilterPath(getScoutFileChooser().getDirectory().getAbsolutePath());
    }
    if (getScoutFileChooser().getFileName() != null) {
      dialog.setFileName(getScoutFileChooser().getFileName());
    }
    String selectedFile = dialog.open();
    if (selectedFile != null && selectedFile.length() > 0) {
      return new File[]{new File(selectedFile)};
    }
    else {
      return new File[0];
    }
  }

  protected File[] showDirecoryDialog() {
    //XXX rap
    /*DirectoryDialog dialog = new DirectoryDialog(getParentShell());
    if (getScoutFileChooser().getDirectory() != null) {
      dialog.setFilterPath(getScoutFileChooser().getDirectory().getAbsolutePath());
    }
    String selectedDirecotry = dialog.open();
    if (selectedDirecotry != null && selectedDirecotry.length() > 0) {
      return new File[]{new File(selectedDirecotry)};
    }
    else {
      return new File[0];
    }
    */
    LOG.error("IFileChooserField.isFolderMode() == true is not possible in RAP");
    return new File[0];
  }

  public IFileChooser getScoutFileChooser() {
    return m_scoutFileChooser;
  }

  public Shell getParentShell() {
    return m_parentShell;
  }

}
