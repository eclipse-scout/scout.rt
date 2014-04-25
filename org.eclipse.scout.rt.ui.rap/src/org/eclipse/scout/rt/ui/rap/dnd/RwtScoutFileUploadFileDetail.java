/*******************************************************************************
 * Copyright (c) 2010, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.dnd;

/**
 * More detailed information about the uploaded files. Belongs to {@link RwtScoutFileUploadEvent}
 * 
 * @since 4.0.0-M7
 */
public class RwtScoutFileUploadFileDetail {
  private final String m_contentType;
  private final long m_contentLength;
  private final String m_fileName;

  public RwtScoutFileUploadFileDetail(String fileName, String contentType, long contentLength) {
    m_fileName = fileName;
    m_contentType = contentType;
    m_contentLength = contentLength;
  }

  public String getContentType() {
    return m_contentType;
  }

  public long getContentLength() {
    return m_contentLength;
  }

  public String getFileName() {
    return m_fileName;
  }

}
