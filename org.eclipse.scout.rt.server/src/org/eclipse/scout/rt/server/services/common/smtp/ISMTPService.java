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

import org.eclipse.scout.commons.MailUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

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
 * @see MailUtility
 */
@SuppressWarnings("restriction")
public interface ISMTPService extends IService {

  /**
   * sends the message over the configured session. Ensure to have set receiver
   * email addresses, sender email addresses, subject and body parts.
   * 
   * @param message
   * @throws ProcessingException
   * @see {@link MailUtility} to create a message
   */
  void sendMessage(MimeMessage message) throws ProcessingException;

  /**
   * sends the message over the session given as an argument. Ensure to have set
   * receiver email addresses, sender email addresses, subject and body parts.
   * 
   * @param session
   *          to send the message.
   * @throws ProcessingException
   */
  void sendMessage(MimeMessage message, Session session) throws ProcessingException;

  String getHost();

  void setHost(String host);

  int getPort();

  void setPort(int port);

  boolean isUseAuthentication();

  void setUseAuthentication(boolean useAuthentication);

  String getUsername();

  void setUsername(String username);

  String getPassword();

  void setPassword(String password);

  /**
   * The prefix will be added to every emails subject sent with the smtp
   * service. <br>
   * subject = prefix+message.subject
   * 
   * @param prefix
   */
  void setSubjectPrefix(String prefix);

  String getSubjectPrefix();

  String getDefaultFromEmail();

  void setDefaultFromEmail(String defaultFromEmail);

  /**
   * This email is used when no sender address is set on the message to send.
   * 
   * @return
   */
  String getDebugReceiverEmail();

  /**
   * @return Returns <code>true</code> if the communication between this
   *         service and the SMTP host is encrypted (i.e. SMTPS).
   */
  boolean isUseSmtps();

  /**
   * Controls whether the communication between this service and the SMTP
   * host is encrypted (i.e. is using SMTPS)
   * 
   * @param useSmtps
   */
  void setUseSmtps(boolean useSmtps);

  /**
   * @return Returns a comma-separated list of SSL protocols used for
   *         establishing the connection to the SMTP host.
   */
  String getSslProtocols();

  /**
   * Sets the list of supported SSL protocols used for establishing a
   * connection to the SMTP host.
   * 
   * @param sslProtocols
   *          comma-separated list of protocol names.
   */
  void setSslProtocols(String sslProtocols);

}
