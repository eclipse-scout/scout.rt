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
package org.eclipse.scout.commons.logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class EclipseLogUtility {

  private EclipseLogUtility() {
  }

  /**
   * convert between Eclipse status {@link IStatus} and Scout level (int-based)
   */
  public static int eclipseToScoutLevel(int level) {
    switch (level) {
      case IStatus.ERROR:
      case IStatus.CANCEL:
        return IScoutLogger.LEVEL_ERROR;
      case IStatus.WARNING:
        return IScoutLogger.LEVEL_WARN;
      case IStatus.INFO:
      case IStatus.OK:
        return IScoutLogger.LEVEL_INFO;
      default:
        return IScoutLogger.LEVEL_INFO;
    }
  }

  /**
   * convert between Scout log {@link IScoutLogger} and Eclipse level
   */
  public static int scoutToEclipseLevel(int level) {
    switch (level) {
      case IScoutLogger.LEVEL_OFF: {
        return Status.ERROR;
      }
      case IScoutLogger.LEVEL_ERROR: {
        return Status.ERROR;
      }
      case IScoutLogger.LEVEL_WARN: {
        return Status.WARNING;
      }
      case IScoutLogger.LEVEL_INFO: {
        return Status.INFO;
      }
      case IScoutLogger.LEVEL_DEBUG: {
        return Status.INFO;
      }
      case IScoutLogger.LEVEL_TRACE: {
        return Status.INFO;
      }
      default:
        return Status.INFO;
    }
  }
}
