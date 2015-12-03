/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * This interface is used to check fields for valid content and - in case invalid - activate / select / focus the
 * appropriate location
 * <p>
 * see {@link IFormField#validateContent()}
 */
public interface IValidateContentDescriptor {

  /**
   * @return the name of the location/field/part
   */
  String getDisplayText();

  IStatus getErrorStatus();

  /**
   * activate / select / focus the appropriate location
   */
  void activateProblemLocation();
}
