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
package org.eclipse.scout.rt.ui.swt.form.fields.htmlfield;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
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

public class SwtScoutHtmlField extends SwtScoutValueFieldComposite<IHtmlField> implements ISwtScoutHtmlField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutHtmlField.class);

  private File m_tempDir;

  public SwtScoutHtmlField() {
    File tempFile;
    try {
      tempFile = File.createTempFile("scoutHtmlField", "");
      tempFile.delete();
      tempFile.mkdir();
      tempFile.deleteOnExit();
      m_tempDir = tempFile;

    }
    catch (IOException e) {
      LOG.warn("could not create temp dir");
    }
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

  @Override
  protected void setDisplayTextFromScout(String rawHtml) {
    // create attachments
    RemoteFile[] a = getScoutObject().getAttachments();
    if (a != null) {
      for (RemoteFile f : a) {
        if (f != null && f.exists()) {
          try {
            writeTempFile(f.getPath(), new ByteArrayInputStream(f.extractData()));
          }
          catch (IOException e1) {
            LOG.warn("could not read remote file '" + f.getName() + "'", e1);
          }
        }
      }
    }
    if (rawHtml == null) {
      rawHtml = "";
    }
    String cleanHtml = rawHtml;
    try {
      File indexFile = writeTempFile("index.html", new ByteArrayInputStream(cleanHtml.getBytes()));
      File html = new File(m_tempDir.getAbsolutePath() + "/index.html");
      html.createNewFile();
      getSwtField().setUrl(indexFile.toURI().toURL().toExternalForm());
    }
    catch (IOException e) {
      LOG.error("could not create index file for html: '" + cleanHtml + "'", e);
    }
  }

  private File writeTempFile(String relFullName, InputStream content) {
    relFullName = relFullName.replaceAll("\\\\", "/");
    if (relFullName == null || relFullName.length() == 0) {
      return null;
    }
    if (!relFullName.startsWith("/")) {
      relFullName = "/" + relFullName;
    }
    File ioF = new File(m_tempDir.getAbsolutePath(), relFullName);
    ioF.getParentFile().mkdirs();
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(ioF);
      byte[] buffer = new byte[1026];
      int bytesRead;

      while ((bytesRead = content.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead); // write
      }
      out.flush();
      ioF.deleteOnExit();
      return ioF;
    }
    catch (IOException e) {
      LOG.error("could not create file in temp dir: '" + relFullName + "'", e);
      return null;
    }
    finally {
      if (out != null) {
        try {
          out.close();
        }
        catch (IOException e) {

        }
      }
    }

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

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
  }

}
