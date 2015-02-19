package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.DataModelAggregationFieldChains.DataModelAggregationFieldAttributeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ISmartFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractDataModelAggregationField;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

public interface IDataModelAggregationFieldExtension<OWNER extends AbstractDataModelAggregationField> extends ISmartFieldExtension<Integer, OWNER> {

  void execAttributeChanged(DataModelAggregationFieldAttributeChangedChain chain, IDataModelAttribute attribute) throws ProcessingException;
}
