/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
