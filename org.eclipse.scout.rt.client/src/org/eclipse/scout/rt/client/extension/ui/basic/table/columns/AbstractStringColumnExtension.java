package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;

public abstract class AbstractStringColumnExtension<OWNER extends AbstractStringColumn> extends AbstractColumnExtension<String, OWNER> implements IStringColumnExtension<OWNER> {

  public AbstractStringColumnExtension(OWNER owner) {
    super(owner);
  }
}
