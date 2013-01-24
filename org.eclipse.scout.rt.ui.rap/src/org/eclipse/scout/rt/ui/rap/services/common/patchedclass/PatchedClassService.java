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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.ui.rap.basic.table.IRwtScoutTableForPatch;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTable;
import org.eclipse.scout.rt.ui.rap.ext.DropDownButton;
import org.eclipse.scout.rt.ui.rap.ext.DropDownFileUpload;
import org.eclipse.scout.rt.ui.rap.ext.IDropDownButtonForPatch;
import org.eclipse.scout.rt.ui.rap.ext.IDropDownFileUploadForPatch;
import org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar.IRwtScoutFormButtonForPatch;
import org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar.RwtScoutFormButton;
import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.IRwtScoutToolButtonForPatch;
import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.RwtScoutToolButton;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.swt.widgets.Composite;

@Priority(-1000)
public class PatchedClassService extends AbstractService implements IPatchedClassService {

  @Override
  public IRwtScoutTableForPatch createRwtScoutTable() {
    return new RwtScoutTable();
  }

  @Override
  public IRwtScoutTableForPatch createRwtScoutTable(String variant) {
    return new RwtScoutTable(variant);
  }

  @Override
  public IDropDownButtonForPatch createDropDownButton(Composite parent, int style) {
    return new DropDownButton(parent, style);
  }

  @Override
  public IDropDownFileUploadForPatch createDropDownFileUpload(Composite parent, int style) {
    return new DropDownFileUpload(parent, style);
  }

  @Override
  public IRwtScoutFormButtonForPatch createRwtScoutFormButton(boolean textVisible, boolean iconVisible, String variantInActive, String variantActive) {
    return new RwtScoutFormButton(textVisible, iconVisible, variantInActive, variantActive);
  }

  @Override
  public IRwtScoutToolButtonForPatch createRwtScoutToolButton(boolean textVisible, boolean iconVisible, String variantInActive, String variantActive) {
    return new RwtScoutToolButton(textVisible, iconVisible, variantInActive, variantActive);
  }
}
