package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.DataModelAggregationFieldChains.DataModelAggregationFieldAttributeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.AbstractSmartFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractDataModelAggregationField;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

public abstract class AbstractDataModelAggregationFieldExtension<OWNER extends AbstractDataModelAggregationField> extends AbstractSmartFieldExtension<Integer, OWNER> implements IDataModelAggregationFieldExtension<OWNER> {

  public AbstractDataModelAggregationFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execAttributeChanged(DataModelAggregationFieldAttributeChangedChain chain, IDataModelAttribute attribute) throws ProcessingException {
    chain.execAttributeChanged(attribute);
  }
}
