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
package org.eclipse.scout.rt.ui.rap.form.fields.button;

import org.eclipse.swt.widgets.Widget;

public class SeparatorDummyWidget extends Widget {

  private static final long serialVersionUID = 1L;

  public static int STYLE_SEPARATOR = 1 << 8;

  public SeparatorDummyWidget(Widget parent) {
    super(parent, STYLE_SEPARATOR);
  }

}
