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
package org.eclipse.scout.rt.ui.swt.keystroke;

import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.swt.widgets.Widget;

public interface IKeyStrokeManager {

  String DATA_KEY_STROKES = Activator.PLUGIN_ID + ".keyStrokes";
  String DATA_KEY_STROKE_FILTERS = Activator.PLUGIN_ID + ".keyStrokeFilters";

  /**
   * global key strokes will be executed when and only when no key stroke of the
   * control hierarchy starting at the event's source control consumed
   * (event.doit = false) the event.
   * 
   * @param stroke
   * @return
   */
  void addGlobalKeyStroke(ISwtKeyStroke stroke);

  /**
   * global key strokes will be executed when and only when no key stroke of the
   * control hierarchy starting at the event's source control consumed
   * (event.doit = false) the event.
   * 
   * @param stroke
   * @return
   */
  boolean removeGlobalKeyStroke(ISwtKeyStroke stroke);

  /**
   * Commodity to access the key stroke filters set as data on the widget
   * 
   * @param widget
   * @param stroke
   */
  void addKeyStroke(Widget widget, ISwtKeyStroke stroke);

  /**
   * Commodity to access the key stroke filters set as data on the widget
   * 
   * @param widget
   * @param stroke
   * @return true if the key stroke has been removed false otherwise
   */
  boolean removeKeyStroke(Widget widget, ISwtKeyStroke stroke);

  /**
   * Commodity to access the key stroke filters set as data on the widget
   * 
   * @param widget
   * @param stroke
   */
  void addKeyStrokeFilter(Widget widget, ISwtKeyStrokeFilter stroke);

  /**
   * Commodity to access the key stroke filters set as data on the widget
   * 
   * @param widget
   * @param stroke
   * @return true if the key stroke filter has been removed false otherwise
   */
  boolean removeKeyStrokeFilter(Widget widget, ISwtKeyStrokeFilter stroke);

}
