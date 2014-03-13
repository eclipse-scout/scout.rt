/**
 *
 */
package org.eclipse.scout.rt.ui.swt.action;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;

public class SwtScoutActionContributionItem extends AbstractSwtScoutAction {

  private IContributionItem m_contributionItem;

  public SwtScoutActionContributionItem(IAction scoutAction, ISwtEnvironment environment, IContributionItem contributionItem) {
    this(scoutAction, environment, contributionItem, true);
  }

  public SwtScoutActionContributionItem(IAction scoutAction, ISwtEnvironment environment, IContributionItem contributionItem, boolean attachScout) {
    super(scoutAction, environment, false);
    m_contributionItem = contributionItem;
    if (attachScout) {
      attachScout();
    }
  }

  @Override
  protected void updateUi() {
    m_contributionItem.update();
  }
}
