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
package org.eclipse.scout.rt.ui.rap.extension;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface ILookAndFeelDecorations {
  int STAR_MARKER_NONE = 0;
  int STAR_MARKER_BEFORE_LABEL = 1;
  int STAR_MARKER_AFTER_LABEL = 2;

  int getScope();

  int getStarMarkerPosition();

  FontSpec getMandatoryLabelFont();

  String getMandatoryFieldBackgroundColor();

  String getMandatoryLabelTextColor();
}
