package org.eclipse.scout.rt.ui.html.json.form.fields.composer;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonComposerField<COMPOSER_FIELD extends IComposerField> extends JsonFormField<COMPOSER_FIELD> {

  public JsonComposerField(COMPOSER_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ComposerField";
  }

  @Override
  protected void initJsonProperties(COMPOSER_FIELD model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<COMPOSER_FIELD>(ITreeField.PROP_TREE, model, getUiSession()) {
      @Override
      protected ITree modelValue() {
        return getModel().getTree();
      }
    });
  }
}
