/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail.smtp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Consumer;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.eclipse.scout.rt.mail.MailHelper;
import org.eclipse.scout.rt.mail.MailIDNConverter;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The helper to send messages over the smtp protocol.
 * <p>
 * Usage Example:
 *
 * <pre>
 * DataSource att = MailUtility.createDataSource(new File(&quot;D:\\pictures\\backgroundCorsaire.jpg&quot;));
 * MimeMessage message = MailUtility.createMimeMessage(&quot;Some body part&quot;, &quot;&lt;html&gt;&lt;body&gt;&lt;b&gt;TEST&lt;/b&gt; Mail&lt;/body&gt;&lt;/html&gt;&quot;, new DataSource[]{att});
 * message.setSubject(&quot;test mail java&quot;);
 * message.setRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(&quot;aho@bsiag.com&quot;, &quot;Andreas Hoegger&quot;));
 * message.setFrom(new InternetAddress(&quot;aho@bsiag.com&quot;, &quot;Andreas Hoegger&quot;));
 * BEANS.get(SmtpHelper.class).sendMessage(message, config);
 * </pre>
 *
 * @see MailHelper
 */
@ApplicationScoped
public class SmtpHelper {

  private static final Logger LOG = LoggerFactory.getLogger(SmtpHelper.class);

  /**
   * Sends the message over the provided SMTP server.
   *
   * @param config
   *     SMTP server configuration. If the {@link SmtpServerConfig#getPoolSize()} returns a value > 0, the
   *     connection pool will be used to send the message. This also means, that the calling thread is potentially
   *     blocked if no idle connection is available or the pool is at the maximum of its capacity, until a
   *     connection becomes available.
   * @param message
   *     Message to send.
   * @see MailHelper MailHelper to create a message
   */
  public void sendMessage(SmtpServerConfig config, MimeMessage message) {
    Assertions.assertNotNull(config, "SMTP server config must be set");
    Assertions.assertNotNull(message, "Message must be set");

    if (config.getPoolSize() > 0) {
      sendMessageInternal(message, (addresses) -> {
        try {
          BEANS.get(SmtpConnectionPool.class).sendMessage(config, message, addresses);
        }
        catch (MessagingException e) {
          handleMessagingException(e);
        }
      });
    }
    else {
      Session session = createSession(config);
      sendMessage(session, config.getPassword(), message);
    }
  }

  /**
   * Sends the message over the provided session.
   *
   * @param session
   *     Session to use for sending the message.
   * @param password
   *     Optional password if authentication is required.
   * @param message
   *     Message to send.
   * @see MailHelper MailHelper to create a message
   */
  public void sendMessage(Session session, String password, MimeMessage message) {
    Assertions.assertNotNull(message, "Message must be set");
    Assertions.assertNotNull(session, "Session must be set");

    sendMessageInternal(message, (addresses) -> {
      try (Transport transport = session.getTransport()) {
        connect(session, transport, password);
        transport.sendMessage(message, addresses);
      }
      catch (MessagingException e) {
        handleMessagingException(e);
      }
    });
  }

  protected void sendMessageInternal(MimeMessage message, Consumer<Address[]> messageSender) {
    Assertions.assertNotNull(message, "Message must be set");

    try {
      Address[] allRecipients = getAllRecipients(message);

      if (allRecipients == null || allRecipients.length <= 0) {
        LOG.info("No recipients found, email is not sent");
        return;
      }

      message.setSentDate(BEANS.get(IDateProvider.class).currentMillis());
      message.saveChanges();

      messageSender.accept(allRecipients);

      LOG.debug("Sent email with message id {}", BEANS.get(MailHelper.class).getMessageIdSafely(message));
    }
    catch (MessagingException e) {
      handleMessagingException(e);
    }
  }

  protected void handleMessagingException(MessagingException e) {
    throw new ProcessingException("Cannot send message.", e);
  }

  /**
   * Returns all recipients for the message. If a debug receiver is set, the debug receiver is the only recipient.
   */
  protected Address[] getAllRecipients(MimeMessage message) throws MessagingException {
    String debugReceiverEmail = CONFIG.getPropertyValue(SmtpDebugReceiverEmailProperty.class);
    if (!StringUtility.hasText(debugReceiverEmail)) {
      return message.getAllRecipients();
    }

    LOG.debug("SMTP Service: debug receiver email set to: {}", debugReceiverEmail);
    return new Address[]{BEANS.get(MailHelper.class).createInternetAddress(debugReceiverEmail)};
  }

  protected Session createSession(SmtpServerConfig config) {
    Properties props = new Properties();
    String protocol = getProtocol(config);
    String propertyBaseName = "mail." + protocol;

    props.setProperty("mail.transport.protocol", protocol);
    props.setProperty(propertyBaseName + ".quitwait", "false");

    if (StringUtility.hasText(config.getHost())) {
      props.setProperty(propertyBaseName + ".host", config.getHost());
    }
    if (config.getPort() != null && config.getPort() > 0) {
      props.setProperty(propertyBaseName + ".port", "" + config.getPort());
    }
    if (StringUtility.hasText(config.getUsername())) {
      props.setProperty(propertyBaseName + ".user", config.getUsername());
      props.setProperty(propertyBaseName + ".auth", "" + config.isUseAuthentication());

      if (config.isOAuth2()) {
        props.put(propertyBaseName + ".sasl.enable", "true");
        props.put(propertyBaseName + ".sasl.mechanisms", "XOAUTH2");
        props.put(propertyBaseName + ".auth.login.disable", "true");
        props.put(propertyBaseName + ".auth.plain.disable", "true");
      }
    }
    if (StringUtility.hasText(config.getSslProtocols())) {
      props.setProperty(propertyBaseName + ".ssl.protocols", config.getSslProtocols());
    }
    if (config.isUseStartTls()) {
      props.setProperty(propertyBaseName + ".starttls.enable", "true");
    }
    Integer connectionTimeout = CONFIG.getPropertyValue(SmtpConnectionTimeoutProperty.class);
    if (connectionTimeout != null) {
      props.setProperty(propertyBaseName + ".connectiontimeout", Integer.toString(connectionTimeout));
    }
    Integer readTimeout = CONFIG.getPropertyValue(SmtpReadTimeoutProperty.class);
    if (readTimeout != null) {
      props.setProperty(propertyBaseName + ".timeout", Integer.toString(readTimeout));
    }
    if (config.isUseUtf8()) {
      props.setProperty("mail.mime.allowutf8", "true");
    }

    if (!CollectionUtility.isEmpty(config.getAdditionalSessionProperties())) {
      props.putAll(config.getAdditionalSessionProperties());
    }
    LOG.debug("Session created with properties {}", props);

    return Session.getInstance(props, null);
  }

  protected void connect(Session session, Transport transport, String password) throws MessagingException {
    String protocol = session.getProperty("mail.transport.protocol");
    String propertyBaseName = "mail." + protocol;

    String username = session.getProperty(propertyBaseName + ".user");
    String host = session.getProperty(propertyBaseName + ".host");

    if (StringUtility.hasText(username)) {
      transport.connect(host, username, password);
    }
    else {
      transport.connect();
    }
  }

  protected String getProtocol(SmtpServerConfig config) {
    return config.isUseSmtps() ? "smtps" : "smtp";
  }

  /**
   * Return an InternetAddress from a String representation of an email address
   *
   * @param value
   *     String representation of an email address
   * @return the InternetAddress representation of 'value'
   */
  public static InternetAddress toInternetAddress(String value) {
    if (!StringUtility.hasText(value)) {
      return null;
    }

    try {
      InternetAddress internetAddress = new InternetAddress(value);
      // Use constructor to split address from personal, use constructor a second time to handle international email addresses properly,
      // see (partly) https://github.com/eclipse-ee4j/mail/issues/379
      String personal = internetAddress.getPersonal();
      internetAddress = new InternetAddress(BEANS.get(MailIDNConverter.class).toASCII(internetAddress.getAddress()));
      if (personal != null) {
        internetAddress.setPersonal(personal, StandardCharsets.UTF_8.name());
      }
      return internetAddress;
    }
    catch (AddressException | UnsupportedEncodingException e) {
      throw new PlatformException("Failed to parse value '{}' as InternetAddress", value, e);
    }
  }

  /**
   * Ensures from and reply headers. Might modify 'message'.
   */
  public static void ensureDefaultAddresses(MimeMessage message, String defaultFromEmail, String defaultReplyToEmail, boolean enforceDefaults) {
    InternetAddress defaultFromAddress = toInternetAddress(defaultFromEmail);
    InternetAddress defaultReplyToAddress = toInternetAddress(defaultReplyToEmail);

    if (enforceDefaults) {
      // If defaults are enforced, remove headers so that default are applied below.
      try {
        if (defaultFromAddress != null) {
          message.removeHeader("From");
          message.removeHeader("Sender");
        }
        if (defaultReplyToAddress != null) {
          message.removeHeader("Reply-To");
        }
      }
      catch (MessagingException e) {
        throw new PlatformException("Failed to remove header to enforce defaults", e);
      }
    }

    ensureFromAddress(message, defaultFromAddress);
    ensureReplyToAddress(message, defaultReplyToAddress);
  }

  /**
   * Similar as {@link MailHelper#ensureFromAddress(MimeMessage, String)} but working with {@link InternetAddress}
   * instead of {@link String}.
   */
  public static void ensureFromAddress(MimeMessage message, InternetAddress defaultFromAddress) {
    try {
      Address[] fromAddresses = message.getFrom();
      if (fromAddresses != null && fromAddresses.length != 0) {
        // From address is already set
        return;
      }

      if (defaultFromAddress != null) {
        message.setFrom(defaultFromAddress);
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException("Couldn't apply 'default from' to message", e);
    }
  }

  /**
   * If Reply-To header contains no addresses, default reply-to email is used instead. 'message' might be modified.
   */
  public static void ensureReplyToAddress(MimeMessage message, InternetAddress defaultReplyToAddress) {
    try {
      String[] replyToAddresses = message.getHeader("Reply-To"); // use get header because when getReplyTo is used, getFrom is an internal fallback, thus always resulting in a value.
      if (replyToAddresses != null && replyToAddresses.length != 0) {
        // Reply-to address is already set
        return;
      }

      if (defaultReplyToAddress != null) {
        message.setReplyTo(new InternetAddress[]{defaultReplyToAddress});
      }
    }
    catch (MessagingException e) {
      throw new ProcessingException("Couldn't apply 'default reply-to' to message", e);
    }
  }

  public static class SmtpDebugReceiverEmailProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.smtp.debugReceiverEmail";
    }

    @Override
    public String description() {
      return "If specified all emails are sent to this address instead of the real one. This may be useful during development to not send emails to real users by accident.";
    }
  }

  public static class SmtpConnectionTimeoutProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 60000;
    }

    @Override
    public String getKey() {
      return "scout.smtp.connectionTimeout";
    }

    @Override
    public String description() {
      return "Socket connection timeout value in milliseconds.";
    }
  }

  public static class SmtpReadTimeoutProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 60000;
    }

    @Override
    public String getKey() {
      return "scout.smtp.readTimeout";
    }

    @Override
    public String description() {
      return "Socket read timeout value in milliseconds.";
    }
  }
}
