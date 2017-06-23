/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.splitbox;

public interface ISplitBoxUIFacade {

  void setSplitterPositionFromUI(double splitterPosition);

  void setMinSplitterPositionFromUI(Double minSplitterPosition);

  void setFieldCollapsedFromUI(boolean collapsed);

  void setFieldMinimizedFromUI(boolean minimized);
}
