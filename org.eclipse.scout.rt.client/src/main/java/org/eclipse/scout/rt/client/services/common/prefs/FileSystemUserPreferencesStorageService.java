/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.prefs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.client.ClientConfigProperties.UserAreaProperty;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.prefs.AbstractUserPreferencesStorageService;
import org.eclipse.scout.rt.shared.services.common.prefs.IPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for storing preferences on the local file system in user area.
 */
public class FileSystemUserPreferencesStorageService extends AbstractUserPreferencesStorageService {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemUserPreferencesStorageService.class);

  public static final String PROP_USER_HOME = "user.home";

  public static final String SETTINGS_SUB_DIR = ".settings";
  public static final char NODE_NAME_DELIM = '-';
  public static final String SETTINGS_EXT = ".prefs";

  private final AtomicBoolean m_noUserAreaWarningLogged = new AtomicBoolean(false);

  @Override
  public void flush(IPreferences prefs) {
    if (prefs == null) {
      return;
    }

    File prefsLocation = getPrefsLocation(getUserScope(prefs.userScope()), prefs.name());
    LOG.debug("flusing preferences to file '{}'.", prefsLocation.getAbsolutePath());
    Properties propsToFlush = new Properties();
    convertToProperties(prefs, propsToFlush);
    if (propsToFlush.isEmpty()) {
      if (prefsLocation.exists()) {
        // no properties but the file exists -> remove
        boolean deleted = prefsLocation.delete();
        if (!deleted) {
          LOG.warn("Could not delete preference file '{}'.", prefsLocation.getAbsolutePath());
        }
      }
    }
    else {
      flushToDisk(propsToFlush, prefsLocation);
    }
  }

  @Override
  protected void load(String userScope, String nodeId, IPreferences prefsToFill) {
    File prefsLocation = getPrefsLocation(userScope, nodeId);
    if (!prefsLocation.exists()) {
      // fallback: try legacy location
      File legacyPrefsLocation = getLegacyPrefsLocation(nodeId);
      if (legacyPrefsLocation.exists()) {
        LOG.warn("Legacy preference found: '{}'. Will be stored in the new location ('{}') the next time.", legacyPrefsLocation.getAbsolutePath(), prefsLocation.getAbsolutePath());
      }
      prefsLocation = legacyPrefsLocation;
    }

    if (prefsLocation.exists()) {
      Properties propertiesFromDisk = loadFromDisk(prefsLocation);
      convertToPreferences(propertiesFromDisk, prefsToFill);
    }
  }

  protected void convertToProperties(IPreferences src, Properties target) {
    Set<String> keys = src.keys();
    for (String key : keys) {
      String value = src.get(key, null);
      if (value != null) {
        target.setProperty(key, value);
      }
    }
  }

  protected void convertToPreferences(Properties src, IPreferences target) {
    for (Entry<Object, Object> entry : src.entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (value != null) {
        target.put(key, value);
      }
    }
  }

  protected void flushToDisk(Properties props, File prefsLocation) {
    File parentDirectory = prefsLocation.getParentFile();
    if (!parentDirectory.exists()) {
      boolean done = parentDirectory.mkdirs();
      if (!done) {
        throw new ProcessingException("Error writing preferences to file '" + prefsLocation.getAbsolutePath() + "'. Directory could not be created.");
      }
    }

    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(prefsLocation, false))) {
      props.store(out, null);
    }
    catch (IOException e) {
      throw new ProcessingException("Error writing preferences to file '" + prefsLocation.getAbsolutePath() + "'.", e);
    }
  }

  protected Properties loadFromDisk(File prefsLocation) {
    LOG.debug("loading preferences from file '{}'.", prefsLocation.getAbsolutePath());
    Properties result = new Properties();
    try (InputStream input = new BufferedInputStream(new FileInputStream(prefsLocation))) {
      result.load(input);
    }
    catch (IOException e) {
      throw new ProcessingException("Error loading preferences from file '" + prefsLocation.getAbsolutePath() + "'.", e);
    }
    return result;
  }

  protected String computeFullNodeName(String userScope, String nodeId) {
    int len = nodeId.length() + 1 + userScope.length() + SETTINGS_EXT.length();
    return new StringBuilder(len).append(nodeId).append(NODE_NAME_DELIM).append(userScope).append(SETTINGS_EXT).toString();
  }

  protected String computeLegacyFullNodeName(String nodeId) {
    int len = nodeId.length() + SETTINGS_EXT.length();
    return new StringBuilder(len).append(nodeId).append(SETTINGS_EXT).toString();
  }

  protected File getLegacyPrefsLocation(String nodeId) {
    File prefsLoc = new File(new File(getBaseFolder(), SETTINGS_SUB_DIR), computeLegacyFullNodeName(nodeId));
    return prefsLoc;
  }

  protected File getPrefsLocation(String userScope, String nodeId) {
    File prefsLoc = new File(new File(getBaseFolder(), SETTINGS_SUB_DIR), computeFullNodeName(userScope, nodeId));
    return prefsLoc;
  }

  protected String getBaseFolder() {
    IConfigProperty<String> userAreaProperty = BEANS.get(UserAreaProperty.class);
    String location = userAreaProperty.getValue();
    if (location == null) {
      location = new File(ConfigUtility.getProperty(PROP_USER_HOME), "user").getAbsolutePath();
      if (m_noUserAreaWarningLogged.compareAndSet(false, true)) {
        LOG.warn("No user area property found. Using '{}' as fallback. Consider specifying a user area using property '{}'.", location, userAreaProperty.getKey());
      }
    }
    location = location.trim();

    if (location.startsWith("file:")) {
      try {
        location = new File(new URI(location)).getAbsolutePath();
      }
      catch (URISyntaxException e) {
        LOG.warn("invalid URI syntax: '{}'.", location, e);
      }
    }
    return location;
  }
}
