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

import java.util.EventObject;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;

/**
 * The event sent from the {@link IRwtScoutFileUploadHandler}. Contains information about the
 * file upload operation.
 * 
 * @since 4.0.0-M7
 */
public class RwtScoutFileUploadEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final long m_contentLength;
  private final long m_bytesRead;
  private final Exception m_exception;
  private final List<RwtScoutFileUploadFileDetail> m_fileDetails;

  public RwtScoutFileUploadEvent(IRwtScoutFileUploadHandler source, long contentLength, long bytesRead, Exception exception, List<RwtScoutFileUploadFileDetail> fileDetails) {
    super(source);
    m_contentLength = contentLength;
    m_bytesRead = bytesRead;
    m_exception = exception;
    m_fileDetails = fileDetails;
  }

  public long getContentLength() {
    return m_contentLength;
  }

  public long getBytesRead() {
    return m_bytesRead;
  }

  public List<RwtScoutFileUploadFileDetail> getListFileDetails() {
    return CollectionUtility.arrayList(m_fileDetails);
  }

}
