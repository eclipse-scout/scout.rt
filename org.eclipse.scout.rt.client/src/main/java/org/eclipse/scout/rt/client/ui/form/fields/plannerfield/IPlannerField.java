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
package org.eclipse.scout.rt.client.ui.form.fields.plannerfield;

import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * The planner field is mainly a wrapper for a {@link IPlanner}.
 *
 * @see IPlanner
 */
public interface IPlannerField<P extends IPlanner<?, ?>> extends IFormField {

  String PROP_SPLITTER_POSITION = "splitterPosition";

  P getPlanner();

  /**
   * @deprecated will be removed with 7.1
   */
  @Deprecated
  int getSplitterPosition();

  /**
   * @deprecated will be removed with 7.1
   */
  @Deprecated
  void setSplitterPosition(int splitterPosition);

  /**
   * (re)load table data
   */
  void loadResources();

  IPlannerFieldUIFacade getUIFacade();

}
