package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
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
public interface IBreadCrumbsNavigationService extends IService2 {

  void stepBack() throws ProcessingException;

  boolean isSteppingBackPossible();

  void goHome() throws ProcessingException;

  boolean isGoingHomePossible();

  IForm getCurrentNavigationForm();

  void addBreadCrumbsListener(IDesktop desktop, BreadCrumbsListener listener);

  void addBreadCrumbsListener(BreadCrumbsListener listener);

  void removeBreadCrumbsListener(BreadCrumbsListener listener);

  IBreadCrumbsNavigation getBreadCrumbsNavigation();

  void trackDisplayViewId(String displayViewId);
}
