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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.BlockingCondition;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.OfficialVersion;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class MessageBox extends AbstractPropertyObserver implements IMessageBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MessageBox.class);

  /**
   * Convenience function for simple info message box
   */
  public static int showOkMessage(String title, String header, String info) {
    MessageBox mbox = new MessageBox(
        title,
        header,
        info,
        ScoutTexts.get("OkButton"),
        null,
        null
        );
    return mbox.startMessageBox();
  }

  /**
   * Convenience function for simple yes/no message box
   */
  public static int showYesNoMessage(String title, String header, String info) {
    MessageBox mbox = new MessageBox(
        title,
        header,
        info,
        ScoutTexts.get("YesButton"),
        ScoutTexts.get("NoButton"),
        null
        );
    return mbox.startMessageBox();
  }

  /**
   * Convenience function for simple yes/no/cancel message box
   */
  public static int showYesNoCancelMessage(String title, String header, String info) {
    MessageBox mbox = new MessageBox(
        title,
        header,
        info,
        ScoutTexts.get("YesButton"),
        ScoutTexts.get("NoButton"),
        ScoutTexts.get("CancelButton")
        );
    return mbox.startMessageBox();
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param items
   *          one item or array of multiple items
   */
  public static boolean showDeleteConfirmationMessage(Object items) {
    return showDeleteConfirmationMessage(null, items);
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param items
   *          a list of multiple items
   * @since Scout 4.0.1
   */
  public static boolean showDeleteConfirmationMessage(Collection<?> items) {
    return showDeleteConfirmationMessage(null, items);
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param itemType
   *          display text in plural such as "Persons", "Relations", "Tickets",
   *          ...
   * @param items
   *          one item or array of multiple items
   */
  public static boolean showDeleteConfirmationMessage(String itemType, Object items) {
    if (items == null) {
      return showDeleteConfirmationMessage(itemType, Collections.emptyList());
    }
    else if (items instanceof Object[]) {
      return showDeleteConfirmationMessage(itemType, Arrays.asList((Object[]) items));
    }
    else if (items instanceof Collection) {
      return showDeleteConfirmationMessage(itemType, (Collection) items);
    }
    else {
      return showDeleteConfirmationMessage(itemType, Collections.singletonList(items));
    }
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param itemType
   *          display text in plural such as "Persons", "Relations", "Tickets",
   *          ...
   * @param items
   *          a list of multiple items
   * @since Scout 4.0.1
   */
  public static boolean showDeleteConfirmationMessage(String itemType, Collection<?> items) {
    StringBuilder t = new StringBuilder();

    int n = 0;
    if (items != null) {
      n = items.size();
      int i = 0;
      for (Object item : items) {
        if (i < 10 || i == n - 1) {
          t.append("- ");
          t.append(StringUtility.emptyIfNull(item));
          t.append("\n");
        }
        else if (i == 10) {
          t.append("  ...\n");
        }
        else {
        }
        i++;
      }
    }
    //
    String intro = null;
    String action = null;
    if (itemType != null) {
      intro = (n > 0 ? ScoutTexts.get("DeleteConfirmationTextX", itemType) : ScoutTexts.get("DeleteConfirmationTextNoItemListX", itemType));
      action = (n > 0 ? t.toString() : null);
    }
    else {
      intro = (n > 0 ? ScoutTexts.get("DeleteConfirmationText") : ScoutTexts.get("DeleteConfirmationTextNoItemList"));
      action = (n > 0 ? t.toString() : null);
    }
    MessageBox mbox = new MessageBox(
        ScoutTexts.get("DeleteConfirmationTitle"),
        intro,
        action,
        ScoutTexts.get("YesButton"),
        ScoutTexts.get("NoButton"),
        null
        );
    return mbox.startMessageBox() == IMessageBox.YES_OPTION;
  }

  /**
   * Instance
   */
  private final EventListenerList m_listenerList = new EventListenerList();
  private final IMessageBoxUIFacade m_uiFacade;
  private long m_autoCloseMillis;
  private String m_title;
  private String m_iconId;
  private String m_introText;
  private String m_actionText;
  private String m_yesButtonText;
  private String m_noButtonText;
  private String m_cancelButtonText;
  private String m_hiddenText;
  private String m_copyPasteText;
  private boolean m_htmlEnabled;
  // cached
  private String m_copyPasteTextInternal;
  // modality
  private final BlockingCondition m_blockingCondition = new BlockingCondition(false);
  private Job m_autoCloseJob;
  // result
  private int m_answer;
  private boolean m_answerSet;
  private int m_severity;

  public MessageBox(String title, String introText, String okButtonText) {
    this(title, introText, null, okButtonText, null, null);
  }

  public MessageBox(String title, String introText, String actionText, String yesButtonText, String noButtonText, String cancelButtonText) {
    this(title, introText, actionText, yesButtonText, noButtonText, cancelButtonText, null, null);
  }

  public MessageBox(String title, String introText, String actionText, String yesButtonText, String noButtonText, String cancelButtonText, String hiddenText, String iconId) {
    m_uiFacade = new P_UIFacade();
    m_title = title;
    m_introText = introText;
    m_actionText = actionText;
    m_hiddenText = hiddenText;
    m_yesButtonText = yesButtonText;
    m_noButtonText = noButtonText;
    m_cancelButtonText = cancelButtonText;
    m_iconId = iconId;
    m_autoCloseMillis = -1;
    //
    if (m_title == null) {
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop != null) {
        m_title = desktop.getTitle();
      }
    }
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
  public String getTitle() {
    return m_title;
  }

  @Override
  public void setTitle(String s) {
    m_title = s;
    m_copyPasteTextInternal = null;
  }

  @Override
  public String getIntroText() {
    return m_introText;
  }

  @Override
  public void setIntroText(String s) {
    m_introText = s;
    m_copyPasteTextInternal = null;
  }

  @Override
  public String getActionText() {
    return m_actionText;
  }

  @Override
  public void setActionText(String s) {
    m_actionText = s;
    m_copyPasteTextInternal = null;
  }

  @Override
  public String getHiddenText() {
    return m_hiddenText;
  }

  @Override
  public void setHiddenText(String s) {
    m_hiddenText = s;
    m_copyPasteTextInternal = null;
  }

  @Override
  public String getYesButtonText() {
    return m_yesButtonText;
  }

  @Override
  public void setYesButtonText(String s) {
    m_yesButtonText = s;
  }

  @Override
  public String getNoButtonText() {
    return m_noButtonText;
  }

  @Override
  public void setNoButtonText(String s) {
    m_noButtonText = s;
  }

  @Override
  public String getCancelButtonText() {
    return m_cancelButtonText;
  }

  @Override
  public void setCancelButtonText(String s) {
    m_cancelButtonText = s;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public void setIconId(String iconId) {
    m_iconId = iconId;
  }

  @Override
  public int getSeverity() {
    return m_severity;
  }

  @Override
  public void setSeverity(int severity) {
    m_severity = severity;
  }

  @Override
  public long getAutoCloseMillis() {
    return m_autoCloseMillis;
  }

  @Override
  public void setAutoCloseMillis(long millis) {
    m_autoCloseMillis = millis;
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
  public void setCopyPasteText(String s) {
    m_copyPasteText = s;
  }

  private void updateCopyPasteTextInternal() {
    if (m_copyPasteTextInternal == null) {
      StringBuffer buf = new StringBuffer();
      if (OfficialVersion.customCopyrightText != null) {
        buf.append(OfficialVersion.customCopyrightText + "\n");
      }
      else {
        buf.append(OfficialVersion.COPYRIGHT + "\n");
      }
      buf.append("java.vm.version: " + System.getProperty("java.vm.version") + "\n");
      buf.append("os.name_version: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n");
      buf.append("user.name: " + System.getProperty("user.name") + "\n");
      buf.append("\n");
      if (m_title != null) {
        buf.append(m_title + "\n\n");
      }
      if (m_introText != null) {
        buf.append(m_introText + "\n\n");
      }
      if (m_actionText != null) {
        buf.append(m_actionText + "\n\n");
      }
      if (m_hiddenText != null) {
        buf.append(m_hiddenText + "\n\n");
      }
      m_copyPasteTextInternal = buf.toString();
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

  @Override
  public int startMessageBox() {
    return startMessageBox(CANCEL_OPTION);
  }

  @Override
  public int startMessageBox(int defaultResult) {
    m_answerSet = false;
    m_answer = defaultResult;
    if (ClientSyncJob.getCurrentSession() != null) {
      m_blockingCondition.setBlocking(true);
      try {
        // check if the desktop is observing this process
        IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
        if (desktop == null || !desktop.isOpened()) {
          LOG.warn("there is no desktop or the desktop has not yet been opened in the ui, default answer is CANCEL");
          m_answerSet = true;
          m_answer = CANCEL_OPTION;
        }
        else {
          // request a gui
          desktop.addMessageBox(this);
          // attach auto-cancel timer
          if (getAutoCloseMillis() > 0) {
            final long dt = getAutoCloseMillis();
            m_autoCloseJob = new Job("Auto-close " + getTitle()) {
              @Override
              protected IStatus run(IProgressMonitor monitor) {
                if (this == m_autoCloseJob) {
                  closeMessageBox();
                }
                return Status.OK_STATUS;
              }
            };
            m_autoCloseJob.schedule(dt);
          }
          // start sub event dispatch thread
          waitFor();
          if (m_autoCloseJob != null) {
            m_autoCloseJob.cancel();
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
    catch (InterruptedException e) {
      LOG.info(ScoutTexts.get("UserInterrupted"));
    }
  }

  private void closeMessageBox() {
    m_autoCloseJob = null;
    m_blockingCondition.setBlocking(false);
  }

  /**
   * Enables HTML rendering of {@link #m_introText} and {@link #m_actionText}. The setter must be called before starting
   * the message box.
   * Subsequent changes of the html enabled flag have no effect after the widget has been initialized.
   */
  @Override
  public void setHtmlEnabled(boolean enabled) {
    m_htmlEnabled = enabled;
  }

  @Override
  public boolean isHtmlEnabled() {
    return m_htmlEnabled;
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
