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
package org.eclipse.scout.rt.client.ui.form.fields.svgfield;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;

/**
 * The field supports for a simple set of SVG XML. Use {@link #setScoutSVG(String)} to set the content of the field
 * using Scout SVG syntax.
 */
public interface ISvgField extends IValueField<ScoutSVGModel> {
  String PROP_SELECTED_ELEMENT = "selectedElement";

  IScoutSVGElement getSelectedElement();

  void setSelectedElement(IScoutSVGElement element);

  IMenu[] getMenus();

  void addSvgFieldListener(ISvgFieldListener listener);

  void removeSvgFieldListener(ISvgFieldListener listener);

  ISvgFieldUIFacade getUIFacade();

}
