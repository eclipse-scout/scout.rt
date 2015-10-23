package org.eclipse.scout.rt.ui.html.json.form.fields.graphfield;

import org.eclipse.scout.rt.client.ui.basic.graph.IGraph;
import org.eclipse.scout.rt.client.ui.form.fields.graphfield.IGraphField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonGraphField<GRAPH_FIELD extends IGraphField> extends JsonFormField<GRAPH_FIELD> {

  public JsonGraphField(GRAPH_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "GraphField";
  }

  @Override
  protected void initJsonProperties(GRAPH_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<GRAPH_FIELD>(IGraphField.PROP_GRAPH, model, getUiSession()) {
      @Override
      protected IGraph modelValue() {
        return getModel().getGraph();
      }
    });
  }
}
