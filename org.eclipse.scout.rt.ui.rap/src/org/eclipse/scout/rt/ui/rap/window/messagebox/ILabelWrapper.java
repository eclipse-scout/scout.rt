/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.messagebox;

import org.eclipse.swt.widgets.Control;

/**
 * @since 4.2
 */
public interface ILabelWrapper {

  boolean isHtmlEnabled();

  Control getLabel();

  String getLabelText();

  void setLabelText(String text);
}
