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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser;

import java.util.Date;

import org.eclipse.swt.widgets.Event;

/**
 * Class DateSelectionEvent. is used to pass a date to DateSelectionListener.
 * 
 * @version 1.0
 * @since 2005
 * @see ch.post.pf.gui.ocp.wt.ext.listener.AbstractDateSelectionListener
 */
public class DateSelectionEvent extends Event {
  private Object m_data;

  /**
   * Constructor for DateSelectionEvent
   * 
   * @param date
   *          the date to distribute
   */
  public DateSelectionEvent(Date date) {
    super();
    m_data = date;

  }

  /**
   * @return *
   */
  public Object getData() {
    return m_data;
  }
}
