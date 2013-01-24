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
package org.eclipse.scout.rt.ui.swt.window.filechooser;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>SwtScoutFileChooser</h3> ...
 * 
 * @since 1.0.0 02.05.2008
 */
public class SwtScoutFileChooser {

  private final IFileChooser m_scoutFileChooser;
  private final ISwtEnvironment m_environment;
  private final Shell m_parentShell;

  public SwtScoutFileChooser(Shell parentShell, IFileChooser fileChooser, ISwtEnvironment environment) {
    m_parentShell = parentShell;
    m_scoutFileChooser = fileChooser;
    m_environment = environment;
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
      getEnvironment().invokeScoutLater(job, 0);
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
    DirectoryDialog dialog = new DirectoryDialog(getParentShell());
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
  }

  public IFileChooser getScoutFileChooser() {
    return m_scoutFileChooser;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public Shell getParentShell() {
    return m_parentShell;
  }

}
