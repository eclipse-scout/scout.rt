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
package org.eclipse.scout.rt.ui.rap.window;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.rwt.internal.widgets.JSExecutor;
import org.eclipse.rwt.widgets.ExternalBrowser;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.rap.AbstractRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.window.filedownloader.RwtScoutDownloadHandler;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.8.0
 */
@SuppressWarnings("restriction")
public class BrowserWindowHandler {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtEnvironment.class);

  public void openLink(String link) {
    if (link == null) {
      return;
    }

    if (isEmailLink(link)) {
      openLinkInSameBrowserWindow(link);
    }
    else if (isTelLink(link)) {
      openLinkInSameBrowserWindow(link);
    }
    else if (isHttpLink(link)) {
      openLinkInNewBrowserWindow(link);
    }
    else {
      downloadFile(link);
    }
  }

  public boolean isEmailLink(String link) {
    if (link != null && link.startsWith("mailto:")) {
      return true;
    }

    return false;
  }

  public boolean isTelLink(String link) {
    if (link != null && link.startsWith("tel:")) {
      return true;
    }

    return false;
  }

  public boolean isHttpLink(String link) {
    if ((StringUtility.find(link, "http://") >= 0) || (StringUtility.find(link, "https://") >= 0)) {
      return true;
    }

    return false;
  }

  public void openLinkInNewBrowserWindow(String link) {
    if (!isHttpLink(link)) {
      return;
    }

    String nextId = UUID.randomUUID().toString();
    ExternalBrowser.open(nextId, link, ExternalBrowser.STATUS | ExternalBrowser.LOCATION_BAR | ExternalBrowser.NAVIGATION_BAR);
  }

  public void openLinkInSameBrowserWindow(String link) {
    if (link == null) {
      return;
    }

    JSExecutor.executeJS("window.location='" + link + "'");
  }

  public void downloadFile(String link) {
    try {
      File file = validatelink(link);
      String nextId = UUID.randomUUID().toString();
      final RwtScoutDownloadHandler handler = new RwtScoutDownloadHandler(nextId, file, "", file.getName());
      //do not use an existing shell since this one might disappear before the download completed...
      Shell parentShell = new Shell();
      parentShell.addDisposeListener(new DisposeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetDisposed(DisposeEvent event) {
          handler.dispose();
        }
      });
      handler.startDownload(parentShell);
    }
    catch (IOException e) {
      LOG.error("Unexpected: " + link, e);
    }
  }

  public File validatelink(String link) throws IOException {
    String px = link.replace('\\', File.separatorChar);
    File file = new File(px);
    if (file.exists()) {
      px = file.getCanonicalPath();
      String osName = System.getProperty("os.name");
      if (osName != null && osName.startsWith("Mac OS")) {
        //mac is not able to open files with a space, even when in quotes
        String ext = px.substring(px.lastIndexOf('.'));
        File f = new File(file.getParentFile(), "" + System.nanoTime() + ext);
        file.renameTo(f);
        f.deleteOnExit();
      }
    }
    return file;
  }
}
