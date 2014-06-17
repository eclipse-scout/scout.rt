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
package org.eclipse.scout.rt.spec.client.gen.extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.config.ConfigRegistry;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;
import org.eclipse.scout.rt.spec.client.utility.SpecIOUtility;

/**
 * Extractor for icons of {@link IPageWithTable}. The {@link TablePageIconExtractor#getText(IPageWithTable)} copies the
 * icon to {@link SpecFileConfig#getImageDir()}/icons and returns a mediawiki link to the copied icon file.
 */
public class TablePageIconExtractor extends AbstractNamedTextExtractor<IPageWithTable<? extends ITable>> {
  private static final String ICONS_SUBDIR = "icons";
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TablePageIconExtractor.class);

  /**
   * @param name
   */
  public TablePageIconExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.icon"));
  }

  @Override
  public String getText(IPageWithTable<? extends ITable> page) {
    String iconId = page.getCell().getIconId();

    File file = copyIcon(iconId);
    if (file != null) {
      File baseDir;
      try {
        baseDir = ConfigRegistry.getSpecFileConfigInstance().getSpecDir();
      }
      catch (ProcessingException e) {
        LOG.error("Could not get configuration for mediawiki-dir.");
        return "";
      }
      return MediawikiUtility.createImageLink("../" + SpecIOUtility.getReleativePath(baseDir, file));
    }
    else {
      return "";
    }

  }

  protected File copyIcon(String iconId) {
    FileOutputStream outputStream = null;
    IClientSession clientSession = ClientJob.getCurrentSession();
    IconSpec iconSpec = clientSession.getIconLocator().getIconSpec(iconId);
    File icon = null;
    if (iconSpec != null) {
      String fileName = iconSpec.getName();
      try {
        File dir = new File(ConfigRegistry.getSpecFileConfigInstance().getImageDir(), ICONS_SUBDIR);
        dir.mkdirs();
        icon = new File(dir, fileName);
        if (!icon.exists()) {
          outputStream = new FileOutputStream(icon);
          outputStream.write(iconSpec.getContent());
        }
      }
      catch (ProcessingException e) {
        logException(iconId, e);
      }
      catch (FileNotFoundException e) {
        logException(iconId, e);
      }
      catch (IOException e) {
        logException(iconId, e);
      }
      finally {
        if (outputStream != null) {
          try {
            outputStream.close();
          }
          catch (IOException e) {
            LOG.error("Could not close outputstream.", e);
          }
        }
      }
    }
    return icon;
  }

  protected void logException(String iconId, Exception e) {
    LOG.error("Could not create icon image for " + iconId, e);
  }

}
