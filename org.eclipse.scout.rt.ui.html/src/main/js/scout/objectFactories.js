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
  objectType: 'FormToolButton',
  create: function() {
    return new scout.FormToolButton();
  }
}, {
  objectType: 'DataModel',
  create: function() {
    return new scout.DataModel();
  }
}, {
  objectType: 'ChartTableControl',
  create: function() {
    return new scout.ChartTableControl();
  }
}, {
  objectType: 'MapTableControl',
  create: function() {
    return new scout.MapTableControl();
  }
}, {
  objectType: 'GraphTableControl',
  create: function() {
    return new scout.GraphTableControl();
  }
}, {
  objectType: 'AnalysisTableControl',
  create: function() {
    return new scout.AnalysisTableControl();
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
},  {
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
  objectType: 'UserTableFilter',
  create: function() {
    return new scout.UserTableFilter();
  }
}, {
  objectType: 'ColumnUserTableFilter',
  create: function() {
    return new scout.ColumnUserTableFilter();
  }
}, {
  objectType: 'TextUserTableFilter',
  create: function() {
    return new scout.TextUserTableFilter();
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
  objectType: 'MailField',
  create: function() {
    return new scout.MailField();
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
  objectType: 'RichTextField',
  create: function() {
    return new scout.RichTextField();
  }
}, {
  objectType: 'TagCloudField',
  create: function() {
    return new scout.TagCloudField();
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
  objectType: 'GraphField',
  create: function() {
    return new scout.GraphField();
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
}];
