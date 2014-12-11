/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.mail;

import javax.activation.DataSource;

/**
 * Class representing a mail attachment.
 */
public class MailAttachment {
  private final DataSource m_dataSource;
  private String m_contentId;

  public MailAttachment(DataSource dataSource) {
    this(dataSource, null);
  }

  public MailAttachment(DataSource dataSource, String contentId) {
    super();
    if (dataSource == null) {
      throw new IllegalArgumentException("DataSource is missing");
    }
    m_dataSource = dataSource;
    m_contentId = contentId;
  }

  public DataSource getDataSource() {
    return m_dataSource;
  }

  public String getContentId() {
    return m_contentId;
  }

  public void setContentId(String contentId) {
    m_contentId = contentId;
  }
}
