package org.eclipse.scout.rt.svg.client.extension.svgfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldAppLinkActionChain;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldClickedChain;
import org.eclipse.scout.rt.svg.client.svgfield.AbstractSvgField;
import org.eclipse.scout.rt.svg.client.svgfield.SvgFieldEvent;

public abstract class AbstractSvgFieldExtension<OWNER extends AbstractSvgField> extends AbstractFormFieldExtension<OWNER> implements ISvgFieldExtension<OWNER> {

  public AbstractSvgFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execClicked(SvgFieldClickedChain chain, SvgFieldEvent e) {
    chain.execClicked(e);
  }

  @Override
  public void execAppLinkAction(SvgFieldAppLinkActionChain chain, String ref) {
    chain.execAppLinkAction(ref);
  }
}
