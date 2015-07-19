package org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield.PageFieldChains.PageFieldPageChangedChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.AbstractPageField;

public interface IPageFieldExtension<PAGE extends IPage, OWNER extends AbstractPageField<PAGE>> extends IGroupBoxExtension<OWNER> {

  void execPageChanged(PageFieldPageChangedChain<PAGE> chain, PAGE oldPage, PAGE newPage) throws ProcessingException;
}
