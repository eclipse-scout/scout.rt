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
package org.eclipse.scout.rt.client.mobile.navigation;

import java.util.List;
import java.util.Stack;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public interface IBreadCrumbsNavigation {
  void goHome();

  void stepBack();

  boolean isSteppingBackPossible();

  boolean isGoingHomePossible();

  IForm getCurrentNavigationForm();

  List<IForm> getCurrentNavigationForms();

  Stack<IBreadCrumb> getBreadCrumbs();

  boolean containsFormInHistory(IForm form);

  void addBreadCrumbsListener(BreadCrumbsListener listener);

  void removeBreadCrumbsListener(BreadCrumbsListener listener);

  void trackDisplayViewId(String displayViewId);

  IDesktop getDesktop();
}
