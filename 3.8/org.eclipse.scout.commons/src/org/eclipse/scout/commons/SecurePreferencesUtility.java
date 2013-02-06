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
package org.eclipse.scout.commons;

import java.io.IOException;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

public final class SecurePreferencesUtility {

  private SecurePreferencesUtility() {
  }

  /**
   * @param path
   *          normally of the form host/path, see {@link ISecurePreferences#node(String)} Store the data in the
   *          {@link ISecurePreferences} store
   */
  public static void storeCredentials(String path, String username, String password) throws StorageException, IOException {
    ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
    ISecurePreferences node = securePreferences.node(path);
    node.put("username", username, false);
    node.put("password", password, true);
    securePreferences.flush();
  }

  /**
   * @param path
   *          normally of the form host/path, see {@link ISecurePreferences#node(String)}
   * @return String[2]{username,password} if it exists and null otherwise
   * @throws StorageException
   *           Load the data from the {@link ISecurePreferences} store
   */
  public static String[] loadCredentials(String path) throws StorageException {
    ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
    ISecurePreferences node = securePreferences.node(path);
    String username = node.get("username", null);
    String password = node.get("password", null);
    if (username != null && password != null) {
      return new String[]{username, password};
    }
    else {
      return null;
    }
  }

  /**
   * @param path
   *          normally of the form host/path, see {@link ISecurePreferences#node(String)} Clear the data in the
   *          {@link ISecurePreferences} store
   */
  public static void removeCredentials(String path) throws IOException {
    ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
    ISecurePreferences node = securePreferences.node(path);
    node.removeNode();
    securePreferences.flush();
  }
}
