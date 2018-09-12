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
package org.eclipse.scout.rt.shared.mail;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Utility class handles MimeMessages and MailMessages. A MimeMessage is a physical representation of an mail. It
 * represents most likely a 'rfc822' message. A MailMessage represents the logical representation of an mail.
 *
 * <pre>
 * Example:
 * Representation of email address 'thomas@müller.de':
 *    MailMessage: 'thomas@müller.de'
 *    MimeMessage: 'xn--thomas@mller-klb.de'
 * </pre>
 *
 * @since 7.0
 * @deprecated Use {@link org.eclipse.scout.rt.mail.MailHelper} instead.
 */
@Deprecated
@ApplicationScoped
public class MailHelper extends org.eclipse.scout.rt.mail.MailHelper {

  public static final String CONTENT_TYPE_ID = org.eclipse.scout.rt.mail.MailHelper.CONTENT_TYPE_ID;
  public static final String CONTENT_ID_ID = org.eclipse.scout.rt.mail.MailHelper.CONTENT_ID_ID;

  public static final String CONTENT_TRANSFER_ENCODING_ID = org.eclipse.scout.rt.mail.MailHelper.CONTENT_TRANSFER_ENCODING_ID;
  public static final String QUOTED_PRINTABLE = org.eclipse.scout.rt.mail.MailHelper.QUOTED_PRINTABLE;

  public static final String CONTENT_TYPE_TEXT_HTML = org.eclipse.scout.rt.mail.MailHelper.CONTENT_TYPE_TEXT_HTML;
  public static final String CONTENT_TYPE_TEXT_PLAIN = org.eclipse.scout.rt.mail.MailHelper.CONTENT_TYPE_TEXT_PLAIN;
  public static final String CONTENT_TYPE_MESSAGE_RFC822 = org.eclipse.scout.rt.mail.MailHelper.CONTENT_TYPE_MESSAGE_RFC822;
  public static final String CONTENT_TYPE_IMAGE_PREFIX = org.eclipse.scout.rt.mail.MailHelper.CONTENT_TYPE_IMAGE_PREFIX;
  public static final String CONTENT_TYPE_MULTIPART_PREFIX = org.eclipse.scout.rt.mail.MailHelper.CONTENT_TYPE_MULTIPART_PREFIX;
}
