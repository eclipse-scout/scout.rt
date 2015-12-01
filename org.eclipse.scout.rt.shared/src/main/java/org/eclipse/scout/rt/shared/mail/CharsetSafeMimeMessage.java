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
package org.eclipse.scout.rt.shared.mail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class CharsetSafeMimeMessage extends MimeMessage {
  private String m_charset;

  public CharsetSafeMimeMessage() {
    super((Session) null);
    m_charset = StandardCharsets.UTF_8.name();
  }

  public CharsetSafeMimeMessage(String charset) {
    super((Session) null);
    m_charset = charset;
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
      if (inet.getPersonal() != null && inet.getPersonal().length() > 0) {
        try {
          inet.setPersonal(MimeUtility.encodeText(inet.getPersonal(), m_charset, "Q"));
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
