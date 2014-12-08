package org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.TabBoxChains.TabBoxTabSelectedChain;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;

public interface ITabBoxExtension<OWNER extends AbstractTabBox> extends ICompositeFieldExtension<OWNER> {

  void execTabSelected(TabBoxTabSelectedChain chain, IGroupBox selectedBox) throws ProcessingException;
}
