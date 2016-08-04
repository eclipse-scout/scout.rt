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
package org.eclipse.scout.rt.server.services.common.pop3;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POP3Adapter {

  public static final String TRASH_FOLDER_NAME = "Trash";
  private static final Logger LOG = LoggerFactory.getLogger(POP3Adapter.class);

  private boolean m_useSSL;
  private String m_host;
  private int m_port;
  private String m_username;
  private String m_password;
  private String m_defaultFolderName;
  private Store m_store;
  private HashMap<String, Folder> m_cachedFolders;
  private boolean m_connected = false;

  public POP3Adapter() {
    m_cachedFolders = new HashMap<String, Folder>();
  }

  public POP3Adapter(String host, int port, String username, String password) {
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

  public String[] getUnseenMessageSubjects() {
    return getUnseenMessageSubjects(getDefaultFolderName());
  }

  public String[] getUnseenMessageSubjects(String folderName) {
    connect();
    ArrayList<Message> messages = new ArrayList<Message>();
    Folder folder = null;
    String[] subjects = null;
    try {
      folder = findFolder(folderName);
      if (folder != null) {
        if (!folder.isOpen()) {
          folder.open(Folder.READ_WRITE);
        }
        Message item;
        Message[] m = folder.getMessages();
        for (int i = 0; i < Array.getLength(m); i++) {
          item = m[i];
          if (!item.isSet(Flags.Flag.SEEN)) {
            messages.add(item);
            item.setFlag(Flags.Flag.DELETED, true);

          }
        }
        subjects = new String[messages.size()];
        Message[] messageArray = messages.toArray(new Message[messages.size()]);
        for (int i = 0; i < messages.size(); i++) {
          subjects[i] = messageArray[i].getSubject();
        }
        folder.close(true);
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }

    return subjects;
  }

  public Message[] getUnseenMessages(final Flags.Flag markAfterRead) {
    final ArrayList<Message> list = new ArrayList<Message>();
    visitUnseenMessages(getDefaultFolderName(), new IPOP3MessageVisitor() {
      @Override
      public boolean visit(Message m) throws MessagingException {
        list.add(m);
        m.setFlag(markAfterRead, true);
        return true;
      }
    });
    return list.toArray(new Message[list.size()]);
  }

  public void visitUnseenMessages(String folderName, IPOP3MessageVisitor visitor) {
    connect();
    Folder folder = null;
    try {
      folder = findFolder(folderName);
      if (folder != null) {
        if (!folder.isOpen()) {
          folder.open(Folder.READ_WRITE);
        }
        int count = folder.getMessageCount();
        for (int i = 0; i < count; i++) {
          Message m = folder.getMessage(i + 1);
          if (!m.isSet(Flags.Flag.SEEN)) {
            boolean ok = visitor.visit(m);
            if (ok) {
            }
            else {
              break;
            }
          }
        }
        folder.close(true);
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException("reading folder " + folderName, e);
    }
  }

  protected void connect() {
    try {
      final Properties props = new Properties();
      props.setProperty("mail.pop3.host", getHost());
      props.setProperty("mail.pop3.user", getUsername());
      props.setProperty("mail.pop3.password", getPassword());
      props.setProperty("mail.pop3.port", "" + getPort());
      props.setProperty("mail.pop3.auth", "" + isUseSSL());

      Session session = Session.getInstance(props, new javax.mail.Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(props.getProperty("mail.pop3.user"),
              props.getProperty("mail.pop3.password"));
        }
      });

      m_store = session.getStore("pop3");
      m_store.connect();
    }
    catch (MessagingException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
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
          folder.open(Folder.READ_WRITE);
          m_cachedFolders.put(name, folder);
        }
      }
      catch (MessagingException e) {
        throw new ProcessingException("could not open folder: " + name, e);
      }
    }
    return folder;
  }

  public void closeConnection() {
    if (isConnected()) {
      List<MessagingException> exceptions = new ArrayList<MessagingException>();
      for (Folder folder : m_cachedFolders.values()) {
        try {

          folder.close(true);
        }
        catch (MessagingException e) {
          exceptions.add(e);
        }
        finally {
          try {
            folder.close(false);
          }
          catch (MessagingException e) {
            LOG.warn("Could not close folder", e);
          }
        }
      }
      try {
        m_store.close();
      }
      catch (MessagingException e) {
        exceptions.add(e);
      }
      if (exceptions.size() > 0) {
        throw new ProcessingException(exceptions.get(0).getMessage());
      }
      m_cachedFolders.clear();
      m_connected = false;
    }
  }

  public boolean hasAttachments(Message message, boolean includingInlineAttachments) {
    connect();
    try {
      Object content = message.getContent();
      if (content instanceof Multipart) {
        Multipart multipart = (Multipart) content;
        for (int i = 0; i < multipart.getCount(); i++) {
          if (multipart.getBodyPart(i).getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
            return true;
          }
          else if (includingInlineAttachments && multipart.getBodyPart(i).getDisposition().equalsIgnoreCase(Part.INLINE)) {
            return true;
          }
        }
        return false;
      }
      else {
        return false;
      }
    }
    catch (MessagingException me) {
      throw new ProcessingException("could not read message!", me);
    }
    catch (IOException ioe) {
      throw new ProcessingException("could not read message content!", ioe);
    }
  }

  public boolean isConnected() {
    return m_connected;
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

  public void setUseSSL(String s) {
    m_useSSL = StringUtility.parseBoolean(s, true);
  }
}
