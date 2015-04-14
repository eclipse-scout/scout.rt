/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.internal;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldExtensions;

public class SwingPlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanManagerPrepared) {
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.button.IButton.class, org.eclipse.scout.rt.ui.swing.form.fields.button.ButtonFactory.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.chartbox.IChartBox.class, org.eclipse.scout.rt.ui.swing.form.fields.chartbox.SwingScoutChartBox.class);
      // Use rayo version for now
      // FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField.class, org.eclipse.scout.rt.ui.swing.form.fields.datefield.DateFieldFactory.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField.class, org.eclipse.scout.rt.ui.swing.form.fields.checkbox.CheckBoxFactory.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField.class, org.eclipse.scout.rt.ui.swing.form.fields.filechooserfield.SwingScoutFileChooserField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox.class, org.eclipse.scout.rt.ui.swing.form.fields.groupbox.SwingScoutGroupBox.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField.class, org.eclipse.scout.rt.ui.swing.form.fields.htmlfield.SwingScoutHtmlField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField.class, org.eclipse.scout.rt.ui.swing.form.fields.imagebox.SwingScoutImageField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField.class, org.eclipse.scout.rt.ui.swing.form.fields.labelfield.SwingScoutLabelField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox.class, org.eclipse.scout.rt.ui.swing.form.fields.listbox.SwingScoutListBox.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField.class, org.eclipse.scout.rt.ui.swing.form.fields.numberfield.SwingScoutNumberField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.mailfield.IMailField.class, org.eclipse.scout.rt.ui.swing.form.fields.mailfield.SwingScoutMailField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup.class, org.eclipse.scout.rt.ui.swing.form.fields.radiobuttongroup.SwingScoutRadioButtonGroup.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox.class, org.eclipse.scout.rt.ui.swing.form.fields.rangebox.SwingScoutSequenceBox.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField.class, org.eclipse.scout.rt.ui.swing.form.fields.smartfield.SwingScoutSmartField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox.class, org.eclipse.scout.rt.ui.swing.form.fields.tabbox.SwingScoutTabBox.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField.class, org.eclipse.scout.rt.ui.swing.form.fields.tablefield.SwingScoutTableField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField.class, org.eclipse.scout.rt.ui.swing.internal.StringFieldFactory.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox.class, org.eclipse.scout.rt.ui.swing.form.fields.treebox.SwingScoutTreeBox.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField.class, org.eclipse.scout.rt.ui.swing.form.fields.treefield.SwingScoutTreeField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField.class, org.eclipse.scout.rt.ui.swing.form.fields.composer.SwingScoutComposerField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField.class, org.eclipse.scout.rt.ui.swing.form.fields.calendarfield.SwingScoutCalendarField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField.class, org.eclipse.scout.rt.ui.swing.form.fields.plannerfield.SwingScoutPlannerField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField.class, org.eclipse.scout.rt.ui.swing.form.fields.wrappedformfield.SwingScoutWrappedFormField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox.class, org.eclipse.scout.rt.ui.swing.form.fields.splitbox.SwingScoutSplitBox.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField.class, org.eclipse.scout.rt.ui.swing.form.fields.placeholder.SwingScoutPlaceholderField.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.IFormField.class, org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFormFieldPlaceholder.class);
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.colorfield.IColorField.class, org.eclipse.scout.rt.ui.swing.form.fields.colorfield.SwingScoutColorField.class);
    }
  }
}
