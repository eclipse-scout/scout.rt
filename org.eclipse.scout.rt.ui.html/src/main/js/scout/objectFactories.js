/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
// FIXME AWE/CGU: (object-factories) refactoren, so dass alles per "reflection" funktioniert
// für Object-Type "Desktop" wird automagisch scout.Desktop instanziert. Es muss aber noch die
// Möglichkeit angeboten werden, das Default-Verhalten zu übersteuern.
scout.defaultObjectFactories = [{
  objectType: 'Desktop',
  create: function() {
    return new scout.Desktop();
  }
}, {
  objectType: 'SearchOutline',
  create: function() {
    return new scout.SearchOutline();
  }
}, {
  objectType: 'Outline',
  create: function() {
    return new scout.Outline();
  }
}, {
  objectType: 'OutlineOverview',
  create: function() {
    return new scout.OutlineOverview();
  }
}, {
  objectType: 'ViewButton',
  create: function() {
    return new scout.ViewButton();
  }
}, {
  objectType: 'OutlineViewButton',
  create: function() {
    return new scout.OutlineViewButton();
  }
}, {
  objectType: 'NavigateDownMenu',
  create: function() {
    return new scout.NavigateDownMenu();
  }
}, {
  objectType: 'NavigateUpMenu',
  create: function() {
    return new scout.NavigateUpMenu();
  }
}, {
  objectType: 'FormToolButton',
  create: function() {
    return new scout.FormToolButton();
  }
}, {
  objectType: 'AggregateTableControl',
  create: function() {
    return new scout.AggregateTableControl();
  }
}, {
  objectType: 'Table',
  create: function() {
    return new scout.Table();
  }
}, {
  objectType: 'Column',
  create: function() {
    return new scout.Column();
  }
}, {
  objectType: 'BooleanColumn',
  create: function() {
    return new scout.CheckBoxColumn();
  }
}, {
  objectType: 'BeanColumn',
  create: function() {
    return new scout.BeanColumn();
  }
}, {
  objectType: 'DateColumn',
  create: function() {
    return new scout.DateColumn();
  }
}, {
  objectType: 'IconColumn',
  create: function() {
    return new scout.IconColumn();
  }
}, {
  objectType: 'TableControl',
  create: function() {
    return new scout.TableControl();
  }
}, {
  objectType: 'ColumnUserFilter',
  create: function() {
    return new scout.ColumnUserFilter();
  }
}, {
  objectType: 'TableTextUserFilter',
  create: function() {
    return new scout.TableTextUserFilter();
  }
}, {
  objectType: 'Tree',
  create: function() {
    return new scout.Tree();
  }
}, {
  objectType: 'Tree.Compact',
  create: function() {
    return new scout.TreeCompact();
  }
}, {
  objectType: 'Form',
  create: function() {
    return new scout.Form();
  }
}, {
  objectType: 'MessageBox',
  create: function() {
    return new scout.MessageBox();
  }
}, {
  objectType: 'Action',
  create: function() {
    return new scout.Action();
  }
}, {
  objectType: 'Menu',
  create: function() {
    return new scout.Menu();
  }
}, {
  objectType: 'FileChooserField',
  create: function() {
    return new scout.FileChooserField();
  }
}, {
  objectType: 'BrowserField',
  create: function() {
    return new scout.BrowserField();
  }
}, {
  objectType: 'ColorField',
  create: function() {
    return new scout.ColorField();
  }
}, {
  objectType: 'Button',
  create: function() {
    return new scout.Button();
  }
}, {
  objectType: 'CheckBoxField',
  create: function() {
    return new scout.CheckBoxField();
  }
}, {
  objectType: 'RadioButtonGroup',
  create: function() {
    return new scout.RadioButtonGroup();
  }
}, {
  objectType: 'RadioButton',
  create: function() {
    return new scout.RadioButton();
  }
}, {
  objectType: 'LabelField',
  create: function() {
    return new scout.LabelField();
  }
}, {
  objectType: 'ImageField',
  create: function() {
    return new scout.ImageField();
  }
}, {
  objectType: 'NumberField',
  create: function() {
    return new scout.NumberField();
  }
}, {
  objectType: 'StringField',
  create: function() {
    return new scout.StringField();
  }
}, {
  objectType: 'SmartField',
  create: function(model) {
    return new scout.SmartField();
  }
}, {
  objectType: 'SmartFieldMultiline',
  create: function(model) {
    return new scout.SmartFieldMultiline();
  }
}, {
  objectType: 'DateField',
  create: function() {
    return new scout.DateField();
  }
}, {
  objectType: 'TableField',
  create: function() {
    return new scout.TableField();
  }
}, {
  objectType: 'ListBox',
  create: function() {
    return new scout.ListBox();
  }
}, {
  objectType: 'TreeField',
  create: function() {
    return new scout.TreeField();
  }
}, {
  objectType: 'TreeBox',
  create: function() {
    return new scout.TreeBox();
  }
}, {
  objectType: 'GroupBox',
  create: function() {
    return new scout.GroupBox();
  }
}, {
  objectType: 'TabBox',
  create: function() {
    return new scout.TabBox();
  }
}, {
  objectType: 'TabItem',
  create: function() {
    return new scout.TabItem();
  }
}, {
  objectType: 'SequenceBox',
  create: function() {
    return new scout.SequenceBox();
  }
}, {
  objectType: 'Calendar',
  create: function() {
    return new scout.Calendar();
  }
}, {
  objectType: 'CalendarComponent',
  create: function() {
    return new scout.CalendarComponent();
  }
}, {
  objectType: 'CalendarField',
  create: function() {
    return new scout.CalendarField();
  }
}, {
  objectType: 'Planner',
  create: function() {
    return new scout.Planner();
  }
}, {
  objectType: 'PlannerField',
  create: function() {
    return new scout.PlannerField();
  }
}, {
  objectType: 'WrappedFormField',
  create: function() {
    return new scout.WrappedFormField();
  }
}, {
  objectType: 'SplitBox',
  create: function() {
    return new scout.SplitBox();
  }
}, {
  objectType: 'PlaceholderField',
  create: function() {
    return new scout.PlaceholderField();
  }
}, {
  objectType: 'WizardProgressField',
  create: function() {
    return new scout.WizardProgressField();
  }
}, {
  objectType: 'HtmlField',
  create: function() {
    return new scout.HtmlField();
  }
}, {
  objectType: 'KeyStroke',
  create: function() {
    return new scout.Action(); // a model keystroke is represented as an Action
  }
}, {
  objectType: 'ProposalChooser',
  create: function() {
    return new scout.ProposalChooser();
  }
}, {
  objectType: 'ComposerField',
  create: function() {
    // Composer is actually just a tree field, there is currently no need to duplicate the js/css code
    return new scout.TreeField();
  }
}, {
  objectType: 'BeanField',
  create: function() {
    return new scout.BeanField();
  }
}, {
  objectType: 'FileChooser',
  create: function() {
    return new scout.FileChooser();
  }
}, {
  objectType: 'ButtonAdapterMenu',
  create: function() {
    return new scout.ButtonAdapterMenu();
  }
}, {
  objectType: 'ClipboardField',
  create: function() {
    return new scout.ClipboardField();
  }
}, {
  objectType: 'FilterFieldsGroupBox',
  create: function() {
    return new scout.FilterFieldsGroupBox();
  }
}];
