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
package org.eclipse.scout.rt.ui.rap.services.common.patchedclass;

import org.eclipse.scout.rt.ui.rap.basic.table.IRwtScoutTableForPatch;
import org.eclipse.scout.rt.ui.rap.ext.IDropDownButtonForPatch;
import org.eclipse.scout.rt.ui.rap.ext.IDropDownFileUploadForPatch;
import org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar.IRwtScoutFormButtonForPatch;
import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.IRwtScoutToolButtonForPatch;
import org.eclipse.scout.service.IService2;
import org.eclipse.swt.widgets.Composite;

public interface IPatchedClassService extends IService2 {

  IRwtScoutTableForPatch createRwtScoutTable();

  IRwtScoutTableForPatch createRwtScoutTable(String variant);

  IDropDownButtonForPatch createDropDownButton(Composite parent, int style);

  IDropDownFileUploadForPatch createDropDownFileUpload(Composite parent, int style);

  IRwtScoutFormButtonForPatch createRwtScoutFormButton(boolean textVisible, boolean iconVisible, String variantInActive, String variantActive);

  IRwtScoutToolButtonForPatch createRwtScoutToolButton(boolean textVisible, boolean iconVisible, String variantInActive, String variantActive);
}
