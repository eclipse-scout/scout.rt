package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineTableFieldChains.OutlineTableFieldTableTitleChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.AbstractTableFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;

public abstract class AbstractOutlineTableFieldExtension<OWNER extends AbstractOutlineTableField> extends AbstractTableFieldExtension<ITable, OWNER> implements IOutlineTableFieldExtension<OWNER> {

  public AbstractOutlineTableFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execTableTitleChanged(OutlineTableFieldTableTitleChangedChain chain) {
    chain.execTableTitleChanged();
  }
}
