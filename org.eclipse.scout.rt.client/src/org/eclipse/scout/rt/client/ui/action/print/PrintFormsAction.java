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
package org.eclipse.scout.rt.client.ui.action.print;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.osgi.framework.Bundle;

/**
 * Create a screenshot of one or more form into a destination folder. For every {@link ITabBox} a separate image is
 * created for every tab. Default format is
 * image/jpg.
 * <ul>
 * <li>Naming for forms is &lt;form fully qualified class name&gt;.jpg</li>
 * <li>If a form type has multiple instances, the name is &lt;form fully qualified class name&gt;_&lt;index&gt;.jpg</li>
 * <li>Naming for tab boxes is &lt;form fully qualified class name&gt;_&lt;index&gt;_&lt;tabbox simple class
 * name&gt;_&lt;tab simple class name&gt;.jpg</li>
 * </ul>
 */
public class PrintFormsAction extends AbstractAction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PrintFormsAction.class);

  private Class<?>[] m_formTypes;
  private String m_contentType;
  private File m_destinationFolder;
  // output files
  private final List<File> m_printedFiles;
  // printing index
  private int m_formImageIndex;
  // printing statistics
  private int m_formCount;
  private int m_imageCount;
  private int m_errorCount;

  public PrintFormsAction() {
    super();
    m_contentType = "image/jpg";
    m_printedFiles = new ArrayList<File>();
  }

  public Class<?>[] getFormTypes() {
    return m_formTypes;
  }

  public void setFormTypes(Class<?>[] formTypes) {
    m_formTypes = formTypes;
  }

  /**
   * Convenience setter to choose all existing form types in a specific plugin
   */
  public void setFormTypesByBundle(Bundle bundle) {
    BundleBrowser b = new BundleBrowser(bundle.getSymbolicName(), bundle.getSymbolicName());
    ArrayList<Class<?>> list = new ArrayList<Class<?>>();
    for (String name : b.getClasses(false, true)) {
      try {
        Class<?> c = bundle.loadClass(name);
        if (IForm.class.isAssignableFrom(c)) {
          list.add(c);
        }
      }
      catch (ClassNotFoundException e) {
        // nop
      }
    }
    m_formTypes = list.toArray(new Class[list.size()]);
  }

  public String getContentType() {
    return m_contentType;
  }

  public void setContentType(String s) {
    m_contentType = s;
  }

  public File getDestinationFolder() {
    return m_destinationFolder;
  }

  public void setDestinationFolder(File folder) {
    m_destinationFolder = folder;
  }

  public File[] getPrintedFiles() {
    return m_printedFiles.toArray(new File[m_printedFiles.size()]);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void execAction() throws ProcessingException {
    if (getFormTypes() == null) {
      throw new VetoException("formTypes array is null");
    }
    if (getDestinationFolder() == null) {
      throw new VetoException("destinationFolder is null");
    }
    m_printedFiles.clear();
    m_formCount = 0;
    m_imageCount = 0;
    m_errorCount = 0;
    for (Class<?> c : getFormTypes()) {
      try {
        m_formImageIndex = 0;
        if (IForm.class.isAssignableFrom(c)) {
          for (IForm f : createFormInstancesFor((Class<? extends IForm>) c)) {
            m_formCount++;
            processForm(f);
          }
        }
      }
      catch (Throwable t) {
        m_errorCount++;
        LOG.error(c.getName(), t);
      }
    }
  }

  protected List<IForm> createFormInstancesFor(Class<? extends IForm> formClass) throws Throwable {
    ArrayList<IForm> list = new ArrayList<IForm>();
    list.add(formClass.newInstance());
    return list;
  }

  protected void processForm(final IForm f) throws Throwable {
    f.setModal(false);
    f.setAutoAddRemoveOnDesktop(true);
    f.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    f.setDisplayViewId(null);
    AbstractFormHandler handler = new AbstractFormHandler() {
    };
    Method m = AbstractForm.class.getDeclaredMethod("startInternal", IFormHandler.class);
    m.setAccessible(true);
    m.invoke(f, handler);
    final P_FormListener listener = new P_FormListener();
    f.addFormListener(listener);

    new ClientSyncJob("print " + f.getClass().getSimpleName(), ClientSyncJob.getCurrentSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        printForm(f, null);
        // set all tabboxes visible
        for (IFormField field : f.getAllFields()) {
          if (field instanceof ITabBox && !field.isVisible()) {
            field.setVisible(true);
          }
        }
        // collect all tabbox tabs and print them
        for (IFormField field : f.getAllFields()) {
          if (field instanceof ITabBox && field.isVisible()) {
            final ITabBox tabBox = (ITabBox) field;
            IGroupBox selectedTab = null;
            if (tabBox.isVisible()) {
              selectedTab = tabBox.getSelectedTab();
            }
            if (tabBox.isVisible()) {
              for (final IGroupBox g : tabBox.getGroupBoxes()) {
                if (g != selectedTab) {
                  listener.addRunnable(new Runnable() {
                    @Override
                    public void run() {
                      tabBox.setSelectedTab(g);
                      printFormField(f, tabBox, "_" + g.getClass().getSimpleName());
                    }
                  });
                }
              }
            }
          }
        }
        // add form close runnable
        listener.addRunnable(new Runnable() {
          @Override
          public void run() {
            try {
              f.doClose();
            }
            catch (ProcessingException e) {
              LOG.error("could not close form '" + f.getClass().getSimpleName() + "'", e);
            }
          }
        });

      }
    }.schedule();
    f.waitFor();
  }

  private class P_FormListener implements FormListener {
    List<Runnable> m_runnables = Collections.synchronizedList(new ArrayList<Runnable>());

    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      if (e.getType() == FormEvent.TYPE_PRINTED) {
        m_printedFiles.add(e.getPrintedFile());
        if (!m_runnables.isEmpty()) {
          Runnable r = m_runnables.remove(0);
          r.run();
        }
      }
    }

    public void addRunnable(Runnable r) {
      m_runnables.add(r);
    }
  }

  protected int getFormImageIndex() {
    return m_formImageIndex;
  }

  protected void nextFormImageIndex() {
    m_formImageIndex++;
  }

  public int getResultFormCount() {
    return m_formCount;
  }

  public int getResultImageCount() {
    return m_imageCount;
  }

  public int getResultErrorCount() {
    return m_errorCount;
  }

  protected void printForm(IForm f, String contextName) {
    String name = f.getClass().getName() + (getFormImageIndex() > 0 ? "_" + getFormImageIndex() : "");
    String ext = getContentType().substring(getContentType().lastIndexOf("/") + 1);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("file", new File(getDestinationFolder(), name + (contextName != null ? contextName : "") + "." + ext));
    parameters.put("contentType", getContentType());
    f.printForm(PrintDevice.File, parameters);
    nextFormImageIndex();
    m_imageCount++;
  }

  protected void printFormField(IForm form, IFormField f, String contextName) {
    String name = form.getClass().getName() + "_" + getFormImageIndex() + "_" + f.getClass().getSimpleName();
    String ext = getContentType().substring(getContentType().lastIndexOf("/") + 1);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("file", new File(getDestinationFolder(), name + (contextName != null ? contextName : "") + "." + ext));
    parameters.put("contentType", getContentType());
    form.printField(f, PrintDevice.File, parameters);
    nextFormImageIndex();
    m_imageCount++;
  }

}
