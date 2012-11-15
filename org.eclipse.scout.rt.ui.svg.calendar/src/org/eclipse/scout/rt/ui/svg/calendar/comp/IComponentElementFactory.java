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
package org.eclipse.scout.rt.ui.svg.calendar.comp;

import java.util.Date;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.w3c.dom.Element;

/**
 * Factories to create svg elements based on scout calendar components
 */
public interface IComponentElementFactory {
  /**
   * creates svg elements for all calendar components passed.
   * 
   * @param container
   *          The container in which the elements should be placed
   * @param elementDate
   *          The date (day) for which the elements should be generated.
   * @param components
   *          The components. Elements for this list are created.
   * @return A map with the components passed and the corresponding svg element created.
   */
  Map<CalendarComponent, Element> create(Element container, Date elementDate, CalendarComponent[] components);

  /**
   * defines which calendar component is currently selected.
   * 
   * @param c
   */
  void setSelectedComponent(CalendarComponent c);
}
