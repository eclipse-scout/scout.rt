/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.util.Map;

/**
 * {@link ConfigIniUtility} properties provided by this module
 */
public interface ConfigIniConstants {
  /**
   * default: java
   */
  String logStrategy = "org.eclipse.scout.log";

  /**
   * Performance optimization: sometimes a {@link Map#contains(Object)} is faster that the {@link Map#get(Object)}
   * <p>
   * default: false
   */
  String nlsCheckContainsKey = "scout.resourceBundle.checkContainsKey";
}
