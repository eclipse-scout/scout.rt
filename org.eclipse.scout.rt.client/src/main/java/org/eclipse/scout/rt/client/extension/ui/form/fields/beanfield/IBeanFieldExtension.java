package org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield.BeanFieldChains.BeanFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.beanfield.AbstractBeanField;

public interface IBeanFieldExtension<VALUE, OWNER extends AbstractBeanField<VALUE>> extends IValueFieldExtension<VALUE, OWNER> {

  void execAppLinkAction(BeanFieldAppLinkActionChain<VALUE> chain, String ref) throws ProcessingException;
}
