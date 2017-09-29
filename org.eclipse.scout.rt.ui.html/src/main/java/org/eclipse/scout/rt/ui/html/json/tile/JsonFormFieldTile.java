package org.eclipse.scout.rt.ui.html.json.tile;

import org.eclipse.scout.rt.client.ui.tile.IFormFieldTile;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * @since 7.1
 */
public class JsonFormFieldTile<T extends IFormFieldTile> extends JsonWidgetTile<T> {

  public JsonFormFieldTile(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "FormFieldTile";
  }

}
