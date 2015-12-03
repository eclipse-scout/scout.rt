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

import javax.mail.Message;
import javax.mail.Store;

public interface IImapAdapter {

  Message[] getUnseenMessages();

  Message[] getUnseenMessages(String folderName);

  Message[] getAllMessages();

  Message[] getAllMessages(String folderName);

  void moveToTrash(Message[] messages);

  void moveMessages(String destFolderName, Message[] messages);

  void copyMessages(String destFolderName, Message[] messages);

  /**
   * messages are flagged as DELETED and their folder is closed in order to delete them on the server
   */
  void deleteMessagesPermanently(Message[] messages);

  void createFolder(String folderName);

  void removeFolder(String folderName);

  void connect();

  void closeConnection();

  boolean isConnected();

  Store getStore();

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
