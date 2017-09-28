/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
 * Common UI facade for value fields with value, displayText and error-status.
 * 
 * @since 7.0
 */
public interface IValueFieldUIFacade<VALUE> {

  void setValueFromUI(VALUE value);

  void setDisplayTextFromUI(String text);

  void setErrorStatusFromUI(IStatus errorStatus);

}
