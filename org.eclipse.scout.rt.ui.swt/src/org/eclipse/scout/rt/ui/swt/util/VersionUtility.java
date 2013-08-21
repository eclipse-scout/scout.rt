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
package org.eclipse.scout.rt.ui.swt.util;

import org.eclipse.scout.rt.ui.swt.Activator;
import org.osgi.framework.Version;

public class VersionUtility {

  /**
   * Lotus Notes 8.5.2 requires Eclipse 3.4. With this function it is possible to check the version to ensure backward
   * compatibility.
   */
  public static boolean isEclipseVersionLessThan35() {

    String versionProperty = Activator.getDefault().getBundle().getBundleContext().getProperty("osgi.framework.version");
    if (versionProperty != null) {
      Version frameworkVersion = new Version(versionProperty);
      if (frameworkVersion.getMajor() == 3 && frameworkVersion.getMinor() <= 5) {
        return true;
      }
    }

    return false;
  }
}
