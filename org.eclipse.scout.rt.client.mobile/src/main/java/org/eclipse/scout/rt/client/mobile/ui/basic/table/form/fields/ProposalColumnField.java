/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form.fields;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IProposalColumn;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;

/**
 * @since 3.9.0
 */
public class ProposalColumnField<T> extends AbstractProposalField<T> implements IColumnWrapper<IProposalColumn<T>> {
  private ProposalColumnFieldPropertyDelegator<T> m_propertyDelegator;

  public ProposalColumnField(IProposalColumn<T> column) {
    super(false);
    m_propertyDelegator = new ProposalColumnFieldPropertyDelegator<T>(column, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  protected void execDisposeField() {
    m_propertyDelegator.dispose();
  }

  @Override
  public IProposalColumn<T> getWrappedObject() {
    return m_propertyDelegator.getSender();
  }

  @Override
  public Class<String> getHolderType() {
    return getWrappedObject().getDataType();
  }
}
