package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractProposalColumn;

public abstract class AbstractProposalColumnExtension<LOOKUP_TYPE, OWNER extends AbstractProposalColumn<LOOKUP_TYPE>> extends AbstractContentAssistColumnExtension<String, LOOKUP_TYPE, OWNER> implements IProposalColumnExtension<LOOKUP_TYPE, OWNER> {

  public AbstractProposalColumnExtension(OWNER owner) {
    super(owner);
  }
}
