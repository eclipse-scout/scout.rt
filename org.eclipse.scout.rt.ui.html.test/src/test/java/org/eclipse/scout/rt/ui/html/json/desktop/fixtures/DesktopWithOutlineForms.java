/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("aea7c72f-c69c-4788-aa9c-f3f86834581a")
public class DesktopWithOutlineForms extends DesktopWithOneOutline {

  @Override
  protected void execOpened() {
    IOutline firstOutline = CollectionUtility.firstElement(getAvailableOutlines());
    activateOutline(firstOutline);
  }

}
