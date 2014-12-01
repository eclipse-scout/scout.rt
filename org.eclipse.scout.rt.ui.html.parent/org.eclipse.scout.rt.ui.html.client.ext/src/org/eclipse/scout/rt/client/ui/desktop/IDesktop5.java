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
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.List;

public interface IDesktop5 extends IDesktop {

  /**
   * Returns a list of untyped add-ons for the Desktop.
   *
   * @return
   */
  List<Object> getAddOns();

  //FIXME imo also add
  //void addAddOn(Object addOn) ;
}
