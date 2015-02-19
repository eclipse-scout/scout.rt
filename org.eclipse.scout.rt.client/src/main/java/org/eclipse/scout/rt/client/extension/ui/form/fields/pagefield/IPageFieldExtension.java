package org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.AbstractPageField;

public interface IPageFieldExtension<T extends IPage, OWNER extends AbstractPageField<T>> extends IGroupBoxExtension<OWNER> {
}
