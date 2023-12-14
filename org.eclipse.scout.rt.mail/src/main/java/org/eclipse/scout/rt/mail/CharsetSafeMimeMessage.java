/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class CharsetSafeMimeMessage extends MimeMessage {

  private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("<.+@.+>");

  private final String m_charset;
  private String m_customMessageId;

  public CharsetSafeMimeMessage() {
    super((Session) null);
    m_charset = StandardCharsets.UTF_8.name();
  }

  public CharsetSafeMimeMessage(String charset) {
    super((Session) null);
    m_charset = charset;
  }

  public String getCharset() {
    return m_charset;
  }

  public String getCustomMessageId() {
    return m_customMessageId;
  }

  /**
   * Sets a custom message id. The message ID must be in an appropriate format, see
   * https://tools.ietf.org/html/rfc5322#section-3.6.4. If no custom message id is set, the default implementation from
   * {@link MimeMessage} is used.
   * <p>
   * The message id must be a unique identifier, thus always create a new custom message id for a new mime message.
   * <p>
   * A call to {@link MimeMessage#saveChanges()} is required in order to update the message ID.
   */
  public void setCustomMessageId(String customMessageId) {
    if (customMessageId != null && !MESSAGE_ID_PATTERN.matcher(customMessageId).matches()) {
      throw new IllegalArgumentException("Provided custom message id doesn't match required format: " + customMessageId);
    }
    m_customMessageId = customMessageId;
  }

  @Override
  protected void updateMessageID() throws MessagingException {
    if (getCustomMessageId() == null) {
      super.updateMessageID();
    }
    else {
      setHeader("Message-ID", getCustomMessageId());
    }
  }

  @Override
  public void setFrom(Address address) throws MessagingException {
    super.setFrom(encodeAddress(address));
  }

  @Override
  public void addFrom(Address[] addresses) throws MessagingException {
    super.addFrom(encodeAddresses(addresses));
  }

  @Override
  public void addRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
    super.addRecipients(type, encodeAddresses(addresses));
  }

  @Override
  public void addRecipients(Message.RecipientType type, String addresses) throws MessagingException {
    if (type == RecipientType.NEWSGROUPS) {
      super.addRecipients(type, addresses);
    }
    else {
      addRecipients(type, InternetAddress.parse(addresses));
    }
  }

  @Override
  public void setDescription(String description) throws MessagingException {
    super.setDescription(description, m_charset);
  }

  @Override
  public void setFrom() throws MessagingException {
    InternetAddress me = InternetAddress.getLocalAddress(session);
    if (me != null) {
      setFrom(encodeAddress(me));
    }
    else {
      throw new MessagingException("No From address");
    }
  }

  @Override
  public void setRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
    super.setRecipients(type, encodeAddresses(addresses));
  }

  @Override
  public void setRecipients(Message.RecipientType type, String addresses) throws MessagingException {
    if (type == RecipientType.NEWSGROUPS) {
      super.setRecipients(type, addresses);
    }
    else {
      setRecipients(type, InternetAddress.parse(addresses));
    }
  }

  @Override
  public void setReplyTo(Address[] addresses) throws MessagingException {
    super.setReplyTo(encodeAddresses(addresses));
  }

  @Override
  public void setSender(Address address) throws MessagingException {
    super.setSender(encodeAddress(address));
  }

  @Override
  public void setSubject(String subject) throws MessagingException {
    super.setSubject(subject, m_charset);
  }

  @Override
  public void setText(String text) throws MessagingException {
    super.setText(text, m_charset);
  }

  private Address encodeAddress(Address address) throws MessagingException {
    if (address instanceof InternetAddress) {
      InternetAddress inet = (InternetAddress) address;
      if (inet.getPersonal() != null && !inet.getPersonal().isEmpty()) {
        try {
          inet.setPersonal(inet.getPersonal(), m_charset);
        }
        catch (UnsupportedEncodingException e) {
          throw new MessagingException("Unable to encode from address", e);
        }
      }
      return inet;
    }
    return address;
  }

  private Address[] encodeAddresses(Address[] addresses) throws MessagingException {
    if (addresses != null && addresses.length > 0) {
      for (Address a : addresses) {
        encodeAddress(a);
      }
    }
    return addresses;
  }
}
