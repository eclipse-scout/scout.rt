package org.eclipse.scout.svg.ui.rap;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.browser.Browser;

public interface IRwtScoutSvgComposite<T extends IFormField> extends IRwtScoutFormField<T> {
  @Override
  Browser getUiField();
}
