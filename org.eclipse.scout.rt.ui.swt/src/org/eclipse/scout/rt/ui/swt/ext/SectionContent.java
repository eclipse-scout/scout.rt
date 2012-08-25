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
package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class SectionContent extends Composite {

  public SectionContent(Composite parent, int style) {
    super(parent, style);
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    if (hint == SWT.DEFAULT && hint2 == SWT.DEFAULT) {
      hint = getParent().getClientArea().width;
    }
    return super.computeSize(hint, hint2, changed);
  }

}
