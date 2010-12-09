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
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
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
  private URL m_currentURL;

  public SwtScoutBrowserField() {
  }

  private void deleteCache(File file) {
    IOUtility.deleteDirectory(file);
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    Browser browser = getEnvironment().getFormToolkit().createBrowser(container, SWT.NONE);
    browser.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (m_tempDir != null) {
          deleteCache(m_tempDir);
        }
      }
    });
    browser.addLocationListener(new LocationAdapter() {
      @Override
      public void changing(LocationEvent event) {
        URL url = null;
        try {
          url = new URL(event.location);
        }
        catch (MalformedURLException e) {
          try {
            url = new File(event.location).toURI().toURL();
          }
          catch (MalformedURLException e1) {
            e1.printStackTrace();
          }
        }
        if (url != null) {
          event.doit = url.getProtocol().equals("file");
          if (!event.doit) {
            handleSwtLinkAction(url);
          }
        }

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

  /*
   * scout properties
   */
  @Override
  protected void attachScout() {
    super.attachScout();
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
    if (IBrowserField.PROP_EXTERNAL_URL.equals(name)) {
      setExternalURLFromScout();
    }
  }

  @Override
  protected void setValueFromScout() {
    setExternalURLFromScout();
  }

  protected void setExternalURLFromScout() {
    URL url = getScoutObject().getExternalURL();
    RemoteFile r = getScoutObject().getValue();
    if (url == null && r != null && r.exists()) {
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
              url = f.toURI().toURL();
              break;
            }
          }
        }
        else {
          File f = new File(m_tempDir, r.getName());
          r.writeData(f);
          url = f.toURI().toURL();
        }
      }
      catch (Throwable t) {
        LOG.error("preparing html content for " + r, t);
      }
    }
    m_currentURL = url;
    if (m_currentURL != null) {
      getSwtField().setUrl(m_currentURL.toExternalForm());
    }
    else {
      getSwtField().setText("");
    }
  }

  protected void handleSwtLinkAction(final URL location) {
    Runnable job = new Runnable() {
      @Override
      public void run() {
        // try {

        getScoutObject().getUIFacade().fireHyperlinkActionFromUI(location);
        // } catch (MalformedURLException e) {
        // LOG.warn("could not create an URL out of '"+location.toExternalForm()+"'.",e);
        // }
      }
    };
    getEnvironment().invokeScoutLater(job, 0);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
  }

  protected void fireHyperlinkActionFromSwt(final URL url) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireHyperlinkActionFromUI(url);
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

}
