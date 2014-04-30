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
package org.eclipse.scout.rt.ui.swing.window.filechooser;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class SwingScoutFileChooser implements ISwingScoutFileChooser {
  private ISwingEnvironment m_env;
  private IFileChooser m_scoutFileChooser;
  private Window m_owner;
  private boolean m_useAWT;

  public SwingScoutFileChooser(ISwingEnvironment env, IFileChooser fc, Window owner, boolean useAWT) {
    m_env = env;
    m_scoutFileChooser = fc;
    m_owner = owner;
    m_useAWT = useAWT;
  }

  @Override
  public void showFileChooser() {
    List<File> files = null;
    try {
      if (m_useAWT && !m_scoutFileChooser.isFolderMode()) {
        files = showFileChooserAWT();
      }
      else {
        files = showFileChooserSwing();
      }
    }
    finally {
      final List<File> finalFiles = files;
      Runnable t = new Runnable() {
        @Override
        public void run() {
          m_scoutFileChooser.setFiles(finalFiles);
        }
      };

      m_env.invokeScoutLater(t, 0);
    }
  }

  protected List<File> showFileChooserSwing() {
    List<String> extensions = m_scoutFileChooser.getFileExtensions();
    boolean openMode = m_scoutFileChooser.isTypeLoad();
    File curDir = m_scoutFileChooser.getDirectory();
    boolean folderMode = m_scoutFileChooser.isFolderMode();
    String fileName = m_scoutFileChooser.getFileName();
    //
    JFileChooser dlg;
    // workaround for java accidentally accessing A: when choosing files
    try {
      SecurityManager sm = System.getSecurityManager();
      System.setSecurityManager(null);
      dlg = createFileChooserSwing(curDir);
      System.setSecurityManager(sm);
    }
    catch (Exception e) {
      dlg = createFileChooserSwing(curDir);
    }
    // end workaround
    if (folderMode) {
      dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
    else {
      dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    if (extensions.size() > 0) {
      // remove old
      javax.swing.filechooser.FileFilter[] filters = dlg.getChoosableFileFilters();
      for (int i = 0; filters != null && i < filters.length; i++) {
        dlg.removeChoosableFileFilter(filters[i]);
      }

      dlg.addChoosableFileFilter(new ExtensionFileFilter(extensions));
      for (int i = 0; i < extensions.size(); i++) {
        String oneExt = extensions.get(i);
        dlg.addChoosableFileFilter(new ExtensionFileFilter(Collections.singletonList(oneExt)));
      }
    }

    if (StringUtility.hasText(fileName) && !folderMode) {
      dlg.setSelectedFile(new File(fileName));
    }

    File f = null;
    int result;
    while (true) {
      if (openMode) {
        result = dlg.showOpenDialog(m_owner);
      }
      else {
        result = dlg.showSaveDialog(m_owner);
      }
      if (result == JFileChooser.APPROVE_OPTION) {
        f = dlg.getSelectedFile();
        // add extension
        if (!folderMode) {
          if (f != null && (dlg.getFileFilter() instanceof ExtensionFileFilter)) {
            List<String> selectedExtensions = ((ExtensionFileFilter) dlg.getFileFilter()).getExtensions();
            String path = f.getAbsolutePath();
            if (selectedExtensions.size() == 1 && !dlg.getFileFilter().accept(f)) {
              f = new File(path + "." + selectedExtensions.get(0));
            }
          }
        }
        // check if exists
        if ((!openMode) && f != null && f.exists() && !f.isDirectory()) {
          int msgBoxResult = JOptionPane.showConfirmDialog(m_owner, SwingUtility.getNlsText("FileExistsOwerwriteQuestion"), null, JOptionPane.YES_NO_OPTION);
          if (msgBoxResult == JOptionPane.YES_OPTION) {
            break;
          }
        }
        else {
          break;
        }
      }
      else {
        f = null;
        break;
      }
    }
    return CollectionUtility.arrayList(f);
  }

  protected JFileChooser createFileChooserSwing(File curDir) {
    if (curDir != null) {
      return new JFileChooser(curDir);
    }
    return new JFileChooser();
  }

  protected List<File> showFileChooserAWT() {
    List<String> extensions = m_scoutFileChooser.getFileExtensions();
    boolean openMode = m_scoutFileChooser.isTypeLoad();
    File curDir = m_scoutFileChooser.getDirectory();
    boolean folderMode = m_scoutFileChooser.isFolderMode();
    String fileName = m_scoutFileChooser.getFileName();
    //
    FileDialog dlg;
    // workaround for java accidentally accessing A: when choosing files
    SecurityManager sm = System.getSecurityManager();
    try {
      System.setSecurityManager(null);
      //
      StringBuffer buf = new StringBuffer();
      if (extensions != null) {
        for (int i = 0; i < extensions.size(); i++) {
          if (i > 0) {
            buf.append(", ");
          }
          buf.append("*." + extensions.get(i));
        }
      }
      if (buf.length() == 0) {
        buf.append("*.*");
      }
      dlg = createFileChooserAWT(m_owner, buf.toString(), openMode);
      System.setSecurityManager(sm);
    }
    catch (Exception e) {
      dlg = createFileChooserAWT(m_owner);
    }
    finally {
      try {
        System.setSecurityManager(sm);
      }
      catch (Throwable t) {
      }
    }
    // end workaround
    if (curDir != null) {
      File f = curDir;
      if (!f.isDirectory()) {
        f = f.getAbsoluteFile().getParentFile();
      }
      dlg.setDirectory(f.getAbsolutePath());
    }
    if (folderMode) {
      // not implemented in windows dialog
    }

    if (extensions != null && extensions.isEmpty()) {
      final List<String> extListFinal = extensions;
      if (StringUtility.hasText(fileName)) {
        dlg.setFile(fileName);
      }
      else {
        StringBuffer extBuf = new StringBuffer();
        for (int i = 0; i < extListFinal.size(); i++) {
          if (extBuf.length() > 0) {
            extBuf.append(";");
          }
          extBuf.append("*." + extListFinal.get(i));
        }
        dlg.setFile(extBuf.toString());
      }
      dlg.setFilenameFilter(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          for (int i = 0; i < extListFinal.size(); i++) {
            if (name.toLowerCase().endsWith("." + extListFinal.get(i))) {
              return true;
            }
          }
          return false;
        }
      });
    }

    File f = null;
    while (true) {
      //m_env.interruptWaitingForSwing();
      dlg.setVisible(true);
      if (dlg.getFile() != null) {
        f = new File(dlg.getDirectory(), dlg.getFile());
        // add extension
        if (!folderMode) {
          if (extensions.size() == 1) {
            String path = f.getAbsolutePath();
            if (!path.toLowerCase().endsWith("." + extensions.get(0))) {
              f = new File(path + "." + extensions.get(0));
            }
          }
        }
        // check if exists
        // windows does this automatically
        break;
      }
      else {
        f = null;
        break;
      }
    }
    return CollectionUtility.arrayList(f);
  }

  protected FileDialog createFileChooserAWT(Window owner) {
    return createFileChooserAWT(owner, "", true);
  }

  protected FileDialog createFileChooserAWT(Window owner, String title, boolean openMode) {
    if (owner instanceof Dialog) {
      return new FileDialog((Dialog) owner, title, openMode ? FileDialog.LOAD : FileDialog.SAVE);
    }
    else if (m_owner instanceof Frame) {
      return new FileDialog((Frame) m_owner, title, openMode ? FileDialog.LOAD : FileDialog.SAVE);
    }
    return new FileDialog(new Frame(), title, openMode ? FileDialog.LOAD : FileDialog.SAVE);
  }

  public static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
    private final List<String> m_extensions;

    public ExtensionFileFilter(List<String> extensions) {
      if (extensions == null) {
        m_extensions = CollectionUtility.emptyArrayList();
      }
      else {
        ArrayList<String> list = new ArrayList<String>(extensions.size());
        for (String extension : extensions) {
          if (extension != null && !extension.equals("*")) {
            list.add(extension.toLowerCase());
          }
        }
        m_extensions = list;
      }
    }

    @Override
    public String getDescription() {
      if (m_extensions.isEmpty()) {
        return "*.*";
      }
      else {
        StringBuilder s = new StringBuilder();
        for (String extension : m_extensions) {
          s.append("*.");
          s.append(extension);
          s.append(";");
        }
        s.deleteCharAt(s.length() - 1);
        return s.toString();
      }
    }

    public List<String> getExtensions() {
      return CollectionUtility.arrayList(m_extensions);
    }

    @Override
    public boolean accept(File f) {
      return f.isDirectory() || m_extensions.isEmpty() || fileNameMatches(f);
    }

    private boolean fileNameMatches(File f) {
      String name = f.getName().toLowerCase();
      for (String extension : m_extensions) {
        if (name.endsWith(extension)) {
          return true;
        }
      }
      return false;
    }
  }// end private class

}
