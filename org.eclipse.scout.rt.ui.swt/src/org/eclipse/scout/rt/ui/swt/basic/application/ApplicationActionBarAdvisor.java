package org.eclipse.scout.rt.ui.swt.basic.application;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironmentListener;
import org.eclipse.scout.rt.ui.swt.SwtEnvironmentEvent;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutToolbarAction;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * <h3>ApplicationActionBarAdvisor</h3> Used for menu contributions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

  private static IScoutLogger logger = ScoutLogManager.getLogger(ApplicationActionBarAdvisor.class);

  private IActionBarConfigurer m_configurer;
  private ToolBarManager m_toolbar;

  private ISwtEnvironment m_environment;

  public ApplicationActionBarAdvisor(IActionBarConfigurer configurer, ISwtEnvironment environment) {
    super(configurer);
    m_configurer = configurer;
    m_environment = environment;
    m_environment.addEnvironmentListener(new ISwtEnvironmentListener() {

      @Override
      public void environmentChanged(SwtEnvironmentEvent e) {
        if (e.getType() == SwtEnvironmentEvent.STARTED) {
          if (m_toolbar.getControl() != null && !m_toolbar.getControl().isDisposed()) {
            initViewButtons(getEnvironment().getScoutDesktop());
          }
        }
      }
    });
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  @Override
  protected void fillMenuBar(IMenuManager menuBar) {
    menuBar.add(new MenuManager("", IWorkbenchActionConstants.M_FILE));
  }

  public void initViewButtons(IDesktop d) {
    if (d != null) {
      for (IViewButton scoutViewButton : d.getViewButtons()) {
        if (scoutViewButton.isVisible() && scoutViewButton.isVisibleGranted()) {
          SwtScoutToolbarAction swtAction = new SwtScoutToolbarAction(scoutViewButton, m_toolbar, getEnvironment());
          contributeToCoolBar(swtAction);
        }
      }

      for (IToolButton scoutToolButton : d.getToolButtons()) {
        if (scoutToolButton.isVisible() && scoutToolButton.isVisibleGranted()) {
          SwtScoutToolbarAction swtAction = new SwtScoutToolbarAction(scoutToolButton, m_toolbar, getEnvironment());
          contributeToCoolBar(swtAction);
        }
      }

      m_configurer.getCoolBarManager().update(true);
    }
  }

  protected void contributeToCoolBar(Action swtAction) {
    ActionContributionItem contributionItem = new ActionContributionItem(swtAction);
    contributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
    m_toolbar.add(contributionItem);
  }

  @Override
  protected void fillCoolBar(ICoolBarManager coolBar) {
    m_toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    ToolBarContributionItem item = new ToolBarContributionItem(m_toolbar, "main");
    coolBar.add(item);
  }
}
