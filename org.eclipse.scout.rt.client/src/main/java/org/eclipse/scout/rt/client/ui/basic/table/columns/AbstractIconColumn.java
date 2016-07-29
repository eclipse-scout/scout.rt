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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * A column that renders its content as a font icon or image
 */
@ClassId("d5beef55-b40e-44f6-a539-be3bd75d3e33")
public abstract class AbstractIconColumn extends AbstractColumn<String> implements IIconColumn {

  @Override
  protected int getConfiguredWidth() {
    return 32;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0;
  }

}
