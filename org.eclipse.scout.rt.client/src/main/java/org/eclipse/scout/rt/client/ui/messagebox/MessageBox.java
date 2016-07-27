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
package org.eclipse.scout.rt.client.ui.messagebox;

import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.DisplayParentResolver;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of message box.<br/>
 * Use {@link MessageBoxes} to create a message box.
 */
@Bean
public class MessageBox extends AbstractPropertyObserver implements IMessageBox {

  private static final Logger LOG = LoggerFactory.getLogger(MessageBox.class);

  private final EventListenerList m_listenerList = new EventListenerList();
  private final IMessageBoxUIFacade m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());

  private IDisplayParent m_displayParent;

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
  private final IBlockingCondition m_blockingCondition = Jobs.newBlockingCondition(false);
  // result
  private int m_answer;
  private boolean m_answerSet;
  private int m_severity;

  @PostConstruct
  protected void initConfig() {
    m_displayParent = BEANS.get(DisplayParentResolver.class).resolve(this);
  }

  @Override
  public IDisplayParent getDisplayParent() {
    return m_displayParent;
  }

  @Override
  public IMessageBox withDisplayParent(IDisplayParent displayParent) {
    Assertions.assertFalse(ClientSessionProvider.currentSession().getDesktop().isShowing(this), "Property 'displayParent' cannot be changed because message box is already showing [messageBox={}]", this);

    if (displayParent == null) {
      displayParent = BEANS.get(DisplayParentResolver.class).resolve(this);
    }

    m_displayParent = Assertions.assertNotNull(displayParent, "'displayParent' must not be null");

    return this;
  }

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
  public String getHeader() {
    return m_header;
  }

  @Override
  public MessageBox withHeader(String header) {
    m_header = header;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public String getBody() {
    return m_body;
  }

  @Override
  public MessageBox withBody(String body) {
    m_body = body;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public IHtmlContent getHtml() {
    return m_html;
  }

  @Override
  public MessageBox withHtml(IHtmlContent html) {
    m_html = html;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public String getHiddenText() {
    return m_hiddenText;
  }

  @Override
  public MessageBox withHiddenText(String hiddenText) {
    m_hiddenText = hiddenText;
    m_copyPasteTextInternal = null;
    return this;
  }

  @Override
  public String getYesButtonText() {
    return m_yesButtonText;
  }

  @Override
  public MessageBox withYesButtonText(String yesButtonText) {
    m_yesButtonText = yesButtonText;
    return this;
  }

  @Override
  public String getNoButtonText() {
    return m_noButtonText;
  }

  @Override
  public MessageBox withNoButtonText(String noButtonText) {
    m_noButtonText = noButtonText;
    return this;
  }

  @Override
  public String getCancelButtonText() {
    return m_cancelButtonText;
  }

  @Override
  public MessageBox withCancelButtonText(String cancelButtonText) {
    m_cancelButtonText = cancelButtonText;
    return this;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public MessageBox withIconId(String iconId) {
    m_iconId = iconId;
    return this;
  }

  @Override
  public int getSeverity() {
    return m_severity;
  }

  @Override
  public MessageBox withSeverity(int severity) {
    m_severity = severity;
    return this;
  }

  @Override
  public long getAutoCloseMillis() {
    return m_autoCloseMillis;
  }

  @Override
  public MessageBox withAutoCloseMillis(long autoCloseMillis) {
    m_autoCloseMillis = autoCloseMillis;
    return this;
  }

  @Override
  public String getCopyPasteText() {
    if (m_copyPasteText == null) {
      updateCopyPasteTextInternal();
      return m_copyPasteTextInternal;
    }
    else {
      return m_copyPasteText;
    }
  }

  @Override
  public MessageBox withCopyPasteText(String copyPasteText) {
    m_copyPasteText = copyPasteText;
    return this;
  }

  protected void updateCopyPasteTextInternal() {
    if (m_copyPasteTextInternal == null) {
      m_copyPasteTextInternal = StringUtility.join("\n\n",
          m_header,
          m_body,
          m_html == null ? null : BEANS.get(HtmlHelper.class).toPlainText(m_html.toHtml()),
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

  protected void fireClosed() {
    fireMessageBoxEvent(new MessageBoxEvent(this, MessageBoxEvent.TYPE_CLOSED));
  }

  protected void fireMessageBoxEvent(MessageBoxEvent e) {
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
   * If {@link #getAutoCloseMillis()} is set, the message box will return with {@link IMessageBox#CANCEL_OPTION} after
   * the specific time.
   */
  @Override
  public int show() {
    return show(CANCEL_OPTION);
  }

  /**
   * Displays the message box and waits for a response.
   * <p>
   * If {@link #getAutoCloseMillis()} is set, the message box will return with given response after the specific time.
   */
  @Override
  public int show(int defaultResult) {
    m_answerSet = false;
    m_answer = defaultResult;

    if (ClientSessionProvider.currentSession() == null) {
      LOG.warn("outside ScoutSessionThread, default answer is CANCEL");
      m_answerSet = true;
      m_answer = CANCEL_OPTION;
      return m_answer;
    }

    m_blockingCondition.setBlocking(true);
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    try {
      // check if the desktop is observing this process
      if (desktop == null || !desktop.isOpened()) {
        LOG.warn("there is no desktop or the desktop has not yet been opened in the ui, default answer is CANCEL");
        m_answerSet = true;
        m_answer = CANCEL_OPTION;
      }
      else {
        // request a gui
        desktop.showMessageBox(this);
        // attach auto-cancel timer
        IFuture<Void> autoCloseFuture = null;
        if (getAutoCloseMillis() > 0) {
          final long closeDelay = getAutoCloseMillis();
          autoCloseFuture = Jobs.schedule(new IRunnable() {
            @Override
            public void run() throws Exception {
              closeMessageBox();
            }
          }, Jobs.newInput()
              .withName("Closing message box")
              .withRunContext(ClientRunContexts.copyCurrent())
              .withExecutionTrigger(Jobs.newExecutionTrigger()
                  .withStartIn(closeDelay, TimeUnit.MILLISECONDS)));
        }
        // start sub event dispatch thread
        waitFor();

        if (autoCloseFuture != null && !autoCloseFuture.isDone()) {
          autoCloseFuture.cancel(true);
        }
      }
    }
    finally {
      if (desktop != null) {
        desktop.hideMessageBox(this);
      }
      fireClosed();
    }
    return m_answer;
  }

  protected void waitFor() {
    // Do not exit upon interruption, as the message box would be closed immediately otherwise.
    m_blockingCondition.waitForUninterruptibly(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
  }

  protected void closeMessageBox() {
    m_blockingCondition.setBlocking(false);
  }

  protected class P_UIFacade implements IMessageBoxUIFacade {

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
