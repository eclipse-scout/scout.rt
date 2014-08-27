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
package org.eclipse.scout.commons.internal.runtime;

import org.eclipse.scout.commons.BundleContextUtility;
import org.osgi.framework.Version;

/**
 * @since 3.10.1
 */
public class CompatibilityUtility {

  public static boolean isEclipseVersionLessThan(Version version) {
    String versionProperty = BundleContextUtility.getProperty("osgi.framework.version");
    if (versionProperty != null) {
      Version frameworkVersion = new Version(versionProperty);
      return frameworkVersion.compareTo(version) < 0;
    }

    return false;
  }
}
