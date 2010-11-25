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
package org.eclipse.scout.rt.ui.swing.form.fields.plannerfield.layout;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class PlannerFieldLayoutConstraints {
  public static final int PLANNER = 1;
  public static final int MINI_CALENDARS = 2;

  public int fieldType;

  private IFormField m_scoutFormField;

  public PlannerFieldLayoutConstraints(int fieldType) {
    this.fieldType = fieldType;
  }

}
