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
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;

public interface ISvgFieldUIFacade {

  void setSelectedElementFromUI(IScoutSVGElement element);

  /**
   * implicitly selects the element
   */
  void fireElementClickFromUI(IScoutSVGElement element);

  /**
   * implicitly selects the element
   */
  IMenu[] fireElementPopupFromUI(IScoutSVGElement element);

}
