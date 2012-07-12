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
package org.eclipse.scout.rt.testing.shared;

/**
 * Utility class to check for the underlying Operating System name.
 * Currently, only the latest Windows versions are implemented.
 */
public class OsUtility {

  private static final String WIN_7 = "windows 7";
  private static final String WIN_VISTA = "windows vista";
  private static final String WIN_XP = "windows xp";

  private OsUtility() {
  }

  private static String getOsName() {
    return System.getProperty("os.name").toLowerCase();
  }

  public static boolean isWindows7() {
    return WIN_7.equals(getOsName());
  }

  public static boolean isWindowsVista() {
    return WIN_VISTA.equals(getOsName());
  }

  public static boolean isWindowsXP() {
    return WIN_XP.equals(getOsName());
  }
}
