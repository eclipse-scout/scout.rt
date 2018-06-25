/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.mail;

import javax.activation.DataSource;

import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * Class representing a mail attachment.
 *
 * @deprecated Use {@link org.eclipse.scout.rt.mail.MailAttachment} instead.
 */
@Deprecated
public class MailAttachment extends org.eclipse.scout.rt.mail.MailAttachment {

  public MailAttachment(DataSource dataSource) {
    super(dataSource);
  }

  public MailAttachment(DataSource dataSource, String contentType, String name, String contentId) {
    super(dataSource, contentType, name, contentId);
  }

  public MailAttachment(BinaryResource binaryResource) {
    super(binaryResource);
  }

  public MailAttachment(BinaryResource binaryResource, String contentId) {
    super(binaryResource, contentId);
  }

  public MailAttachment(org.eclipse.scout.rt.mail.MailAttachment mailAttachment) {
    super(mailAttachment.getDataSource(), mailAttachment.getContentType(), mailAttachment.getName(), mailAttachment.getContentId(), mailAttachment.getContentInternal());
  }
}
