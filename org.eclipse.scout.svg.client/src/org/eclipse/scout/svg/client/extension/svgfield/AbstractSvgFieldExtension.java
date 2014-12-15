package org.eclipse.scout.svg.client.extension.svgfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.svg.client.svgfield.AbstractSvgField;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.svg.client.extension.svgfield.SvgFieldChains.SvgFieldClickedChain;
import org.eclipse.scout.svg.client.svgfield.SvgFieldEvent;
import org.eclipse.scout.svg.client.extension.svgfield.SvgFieldChains.SvgFieldHyperlinkChain;

public abstract class AbstractSvgFieldExtension<OWNER extends AbstractSvgField> extends AbstractFormFieldExtension<OWNER> implements ISvgFieldExtension<OWNER> {

  public AbstractSvgFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execClicked(SvgFieldClickedChain chain, SvgFieldEvent e) throws ProcessingException {
    chain.execClicked(e);
  }

  @Override
  public void execHyperlink(SvgFieldHyperlinkChain chain, SvgFieldEvent e) throws ProcessingException {
    chain.execHyperlink(e);
  }
}
