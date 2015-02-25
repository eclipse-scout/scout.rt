package org.eclipse.scout.svg.client.extension.svgfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.svg.client.svgfield.AbstractSvgField;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.svg.client.extension.svgfield.SvgFieldChains.SvgFieldClickedChain;
import org.eclipse.scout.svg.client.svgfield.SvgFieldEvent;
import org.eclipse.scout.svg.client.extension.svgfield.SvgFieldChains.SvgFieldHyperlinkChain;

public interface ISvgFieldExtension<OWNER extends AbstractSvgField> extends IFormFieldExtension<OWNER> {

  void execClicked(SvgFieldClickedChain chain, SvgFieldEvent e) throws ProcessingException;

  void execHyperlink(SvgFieldHyperlinkChain chain, SvgFieldEvent e) throws ProcessingException;
}
