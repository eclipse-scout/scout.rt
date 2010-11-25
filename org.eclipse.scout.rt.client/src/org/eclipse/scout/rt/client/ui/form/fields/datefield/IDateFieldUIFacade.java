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
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import java.util.Date;

public interface IDateFieldUIFacade {

  boolean setTextFromUI(String newText);

  void setDateFromUI(Date d, boolean includesTime);

  /**
   * see {@link AbstractDateField#execShiftDate(int, int)}
   */
  void fireDateShiftActionFromUI(int level, int value);

}
