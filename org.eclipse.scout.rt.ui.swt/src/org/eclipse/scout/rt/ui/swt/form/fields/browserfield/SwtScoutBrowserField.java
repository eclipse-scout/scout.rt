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
package org.eclipse.scout.rt.ui.swt.form.fields.browserfield;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

public class SwtScoutBrowserField extends SwtScoutValueFieldComposite<IBrowserField> implements ISwtScoutBrowserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutBrowserField.class);

  private File m_tempDir;
  private String m_currentLocation;

  public SwtScoutBrowserField() {
  }

  private void deleteCache(File file) {
    IOUtility.deleteDirectory(file);
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    Browser browser = getEnvironment().getFormToolkit().createBrowser(container, SWT.NONE);
    browser.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        if (m_tempDir != null) {
          deleteCache(m_tempDir);
        }
      }
    });
    browser.addLocationListener(new LocationAdapter() {
      @Override
      public void changing(LocationEvent event) {
        event.doit = fireBeforeLocationChangedFromSwt(event.location);
      }

      @Override
      public void changed(LocationEvent event) {
        fireAfterLocationChangedFromSwt(event.location);
      }
    });
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(browser);
    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));

  }

  @Override
  public Browser getSwtField() {
    return (Browser) super.getSwtField();
  }

  @Override
  protected void detachScout() {
    if (m_tempDir != null) {
      IOUtility.deleteDirectory(m_tempDir);
      m_tempDir = null;
    }
    super.detachScout();
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IBrowserField.PROP_LOCATION.equals(name)) {
      setLocationFromScout();
    }
  }

  @Override
  protected void setValueFromScout() {
    setLocationFromScout();
  }

  protected void setLocationFromScout() {
    String location = getScoutObject().getLocation();
    RemoteFile r = getScoutObject().getValue();
    if (location == null && r != null && r.exists()) {
      try {
        if (m_tempDir == null) {
          try {
            m_tempDir = IOUtility.createTempDirectory("html");
          }
          catch (ProcessingException e) {
            LOG.error("create temporary folder", e);
          }
        }
        if (r.getName().matches(".*\\.(zip|jar)")) {
          r.writeZipContentToDirectory(m_tempDir);
          String simpleName = r.getName().replaceAll("\\.(zip|jar)", ".htm");
          for (File f : m_tempDir.listFiles()) {
            if (f.getName().startsWith(simpleName)) {
              location = f.toURI().toURL().toExternalForm();
              break;
            }
          }
        }
        else {
          File f = new File(m_tempDir, r.getName());
          r.writeData(f);
          location = f.toURI().toURL().toExternalForm();
        }
      }
      catch (Throwable t) {
        LOG.error("preparing html content for " + r, t);
      }
    }
    m_currentLocation = location;
    if (m_currentLocation != null) {
      getSwtField().setUrl(m_currentLocation);
    }
    else {
      getSwtField().setText("");
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
  }

  protected boolean fireBeforeLocationChangedFromSwt(final String location) {
    final AtomicReference<Boolean> accept = new AtomicReference<Boolean>();
    synchronized (accept) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          synchronized (accept) {
            accept.set(getScoutObject().getUIFacade().fireBeforeLocationChangedFromUI(location));
            accept.notifyAll();
          }
        }
      };
      getEnvironment().invokeScoutLater(t, 0);
      // end notify
      //wait at most 10 seconds
      try {
        accept.wait(10000L);
      }
      catch (InterruptedException e) {
        //nop
      }
    }
    return accept.get() != null ? accept.get().booleanValue() : false;
  }

  protected void fireAfterLocationChangedFromSwt(final String location) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireAfterLocationChangedFromUI(location);
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

}
