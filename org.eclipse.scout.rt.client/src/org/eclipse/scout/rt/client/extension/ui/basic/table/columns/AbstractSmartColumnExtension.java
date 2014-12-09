package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;

public abstract class AbstractSmartColumnExtension<VALUE, OWNER extends AbstractSmartColumn<VALUE>> extends AbstractMixedSmartColumnExtension<VALUE, VALUE, OWNER> implements ISmartColumnExtension<VALUE, OWNER> {

  public AbstractSmartColumnExtension(OWNER owner) {
    super(owner);
  }
}
