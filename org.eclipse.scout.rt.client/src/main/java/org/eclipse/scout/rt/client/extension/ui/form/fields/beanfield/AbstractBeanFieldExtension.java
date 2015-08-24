package org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield.BeanFieldChains.BeanFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.beanfield.AbstractBeanField;

public abstract class AbstractBeanFieldExtension<VALUE, OWNER extends AbstractBeanField<VALUE>> extends AbstractValueFieldExtension<VALUE, OWNER>implements IBeanFieldExtension<VALUE, OWNER> {

  public AbstractBeanFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execAppLinkAction(BeanFieldAppLinkActionChain chain, String ref) throws ProcessingException {
    chain.execAppLinkAction(ref);
  }
}
