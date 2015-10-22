package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;

public class NodePageWithForm extends AbstractPageWithNodes {

  @Override
  protected String getConfiguredTitle() {
    return "Node";
  }

  @Override
  protected void execPageActivated() {
    if (getDetailForm() == null) {
      FormWithOneField form = new FormWithOneField();
      form.setAllEnabled(false);
      setDetailForm(form);
      form.start();
    }
  }

  @Override
  protected void execDisposePage() {
    if (getDetailForm() != null) {
      getDetailForm().doClose();
      setDetailForm(null);
    }
  }
}
