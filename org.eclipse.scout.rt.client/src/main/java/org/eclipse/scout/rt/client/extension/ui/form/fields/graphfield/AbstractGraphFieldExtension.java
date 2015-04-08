package org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield;

import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield.GraphFieldChains.GraphFieldHyperlinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.graphfield.AbstractGraphField;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;

public abstract class AbstractGraphFieldExtension<OWNER extends AbstractGraphField> extends AbstractValueFieldExtension<GraphModel, OWNER> implements IGraphFieldExtension<OWNER> {

  public AbstractGraphFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execHyperlinkAction(GraphFieldHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException {
    chain.execHyperlinkAction(url, path, local);
  }
}
