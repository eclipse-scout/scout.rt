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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;
import org.osgi.framework.Bundle;

// TODO ASA javadoc
public class ManualSpecTest extends AbstractSpecGen {

  @Test
  public void copy() throws ProcessingException {
    copyMediawikiFiles();
    copyImages();
  }

  protected void copyImages() throws ProcessingException {
    File dest = getFileConfig().getImageDir();
    dest.mkdirs();
    for (Bundle bundle : getFileConfig().getSourceBundles()) {
      for (String file : SpecIOUtility.listFiles(bundle, getFileConfig().getRelativeImagesSourceDirPath(), getFilter())) {
        File destFile = new File(dest, file);
        SpecIOUtility.copyFile(bundle, getFileConfig().getRelativeImagesSourceDirPath() + File.separator + file, destFile);
      }
    }
  }

  protected void copyMediawikiFiles() throws ProcessingException {
    File dest = getFileConfig().getMediawikiDir();
    dest.mkdirs();
    for (Bundle bundle : getFileConfig().getSourceBundles()) {
      for (String file : SpecIOUtility.listFiles(bundle, getFileConfig().getRelativeMediawikiSourceDirPath(), getFilter())) {
        File destFile = new File(dest, file);
        SpecIOUtility.copyFile(bundle, getFileConfig().getRelativeMediawikiSourceDirPath() + File.separator + file, destFile);
      }
    }
  }

  /**
   * When overriding, make sure the returned {@link FilenameFilter} does not depend on the <code>dir</code> parameter in
   * {@link FilenameFilter#accept(File dir, String name)} as this will be null in case of binary bundles.
   */
  protected FilenameFilter getFilter() {
    return new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        return !name.startsWith(".");
      }
    };
  }
}
