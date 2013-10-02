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
package org.eclipse.scout.rt.server.services.common.imap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.AbstractService;

@SuppressWarnings("restriction")
public abstract class AbstractIMAPService extends AbstractService implements IIMAPService {

  private String m_host;
  private int m_port;
  private String m_sslProtocols;
  private String m_mailbox;
  private String m_username;
  private String m_password;
  private boolean m_opened = false;
  private Folder m_folder;
  private Store m_store;

  public AbstractIMAPService() {
    init();
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(10)
  protected String getConfiguredHost() {
    return null;
  }

  public String getHost() {
    return m_host;
  }

  public void setHost(String s) {
    m_host = s;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(20)
  protected int getConfiguredPort() {
    return -1;
  }

  public int getPort() {
    return m_port;
  }

  public void setPort(int i) {
    m_port = i;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(25)
  protected String getConfiguredSslProtocols() {
    return null;
  }

  public String getSslProtocols() {
    return m_sslProtocols;
  }

  public void setSslProtocols(String s) {
    m_sslProtocols = s;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  protected String getConfiguredMailbox() {
    return null;
  }

  public String getMailbox() {
    return m_mailbox;
  }

  public void setMailbox(String s) {
    m_mailbox = s;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(40)
  protected String getConfiguredUserName() {
    return null;
  }

  public String getUserName() {
    return m_username;
  }

  public void setUserName(String s) {
    m_username = s;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(50)
  protected String getConfiguredPassword() {
    return null;
  }

  public String getPassword() {
    return m_password;
  }

  public void setPassword(String s) {
    m_password = s;
  }

  public void openConnection() throws ProcessingException {
    openConnection(false);
  }

  public void openConnection(boolean useSSL) throws ProcessingException {
    try {
      Properties props = new Properties();
      props.put("mail.transport.protocol", "imap");
      if (m_host != null) {
        props.put("mail.imap.host", m_host);
      }
      if (m_port > 0) {
        props.put("mail.imap.port", "" + m_port);
      }
      if (!StringUtility.isNullOrEmpty(getSslProtocols())) {
        props.put("mail.imap.ssl.protocols", getSslProtocols());
      }
      if (m_username != null) {
        props.put("mail.imap.user", m_username);
      }
      if (useSSL) {
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.imap.socketFactory.fallback", "false");
        if (m_port > 0) {
          props.put("mail.imap.socketFactory.port", "" + m_port);
        }
      }
      Session session = Session.getInstance(props, null);
      m_store = session.getStore("imap");
      if (m_username != null && m_host != null) {
        m_store.connect(System.getProperty("mail.imap.host"), m_username, m_password);
      }
      else {
        m_store.connect();
      }
      if (m_mailbox != null) {
        m_folder = m_store.getFolder(m_mailbox);
      }
      else {
        m_folder = m_store.getDefaultFolder();
      }
      m_folder.open(Folder.READ_WRITE);
      m_opened = true;
    }
    catch (Exception e) {
      throw new ProcessingException("opening", e);
    }
  }

  public void closeConnection() throws ProcessingException {
    try {
      m_folder.close(true);
      m_folder = null;
      m_store.close();
      m_store = null;
      m_opened = false;
    }
    catch (Exception e) {
      throw new ProcessingException(e.getMessage(), e);
    }
    finally {
      if (m_folder != null) {
        try {
          m_folder.close(false);
        }
        catch (Throwable fatal) {
        }
      }
      if (m_store != null) {
        try {
          m_store.close();
        }
        catch (Throwable fatal) {
        }
      }
    }
  }

  @Override
  public Message[] getUnreadMessages() throws ProcessingException {
    ReadMailTask task = new ReadMailTask();
    doTask(task);
    return task.getUnreadMessages();
  }

  @Override
  public void deleteAllMessages() throws ProcessingException {
    DeleteMailTask task = new DeleteMailTask(true);
    doTask(task);
  }

  @Override
  public void deleteMessages(Message... toDelete) throws ProcessingException {
    DeleteMailTask task = new DeleteMailTask(false);
    task.setMessagesToDelete(toDelete);
    doTask(task);
  }

  private void doTask(AbstractMailTask task) throws ProcessingException {
    if (m_opened == false) {
      throw new ProcessingException("No connection opened");
    }
    else {
      task.doTask(m_folder);
    }
  }

  private void init() {
    setHost(getConfiguredHost());
    setPort(getConfiguredPort());
    setMailbox(getConfiguredMailbox());
    setUserName(getConfiguredUserName());
    setPassword(getConfiguredPassword());
  }

  private abstract class AbstractMailTask {
    public void doTask(Folder folder) throws ProcessingException {
    }
  }

  private class ReadMailTask extends AbstractMailTask {

    private ArrayList<Message> messages = new ArrayList<Message>();

    @Override
    public void doTask(Folder folder) throws ProcessingException {
      try {
        Message item;
        Message[] m = folder.getMessages();
        for (int i = 0; i < Array.getLength(m); i++) {
          item = m[i];
          if (!item.isSet(Flags.Flag.SEEN)) {
            messages.add(item);
          }
        }
      }
      catch (Exception e) {
        throw new ProcessingException(e.getMessage(), e);
      }
    }

    public Message[] getUnreadMessages() {
      Message[] messageArray = new Message[messages.size()];
      messages.toArray(messageArray);
      return messageArray;
    }
  }

  private class DeleteMailTask extends AbstractMailTask {

    private Message[] toDelete;
    private boolean deleteAll;

    DeleteMailTask(boolean all) {
      deleteAll = all;
    }

    @Override
    public void doTask(Folder folder) throws ProcessingException {
      try {
        Message[] m = folder.getMessages();
        if (deleteAll == true) {
          toDelete = folder.getMessages();
        }
        for (int i = 0; i < Array.getLength(toDelete); i++) {
          Message item = toDelete[i];
          for (int j = 0; j < Array.getLength(m); j++) {
            Message msg = m[j];
            if (item.equals(msg)) {
              msg.setFlag(Flags.Flag.DELETED, true);
            }
          }
        }
      }
      catch (Exception e) {
        throw new ProcessingException(e.getMessage(), e);
      }
    }

    public void setMessagesToDelete(Message[] msgs) {
      toDelete = msgs;
    }

  }
}
