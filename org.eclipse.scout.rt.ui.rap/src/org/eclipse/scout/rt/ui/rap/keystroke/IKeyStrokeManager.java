/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.keystroke;

import org.eclipse.scout.rt.ui.rap.Activator;
import org.eclipse.swt.widgets.Control;

public interface IKeyStrokeManager {

  String DATA_KEY_STROKES = Activator.PLUGIN_ID + ".keyStrokes";
  String DATA_KEY_STROKE_FILTERS = Activator.PLUGIN_ID + ".keyStrokeFilters";

  String PARAMETER_STATE_MASK = Activator.PLUGIN_ID + ".StateMask";
  String PARAMETER_KEY_CODE = Activator.PLUGIN_ID + ".KeyCode";

  /**
   * global key strokes will be executed when and only when no key stroke of the
   * control hierarchy starting at the event's source control consumed
   * (event.doit = false) the event.
   * 
   * @param stroke
   * @param exclusive
   *          true will cancel all other key events from the client
   * @return
   */
  void addGlobalKeyStroke(IRwtKeyStroke stroke, boolean exclusive);

  /**
   * global key strokes will be executed when and only when no key stroke of the
   * control hierarchy starting at the event's source control consumed
   * (event.doit = false) the event.
   * 
   * @param stroke
   * @return
   */
  boolean removeGlobalKeyStroke(IRwtKeyStroke stroke);

  /**
   * Commodity to access the key stroke filters set as data on the control
   * 
   * @param control
   * @param stroke
   * @param exclusive
   *          true will cancel all other key events from the client
   */
  void addKeyStroke(Control control, IRwtKeyStroke stroke, boolean exclusive);

  /**
   * Commodity to access the key stroke filters set as data on the control
   * 
   * @param control
   * @param stroke
   * @return true if the key stroke has been removed false otherwise
   */
  boolean removeKeyStroke(Control control, IRwtKeyStroke stroke);

  /**
   * Commodity to access the key stroke filters set as data on the control
   * 
   * @param control
   * @return true if all the key strokes has been removed false otherwise
   */
  boolean removeKeyStrokes(Control control);
}
