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

import javax.mail.Message;
import javax.mail.Store;

import org.eclipse.scout.commons.exception.ProcessingException;

@SuppressWarnings("restriction")
public interface IImapAdapter {

  Message[] getUnseenMessages() throws ProcessingException;

  Message[] getUnseenMessages(String folderName) throws ProcessingException;

  Message[] getAllMessages() throws ProcessingException;

  Message[] getAllMessages(String folderName) throws ProcessingException;

  void moveToTrash(Message[] messages) throws ProcessingException;

  void moveMessages(String destFolderName, Message[] messages) throws ProcessingException;

  void copyMessages(String destFolderName, Message[] messages) throws ProcessingException;

  /**
   * messages are flagged as DELETED and their folder is closed in order to delete them on the server
   */
  void deleteMessagesPermanently(Message[] messages) throws ProcessingException;

  void createFolder(String folderName) throws ProcessingException;

  void removeFolder(String folderName) throws ProcessingException;

  void connect() throws ProcessingException;

  void closeConnection() throws ProcessingException;

  boolean isConnected();

  Store getStore() throws ProcessingException;

  String getDefaultFolderName();

  void setDefaultFolderName(String defaultFolderName);

  String getHost();

  void setHost(String host);

  int getPort();

  void setPort(int port);

  String getUsername();

  void setUsername(String username);

  String getPassword();

  void setPassword(String password);

  boolean isUseSSL();

  void setUseSSL(boolean useSSL);

  String[] getSSLProtocols();

  void setSSLProtocols(String[] sslProtocols);

}
