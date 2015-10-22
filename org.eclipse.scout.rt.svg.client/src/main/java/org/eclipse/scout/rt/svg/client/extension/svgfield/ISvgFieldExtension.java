package org.eclipse.scout.rt.svg.client.extension.svgfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldAppLinkActionChain;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldClickedChain;
import org.eclipse.scout.rt.svg.client.svgfield.AbstractSvgField;
import org.eclipse.scout.rt.svg.client.svgfield.SvgFieldEvent;

public interface ISvgFieldExtension<OWNER extends AbstractSvgField> extends IFormFieldExtension<OWNER> {

  void execClicked(SvgFieldClickedChain chain, SvgFieldEvent e);

  void execAppLinkAction(SvgFieldAppLinkActionChain chain, String ref);
}
