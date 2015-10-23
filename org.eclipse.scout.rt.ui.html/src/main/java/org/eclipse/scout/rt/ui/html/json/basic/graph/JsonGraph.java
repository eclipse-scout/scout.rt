package org.eclipse.scout.rt.ui.html.json.basic.graph;

import org.eclipse.scout.rt.client.ui.basic.graph.IGraph;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

/**
 * @since 5.2
 */
public class JsonGraph<CHART extends IGraph> extends AbstractJsonPropertyObserver<CHART> {

  public static final String EVENT_NODE_ACTION = "nodeAction";

  public JsonGraph(CHART model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Graph";
  }

  @Override
  protected void initJsonProperties(CHART model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IGraph>(IGraph.PROP_AUTO_COLOR, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoColor();
      }
    });
    putJsonProperty(new JsonProperty<IGraph>(IGraph.PROP_GRAPH_MODEL, model) {
      @Override
      protected GraphModel modelValue() {
        return getModel().getGraphModel();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonGraphModel.toJson((GraphModel) value);
      }
    });
    putJsonProperty(new JsonProperty<IGraph>(IGraph.PROP_ENABLED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IGraph>(IGraph.PROP_VISIBLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isVisible();
      }
    });
    putJsonProperty(new JsonProperty<IGraph>(IGraph.PROP_CLICKABLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isClickable();
      }
    });
    putJsonProperty(new JsonProperty<IGraph>(IGraph.PROP_MODEL_HANDLES_CLICK, model) {
      @Override
      protected Object modelValue() {
        return getModel().isModelHandlesClick();
      }
    });
    putJsonProperty(new JsonProperty<IGraph>(IGraph.PROP_ANIMATED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isAnimated();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_NODE_ACTION.equals(event.getType())) {
      handleUiNodeAction(event);
    }
    else if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiNodeAction(JsonEvent event) {
    // TODO BSH Retrieve node
    getModel().getUIFacade().fireNodeActionFromUI(null);
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().getString("ref");
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }
}
