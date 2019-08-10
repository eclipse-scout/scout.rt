/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormMenu;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * This class is used to create a read-only menu-instance when an existing menu instance which belongs to a component is
 * used somewhere else. For instance: menus of a page-node are automatically copied to the the page-table, because its
 * the logical place to have them there. The menu-wrapper delegates most read- or get-methods to the wrapped menu
 * instance, all write- or set-methods are implemented as NOP or throw an {@link UnsupportedOperationException}. Thus
 * the state of the wrapper should only change, when the original wrapped menu changes.
 * <p>
 * <b>IMPORTANT</b>: do not use this class in cases where you must change the state of the wrapper-instance directly,
 * since the class is not intended for that purpose. Only use it when you want to use an existing menu somewhere else,
 * using the state of the original menu.
 */
@ClassId("19966ccc-1ead-420b-8bad-bb97480230d6")
public class OutlineFormMenuWrapper extends OutlineMenuWrapper implements IFormMenu<IForm> {

  protected OutlineFormMenuWrapper(IFormMenu<? extends IForm> menu, IMenuTypeMapper menuTypeMapper, Predicate<IAction> menuFilter) {
    super(menu, menuTypeMapper, menuFilter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public IFormMenu<IForm> getWrappedMenu() {
    return (IFormMenu<IForm>) super.getWrappedMenu();
  }

  @Override
  public IForm getForm() {
    return getWrappedMenu().getForm();
  }

  @Override
  public void setForm(IForm f) {
    getWrappedMenu().setForm(f);
  }

}
