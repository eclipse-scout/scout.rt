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
package org.eclipse.scout.rt.ui.rap;

import java.io.PrintWriter;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ILayoutExtension;

/**
 * Dynamic layout using logical grid data {@link LogicalGridData} to arrange
 * fields. The grid data per field can be passed when adding the component to
 * the container or set as client property with name {@link LogicalGridData#CLIENT_PROPERTY_NAME}.
 */
public interface ILogicalGridLayout extends ILayoutExtension {
  int MIN = 0;
  int PREF = 1;
  int MAX = 2;

  float EPS = 1E-6f;

  Point computeSize(Composite composite, boolean changed, int wHint, int hHint, int sizeFlag);

  Point computeMinimumSize(Composite parent, boolean changed);

  void setDebug(boolean b);

  void dumpLayoutInfo(Composite parent);

  void dumpLayoutInfo(Composite parent, PrintWriter out);
}
