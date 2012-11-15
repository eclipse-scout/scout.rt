/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.action.menu;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;

/**
 * @since 3.9.0
 */
public interface IMenuExtensionFilter {

  /**
   * This method is called to decide whether an extension is used in the given context. The anchor represents the
   * reference object a menu extension is attached to. It is an instance of the following types:
   * <ul>
   * <li>{@link IPage}</li>
   * <li>{@link IFormField}</li>
   * <li>{@link IMenu}</li>
   * </ul>
   * <p/>
   * The container is the direct parent the menu is attached to. Some examples are:
   * <ul>
   * <li>{@link ITable} of an {@link IPageWithTable}</li>
   * <li>{@link IActivityMap} of an {@link IPlannerField}</li>
   * <li>etc.</li>
   * </ul>
   * 
   * @param anchor
   *          the reference object the menu extension is attached to. e.g. an {@link IPage}, an {@link IFormField} or an
   *          {@link IMenu}.
   * @param container
   *          the direct parent the menu is attached to. Sometimes the same object as <code>anchor</code>.
   * @param menu
   *          <code>null</code> in case of a menu contribution or the existing menu in case of menu modifications and
   *          removals.
   * @return Returns <code>true</code> if the extension has to be applied in the given context. Otherwise
   *         <code>false</code>.
   */
  boolean accept(Object anchor, Object container, IMenu menu);
}
