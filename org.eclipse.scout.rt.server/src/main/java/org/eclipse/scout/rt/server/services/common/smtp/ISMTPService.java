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
package org.eclipse.scout.rt.server.services.common.smtp;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.mail.MailUtility;

/**
 * <h3>ISMTPService</h3> The service to send messages over the smtp protocol. <br>
 * <p>
 * Usage Example:
 *
 * <pre>
 * DataSource att = MailUtility.createDataSource(new File(&quot;D:\\pictures\\backgroundCorsaire.jpg&quot;));
 * MimeMessage message = MailUtility.createMimeMessage(&quot;Some body part&quot;, &quot;&lt;html&gt;&lt;body&gt;&lt;b&gt;TEST&lt;/b&gt; Mail&lt;/body&gt;&lt;/html&gt;&quot;, new DataSource[]{att});
 * message.setSubject(&quot;test mail java&quot;);
 * message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(&quot;aho@bsiag.com&quot;, &quot;Andreas Hoegger&quot;));
 * message.setFrom(new InternetAddress(&quot;aho@bsiag.com&quot;, &quot;Andreas Hoegger&quot;));
 * service.sendMessage(message);
 * </pre>
 *
 * @see org.eclipse.scout.rt.shared.mail.MailUtility
 */
public interface ISMTPService extends IService {

  /**
   * sends the message over the configured session. Ensure to have set receiver email addresses, sender email addresses,
   * subject and body parts.
   *
   * @param message
   * @see {@link MailUtility} to create a message
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
