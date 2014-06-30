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
package org.eclipse.scout.rt.client.officeonline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.client.officeonline.IOfficeWebAppsService;
import org.eclipse.scout.rt.client.officeonline.internal.Activator;
import org.eclipse.scout.rt.client.officeonline.wopi.FileInfo;
import org.eclipse.scout.rt.client.officeonline.wopi.IWopiContentProvider;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.ServiceRegistration;

/**
 * The Office Web Apps Server must be able to callback. This url is set in the iframe urls of
 * {@link IOfficeWebAppsService#createIFrameUrl(org.eclipse.scout.rt.client.officeonline.IOfficeWebAppsService.Zone, org.eclipse.scout.rt.client.officeonline.IOfficeWebAppsService.App, org.eclipse.scout.rt.client.officeonline.IOfficeWebAppsService.Action, String, String)}
 */
@Priority(-10)
public class TestingWopiContentProvider extends AbstractService implements IWopiContentProvider {
  private File m_fileRoot;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    String fileRootText = Activator.getBundleContext().getProperty("wopi.test.file.root");
    if (fileRootText != null && new File(fileRootText).exists()) {
      m_fileRoot = new File(fileRootText);
    }
  }

  @Override
  public FileInfo getFileInfo(String fileId) {
    if (m_fileRoot == null) {
      return null;
    }
    File f = new File(m_fileRoot, fileId);
    FileInfo fi = new FileInfo();
    fi.setFileId(fileId);
    fi.setExists(f.exists());
    fi.setLastModified(f.lastModified());
    fi.setLength(f.length());
    return fi;
  }

  @Override
  public byte[] getFileContent(String fileId) throws IOException {
    if (m_fileRoot == null) {
      throw new IOException("unavailable");
    }
    File f = new File(m_fileRoot, fileId);
    byte[] stream = new byte[(int) f.length()];
    FileInputStream in = new FileInputStream(f);
    try {
      int pos = 0;
      while (pos < stream.length) {
        int n = in.read(stream, pos, stream.length - pos);
        pos += n;
      }
    }
    finally {
      in.close();
    }
    return stream;
  }

  @Override
  public void setFileContent(String fileId, byte[] content) throws IOException {
    if (m_fileRoot == null) {
      throw new IOException("unavailable");
    }
    /*
    File f = new File(m_fileRoot, fileId);
    FileOutputStream out = new FileOutputStream(f);
    try {
      out.write(content);
    }
    finally {
      out.close();
    }
    */
  }

}
