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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.Bean;

/**
 * @since 3.9.0
 */
@Bean
public interface IDeviceTransformer {

  boolean isActive();

  void setDesktop(IDesktop desktop);

  void transformDesktop();

  void transformForm(IForm form);

  void transformFormField(IFormField field);

  void transformOutline(IOutline outline);

  void transformPage(IPage<?> page);

  void transformPageDetailTable(ITable table);

  void adaptDesktopActions(Collection<IAction> actions);

  void notifyDesktopClosing();

  void notifyTablePageLoaded(IPageWithTable<?> tablePage);

  boolean acceptFormAddingToDesktop(IForm form);

  boolean isFormFieldExcluded(IFormField formField);

  boolean isGridDataDirty(IForm form);

  void gridDataRebuilt(IForm form);

  DeviceTransformationConfig getDeviceTransformationConfig();
}
