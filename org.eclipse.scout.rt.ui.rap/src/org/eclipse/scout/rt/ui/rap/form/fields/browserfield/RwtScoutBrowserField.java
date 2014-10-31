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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.browser.BrowserExtension;
import org.eclipse.scout.rt.ui.rap.ext.browser.IHyperlinkCallback;
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

  private BrowserExtension m_browserExtension;

  public RwtScoutBrowserField() {
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    // Super call invokes setValueFromScout.
    // If both value and location are null we don't initialize it a second time.
    // If location is set, it will win over value.
    if (getScoutObject().getLocation() != null) {
      setLocationFromScout();
    }
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    Browser browser = getUiEnvironment().getFormToolkit().createBrowser(container, SWT.NONE);
    setUiField(browser);

    browser.addLocationListener(new LocationAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void changing(LocationEvent event) {
        boolean changeAccepted = fireBeforeLocationChangedFromUi(event.location);
        event.doit = changeAccepted;
      }

      @Override
      public void changed(LocationEvent event) {
        // check: from local to external location?
        if (m_browserExtension != null && (isExternalUrl(event.location))) {
          detachBrowserExtension();
        }
        fireAfterLocationChangedFromUi(event.location);
      }
    });
    //
    setUiContainer(container);
    setUiLabel(label);

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  protected boolean isExternalUrl(String location) {
    boolean externalUrl = false;
    try {
      String host = new URL(location).getHost();
      externalUrl = StringUtility.hasText(host) && StringUtility.notEqualsIgnoreCase("local", host);
    }
    catch (Throwable t) {
      // nop: externalUrl == false
    }
    return externalUrl;
  }

  protected void attachBrowserExtension(Browser browser) {
    detachBrowserExtension();
    final BrowserExtension browserExtension = new BrowserExtension(browser, getUiEnvironment(), new IHyperlinkCallback() {

      @Override
      public void execute(String url) {
        setLocationInternal(url);
      }
    });
    browserExtension.attach();
    browser.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        detachBrowserExtension();
      }
    });
    adaptBrowserExtension(browserExtension);
    m_browserExtension = browserExtension;
  }

  protected void adaptBrowserExtension(BrowserExtension browserExtension) {
    // nop
  }

  protected void detachBrowserExtension() {
    if (m_browserExtension != null) {
      m_browserExtension.detach();
      m_browserExtension = null;
    }
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

  protected void setLocationFromScout() {
    setLocationInternal(getScoutObject().getLocation());
  }

  @Override
  protected void setValueFromScout() {
    RemoteFile remoteFile = getScoutObject().getValue();
    String location = null;
    if (remoteFile != null && remoteFile.exists()) {
      if (m_browserExtension != null) {
        m_browserExtension.clearResourceCache();
        m_browserExtension.clearLocalHyperlinkCache();
      }
      else {
        attachBrowserExtension(getUiField());
      }

      try {
        if (remoteFile.getName().matches(".*\\.(zip|jar)")) {
          location = registerResourcesInZip(m_browserExtension, remoteFile);
        }
        else {
          String content = IOUtility.getContent(remoteFile.getDecompressedReader());
          content = m_browserExtension.adaptHyperlinks(content);
          location = m_browserExtension.addResource(remoteFile.getName(), new ByteArrayInputStream(content.getBytes("UTF-8")));
        }
        //Prevent caching by making the request unique
        if (location != null) {
          location += "?nocache=" + System.currentTimeMillis();
        }
      }
      catch (Throwable t) {
        LOG.error("preparing html content for " + remoteFile, t);
      }
    }
    setLocationInternal(location);
  }

  private String registerResourcesInZip(BrowserExtension browserExtension, RemoteFile zipFile) throws ProcessingException, IOException, UnsupportedEncodingException, FileNotFoundException {
    String location = null;
    File tempDir = IOUtility.createTempDirectory("browser");
    try {
      zipFile.writeZipContentToDirectory(tempDir);
      String simpleName = zipFile.getName().replaceAll("\\.(zip|jar)", ".htm");
      //rewrite local urls and register resource
      int prefixLen = tempDir.getAbsolutePath().length() + 1;
      for (File f : IOUtility.listFilesInSubtree(tempDir, null)) {
        if (f.isFile()) {
          String path = f.getAbsolutePath().substring(prefixLen);
          if (path.toLowerCase().matches(".*\\.(htm|html)")) {
            String content = IOUtility.getContent(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            content = browserExtension.adaptHyperlinks(content);
            if (location == null && path.startsWith(simpleName)) {
              //this is the index.html
              location = browserExtension.addResource(simpleName, new ByteArrayInputStream(content.getBytes("UTF-8")));
            }
            else {
              browserExtension.addResource(path, new ByteArrayInputStream(content.getBytes("UTF-8")));
            }
          }
          else if (path.toLowerCase().matches(".*\\.(svg)")) {
            String content = IOUtility.getContent(new InputStreamReader(new FileInputStream(f)));
            content = browserExtension.adaptHyperlinks(content);
            browserExtension.addResource(path, new ByteArrayInputStream(content.getBytes("UTF-8")));
          }
          else {
            browserExtension.addResource(path, new FileInputStream(f));
          }
        }
      }
    }
    finally {
      if (tempDir != null) {
        IOUtility.deleteDirectory(tempDir);
      }
    }
    return location;
  }

  protected void setLocationInternal(String location) {
    if (StringUtility.hasText(location)) {
      getUiField().setUrl(location);
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
    Runnable t = new Runnable() {
      @Override
      public void run() {
        accept.set(getScoutObject().getUIFacade().fireBeforeLocationChangedFromUI(location));
      }
    };
    JobEx job = getUiEnvironment().invokeScoutLater(t, 0);
    try {
      //wait at most 10 seconds
      job.join(10000L);
    }
    catch (InterruptedException e) {
      //nop
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
