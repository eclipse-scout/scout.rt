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
package org.eclipse.scout.rt.spec.client;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiAnchorCollector;
import org.osgi.framework.Bundle;

/**
 *
 */
public abstract class AbstractManualSpec extends AbstractSpecGen {

  public AbstractManualSpec() {
    super(Platform.getProduct().getDefiningBundle().getSymbolicName());
  }

  protected void copyImages() throws ProcessingException {
    File source = getFileConfig().getImageInDir();
    File dest = getFileConfig().getImageDir();
    SpecIOUtility.copyAll(source, dest, getFilter());
  }

  protected void copyMediawikiFiles() throws ProcessingException {
    File dest = getFileConfig().getMediawikiDir();
    dest.mkdirs();
    for (Bundle bundle : getFileConfig().getSourceBundles()) {
      File source = getFileConfig().getMediawikiInDir(bundle);
      for (File file : source.listFiles(getFilter())) {
        File destFile = new File(dest, file.getName());
        SpecIOUtility.copy(file, destFile);
        convertToHTML(destFile);
        new MediawikiAnchorCollector(destFile).storeAnchors(getFileConfig().getLinksFile());
      }
    }
  }

  protected FilenameFilter getFilter() {
    return new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return !name.startsWith(".");
      }
    };
  }
}
