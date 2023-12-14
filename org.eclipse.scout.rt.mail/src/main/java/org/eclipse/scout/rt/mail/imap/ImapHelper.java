/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail.imap;

import java.util.Properties;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;

import org.eclipse.scout.rt.oauth2.OAuth2Helper;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper to interact with an IMAP server.
 */
@ApplicationScoped
public class ImapHelper {

  private static final Logger LOG = LoggerFactory.getLogger(ImapHelper.class);

  /**
   * Connects to the IMAP server based on the given config.
   */
  public Store connect(ImapServerConfig config) {
    Session session = createSession(config);

    try {
      Store store = session.getStore(ObjectUtility.nvl(config.getCustomStoreProtocol(), "imap"));
      if (!store.isConnected()) {
        if (StringUtility.hasText(config.getUsername()) && StringUtility.hasText(config.getHost())) {
          store.connect(config.getHost(), config.getUsername(), config.getPassword());
        }
        else {
          store.connect();
        }
      }
      return store;
    }
    catch (AuthenticationFailedException e) { // NOSONAR
      throw new ProcessingException("IMAP-Authentication failed on {}@{}:{}", config.getUsername(), config.getHost(), config.getPort());
    }
    catch (MessagingException e) {
      throw new ProcessingException("Failed to connect to imap server {}@{}:{}", config.getUsername(), config.getHost(), config.getPort(), e);
    }
  }

  /**
   * Creates a {@link Session} based on the given config.
   */
  protected Session createSession(ImapServerConfig config) {
    Properties props = new Properties();
    props.setProperty("mail.transport.protocol", "imap"); // always use 'imap' as protocol here
    if (StringUtility.hasText(config.getHost())) {
      props.setProperty("mail.imap.host", config.getHost());
    }
    if (config.getPort() != null && config.getPort() > 0) {
      props.setProperty("mail.imap.port", "" + config.getPort());
    }
    if (StringUtility.hasText(config.getUsername())) {
      props.setProperty("mail.imap.user", config.getUsername());
    }

    if (config.isUseSsl()) {
      props.setProperty("mail.imap.ssl.enable", "true");

      if (StringUtility.hasText(config.getSslProtocols())) {
        props.setProperty("mail.imap.ssl.protocols", config.getSslProtocols());
      }
    }

    if (config.getAuth2Config() != null) {
      props.setProperty("mail.imap.sasl.enable", "true");
      props.setProperty("mail.imap.sasl.mechanisms", "XOAUTH2");
      props.setProperty("mail.imap.auth.login.disable", "true");
      props.setProperty("mail.imap.auth.plain.disable", "true");
      config.withPassword(BEANS.get(OAuth2Helper.class).getToken(config.getAuth2Config()));
    }

    if (!CollectionUtility.isEmpty(config.getAdditionalSessionProperties())) {
      props.putAll(config.getAdditionalSessionProperties());
    }

    LOG.debug("Session created with properties {}", props);

    return Session.getInstance(props, null);
  }

  /**
   * Creates a folder. If the folder already exists, the existing folder is returned.
   */
  public Folder createFolder(Store store, String name) {
    return findFolder(store, name, true);
  }

  /**
   * Finds a folder. If no folder with the given name is found, <code>null</code> is returned.
   */
  public Folder findFolder(Store store, String name) {
    return findFolder(store, name, false);
  }

  protected Folder findFolder(Store store, String name, boolean createNonExisting) {
    try {
      Folder folder = store.getFolder(name);
      if (!folder.exists()) {
        if (!createNonExisting) {
          return null;
        }

        folder.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
        LOG.debug("Created folder {}", name);
      }

      if (!folder.isOpen()) {
        folder.open(Folder.READ_WRITE);
      }

      return folder;
    }
    catch (MessagingException e) {
      throw new ProcessingException("Failed to find or create folder {}", name, e);
    }
  }

  /**
   * Moves the messages from the source folder to the destination folder.
   * <p>
   * All messages must belong to the source folder.
   */
  public void moveMessages(Folder sourceFolder, Folder destinationFolder, Message[] messages) {
    if (messages.length == 0) {
      return;
    }

    try {
      // use copyMessages instead of appendMessages because this can be optimized by the mail-server
      sourceFolder.copyMessages(messages, destinationFolder); // expunge-safe call

      // delete message permanently
      sourceFolder.setFlags(messages, new Flags(Flag.DELETED), true); // expunge-safe call
      if (sourceFolder.isOpen()) {
        sourceFolder.expunge();
      }

      LOG.debug("Moved messages from {} to {}", sourceFolder.getName(), destinationFolder.getName());
    }
    catch (MessagingException e) {
      throw new ProcessingException("Failed to move messages", e);
    }
  }
}
