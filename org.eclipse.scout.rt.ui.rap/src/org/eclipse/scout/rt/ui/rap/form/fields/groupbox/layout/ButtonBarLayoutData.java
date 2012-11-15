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
package org.eclipse.scout.rt.ui.rap.form.fields.groupbox.layout;

import org.eclipse.swt.SWT;

public class ButtonBarLayoutData {
  public int insetTop = 0;
  public int insetBottom = 0;
  public int insetLeft = 0;
  public int insetRight = 0;

  public boolean fillHorizontal;
  public boolean fillVertical;

  public int horizontalAlignment = SWT.NONE;
  public int verticalAlignment = SWT.NONE;

}
