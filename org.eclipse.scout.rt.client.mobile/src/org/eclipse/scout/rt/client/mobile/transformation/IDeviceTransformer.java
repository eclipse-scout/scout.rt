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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

/**
 * @since 3.9.0
 */
public interface IDeviceTransformer {

  void transformForm(IForm form) throws ProcessingException;

  void transformOutline(IOutline outline) throws ProcessingException;

  void transformPageDetailTable(ITable table) throws ProcessingException;

  void adaptFormHeaderLeftActions(IForm form, List<IMenu> menuList);

  void adaptFormHeaderRightActions(IForm form, List<IMenu> menuList);

  void adaptDesktopActions(Collection<IAction> actions);

  void adaptDesktopOutlines(Collection<IOutline> outlines);

  void notifyTablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException;

  boolean acceptFormAddingToDesktop(IForm form);

  boolean acceptMobileTabBoxTransformation(ITabBox tabBox);

  /**
   * @return a list of accepted view ids (IForm#VIEW_ID_* or null to accept all.
   * @see {@link IForm}
   */
  List<String> getAcceptedViewIds();

  DeviceTransformationConfig getDeviceTransformationConfig();
}
