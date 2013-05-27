/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *     Adrian Sacchi <adrian.sacchi@bsiag.com> - Bug 406813 - ImapAdapter: 6 Issues
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.imap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.eclipse.scout.commons.ListUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;

@SuppressWarnings("restriction")
public class ImapAdapter {

  public static final String TRASH_FOLDER_NAME = "Trash";
  public static final int DEFAULT_IMAP_PORT = 143;

  private boolean m_useSSL;
  private String[] m_sslProtocols;
  private String m_host;
  private int m_port = DEFAULT_IMAP_PORT;
  private String m_username;
  private String m_password;
  private String m_defaultFolderName;
  private Store m_store;
  private Map<String, Folder> m_cachedFolders;

  public ImapAdapter() {
    m_cachedFolders = new HashMap<String, Folder>();
  }

  public ImapAdapter(String host, int port, String username, String password) {
    this();
    m_host = host;
    m_port = port;
    m_username = username;
    m_password = password;
  }

  @Override
  protected void finalize() throws Throwable {
    closeConnection();
    super.finalize();
  }

  public Message[] getUnseenMessages() throws ProcessingException {
    return getUnseenMessages(getDefaultFolderName());
  }

  public Message[] getUnseenMessages(String folderName) throws ProcessingException {
    connect();
    ArrayList<Message> messages = new ArrayList<Message>();
    Folder folder = null;
    try {
      folder = findFolder(folderName);
      if (folder != null) {
        Message item;
        Message[] m = folder.getMessages();
        for (int i = 0; i < Array.getLength(m); i++) {
          item = m[i];
          if (!item.isSet(Flags.Flag.SEEN)) {
            messages.add(item);
          }

        }
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
    return messages.toArray(new Message[messages.size()]);
  }

  public Message[] getAllMessages() throws ProcessingException {
    return getAllMessages(getDefaultFolderName());
  }

  public Message[] getAllMessages(String folderName) throws ProcessingException {
    connect();
    Message[] messages = new Message[0];
    Folder folder = null;
    try {
      folder = findFolder(folderName);
      if (folder != null) {
        messages = folder.getMessages();
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
    return messages;
  }

  public void moveToTrash(Message[] messages) throws ProcessingException {
    moveMessages(TRASH_FOLDER_NAME, messages);
  }

  public void moveMessages(String destFolderName, Message[] messages) throws ProcessingException {
    copyMessages(destFolderName, messages, true);
  }

  public void copyMessages(String destFolderName, Message[] messages) throws ProcessingException {
    copyMessages(destFolderName, messages, false);
  }

  protected void copyMessages(String destFolderName, Message[] messages, boolean deleteSourceMessages) throws ProcessingException {
    connect();
    Folder destFolder = null;
    try {
      destFolder = findFolder(destFolderName);
      if (destFolder != null) {
        destFolder.appendMessages(messages);
        if (deleteSourceMessages) {
          deleteMessagesPermanently(messages);
        }
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  /**
   * messages are flagged as DELETED and their folder is closed in order to delete them on the server
   * 
   * @param messages
   * @throws ProcessingException
   */
  public void deleteMessagesPermanently(Message[] messages) throws ProcessingException {
    connect();
    Set<Folder> folders = new HashSet<Folder>();
    try {
      for (Message msg : messages) {
        folders.add(msg.getFolder());
        msg.setFlag(Flags.Flag.DELETED, true);
      }
      for (Folder f : folders) {
        if (f.isOpen()) {
          f.expunge();
        }
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public void createFolder(String folderName) throws ProcessingException {
    findFolder(folderName, true);
  }

  public void removeFolder(String folderName) throws ProcessingException {
    connect();
    try {
      Folder folder = findFolder(folderName);
      if (folder != null && folder.exists()) {
        if (folder.isOpen()) {
          folder.close(true);
        }
        folder.delete(true);
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public void connect() throws ProcessingException {
    if (!isConnected()) {
      m_cachedFolders.clear();
      Properties props = new Properties();
      props.put("mail.transport.protocol", "imap");
      if (getHost() != null) {
        props.put("mail.imap.host", getHost());
      }
      if (getPort() > 0) {
        props.put("mail.imap.port", "" + getPort());
      }
      if (getUsername() != null) {
        props.put("mail.imap.user", getUsername());
      }
      if (isUseSSL()) {
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.port", "" + getPort());

        if (ListUtility.length(getSSLProtocols()) > 0) {
          props.setProperty("mail.imap.ssl.protocols", StringUtility.join(" ", getSSLProtocols()));
        }
      }
      interceptProperties(props);
      Session session = Session.getInstance(props, null);
      try {
        m_store = session.getStore("imap");
        if (!m_store.isConnected()) {
          if (getUsername() != null && getHost() != null) {
            m_store.connect(getHost(), getUsername(), getPassword());
          }
          else {
            m_store.connect();
          }
        }
      }
      catch (AuthenticationFailedException e) {
        throw new ProcessingException("IMAP-Authentication failed on " + (m_host == null ? "?" : m_host) + ":" + m_port + ":" + m_username);
      }
      catch (Exception e) {
        throw new ProcessingException(e.getMessage(), e);
      }
    }
  }

  /**
   * Callback to modify IMAP mail properties.
   * 
   * @param props
   *          live list of mail properties
   */
  protected void interceptProperties(Properties props) {
  }

  protected Folder findFolder(String name) throws ProcessingException {
    return findFolder(name, false);
  }

  protected Folder findFolder(String name, boolean createNonExisting) throws ProcessingException {
    connect();
    Folder folder = m_cachedFolders.get(name);
    if (folder == null) {
      try {
        Folder f = m_store.getFolder(name);
        if (f.exists()) {
          folder = f;
        }
        else if (createNonExisting) {
          f.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
          folder = f;
        }
        if (folder != null) {
          m_cachedFolders.put(name, folder);
        }
      }
      catch (MessagingException e) {
        throw new ProcessingException("could not find folder: " + name, e);
      }
    }
    try {
      if (folder != null && !folder.isOpen()) {
        folder.open(Folder.READ_WRITE);
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException("could not open folder: " + name, e);
    }
    return folder;
  }

  public void closeConnection() throws ProcessingException {
    if (isConnected()) {
      List<MessagingException> exceptions = new ArrayList<MessagingException>();
      for (Folder folder : m_cachedFolders.values()) {
        try {
          if (folder.isOpen()) {
            folder.close(true);
          }
        }
        catch (MessagingException e) {
          exceptions.add(e);
        }
        finally {
          try {
            if (folder.isOpen()) {
              folder.close(false);
            }
          }
          catch (Throwable fatal) {
            // nop
          }
        }
      }
      try {
        if (m_store.isConnected()) {
          m_store.close();
        }
      }
      catch (MessagingException e) {
        exceptions.add(e);
      }
      m_cachedFolders.clear();
      if (!exceptions.isEmpty()) {
        throw new ProcessingException(exceptions.get(0).getMessage());
      }
    }
  }

  public boolean isConnected() {
    return m_store != null && m_store.isConnected();
  }

  public Store getStore() throws ProcessingException {
    connect();
    return m_store;
  }

  public String getDefaultFolderName() {
    return m_defaultFolderName;
  }

  public void setDefaultFolderName(String defaultFolderName) {
    m_defaultFolderName = defaultFolderName;
  }

  public String getHost() {
    return m_host;
  }

  public void setHost(String host) {
    m_host = host;
  }

  public int getPort() {
    return m_port;
  }

  public void setPort(int port) {
    m_port = port;
  }

  public String getUsername() {
    return m_username;
  }

  public void setUsername(String username) {
    m_username = username;
  }

  public String getPassword() {
    return m_password;
  }

  public void setPassword(String password) {
    m_password = password;
  }

  public boolean isUseSSL() {
    return m_useSSL;
  }

  public void setUseSSL(boolean useSSL) {
    m_useSSL = useSSL;
  }

  public String[] getSSLProtocols() {
    if (m_sslProtocols == null) {
      return null;
    }
    return Arrays.copyOf(m_sslProtocols, m_sslProtocols.length);
  }

  public void setSSLProtocols(String[] sslProtocols) {
    if (sslProtocols == null) {
      m_sslProtocols = null;
    }
    else {
      m_sslProtocols = Arrays.copyOf(sslProtocols, sslProtocols.length);
    }
  }
}
