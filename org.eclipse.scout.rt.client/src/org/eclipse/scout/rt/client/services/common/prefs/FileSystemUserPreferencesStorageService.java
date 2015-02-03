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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.prefs.AbstractUserPreferencesStorageService;
import org.eclipse.scout.rt.shared.services.common.prefs.IPreferences;

/**
 * Default implementation for storing preferences on the local file system in user area.
 */
public class FileSystemUserPreferencesStorageService extends AbstractUserPreferencesStorageService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FileSystemUserPreferencesStorageService.class);

  public static final char VAR_PREFIX = '@';
  public static final String PROP_USER_HOME = "user.home";
  public static final String PROP_USER_DIR = "user.dir";
  public static final String USER_AREA = "user.area";
  public static final String USER_HOME = VAR_PREFIX + PROP_USER_HOME;
  public static final String USER_DIR = VAR_PREFIX + PROP_USER_DIR;

  public static final String SETTINGS_SUB_DIR = ".settings";
  public static final char NODE_NAME_DELIM = '-';
  public static final String SETTINGS_EXT = ".prefs";

  @Override
  public void flush(IPreferences prefs) throws ProcessingException {
    if (prefs == null) {
      return;
    }

    File prefsLocation = getPrefsLocation(getUserScope(prefs.userScope()), prefs.name());
    LOG.debug("flusing preferences to file '" + prefsLocation.getAbsolutePath() + "'.");
    Properties propsToFlush = new Properties();
    convertToProperties(prefs, propsToFlush);
    if (propsToFlush.isEmpty()) {
      if (prefsLocation.exists()) {
        // no properties but the file exists -> remove
        boolean deleted = prefsLocation.delete();
        if (!deleted) {
          LOG.warn("Could not delete preference file '" + prefsLocation.getAbsolutePath() + "'.");
        }
      }
    }
    else {
      flushToDisk(propsToFlush, prefsLocation);
    }
  }

  @Override
  protected void load(String userScope, String nodeId, IPreferences prefsToFill) throws ProcessingException {
    File prefsLocation = getPrefsLocation(userScope, nodeId);
    if (!prefsLocation.exists()) {
      // fallback: try legacy location
      File legacyPrefsLocation = getLegacyPrefsLocation(nodeId);
      if (legacyPrefsLocation.exists()) {
        LOG.warn("Legacy preference found: '" + legacyPrefsLocation.getAbsolutePath() + "'. Will be stored in the new location ('" + prefsLocation.getAbsolutePath() + "') the next time.");
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

  protected void flushToDisk(Properties props, File prefsLocation) throws ProcessingException {
    File parentDirectory = prefsLocation.getParentFile();
    if (!parentDirectory.exists()) {
      boolean done = parentDirectory.mkdirs();
      if (!done) {
        throw new ProcessingException("Error writing preferences to file '" + prefsLocation.getAbsolutePath() + "'. Directory could not be created.");
      }
    }

    try (FileOutputStream fos = new FileOutputStream(prefsLocation, false)) {
      OutputStream out = new BufferedOutputStream(fos);
      props.store(out, null);
      out.flush();
      fos.getFD().sync();
    }
    catch (IOException e) {
      throw new ProcessingException("Error writing preferences to file '" + prefsLocation.getAbsolutePath() + "'.", e);
    }
  }

  protected Properties loadFromDisk(File prefsLocation) throws ProcessingException {
    LOG.debug("loading preferences from file '" + prefsLocation.getAbsolutePath() + "'.");
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
    String location = System.getProperty(USER_AREA);
    if (location == null) {
      // legacy
      String legacyUserArea = "osgi.user.area";
      location = System.getProperty(legacyUserArea);
      if (location == null) {
        location = new File(System.getProperty(PROP_USER_HOME), "user").getAbsolutePath();
        LOG.warn("No user area property found. Using '" + location + "' as fallback. Consider specifying a user area using property '" + USER_AREA + "'.");
      }
      else {
        LOG.warn("Legacy user area property found: '" + legacyUserArea + "'. Consider migrating to the new one: '" + USER_AREA + "'.");
      }
    }
    location = location.trim();

    if (location.startsWith("file:")) {
      try {
        location = new File(new URI(location)).getAbsolutePath();
      }
      catch (URISyntaxException e) {
        LOG.warn("invalid URI syntax: '" + location + "'.", e);
      }
    }
    if (location.startsWith(USER_HOME)) {
      return substituteVar(location, USER_HOME, PROP_USER_HOME);
    }
    else if (location.startsWith(USER_DIR)) {
      return substituteVar(location, USER_DIR, PROP_USER_DIR);
    }
    else {
      return location;
    }
  }

  protected String substituteVar(String source, String var, String prop) {
    String value = System.getProperty(prop, "");
    int len = value.length() + source.length() - var.length();
    return new StringBuilder(len).append(value).append(source.substring(var.length())).toString();
  }

}
