/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.print;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.EventListenerList;

public class PrintApplicationAction extends AbstractAction {

  private final IDesktop m_desktop;
  private IFormFilter m_formFilter;
  private final List<ITask> m_taskList;
  private File m_destinationFolder;
  private boolean m_ignoreTabs;
  private final EventListenerList m_printListeners;

  public PrintApplicationAction(IDesktop desktop) {
    this(desktop, null);
  }

  public PrintApplicationAction(IDesktop desktop, File destinationFolder) {
    m_desktop = desktop;
    m_destinationFolder = destinationFolder;
    m_taskList = new ArrayList<ITask>();
    m_printListeners = new EventListenerList();
  }

  @Override
  protected void execAction() {
    if (getDesktop() == null) {
      throw new VetoException("desktop is null");
    }
    if (getDestinationFolder() == null) {
      throw new VetoException("destinationFolder is null");
    }
    firePrintEvent(new PrintEvent(PrintApplicationAction.this, PrintEvent.TYPE_PRINT_START));
    if (m_formFilter == null) {
      // accept all filter
      m_formFilter = new IFormFilter() {
        @Override
        public boolean acceptForm(IForm form) {
          return true;
        }
      };
    }
    m_taskList.clear();
    getDestinationFolder().mkdirs();
    getDesktop().addDesktopListener(new P_DesktopListener());
    getDesktop().printDesktop(PrintDevice.File, createPrintParameters(createFile(getDestinationFolder(), "Desktop", "jpg")));
    for (IForm dialog : getDesktop().getDialogs()) {
      if (getFormFilter().acceptForm(dialog)) {
        processForm(dialog);
      }
    }
    m_taskList.add(new P_EndTask());
  }

  private void processForm(IForm form) {
    // set all tabboxes visible
    for (IFormField field : form.getAllFields()) {
      if (field instanceof ITabBox && !field.isVisible()) {
        field.setVisible(true);
      }
    }
    m_taskList.add(new P_PrintFormTaks(form));
    if (!isIgnoreTabs()) {
      // collect all tabbox tabs and print them
      for (IFormField field : form.getAllFields()) {
        if (field instanceof ITabBox && field.isVisible()) {
          final ITabBox tabBox = (ITabBox) field;
          IGroupBox selectedTab = null;
          if (tabBox.isVisible()) {
            selectedTab = tabBox.getSelectedTab();
          }
          if (tabBox.isVisible()) {
            for (final IGroupBox g : tabBox.getGroupBoxes()) {
              if (g != selectedTab) {
                m_taskList.add(new P_PrintFormTaks(form, g));
              }
            }
          }
          // select originally selected tab
          if (selectedTab != null) {
            final IGroupBox selectedTabFinal = selectedTab;
            m_taskList.add(new ITask() {
              @Override
              public void run() {
                tabBox.setSelectedTab(selectedTabFinal);
                executeNextTask();
              }
            });
          }
        }
      }
    }
  }

  public IDesktop getDesktop() {
    return m_desktop;
  }

  public File getDestinationFolder() {
    return m_destinationFolder;
  }

  public void setDestinationFolder(File destinationFolder) {
    m_destinationFolder = destinationFolder;
  }

  public void setFormFilter(IFormFilter filter) {
    m_formFilter = filter;
  }

  public IFormFilter getFormFilter() {
    return m_formFilter;
  }

  public void setIgnoreTabs(boolean ignoreTabs) {
    m_ignoreTabs = ignoreTabs;
  }

  public boolean isIgnoreTabs() {
    return m_ignoreTabs;
  }

  public void addPrintListener(PrintListener listener) {
    m_printListeners.add(PrintListener.class, listener);
  }

  public void removePrintListener(PrintListener listener) {
    m_printListeners.remove(PrintListener.class, listener);
  }

  private void firePrintEvent(PrintEvent e) {
    for (PrintListener l : m_printListeners.getListeners(PrintListener.class)) {
      l.handlePrintEvent(e);
    }
  }

  private void executeNextTask() {
    if (m_taskList.isEmpty()) {
      return;
    }
    ITask task = m_taskList.remove(0);
    task.run();
  }

  private File createFile(File dir, String filename, String fileExtension) {
    File file = new File(dir, new StringBuilder().append(filename).append('.').append(fileExtension).toString());
    try {
      if (!file.createNewFile()) {
        throw new ProcessingException("Temporary file could not be created");
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Temporary file could not be created", e);
    }
    return file;
  }

  private Map<String, Object> createPrintParameters(File printFile) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("file", printFile);
    parameters.put("contentType", "image/jpg");
    return parameters;
  }

  private class P_PrintFormTaks implements ITask {
    private final IForm m_form;
    private final IGroupBox m_groupBox;

    private P_PrintFormTaks(IForm form) {
      this(form, null);
    }

    private P_PrintFormTaks(IForm form, IGroupBox groupBox) {
      m_form = form;
      m_groupBox = groupBox;
    }

    @Override
    public void run() {
      StringBuilder filename = new StringBuilder();
      filename.append(getForm().getFormId());
      if (getGroupBox() != null) {
        filename.append("_" + getGroupBox().getFieldId());
        ICompositeField parentField = getGroupBox().getParentField();
        if (parentField instanceof ITabBox) {
          ((ITabBox) parentField).setSelectedTab(getGroupBox());
        }
      }
      getForm().addFormListener(new P_FormListener(getForm()));
      getForm().printForm(PrintDevice.File, createPrintParameters(createFile(getDestinationFolder(), filename.toString(), "jpg")));
    }

    public IForm getForm() {
      return m_form;
    }

    public IGroupBox getGroupBox() {
      return m_groupBox;
    }
  } // end class P_PrintFormTaks

  private class P_EndTask implements ITask {
    @Override
    public void run() {
      firePrintEvent(new PrintEvent(PrintApplicationAction.this, PrintEvent.TYPE_PRINT_DONE));
    }
  } // end

  private class P_DesktopListener implements DesktopListener {
    @Override
    public void desktopChanged(DesktopEvent e) {
      if (e.getType() == DesktopEvent.TYPE_PRINTED) {
        getDesktop().removeDesktopListener(P_DesktopListener.this);
        executeNextTask();
      }
    }
  }

  private class P_FormListener implements FormListener {
    private final IForm m_form;

    public P_FormListener(IForm form) {
      m_form = form;
    }

    @Override
    public void formChanged(FormEvent e) {
      if (e.getType() == FormEvent.TYPE_PRINTED) {
        getForm().removeFormListener(P_FormListener.this);
        executeNextTask();
      }
    }

    public IForm getForm() {
      return m_form;
    }
  } // end class P_FormListener

  private interface ITask {
    void run();
  }

  public interface IFormFilter {
    boolean acceptForm(IForm form);
  }

}
