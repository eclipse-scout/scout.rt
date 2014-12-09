package org.eclipse.scout.rt.client.extension.ui.form.fields.chartbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.chartbox.AbstractChartBox;

public abstract class AbstractChartBoxExtension<OWNER extends AbstractChartBox> extends AbstractFormFieldExtension<OWNER> implements IChartBoxExtension<OWNER> {

  public AbstractChartBoxExtension(OWNER owner) {
    super(owner);
  }
}
