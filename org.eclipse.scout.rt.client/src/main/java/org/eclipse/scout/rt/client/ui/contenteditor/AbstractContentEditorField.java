package org.eclipse.scout.rt.client.ui.contenteditor;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("182a9023-67f0-4e15-b7cd-3453fc64a8dd")
public abstract class AbstractContentEditorField extends AbstractFormField implements IContentEditorField {

  @Override
  public void setContent(String content) {
    propertySupport.setPropertyString(PROP_CONTENT, content);
  }

  @Override
  public String getContent() {
    return propertySupport.getPropertyString(PROP_CONTENT);
  }
}
