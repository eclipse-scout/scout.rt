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
package org.eclipse.scout.rt.ui.swing.form.fields.mailfield;

import java.io.File;
import java.io.FileOutputStream;

import javax.mail.MessagingException;
import javax.mail.Part;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

@SuppressWarnings("restriction")
public class SwingMailAttachment {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingMailAttachment.class);
  private Part m_part;
  private File m_file;

  public SwingMailAttachment(Part part, File tempFolder) {
    m_part = part;
    try {
      String filename = m_part.getFileName();
      if (filename != null) {
        filename = filename.replaceAll("[/\\\\]", "_");
      }
      else {
        // try to create a usable filename
        String extension = ".txt";
        if (m_part.getContentType().equals("message/rfc822")) {
          extension = ".eml";
        }
        filename = IOUtility.getTempFileName(extension);
        filename = filename.substring(filename.lastIndexOf('\\') + 1, filename.length());
      }
      m_file = new File(tempFolder, filename);
      IOUtility.writeContent(new FileOutputStream(m_file), IOUtility.getContent(m_part.getInputStream()));
      m_file.deleteOnExit();
    }
    catch (Exception e) {
      LOG.error("could not create temp file of attachement.", e);
    }
  }

  public String getContentId() {
    try {
      String[] ids = m_part.getHeader("Content-ID");
      if (ids != null && ids.length > 0) {
        String cid = ids[0];
        if (cid.matches("<.*>")) {
          cid = cid.substring(1, cid.length() - 1);
        }
        return cid;
      }
    }
    catch (MessagingException e) {
      // nop
    }
    return null;
  }

  public String getContentType() {
    try {
      return m_part.getContentType();
    }
    catch (MessagingException e) {
      // nop
    }
    return null;
  }

  public Part getPart() {
    return m_part;
  }

  public File getFile() {
    return m_file;
  }
}
