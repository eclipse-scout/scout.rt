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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public interface IDeviceTransformer {
  void desktopInit(IDesktop desktop);

  void desktopGuiAttached() throws ProcessingException;

  void desktopGuiDetached() throws ProcessingException;

  void tablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException;

  void transformForm(IForm form);

  void transformOutline(IOutline outline);

  void transformPageDetailTable(ITable table);

  boolean acceptForm(IForm form);
}
