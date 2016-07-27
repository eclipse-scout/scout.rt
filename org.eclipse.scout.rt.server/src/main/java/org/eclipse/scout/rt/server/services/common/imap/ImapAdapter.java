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

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImapAdapter implements IImapAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(ImapAdapter.class);

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

  @Override
  public Message[] getUnseenMessages() {
    return getUnseenMessages(getDefaultFolderName());
  }

  @Override
  public Message[] getUnseenMessages(String folderName) {
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

  @Override
  public Message[] getAllMessages() {
    return getAllMessages(getDefaultFolderName());
  }

  @Override
  public Message[] getAllMessages(String folderName) {
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

  @Override
  public void moveToTrash(Message[] messages) {
    moveMessages(TRASH_FOLDER_NAME, messages);
  }

  @Override
  public void moveMessages(String destFolderName, Message[] messages) {
    copyMessages(destFolderName, messages, true);
  }

  @Override
  public void copyMessages(String destFolderName, Message[] messages) {
    copyMessages(destFolderName, messages, false);
  }

  protected void copyMessages(String destFolderName, Message[] messages, boolean deleteSourceMessages) {
    connect();

    Folder destFolder = null;
    try {
      destFolder = findFolder(destFolderName);
      if (destFolder != null) {
        Map<Folder, Set<Message>> messagesBySourceFolder = groupMessagesBySourceFolder(messages);
        for (Folder sourceFolder : messagesBySourceFolder.keySet()) {
          Set<Message> messageSet = messagesBySourceFolder.get(sourceFolder);
          Message[] messagesForSourceFolder = messageSet.toArray(new Message[messageSet.size()]);
          // use copyMessages instead of appendMessages because this can be optimized by the mail-server
          sourceFolder.copyMessages(messagesForSourceFolder, destFolder);
          if (deleteSourceMessages) {
            deleteMessagesPermanently(messagesForSourceFolder);
          }
        }
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  /**
   * @return the messages grouped by source folder
   */
  protected Map<Folder, Set<Message>> groupMessagesBySourceFolder(Message[] messages) {
    Map<Folder, Set<Message>> messagesByFolder = new HashMap<Folder, Set<Message>>();
    if (messages == null || messages.length == 0) {
      return messagesByFolder;
    }

    for (Message message : messages) {
      if (message.isExpunged()) {
        LOG.debug("Ignoring expunged message {}", message);
        continue;
      }
      if (message.getFolder() == null) {
        LOG.warn("Folder is empty for message {}", message);
        continue;
      }
      if (!messagesByFolder.containsKey(message.getFolder())) {
        messagesByFolder.put(message.getFolder(), new HashSet<Message>());
      }
      messagesByFolder.get(message.getFolder()).add(message);
    }
    return messagesByFolder;
  }

  /**
   * messages are flagged as DELETED and their folder is closed in order to delete them on the server
   */
  @Override
  public void deleteMessagesPermanently(Message[] messages) {
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

  @Override
  public void createFolder(String folderName) {
    findFolder(folderName, true);
  }

  @Override
  public void removeFolder(String folderName) {
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

  @Override
  public void connect() {
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
        props.setProperty("mail.imap.ssl.enable", "true");

        if (CollectionUtility.hasElements(getSSLProtocols())) {
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
      catch (MessagingException e) {
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
    // subclasses may modify properties
  }

  protected Folder findFolder(String name) {
    return findFolder(name, false);
  }

  protected Folder findFolder(String name, boolean createNonExisting) {
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

  @Override
  public void closeConnection() {
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

  @Override
  public boolean isConnected() {
    return m_store != null && m_store.isConnected();
  }

  @Override
  public Store getStore() {
    connect();
    return m_store;
  }

  @Override
  public String getDefaultFolderName() {
    return m_defaultFolderName;
  }

  @Override
  public void setDefaultFolderName(String defaultFolderName) {
    m_defaultFolderName = defaultFolderName;
  }

  @Override
  public String getHost() {
    return m_host;
  }

  @Override
  public void setHost(String host) {
    m_host = host;
  }

  @Override
  public int getPort() {
    return m_port;
  }

  @Override
  public void setPort(int port) {
    m_port = port;
  }

  @Override
  public String getUsername() {
    return m_username;
  }

  @Override
  public void setUsername(String username) {
    m_username = username;
  }

  @Override
  public String getPassword() {
    return m_password;
  }

  @Override
  public void setPassword(String password) {
    m_password = password;
  }

  @Override
  public boolean isUseSSL() {
    return m_useSSL;
  }

  @Override
  public void setUseSSL(boolean useSSL) {
    m_useSSL = useSSL;
  }

  @Override
  public String[] getSSLProtocols() {
    if (m_sslProtocols == null) {
      return null;
    }
    return Arrays.copyOf(m_sslProtocols, m_sslProtocols.length);
  }

  @Override
  public void setSSLProtocols(String[] sslProtocols) {
    if (sslProtocols == null) {
      m_sslProtocols = null;
    }
    else {
      m_sslProtocols = Arrays.copyOf(sslProtocols, sslProtocols.length);
    }
  }
}
