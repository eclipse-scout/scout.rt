package org.eclipse.scout.rt.client.mobile.services;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.IService2;

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

/**
 * @since 3.8.0
 */
public interface IMobileNavigationService extends IService2 {

  /**
   * Installs a navigator which is necessary to use this service.<br/>
   * The navigator automatically gets uninstalled when the desktop closes.
   */
  void installNavigator();

  void stepBack() throws ProcessingException;

  boolean steppingBackPossible();

  IForm getCurrentForm();

}
