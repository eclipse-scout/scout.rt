package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.ui.html.IUiSession;

/**
 * @since 7.1
 */
public abstract class AbstractJsonWidget<T extends IWidget> extends AbstractJsonPropertyObserver<T> {

  public AbstractJsonWidget(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Widget";
  }
}
