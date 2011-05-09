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
package org.eclipse.scout.rt.ui.swing.form.fields.htmlfield;

/**
 *  Copyright (c) 2001,2004 BSI AG
 */
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextPaneEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutHtmlField extends SwingScoutValueFieldComposite<IHtmlField> implements ISwingScoutHtmlField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutHtmlField.class);

  private HTMLEditorKit m_htmlKit;
  private HTMLDocument m_htmlDoc;
  private StyleSheet m_styleSheet;
  private JTextPane m_htmlView;
  private JScrollPane m_scrollPane;
  private JPanelEx m_htmlViewPanel;
  private String m_originalText;
  private File m_tempFolder;

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanel container = new JPanelEx();
    container.setName(getScoutObject().getClass().getSimpleName() + ".container");
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    // viewer
    m_htmlKit = new HTMLEditorKit();
    m_htmlDoc = (HTMLDocument) (m_htmlKit.createDefaultDocument());
    m_styleSheet = m_htmlDoc.getStyleSheet();
    //
    m_htmlView = new JTextPaneEx();
    m_htmlView.setName(getScoutObject().getClass().getSimpleName() + ".htmlView");
    m_htmlView.setEditorKit(m_htmlKit);
    m_htmlView.setDocument(m_htmlDoc);
    m_htmlView.setEditable(false);
    m_htmlView.setBorder(null);
    m_htmlView.setMargin(null);
    m_htmlView.setCaretPosition(0);
    m_htmlView.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
          fireHyperlinkActionFromSwing(e.getURL());
        }
      }
    });
    //
    m_htmlViewPanel = new JPanelEx(new SingleLayout());
    m_htmlView.setName(getScoutObject().getClass().getSimpleName() + ".htmlViewPanel");
    m_htmlViewPanel.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
    if (getScoutObject().isScrollBarEnabled()) {
      m_scrollPane = new JScrollPaneEx(m_htmlView);
      m_htmlViewPanel.add(m_scrollPane);
    }
    else {
      m_htmlViewPanel.add(m_htmlView);
    }
    container.add(m_htmlViewPanel);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(m_htmlView);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JTextPane getSwingHtmlField() {
    return m_htmlView;
  }

  protected JScrollPane getScrollPane() {
    return m_scrollPane;
  }

  protected HTMLEditorKit getHtmlKit() {
    return m_htmlKit;
  }

  protected String getOriginalText() {
    return m_originalText;
  }

  /*
   * scout properties
   */
  @Override
  protected void attachScout() {
    super.attachScout();
  }

  protected File getTempFolder(boolean autoCreate) {
    if (m_tempFolder == null) {
      if (autoCreate) {
        File folder = null;
        try {
          folder = File.createTempFile("attachements." + hashCode(), "");
          folder.delete();
          folder.mkdir();
          folder.deleteOnExit();
        }
        catch (IOException e) {
          LOG.error("could not create temp directory for mail attachement.", e);
        }
        m_tempFolder = folder;
      }
    }
    return m_tempFolder;
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    // create attachments
    RemoteFile[] a = getScoutObject().getAttachments();
    if (a != null) {
      for (RemoteFile f : a) {
        if (f != null && f.exists()) {
          try {
            writeTempFile(f.getPath(), new ByteArrayInputStream(f.extractData()));
          }
          catch (Exception e1) {
            LOG.warn("could not read remote file '" + f.getName() + "'", e1);
          }
        }
      }
    }
    // set content
    JTextPane swingField = getSwingHtmlField();
    if (s == null) {
      s = "";
    }
    if (m_originalText == null) {
      m_originalText = "";
    }
    if (m_originalText.equals(s)) {
      return;
    }
    String wellFormedHtml = getSwingEnvironment().styleHtmlText(this, s);
    m_originalText = wellFormedHtml;
    //
    int oldPos = swingField.getCaretPosition();
    m_htmlDoc = (HTMLDocument) (m_htmlKit.createDefaultDocument());
    File tempFolder = getTempFolder(false);
    if (tempFolder != null) {
      try {
        m_htmlDoc.setBase(tempFolder.toURI().toURL());
      }
      catch (MalformedURLException e) {
        LOG.warn("Setting document base", e);
      }
    }
    m_styleSheet = m_htmlDoc.getStyleSheet();
    m_htmlView.setDocument(m_htmlDoc);
    m_htmlView.setText(m_originalText);
    int newPos = Math.max(0, Math.min(oldPos, swingField.getDocument().getLength()));
    swingField.setCaretPosition(newPos);
  }

  private File writeTempFile(String relFullName, InputStream content) {
    relFullName = relFullName.replaceAll("\\\\", "/");
    if (relFullName == null || relFullName.length() == 0) {
      return null;
    }
    if (!relFullName.startsWith("/")) {
      relFullName = "/" + relFullName;
    }
    File ioF = new File(getTempFolder(true), relFullName);
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

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_htmlView.setEditable(getScoutObject().isHtmlEditor());
  }

  protected void fireHyperlinkActionFromSwing(final URL url) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireHyperlinkActionFromUI(url);
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    String htmlText = getSwingHtmlField().getText();
    // if default initial html text, set text to empty text
    if (isDefaultHtmlText(htmlText)) {
      htmlText = "";
    }
    final String text = htmlText;
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getSwingEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    // end notify
    getSwingEnvironment().dispatchImmediateSwingJobs();
    return true;// continue always
  }

  public static boolean isDefaultHtmlText(String s) {
    if (s == null || s.length() == 0) {
      return false;
    }
    //
    String canonicalText = s.replaceAll("[ \\n\\r\\t]+", "").trim();
    String defaultText = "<html><head></head><body><p></p></body></html>";
    return defaultText.equalsIgnoreCase(canonicalText);
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
  }
}
