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
package org.eclipse.scout.rt.spec.client.config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.osgi.framework.Bundle;

/**
 * Configuration of input and output files
 */
public class SpecFileConfig {
  private static final String SPEC_OUT_DIR_NAME = "target" + File.separator + "spec";
  private static final String SPEC_IN_DIR_NAME = "resources" + File.separator + "spec";

  private static final String IMAGES_DIR_NAME = "images";
  private static final String MEDIAWIKI_DIR_NAME = "mediawiki";
  private static final String LINKS_FILE_NAME = "links.properties";

  private final String m_pluginName;

  public SpecFileConfig(String pluginName) {
    m_pluginName = pluginName;
  }

  /**
   * @return root of the bundle output
   * @throws ProcessingException
   */
  private File getBundleRoot() throws ProcessingException {
    URI uri;
    try {
      URL bundleRoot = getBundle().getEntry("/");
      uri = FileLocator.resolve(bundleRoot).toURI();
      return new File(uri);
    }
    catch (URISyntaxException e) {
      throw new ProcessingException("Bundle Root File not found", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Bundle Root File not found", e);
    }
  }

  public Bundle getBundle() {
    return Platform.getBundle(m_pluginName);
  }

  /**
   * @return root directory for the generated output
   * @throws ProcessingException
   */
  public File getSpecDir() throws ProcessingException {
    return new File(getBundleRoot(), SPEC_OUT_DIR_NAME);
  }

  /**
   * Location of referenced images
   * 
   * @return image directory
   * @throws ProcessingException
   */
  public File getImageDir() throws ProcessingException {
    return new File(getSpecDir(), IMAGES_DIR_NAME);
  }

  /**
   * Location of referenced images
   * 
   * @return image directory
   * @throws ProcessingException
   */
  public File getImageInDir() throws ProcessingException {
    return new File(getSpecInDir(), IMAGES_DIR_NAME);
  }

  /**
   * @return Html output
   * @throws ProcessingException
   */
  public File getHtmlDir() throws ProcessingException {
    return new File(getSpecDir(), "html");
  }

  /**
   * @return mediawiki output
   * @throws ProcessingException
   */
  public File getMediawikiDir() throws ProcessingException {
    return new File(getSpecDir(), MEDIAWIKI_DIR_NAME);
  }

  /**
   * @return mediawiki raw output (without postprocessing)
   * @throws ProcessingException
   */
  public File getMediawikiRawDir() throws ProcessingException {
    return new File(getSpecDir(), "mediawiki_raw");
  }

  /**
   * @return location of manually written input files, root directory
   * @throws ProcessingException
   */
  public File getSpecInDir() throws ProcessingException {
    return new File(getBundleRoot(), SPEC_IN_DIR_NAME);
  }

  /**
   * @return mediawiki output
   * @throws ProcessingException
   */
  public File getMediawikiInDir() throws ProcessingException {
    return new File(getSpecInDir(), MEDIAWIKI_DIR_NAME);
  }

  public File getLinksFile() throws ProcessingException {
    return new File(getSpecInDir(), LINKS_FILE_NAME);
  }

}
