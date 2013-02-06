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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.prefs.UserScope;
import org.eclipse.scout.rt.client.Activator;
import org.eclipse.scout.rt.shared.services.common.prefs.IUserPreferencesStorageService;
import org.eclipse.scout.service.AbstractService;
import org.osgi.service.prefs.Preferences;

/**
 * Default implementation for storing UI preferences (Windows, Column widths, Table Customizers, ...) on local file
 * system in user area.
 */
public class FileSystemUserPreferencesStorageService extends AbstractService implements IUserPreferencesStorageService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FileSystemUserPreferencesStorageService.class);

  @Override
  public Preferences loadPreferences() {
    return new UserScope().getNode(Activator.PLUGIN_ID);
  }

}
