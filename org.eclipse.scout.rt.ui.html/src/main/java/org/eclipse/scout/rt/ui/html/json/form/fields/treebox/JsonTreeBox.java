package org.eclipse.scout.rt.ui.html.json.form.fields.treebox;

import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

public class JsonTreeBox<T extends ITreeBox> extends JsonValueField<T> {

  public JsonTreeBox(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TreeBox";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getTree());
    attachAdapter(getTreeBoxFilterBoxModel());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "tree", getModel().getTree());
    putAdapterIdProperty(json, "filterBox", getTreeBoxFilterBoxModel());
    return json;
  }

  protected IFormField getTreeBoxFilterBoxModel() {
    List<IFormField> childFields = getModel().getFields();
    if (CollectionUtility.hasElements(childFields)) {
      return CollectionUtility.firstElement(childFields);
    }
    return null;
  }
}
