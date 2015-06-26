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
package org.eclipse.scout.rt.client.ui.messagebox;

import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.html.IHtmlContent;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Implementation of message box.<br/>
 * Use {@link MessageBoxes} to create a message box.
 */
@Bean
public class MessageBox extends AbstractPropertyObserver implements IMessageBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MessageBox.class);

  /**
   * Do not use, use {@link MessageBoxes#create()} instead.
   */
  public MessageBox() {
  }

  /**
   * Instance
   */
  private final EventListenerList m_listenerList = new EventListenerList();
  private final IMessageBoxUIFacade m_uiFacade = new P_UIFacade();

  private long m_autoCloseMillis = -1;

  private String m_iconId;

  private String m_header;
  private String m_body;
  private IHtmlContent m_html;

  private String m_yesButtonText;
  private String m_noButtonText;
  private String m_cancelButtonText;

  private String m_hiddenText;
  private String m_copyPasteText;
  // cached
  private String m_copyPasteTextInternal;
  // modality
  private final IBlockingCondition m_blockingCondition = Jobs.getJobManager().createBlockingCondition("block", false);
  private IFuture<Void> m_autoCloseJob;
  // result
  private int m_answer;
  private boolean m_answerSet;
  private int m_severity;

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  @Override
  public String header() {
    return m_header;
  }

  @Override
  public MessageBox header(String header) {
    m_header = header;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public String body() {
    return m_body;
  }

  @Override
  public MessageBox body(String body) {
    m_body = body;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public IHtmlContent html() {
    return m_html;
  }

  @Override
  public MessageBox html(IHtmlContent html) {
    m_html = html;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public String hiddenText() {
    return m_hiddenText;
  }

  @Override
  public MessageBox hiddenText(String hiddenText) {
    m_hiddenText = hiddenText;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public String yesButtonText() {
    return m_yesButtonText;
  }

  @Override
  public MessageBox yesButtonText(String yesButtonText) {
    m_yesButtonText = yesButtonText;
    return this;
  }

  @Override
  public String noButtonText() {
    return m_noButtonText;
  }

  @Override
  public MessageBox noButtonText(String noButtonText) {
    m_noButtonText = noButtonText;
    return this;
  }

  @Override
  public String cancelButtonText() {
    return m_cancelButtonText;
  }

  @Override
  public MessageBox cancelButtonText(String cancelButtonText) {
    m_cancelButtonText = cancelButtonText;
    return this;
  }

  @Override
  public String iconId() {
    return m_iconId;
  }

  @Override
  public MessageBox iconId(String iconId) {
    m_iconId = iconId;
    return this;
  }

  @Override
  public int severity() {
    return m_severity;
  }

  @Override
  public MessageBox severity(int severity) {
    m_severity = severity;
    return this;
  }

  @Override
  public long autoCloseMillis() {
    return m_autoCloseMillis;
  }

  @Override
  public MessageBox autoCloseMillis(long autoCloseMillis) {
    m_autoCloseMillis = autoCloseMillis;
    return this;
  }

  @Override
  public String copyPasteText() {
    if (m_copyPasteText == null) {
      updateCopyPasteTextInternal();
      return m_copyPasteTextInternal;
    }
    else {
      return m_copyPasteText;
    }
  }

  @Override
  public MessageBox copyPasteText(String copyPasteText) {
    m_copyPasteText = copyPasteText;
    return this;
  }

  protected void updateCopyPasteTextInternal() {
    if (m_copyPasteTextInternal == null) {
      m_copyPasteTextInternal = StringUtility.join("\n\n",
          m_header,
          m_body,
          m_html == null ? null : HTMLUtility.getPlainText(m_html.toEncodedHtml()),
              m_hiddenText);
    }
  }

  /*
   * Model observer
   */
  @Override
  public void addMessageBoxListener(MessageBoxListener listener) {
    m_listenerList.add(MessageBoxListener.class, listener);
  }

  @Override
  public void removeMessageBoxListener(MessageBoxListener listener) {
    m_listenerList.remove(MessageBoxListener.class, listener);
  }

  private void fireClosed() {
    fireMessageBoxEvent(new MessageBoxEvent(this, MessageBoxEvent.TYPE_CLOSED));
  }

  private void fireMessageBoxEvent(MessageBoxEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(MessageBoxListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((MessageBoxListener) listeners[i]).messageBoxChanged(e);
      }
    }
  }

  @Override
  public IMessageBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public boolean isOpen() {
    return m_blockingCondition.isBlocking();
  }

  /**
   * Displays the message box and waits for a response.
   * <p>
   * If {@link #autoCloseMillis()} is set, the message box will return with {@link IMessageBox#CANCEL_OPTION} after the
   * specific time.
   */
  @Override
  public int show() {
    return show(CANCEL_OPTION);
  }

  /**
   * Displays the message box and waits for a response.
   * <p>
   * If {@link #autoCloseMillis()} is set, the message box will return with given response after the specific time.
   */
  @Override
  public int show(int defaultResult) {
    m_answerSet = false;
    m_answer = defaultResult;
    if (ClientSessionProvider.currentSession() != null) {
      m_blockingCondition.setBlocking(true);
      try {
        // check if the desktop is observing this process
        IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
        if (desktop == null || !desktop.isOpened()) {
          LOG.warn("there is no desktop or the desktop has not yet been opened in the ui, default answer is CANCEL");
          m_answerSet = true;
          m_answer = CANCEL_OPTION;
        }
        else {
          // request a gui
          desktop.addMessageBox(this);
          // attach auto-cancel timer
          if (autoCloseMillis() > 0) {
            final long dt = autoCloseMillis();
            m_autoCloseJob = ClientJobs.schedule(new IRunnable() {
              @Override
              public void run() throws Exception {
                if (IFuture.CURRENT.get() == m_autoCloseJob) {
                  closeMessageBox();
                }
              }
            }, dt, TimeUnit.MILLISECONDS, ClientJobs.newInput(ClientRunContexts.copyCurrent()).name("Auto-close %s", header()));
          }
          // start sub event dispatch thread
          waitFor();
          if (m_autoCloseJob != null) {
            m_autoCloseJob.cancel(true);
            m_autoCloseJob = null;
          }
        }
      }
      finally {// end request gui
        fireClosed();
      }
    }
    else {
      LOG.warn("outside ScoutSessionThread, default answer is CANCEL");
      m_answerSet = true;
      m_answer = CANCEL_OPTION;
    }
    return m_answer;
  }

  private void waitFor() {
    try {
      m_blockingCondition.waitFor();
    }
    catch (ProcessingException e) {
      if (e.isInterruption()) {
        LOG.info(ScoutTexts.get("UserInterrupted"), e.getCause());
      }
      else {
        LOG.error("Failed to wait for the MessageBox to close", e);
      }

      if (ModelJobs.isModelJob(IFuture.CURRENT.get())) {
        throw new IllegalStateException("Failed to wait for the message box to close. Exit processing because not synchronized with the model-thread anymore.", e);
      }
    }
  }

  private void closeMessageBox() {
    m_autoCloseJob = null;
    m_blockingCondition.setBlocking(false);
  }

  private class P_UIFacade implements IMessageBoxUIFacade {

    @Override
    public void setResultFromUI(int option) {
      switch (option) {
        case YES_OPTION:
        case NO_OPTION:
        case CANCEL_OPTION: {
          if (!m_answerSet) {
            m_answerSet = true;
            m_answer = option;
          }
          closeMessageBox();
          break;
        }
      }
    }
  }

}
