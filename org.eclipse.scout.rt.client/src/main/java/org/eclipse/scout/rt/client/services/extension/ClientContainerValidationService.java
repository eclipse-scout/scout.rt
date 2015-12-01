/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.extension;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.AbstractContainerValidationService;
import org.eclipse.scout.rt.shared.extension.IMoveModelObjectToRootMarker;

@Order(5100)
public class ClientContainerValidationService extends AbstractContainerValidationService {

  @PostConstruct
  protected void initializeContributions() {
    // contributions
    addPossibleContributionForContainer(IFormHandler.class, AbstractForm.class);
    addPossibleContributionForContainer(IActionNode.class, AbstractActionNode.class);
    addPossibleContributionForContainer(IMenu.class, AbstractPlanner.class);
    addPossibleContributionForContainer(ICalendarItemProvider.class, AbstractCalendar.class);
    addPossibleContributionForContainer(IMenu.class, AbstractCalendar.class);
    addPossibleContributionForContainer(IMenu.class, AbstractCalendarItemProvider.class);
    addPossibleContributionForContainer(IColumn.class, AbstractTable.class);
    addPossibleContributionForContainer(IKeyStroke.class, AbstractTable.class);
    addPossibleContributionForContainer(IMenu.class, AbstractTable.class);
    addPossibleContributionForContainer(IKeyStroke.class, AbstractTree.class);
    addPossibleContributionForContainer(IMenu.class, AbstractTree.class);
    addPossibleContributionForContainer(IMenu.class, AbstractTreeNode.class);
    addPossibleContributionForContainer(IOutline.class, AbstractDesktop.class);
    addPossibleContributionForContainer(IAction.class, AbstractDesktop.class);
    addPossibleContributionForContainer(ISearchForm.class, AbstractPageWithTable.class);
    addPossibleContributionForContainer(ITable.class, AbstractPageWithTable.class);
    addPossibleContributionForContainer(IGroupBox.class, AbstractForm.class);
    addPossibleContributionForContainer(IFormField.class, AbstractCompositeField.class);
    addPossibleContributionForContainer(IKeyStroke.class, AbstractFormField.class);
    addPossibleContributionForContainer(IMenu.class, AbstractValueField.class);
    addPossibleContributionForContainer(IMenu.class, AbstractButton.class);
    addPossibleContributionForContainer(ICalendar.class, AbstractCalendarField.class);
    addPossibleContributionForContainer(ITree.class, AbstractComposerField.class);
    addPossibleContributionForContainer(IMenu.class, AbstractImageField.class);
    addPossibleContributionForContainer(IFormField.class, AbstractListBox.class);
    addPossibleContributionForContainer(ITable.class, AbstractListBox.class);
    addPossibleContributionForContainer(IPlanner.class, AbstractPlannerField.class);
    addPossibleContributionForContainer(IFormField.class, AbstractRadioButtonGroup.class);
    addPossibleContributionForContainer(ITable.class, AbstractTableField.class);
    addPossibleContributionForContainer(IFormField.class, AbstractTreeBox.class);
    addPossibleContributionForContainer(ITree.class, AbstractTreeBox.class);
    addPossibleContributionForContainer(ITree.class, AbstractTreeField.class);
    addPossibleContributionForContainer(IWizardStep.class, AbstractWizard.class);
    addPossibleContributionForContainer(IMenu.class, AbstractGroupBox.class);

    // moves
    addPossibleMoveForContainer(IFormField.class, ICompositeField.class);
    addPossibleMoveForContainer(IFormField.class, IMoveModelObjectToRootMarker.class);
    addPossibleMoveForContainer(IActionNode.class, IActionNode.class);
    addPossibleMoveForContainer(IActionNode.class, IMoveModelObjectToRootMarker.class);
  }
}
