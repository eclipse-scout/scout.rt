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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

@ClassId("c333e52d-4678-41b3-8307-01d473864d2e")
public abstract class AbstractSmartColumn<T> extends AbstractMixedSmartColumn<T, T> implements ISmartColumn<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSmartColumn.class);

  public AbstractSmartColumn() {
    super();
  }

  @Override
  protected final T execConvertKeyToValue(T key) {
    return key;
  }

  @Override
  protected final T execConvertValueToKey(T value) {
    return value;
  }
}
