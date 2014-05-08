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
import org.eclipse.scout.rt.spec.client.config.ConfigRegistry;
import org.eclipse.scout.rt.spec.client.utility.SpecIOUtility;

/**
 * Copy manually written mediawiki and image files from source bundles to output directory
 */
public class ManualSpecTest extends AbstractSpecGenTest {

  @Override
  public void generateSpec() throws ProcessingException {
    copyMediawikiFiles();
    copyImages();
  }

  protected void copyImages() throws ProcessingException {
    File dest = ConfigRegistry.getSpecFileConfigInstance().getImageDir();
    dest.mkdirs();
    String bundleRelativeSourceDirPath = ConfigRegistry.getSpecFileConfigInstance().getRelativeImagesSourceDirPath();
    FilenameFilter filenameFilter = getFilter();
    SpecIOUtility.copyFilesFromAllSourceBundles(dest, bundleRelativeSourceDirPath, filenameFilter);
  }

  protected void copyMediawikiFiles() throws ProcessingException {
    File dest = ConfigRegistry.getSpecFileConfigInstance().getMediawikiDir();
    dest.mkdirs();
    String bundleRelativeSourceDirPath = ConfigRegistry.getSpecFileConfigInstance().getRelativeMediawikiSourceDirPath();
    FilenameFilter filenameFilter = getFilter();
    SpecIOUtility.copyFilesFromAllSourceBundles(dest, bundleRelativeSourceDirPath, filenameFilter);
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
