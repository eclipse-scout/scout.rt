/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("8ccc334e-f302-47dd-bbcb-95f4dba75cef")
public class OutlineViewButton extends AbstractOutlineViewButton {

  public OutlineViewButton(IDesktop desktop, Class<? extends IOutline> outlineType) {
    super(desktop, outlineType);
  }

}
