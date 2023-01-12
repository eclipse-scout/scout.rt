/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.DataModelAggregationFieldChains.DataModelAggregationFieldAttributeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ISmartFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractDataModelAggregationField;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

public interface IDataModelAggregationFieldExtension<OWNER extends AbstractDataModelAggregationField> extends ISmartFieldExtension<Integer, OWNER> {

  void execAttributeChanged(DataModelAggregationFieldAttributeChangedChain chain, IDataModelAttribute attribute);
}
