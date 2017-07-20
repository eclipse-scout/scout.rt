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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

public class ProposalField2UIFacade<VALUE> extends SmartField2UIFacade<VALUE> implements IProposalField2UIFacade<VALUE> {

  @SuppressWarnings("unchecked")
  public ProposalField2UIFacade(AbstractProposalField2 proposalField) {
    super(proposalField);
  }

  @Override
  public void setValueAsStringFromUI(String value) {
    getProposalField().setValueAsString(value);
  }

  @SuppressWarnings("unchecked")
  public IProposalField2<VALUE> getProposalField() {
    return (IProposalField2<VALUE>) getSmartField();
  }

}
