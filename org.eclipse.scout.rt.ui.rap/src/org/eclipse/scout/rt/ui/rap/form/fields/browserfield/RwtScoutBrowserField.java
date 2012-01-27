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
package org.eclipse.scout.rt.ui.rap.form.fields.browserfield;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.browser.BrowserExtension;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutBrowserField extends RwtScoutValueFieldComposite<IBrowserField> implements IRwtScoutBrowserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutBrowserField.class);

  private String m_currentLocation;
  private BrowserExtension m_browserExtension;

  public RwtScoutBrowserField() {
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    Browser browser = getUiEnvironment().getFormToolkit().createBrowser(container, SWT.NONE);
    m_browserExtension = new BrowserExtension(browser);
    m_browserExtension.attach();
    browser.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        m_browserExtension.detach();
      }
    });
    browser.addLocationListener(new LocationAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void changing(LocationEvent event) {
        event.doit = fireBeforeLocationChangedFromUi(event.location);
      }

      @Override
      public void changed(LocationEvent event) {
        fireAfterLocationChangedFromUi(event.location);
      }
    });
    //
    setUiContainer(container);
    setUiLabel(label);
    setUiField(browser);
    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  public Browser getUiField() {
    return (Browser) super.getUiField();
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
    m_browserExtension.clearResourceCache();
    m_browserExtension.clearLocalHyperlinkCache();
    String location = getScoutObject().getLocation();
    RemoteFile r = getScoutObject().getValue();
    if (location == null && r != null && r.exists()) {
      try {
        if (r.getName().matches(".*\\.(zip|jar)")) {
          File tempDir = IOUtility.createTempDirectory("browser");
          try {
            r.writeZipContentToDirectory(tempDir);
            String simpleName = r.getName().replaceAll("\\.(zip|jar)", ".htm");
            //rewrite local urls and register resource
            int prefixLen = tempDir.getAbsolutePath().length() + 1;
            for (File f : IOUtility.listFilesInSubtree(tempDir, null)) {
              if (f.isFile()) {
                String path = f.getAbsolutePath().substring(prefixLen);
                if (path.toLowerCase().matches(".*\\.(htm|html)")) {
                  String content = IOUtility.getContent(new InputStreamReader(new FileInputStream(f)));
                  content = m_browserExtension.adaptLocalHyperlinks(content, 1);
                  if (location == null && path.startsWith(simpleName)) {
                    //this is the index.html
                    location = m_browserExtension.addResource(simpleName, new ByteArrayInputStream(content.getBytes("UTF-8")));
                  }
                  else {
                    m_browserExtension.addResource(path, new ByteArrayInputStream(content.getBytes("UTF-8")));
                  }
                }
                else if (path.toLowerCase().matches(".*\\.(svg)")) {
                  String content = IOUtility.getContent(new InputStreamReader(new FileInputStream(f)));
                  content = m_browserExtension.adaptLocalHyperlinks(content, 1);
                  m_browserExtension.addResource(path, new ByteArrayInputStream(content.getBytes("UTF-8")));
                }
                else {
                  m_browserExtension.addResource(path, new FileInputStream(f));
                }
              }
            }
          }
          finally {
            if (tempDir != null) {
              IOUtility.deleteDirectory(tempDir);
            }
          }
        }
        else {
          //rewrite local urls
          String content = IOUtility.getContent(r.getDecompressedReader());
          content = m_browserExtension.adaptLocalHyperlinks(content, 1);
          location = m_browserExtension.addResource(r.getName(), new ByteArrayInputStream(content.getBytes("UTF-8")));
        }
      }
      catch (Throwable t) {
        LOG.error("preparing html content for " + r, t);
      }
    }
    m_currentLocation = location;
    if (m_currentLocation != null) {
      getUiField().setUrl(m_currentLocation);
    }
    else {
      getUiField().setText("");
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    //nop
  }

  protected boolean fireBeforeLocationChangedFromUi(final String location) {
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
      getUiEnvironment().invokeScoutLater(t, 0);
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

  protected void fireAfterLocationChangedFromUi(final String location) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireAfterLocationChangedFromUI(location);
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
    // end notify
  }
}
