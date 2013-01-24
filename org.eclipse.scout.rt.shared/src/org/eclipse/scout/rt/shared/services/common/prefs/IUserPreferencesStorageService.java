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
package org.eclipse.scout.rt.shared.services.common.prefs;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.service.IService;
import org.osgi.service.prefs.Preferences;

/**
 *
 */
@Priority(-3)
@InputValidation(IValidationStrategy.PROCESS.class)
public interface IUserPreferencesStorageService extends IService {

  /**
   * Loads UI Preferences (Windows, Column widths, Table Customizers, ...) from persistent data store.
   * Method is called on any UI change.
   * 
   * @return preferences data
   */
  Preferences loadPreferences();
}
