package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineTableFieldChains.OutlineTableFieldTableTitleChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.ITableFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;

public interface IOutlineTableFieldExtension<OWNER extends AbstractOutlineTableField> extends ITableFieldExtension<ITable, OWNER> {

  void execTableTitleChanged(OutlineTableFieldTableTitleChangedChain chain);
}
