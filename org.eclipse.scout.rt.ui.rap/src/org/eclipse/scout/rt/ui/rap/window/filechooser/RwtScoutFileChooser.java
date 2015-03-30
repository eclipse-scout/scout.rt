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
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>SwtScoutFileChooser</h3> ...
 * RWT File chooser is only designed to UPLOAD files, regardless of the {@link SWT#SAVE} flag.
 * <p>
 * Therefore the DOWNLOAD of files is done by displaying a link to the resource and let the user click on it resp. right
 * click "Save As..." on it. TODO sel/imo
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutFileChooser implements IRwtScoutFileChooser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFileChooser.class);

  private final IFileChooser m_scoutFileChooser;
  private final Shell m_parentShell;

  public RwtScoutFileChooser(Shell parentShell, IFileChooser fileChooser) {
    m_parentShell = parentShell;
    m_scoutFileChooser = fileChooser;
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) m_parentShell.getDisplay().getData(IRwtEnvironment.class.getName());
  }

  @Override
  public void showFileChooser() {
    List<File> files = null;
    try {
      if (getScoutFileChooser().isFolderMode()) {
        LOG.error("IFileChooserField.isFolderMode() == true is not possible in RAP");
        files = CollectionUtility.emptyArrayList();
      }
      else if (!getScoutFileChooser().isTypeLoad()) {
        LOG.info("IFileChooserField.isTypeLoad() == false (SAVE) is not possible in RAP, doing nothing");
        files = CollectionUtility.emptyArrayList();
      }
      else {
        files = uploadFiles();
      }
    }
    finally {
      final List<File> finalFiles = files;
      Runnable job = new Runnable() {
        @Override
        public void run() {
          m_scoutFileChooser.setFiles(finalFiles);
        }
      };
      getUiEnvironment().invokeScoutLater(job, 0);
    }
  }

  protected List<File> uploadFiles() {
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

    // Since version 2.3, the RAP FileDialog does not support filtering for extensions, file paths and names anymore.
    // See bugzilla 433502 and 433501. Code is commented out until the support is available again.
//    String[] extensions = new String[]{"*.*"};
    if (getScoutFileChooser().getFileExtensions() != null) {
      LOG.info("Setting file extensions using IFileChooserField.getFileExtensions() is currently not possible in RAP. Operation is ignored.");
//      List<String> extensionList = new ArrayList<String>();
//      for (String ext : getScoutFileChooser().getFileExtensions()) {
//        extensionList.add("*." + ext);
//      }
//      extensions = extensionList.toArray(new String[extensionList.size()]);
    }
    FileDialog dialog = new FileDialog(getParentShell(), style);
//    dialog.setFilterExtensions(extensions);
    if (getScoutFileChooser().getDirectory() != null) {
      LOG.info("Setting filter path using IFileChooserField.getDirectory() is currently not possible in RAP. Operation is ignored.");
//      dialog.setFilterPath(getScoutFileChooser().getDirectory().getAbsolutePath());
    }
    if (getScoutFileChooser().getFileName() != null) {
      LOG.info("Setting file name using IFileChooserField.getFileName() is currently possible in RAP. Operation is ignored.");
//      dialog.setFileName(getScoutFileChooser().getFileName());
    }
    setFileDialogTitle(dialog, getScoutFileChooser().isMultiSelect());
    dialog.open();
    String[] selectedFiles = dialog.getFileNames();
    if (selectedFiles != null && selectedFiles.length > 0) {
      List<File> files = new ArrayList<File>(selectedFiles.length);
      for (String selectedFile : selectedFiles) {
        files.add(new File(selectedFile));
      }
      return files;
    }
    return CollectionUtility.emptyArrayList();
  }

  private void setFileDialogTitle(FileDialog fileDialog, boolean isMultiselect) {
    if (isMultiselect) {
      fileDialog.setText(ScoutTexts.get("FileChooserRAPMultipleFiles"));
    }
    else {
      fileDialog.setText(ScoutTexts.get("FileChooserRAPSingleFile"));
    }
  }

  public IFileChooser getScoutFileChooser() {
    return m_scoutFileChooser;
  }

  public Shell getParentShell() {
    return m_parentShell;
  }

}
