package org.eclipse.scout.rt.client.ui.contenteditor;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface IContentEditorField extends IFormField {

  String PROP_CONTENT = "content";

  void setContent(String s);

  String getContent();

}
