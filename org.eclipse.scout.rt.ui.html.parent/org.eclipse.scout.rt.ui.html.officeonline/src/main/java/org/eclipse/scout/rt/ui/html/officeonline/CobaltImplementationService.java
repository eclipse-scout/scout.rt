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
package org.eclipse.scout.rt.ui.html.officeonline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.officeonline.internal.Activator;
import org.eclipse.scout.rt.ui.html.officeonline.wopi.FileInfo;
import org.eclipse.scout.rt.ui.html.officeonline.wopi.IWopiContentProvider;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.ServiceRegistration;

/**
 * Default implementation of a {@link IWopiContentProvider} service that supports cobalt by using a BsiWopiService .Net
 * assembly.
 * <p>
 * Requires the config.ini parameters {@link #CONFIG_INI_COBALT_SERVICE_WOPI_URL} and
 * {@link #CONFIG_INI_COBALT_SERVICE_FEED_URL}
 */
@Priority(0)
public class CobaltImplementationService extends AbstractService implements IWopiContentProvider {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CobaltImplementationService.class);

  /**
   * the BsiWopiService url as it is seen by the office web apps server<br/>
   * for example http://localhost:8080/wopi/files
   */
  public static final String CONFIG_INI_COBALT_SERVICE_WOPI_URL = "cobalt.service.wopi.url";

  /**
   * the url to access the BsiWopiService from outside in order to feed and retrieve document content<br/>
   * for example https://office-web-apps-server:8080/wopi/files
   */
  public static final String CONFIG_INI_COBALT_SERVICE_FEED_URL = "cobalt.service.feed.url";

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
  }

  @Override
  public String getWopiBaseUrl() {
    String cobaltServiceWopiUrl = Activator.getBundleContext().getProperty(CONFIG_INI_COBALT_SERVICE_WOPI_URL);
    if (cobaltServiceWopiUrl == null) {
      LOG.warn("Missing config.ini parameter " + CONFIG_INI_COBALT_SERVICE_WOPI_URL + "; COBALT will not be available");
    }
    return cobaltServiceWopiUrl;
  }

  protected String getFeedUrl() {
    String cobaltServiceFeedUrl = Activator.getBundleContext().getProperty(CONFIG_INI_COBALT_SERVICE_FEED_URL);
    if (cobaltServiceFeedUrl == null) {
      LOG.warn("Missing config.ini parameter " + CONFIG_INI_COBALT_SERVICE_FEED_URL + "; COBALT will not be available");
    }
    return cobaltServiceFeedUrl;
  }

  @Override
  public FileInfo getFileInfo(String fileId) throws IOException {
    //not implemented since this provider delegates to the BsiWopiService .Net assembly
    return null;
  }

  @Override
  public void setFileContent(String fileId, byte[] content) throws IOException {
    String feedUrl = getFeedUrl();
    if (feedUrl == null) {
      throw new IOException("Missing config.ini parameter " + CONFIG_INI_COBALT_SERVICE_WOPI_URL + "; COBALT will not be available");
    }
    try {
      URL url = new URL(feedUrl + "/" + fileId + "/contents");
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true);
      OutputStream out = conn.getOutputStream();
      out.write(content);
      out.close();
      conn.getInputStream();
    }
    catch (IOException io) {
      throw new IOException("fileId=" + fileId, io);
    }
  }

  @Override
  public byte[] getFileContent(String fileId) throws IOException {
    return getFileContent(fileId, false);
  }

  public byte[] getFileContent(String fileId, boolean removeRemoteDocument) throws IOException {
    String feedUrl = getFeedUrl();
    if (feedUrl == null) {
      throw new IOException("Missing config.ini parameter " + CONFIG_INI_COBALT_SERVICE_WOPI_URL + "; COBALT will not be available");
    }
    try {
      URL url = new URL(feedUrl + "/" + fileId + "/contents");
      URLConnection conn = url.openConnection();
      if (removeRemoteDocument) {
        conn.setRequestProperty("X-BSI-Hint", "Delete");
      }
      InputStream in = conn.getInputStream();
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[10240];
        int len;
        while ((len = in.read(b)) > 0) {
          out.write(b, 0, len);
        }
        out.close();
        return out.toByteArray();
      }
      finally {
        in.close();
      }
    }
    catch (IOException io) {
      throw new IOException("fileId=" + fileId, io);
    }
  }

}
