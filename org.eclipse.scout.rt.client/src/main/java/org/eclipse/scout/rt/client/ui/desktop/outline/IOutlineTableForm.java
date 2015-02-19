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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Interface for all outline table forms<br>
 * Is used at AbstractDesktop.P_UIFacade.firePreDesktopClosingFromUI() to
 * prevent Outlines to be closed.
 */
public interface IOutlineTableForm extends IForm {

  void setCurrentTable(ITable table);

  ITable getCurrentTable();

}
