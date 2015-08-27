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
package org.eclipse.scout.rt.ui.rap.form.fields.htmlfield;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.browser.BrowserExtension;
import org.eclipse.scout.rt.ui.rap.ext.browser.IHyperlinkCallback;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutHtmlField extends RwtScoutValueFieldComposite<IHtmlField> implements IRwtScoutHtmlField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutHtmlField.class);
  private static final String VARIANT_HTMLFIELD = "htmlfield";

  private BrowserExtension m_browserExtension;

  public RwtScoutHtmlField() {
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    //Make sure the browser has a transparent background as default (necessary because there is no css background property for the browser)
    container.setBackgroundMode(SWT.INHERIT_DEFAULT);

    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    //browserContainer is necessary to align the browser with the label
    final Composite browserContainer = getUiEnvironment().getFormToolkit().createComposite(container);
    browserContainer.setLayout(new FillLayout());
    LogicalGridData layoutData = LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData());
    browserContainer.setLayoutData(layoutData);
    browserContainer.setData(RWT.CUSTOM_VARIANT, VARIANT_HTMLFIELD);

    Browser browser = getUiEnvironment().getFormToolkit().createBrowser(browserContainer, SWT.NONE);
    setUiField(browser);

    attachBrowserExtension(browser);

    browser.addLocationListener(new LocationAdapter() {
      private static final long serialVersionUID = 1L;

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
            LOG.error("", e1);
          }
        }
        if (url != null) {
          event.doit = url.getProtocol().equals("file");
          if (!event.doit) {
            handleUiLinkAction(url);
          }
        }
      }
    });
    //
    setUiContainer(container);
    setUiLabel(label);

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  protected void attachBrowserExtension(Browser browser) {
    detachBrowserExtension();
    final BrowserExtension browserExtension = new BrowserExtension(browser, getUiEnvironment(), new IHyperlinkCallback() {

      @Override
      public void execute(String url) {
        getUiField().setUrl(url);
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
    browserExtension.setDefaultHyperlinkTarget("_top");
  }

  protected void detachBrowserExtension() {
    if (m_browserExtension != null) {
      m_browserExtension.detach();
      m_browserExtension = null;
    }
  }

  @Override
  protected boolean isAutoSetLayoutData() {
    return false;
  }

  @Override
  public Browser getUiField() {
    return (Browser) super.getUiField();
  }

  protected void handleUiLinkAction(final URL url) {
    Runnable job = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireHyperlinkActionFromUI(url);
      }
    };
    getUiEnvironment().invokeScoutLater(job, 0);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
  }

  @Override
  protected void setDisplayTextFromScout(String rawHtml) {
    m_browserExtension.clearResourceCache();
    m_browserExtension.clearLocalHyperlinkCache();
    if (rawHtml == null) {
      rawHtml = "";
    }
    // create attachments
    for (RemoteFile f : getScoutObject().getAttachments()) {
      if (f != null && f.exists()) {
        try {
          m_browserExtension.addResource(f.getPath(), f.getDecompressedInputStream());
        }
        catch (IOException e1) {
          LOG.warn("could not read remote file '" + f.getName() + "'", e1);
        }
      }
    }
    // assume that rawHTML is encoded properly in the model layer
    String cleanHtml = getUiEnvironment().styleHtmlText(this, rawHtml);
    cleanHtml = m_browserExtension.adaptHyperlinks(cleanHtml);
    //fast create of browser content if there are no attachments
    if (CollectionUtility.isEmpty(getScoutObject().getAttachments())) {
      getUiField().setText(cleanHtml);
    }
    else {
      try {
        String indexFile = m_browserExtension.addResource("index.html", new ByteArrayInputStream(cleanHtml.getBytes("UTF-8")));
        // force the iframe to reload its content by making the file url unique
        if (indexFile != null) {
          indexFile += "?nocache=" + System.currentTimeMillis();
        }
        getUiField().setUrl(indexFile);
      }
      catch (UnsupportedEncodingException e) {
        //can not happen
      }
    }
  }

  protected void setScrollToAnchorFromScout(String anchorName) {
    if (!StringUtility.isNullOrEmpty(anchorName)) {
      getUiField().setUrl(getUiField().getUrl() + "#" + anchorName);
    }
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IHtmlField.PROP_SCROLLBAR_SCROLL_TO_END)) {
      getUiField().execute("window.scrollTo(0, document.body.scrollHeight)");
    }
    else if (name.equals(IHtmlField.PROP_SCROLLBAR_SCROLL_TO_ANCHOR)) {
      setScrollToAnchorFromScout(TypeCastUtility.castValue(newValue, String.class));
    }
  }
}
