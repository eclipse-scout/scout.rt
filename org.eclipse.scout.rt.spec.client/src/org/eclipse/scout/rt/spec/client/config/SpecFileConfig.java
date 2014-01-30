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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;

/**
 * Configuration of input and output files
 */
public class SpecFileConfig {
  /**
   * Commandline parameter or SystemProperty (comma separated) for defining additional source plugins for copying
   * mediawiki, image and config files. Source and binary plugins are supported.
   * <p>
   * Attention order matters for copying files: <br>
   * First files from the additional plugins are copied in the same order as the plugins are provided here. Then files
   * from the plugin returned by {@link #getBundle()} are copied. If different plugins contain files with the same name
   * they are overwritten.
   * <p>
   * 
   * @param additionalSourcePlugins
   *          one or more bundle-symbolic-names
   */
  // TODO ASA So far, there is no support for subdirectories. Would we need it?
  private static final String ADDITIONAL_SOURCE_PLUGINS = "additionalSourcePlugins";
  private static final String SPEC_OUT_DIR_NAME = "target" + File.separator + "spec";
  private static final String SPEC_IN_DIR_NAME = "resources" + File.separator + "spec";

  private static final String IMAGES_DIR_NAME = "images";
  private static final String MEDIAWIKI_DIR_NAME = "mediawiki";
  private static final String LINKS_FILE_NAME = "links.properties";

  // TODO ASA configure output dir, remove m_bundle, change m_additionalSourcePlugins to m_sourcePlugin and explicitly configure all
  private String[] m_additionalSourcePlugins = new String[0];
  private Bundle m_bundle;

  /**
   * The bundle property ({@link #getBundle()}) will be set to the defining bundle of the product which was selected
   * when running this Eclipse instance.
   */
  public SpecFileConfig() {
    IProduct product = Platform.getProduct();
    // TODO ASA Nullcheck is only needed for tests. --> Create a product for tests and remove nullcheck.
    if (product != null) {
      m_bundle = product.getDefiningBundle();
    }
    readAdditionalSourcePluginsFromConfig();
  }

  private void readAdditionalSourcePluginsFromConfig() {
    String parameter = getConfigParameter(ADDITIONAL_SOURCE_PLUGINS);
    m_additionalSourcePlugins = StringUtility.split(parameter, ",");
  }

  /**
   * Returns the configuration value for the given parameter that is either configured as
   * command line argument or as system property.
   * 
   * @param parameterName
   * @return
   */
  // TODO ASA copied form ScoutJUnitPluginTestExecutor --> move to Utility
  private static String getConfigParameter(String parameterName) {
    String commandLineArgumentName = "-" + parameterName + "=";
    for (String arg : Platform.getCommandLineArgs()) {
      if (arg != null && arg.startsWith(commandLineArgumentName)) {
        return arg.substring(commandLineArgumentName.length());
      }
    }
    return System.getProperty(parameterName);
  }

  /**
   * The bundle property ({@link #getBundle()}) will be set to provided plugin.
   */
  // TODO ASA only used in tests; refactor: configure output dir instead of bundle
  public SpecFileConfig(String pluginName) {
    m_bundle = Platform.getBundle(pluginName);
  }

  /**
   * @return root of the plugin returned by {@link #getBundle()}
   * @return
   * @throws ProcessingException
   */
  public File getBundleRoot() throws ProcessingException {
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
    return m_bundle;
  }

  private static IScoutLogger LOG = ScoutLogManager.getLogger(SpecFileConfig.class);

  /**
   * @return all source bundles, ordered by priority: lowest priority first, highest last
   */
  public List<Bundle> getSourceBundles() {
    ArrayList<Bundle> arrayList = new ArrayList<Bundle>();
    for (String bundleName : m_additionalSourcePlugins) {
      Bundle bundle = Platform.getBundle(bundleName);
      if (bundle != null) {
        arrayList.add(bundle);
      }
      else {
        LOG.warn("no bundle available with symbolic name: " + bundleName);
      }
    }
    arrayList.add(getBundle());
    return arrayList;
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

  public String getRelativeMediawikiSourceDirPath() {
    return SPEC_IN_DIR_NAME + File.separator + MEDIAWIKI_DIR_NAME;
  }

  public String getRelativeSourceDirPath() {
    return SPEC_IN_DIR_NAME;
  }

  public String getRelativeImagesSourceDirPath() {
    return SPEC_IN_DIR_NAME + File.separator + IMAGES_DIR_NAME;
  }

  /**
   * @return mediawiki raw output (without postprocessing)
   * @throws ProcessingException
   */
  public File getMediawikiRawDir() throws ProcessingException {
    return new File(getSpecDir(), "mediawiki_raw");
  }

  /**
   * @param bundle
   * @return
   * @throws ProcessingException
   */
  // TODO ASA remove when links.properties are not written anymore to input dir
  public File getSpecInDir() throws ProcessingException {
    return new File(getBundleRoot(), SPEC_IN_DIR_NAME);
  }

  public File getLinksFile() throws ProcessingException {
    return new File(getSpecDir(), LINKS_FILE_NAME);
  }

}
