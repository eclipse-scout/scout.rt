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
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ISmartColumnExtension;

@ClassId("c333e52d-4678-41b3-8307-01d473864d2e")
public abstract class AbstractSmartColumn<VALUE> extends AbstractMixedSmartColumn<VALUE, VALUE> implements ISmartColumn<VALUE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSmartColumn.class);

  public AbstractSmartColumn() {
    super();
  }

  @Override
  protected final VALUE execConvertKeyToValue(VALUE key) {
    return key;
  }

  @Override
  protected final VALUE execConvertValueToKey(VALUE value) {
    return value;
  }

  protected static class LocalSmartColumnExtension<VALUE, OWNER extends AbstractSmartColumn<VALUE>> extends LocalMixedSmartColumnExtension<VALUE, VALUE, OWNER> implements ISmartColumnExtension<VALUE, OWNER> {

    public LocalSmartColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ISmartColumnExtension<VALUE, ? extends AbstractSmartColumn<VALUE>> createLocalExtension() {
    return new LocalSmartColumnExtension<VALUE, AbstractSmartColumn<VALUE>>(this);
  }
}
