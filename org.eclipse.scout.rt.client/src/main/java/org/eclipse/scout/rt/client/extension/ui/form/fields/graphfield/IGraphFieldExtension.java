package org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield;

import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield.GraphFieldChains.GraphFieldHyperlinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.graphfield.AbstractGraphField;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;

public interface IGraphFieldExtension<OWNER extends AbstractGraphField> extends IValueFieldExtension<GraphModel, OWNER> {

  void execHyperlinkAction(GraphFieldHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException;
}
