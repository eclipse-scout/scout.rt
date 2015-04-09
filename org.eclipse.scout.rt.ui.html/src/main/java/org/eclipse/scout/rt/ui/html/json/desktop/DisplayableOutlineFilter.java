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
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;

public class DisplayableOutlineFilter<T extends IOutline> implements IFilter<T> {

  @Override
  public boolean accept(T element) {
    return element.isVisibleGranted();
  }

}
