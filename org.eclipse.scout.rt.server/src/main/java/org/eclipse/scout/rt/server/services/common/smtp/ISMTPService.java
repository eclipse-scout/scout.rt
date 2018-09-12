/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.smtp;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.mail.MailHelper;
import org.eclipse.scout.rt.mail.smtp.SmtpHelper;
import org.eclipse.scout.rt.mail.smtp.SmtpServerConfig;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * @deprecated Use {@link SmtpHelper} instead. Map properties to {@link SmtpServerConfig}. If a subject prefix is
 *             required, the prefix must be prepended before calling
 *             {@link SmtpHelper#sendMessage(SmtpServerConfig, MimeMessage)}.
 */
@Deprecated
public interface ISMTPService extends IService {

  /**
   * sends the message over the configured session. Ensure to have set receiver email addresses, sender email addresses,
   * subject and body parts.
   *
   * @param message
   * @see {@link MailHelper} to create a message
   */
  void sendMessage(MimeMessage message);

  /**
   * sends the message over the session given as an argument. Ensure to have set receiver email addresses, sender email
   * addresses, subject and body parts.
   *
   * @param session
   *          to send the message.
   */
  void sendMessage(MimeMessage message, Session session);

  String getHost();

  int getPort();

  boolean isUseAuthentication();

  String getUsername();

  String getPassword();

  String getSubjectPrefix();

  String getDefaultFromEmail();

  /**
   * This email is used when no sender address is set on the message to send.
   *
   * @return
   */
  String getDebugReceiverEmail();

  /**
   * @return Returns <code>true</code> if the communication between this service and the SMTP host is encrypted (i.e.
   *         SMTPS).
   */
  boolean isUseSmtps();

  /**
   * @return Returns a comma-separated list of SSL protocols used for establishing the connection to the SMTP host.
   */
  String getSslProtocols();

}
