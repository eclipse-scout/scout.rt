/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared;

import java.util.Calendar;

public final class OfficialVersion {

  private OfficialVersion() {
  }

  public static final String COPYRIGHT_VERSION = "10";
  public static final String COPYRIGHT = "Scout " + COPYRIGHT_VERSION + ", &copy; BSI Business Systems Integration AG " + 2001 + "," + Calendar.getInstance().get(Calendar.YEAR) + " EPL";

  /**
   * @deprecated unused global variable will be remove in a future release
   */
  @SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S1444"})
  public static String customCopyrightText;

}
