package org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.TabBoxChains.TabBoxTabSelectedChain;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;

public abstract class AbstractTabBoxExtension<OWNER extends AbstractTabBox> extends AbstractCompositeFieldExtension<OWNER> implements ITabBoxExtension<OWNER> {

  public AbstractTabBoxExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execTabSelected(TabBoxTabSelectedChain chain, IGroupBox selectedBox) throws ProcessingException {
    chain.execTabSelected(selectedBox);
  }
}
