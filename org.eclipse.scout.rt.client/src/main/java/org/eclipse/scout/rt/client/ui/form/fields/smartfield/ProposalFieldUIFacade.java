/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

public class ProposalFieldUIFacade<VALUE> extends SmartFieldUIFacade<VALUE> implements IProposalFieldUIFacade<VALUE> {

  @SuppressWarnings("unchecked")
  public ProposalFieldUIFacade(AbstractProposalField proposalField) {
    super(proposalField);
  }

  @Override
  public void setValueAsStringFromUI(String value) {
    getProposalField().setValueAsString(value);
  }

  @SuppressWarnings("unchecked")
  public IProposalField<VALUE> getProposalField() {
    return (IProposalField<VALUE>) getSmartField();
  }
}
