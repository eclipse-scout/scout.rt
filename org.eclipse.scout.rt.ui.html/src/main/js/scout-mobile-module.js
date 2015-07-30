// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//

__include("jquery/jquery-scout.js");
// protects $ and undefined from being redefined by another library
(function(scout, $, undefined) {
__include("scout/main.js");
__include("scout/objectFactories.js");
__include("scout/widget/Widget.js");
// Basic utilities
__include("scout/util/arrays.js");
__include("scout/util/dates.js");
__include("scout/util/defaultValues.js");
__include("scout/util/DetachHelper.js");
__include("scout/util/Device.js");
__include("scout/util/dragAndDrop.js");
__include("scout/util/EventSupport.js");
__include("scout/util/helpers.js");
__include("scout/util/icons.js");
__include("scout/util/keys.js");
__include("scout/util/KeyStrokeManager.js");
__include("scout/util/FocusManager.js");
__include("scout/util/FocusContext.js");
__include("scout/util/logging.js");
__include("scout/util/MimeTypes.js");
__include("scout/util/ModelAdapter.js");
__include("scout/util/numbers.js");
__include("scout/util/ObjectFactory.js");
__include("scout/util/objects.js");
__include("scout/util/polyfills.js");
__include("scout/util/status.js");
__include("scout/util/strings.js");
__include("scout/util/URL.js");
__include("scout/util/GlassPaneRenderer.js");
// Session related
__include("scout/session/Event.js");
__include("scout/session/Locale.js");
__include("scout/session/Reconnector.js");
__include("scout/session/Session.js");
__include("scout/session/UserAgent.js");
// Basic layout
__include("scout/layout/graphics.js");
__include("scout/layout/AbstractLayout.js");
__include("scout/layout/HtmlComponent.js");
__include("scout/layout/LayoutConstants.js");
__include("scout/layout/LayoutValidator.js");
__include("scout/layout/LogicalGridData.js");
__include("scout/layout/LogicalGridLayout.js");
__include("scout/layout/LogicalGridLayoutInfo.js");
__include("scout/layout/NullLayout.js");
__include("scout/layout/SingleLayout.js");
// Misc. elements
__include("scout/datamodel/DataModel.js");
__include("scout/scrollbar/Scrollbar.js");
__include("scout/scrollbar/scrollbars.js");
__include("scout/splitter/Splitter.js");
__include("scout/text/DateFormat.js");
__include("scout/text/DecimalFormat.js");
__include("scout/text/Texts.js");
__include("scout/tooltip/Tooltip.js");
__include("scout/util/tooltips.js");
__include("scout/action/Action.js");
__include("scout/action/keystroke/AbstractKeyStrokeAdapter.js");
__include("scout/action/keystroke/KeyStroke.js");
__include("scout/action/keystroke/ContextMenuKeyStroke.js");
__include("scout/action/keystroke/MnemonicKeyStroke.js");
__include("scout/action/keystroke/GlassPaneKeyStrokeAdapter.js");
__include("scout/popup/Popup.js");
__include("scout/popup/PopupWithHead.js");
__include("scout/popup/PopupCloseKeyStroke.js");
__include("scout/popup/PopupKeyStrokeAdapter.js");
__include("scout/datepicker/DatePicker.js");
__include("scout/datepicker/DatePickerPopup.js");
__include("scout/menu/menus.js");
__include("scout/menu/Menu.js");
__include("scout/menu/MenuBar.js");
__include("scout/menu/MenuBarKeyStrokeAdapter.js");
__include("scout/menu/MenuBarLayout.js");
__include("scout/menu/MenuKeyStrokeAdapter.js");
__include("scout/menu/MenuItemsOrder.js");
__include("scout/menu/ContextMenuPopup.js");
__include("scout/menu/ContextMenuKeyStrokeAdapter.js");
__include("scout/menu/MenuControlKeyStrokes.js");
__include("scout/menu/MenuPopupCloseKeyStrokes.js");
__include("scout/menu/MenuBarPopup.js");
__include("scout/menu/ButtonAdapterMenu.js");
__include("scout/calendar/Calendar.js");
__include("scout/calendar/CalendarComponent.js");
__include("scout/calendar/CalendarListComponent.js");
__include("scout/calendar/CalendarLayout.js");
__include("scout/calendar/Range.js");
__include("scout/calendar/YearPanel.js");
__include("scout/planner/Planner.js");
__include("scout/planner/PlannerHeader.js");
__include("scout/planner/PlannerLayout.js");
__include("scout/planner/PlannerMenuItemsOrder.js");
__include("scout/filechooser/FileChooser.js");
__include("scout/filechooser/FileChooserController.js");
// Table
__include("scout/table/Column.js");
__include("scout/table/BeanColumn.js"); // requires Column.js
__include("scout/table/CheckBoxColumn.js"); // requires Column.js
__include("scout/table/control/TableControl.js");
__include("scout/table/control/AnalysisTableControl.js"); // requires TableControl.js
__include("scout/table/control/ChartTableControl.js"); // requires TableControl.js
__include("scout/table/control/ChartTableControlMatrix.js"); // requires TableControl.js
__include("scout/table/control/GraphTableControl.js"); // requires TableControl.js
__include("scout/table/control/MapTableControl.js"); // requires TableControl.js
__include("scout/table/Table.js");
__include("scout/table/TableFooter.js");
__include("scout/table/TableControlKeyStrokeAdapter.js");
__include("scout/table/TableAdditionalControlsKeyStrokes.js");
__include("scout/table/TableHeader.js");
__include("scout/table/TableHeaderMenu.js");
__include("scout/table/TableKeyStrokeAdapter.js");
__include("scout/table/FilterInputKeyStrokeAdapter.js");
__include("scout/table/TableLayout.js");
__include("scout/table/TableSelectionHandler.js");
__include("scout/table/TableControlKeyStrokes.js");
__include("scout/table/TableFilterControlKeyStrokes.js");
__include("scout/table/TableTooltip.js");
__include("scout/table/editor/CellEditorPopup.js");
__include("scout/table/editor/CellEditorPopupLayout.js");
__include("scout/table/editor/CellEditorPopupKeyStrokeAdapter.js");
// Desktop
__include("scout/tree/Tree.js");
__include("scout/tree/TreeCompact.js");
__include("scout/tree/TreeLayout.js");
__include("scout/tree/TreeCompactLayout.js");
__include("scout/tree/TreeControlKeyStrokes.js");
__include("scout/tree/TreeKeyStrokeAdapter.js");
__include("scout/desktop/BaseDesktop.js");
__include("scout/desktop/Desktop.js");
__include("scout/desktop/DesktopKeyStrokeAdapter.js");
__include("scout/desktop/DesktopNavigationKeyStroke.js");
__include("scout/desktop/DesktopNavigationKeyStrokeAdapter.js");
__include("scout/desktop/DesktopTaskBarLayout.js");
__include("scout/desktop/DesktopViewTab.js");
__include("scout/desktop/SearchFieldKeyStrokeAdapter.js");
__include("scout/desktop/ViewMenuPopup.js");
__include("scout/desktop/ViewMenuPopupKeyStrokeAdapter.js");
__include("scout/desktop/ViewMenuTab.js");
__include("scout/desktop/DesktopTableKeyStrokeAdapter.js");
__include("scout/desktop/ViewTabAutoKeyStroke.js");
__include("scout/desktop/DesktopNavigation.js");
__include("scout/desktop/NullDesktopNavigation.js");
__include("scout/desktop/FormToolButton.js");
__include("scout/desktop/FormToolPopup.js");
__include("scout/desktop/Outline.js");
__include("scout/desktop/OutlineKeyStrokeAdapter.js");
__include("scout/desktop/ViewButton.js");
__include("scout/desktop/ViewButtonsLayout.js");
__include("scout/desktop/OutlineViewButton.js");
__include("scout/desktop/SearchOutline.js");
__include("scout/desktop/ViewTabsController.js");
// Basics for form fields
__include("scout/form/Form.js");
__include("scout/form/FormLayout.js");
__include("scout/form/DialogLayout.js");
__include("scout/form/fields/fields.js");
__include("scout/form/fields/AppLinkKeyStroke.js");
__include("scout/form/fields/FormField.js");
__include("scout/form/fields/FormFieldKeyStrokeAdapter.js");
__include("scout/form/fields/FormFieldLayout.js");
__include("scout/form/fields/CompositeField.js");
__include("scout/form/fields/ValueField.js");
__include("scout/form/FormController.js");
// Basics for message boxes
__include("scout/messagebox/MessageBox.js");
__include("scout/messagebox/MessageBoxButtons.js");
__include("scout/messagebox/MessageBoxControlKeyStrokes.js");
__include("scout/messagebox/MessageBoxKeyStrokeAdapter.js");
__include("scout/messagebox/MessageBoxModelAdapter.js");
__include("scout/messagebox/MessageBoxController.js");
// Form fields (A-Z)
__include("scout/form/fields/beanfield/BeanField.js");
__include("scout/form/fields/browserfield/BrowserField.js");
__include("scout/form/fields/button/Button.js");
__include("scout/form/fields/button/ButtonLayout.js");
__include("scout/form/fields/button/ButtonMnemonicKeyStroke.js");
__include("scout/form/fields/button/ButtonKeyStroke.js");
__include("scout/form/fields/button/ButtonKeyStrokeAdapter.js");
__include("scout/form/fields/calendarfield/CalendarField.js");
__include("scout/form/fields/checkbox/CheckBoxField.js");
__include("scout/form/fields/checkbox/CheckBoxKeyStrokeAdapter.js");
__include("scout/form/fields/clipboardfield/ClipboardField.js");
__include("scout/form/fields/colorfield/ColorField.js");
__include("scout/form/fields/datefield/DateField.js");
__include("scout/form/fields/datefield/DateFieldKeyStrokeAdapter.js");
__include("scout/form/fields/datefield/DateTimeCompositeLayout.js");
__include("scout/form/fields/filechooserfield/FileChooserField.js");
__include("scout/form/fields/graphfield/GraphField.js");
__include("scout/form/fields/groupbox/GroupBox.js");
__include("scout/form/fields/groupbox/GroupBoxKeyStrokeAdapter.js");
__include("scout/form/fields/groupbox/GroupBoxLayout.js");
__include("scout/form/fields/groupbox/GroupBoxMenuItemsOrder.js");
__include("scout/form/fields/htmlfield/HtmlField.js");
__include("scout/form/fields/htmlfield/HtmlFieldLayout.js");
__include("scout/form/fields/imagefield/ImageField.js");
__include("scout/form/fields/imagefield/ImageFieldLayout.js");
__include("scout/form/fields/labelfield/LabelField.js");
__include("scout/form/fields/listbox/ListBox.js");
__include("scout/form/fields/listbox/ListBoxLayout.js");
__include("scout/form/fields/mailfield/MailField.js");
__include("scout/form/fields/numberfield/NumberField.js");
__include("scout/form/fields/placeholder/PlaceholderField.js");
__include("scout/form/fields/plannerfield/PlannerField.js");
__include("scout/form/fields/radiobutton/RadioButton.js");
__include("scout/form/fields/radiobutton/RadioButtonGroup.js");
__include("scout/form/fields/radiobutton/RadioButtonGroupKeyStrokeAdapter.js");
__include("scout/form/fields/richtextfield/RichTextField.js");
__include("scout/form/fields/richtextfield/RichTextFieldKeyStrokeAdapter.js");
__include("scout/form/fields/sequencebox/SequenceBox.js");
__include("scout/form/fields/smartfield/SmartField.js");
__include("scout/form/fields/smartfield/SmartFieldLayout.js");
__include("scout/form/fields/smartfield/SmartFieldKeyStrokeAdapter.js");
__include("scout/form/fields/smartfield/SmartFieldPopup.js");
__include("scout/form/fields/smartfield/SmartFieldPopupLayout.js");
__include("scout/form/fields/smartfield/ProposalChooser.js");
__include("scout/form/fields/smartfield/ProposalChooserLayout.js");
__include("scout/form/fields/smartfield/SmartFieldMultiline.js");
__include("scout/form/fields/smartfield/SmartFieldMultilineLayout.js");
__include("scout/form/fields/splitbox/SplitBox.js");
__include("scout/form/fields/splitbox/SplitBoxLayout.js");
__include("scout/form/fields/stringfield/StringField.js");
__include("scout/form/fields/stringfield/StringFieldKeyStrokeAdapter.js");
__include("scout/form/fields/tabbox/TabAreaLayout.js");
__include("scout/form/fields/tabbox/TabBox.js");
__include("scout/form/fields/tabbox/TabItemMnemonicKeyStroke.js");
__include("scout/form/fields/tabbox/TabBoxLayout.js");
__include("scout/form/fields/tabbox/TabItem.js");
__include("scout/form/fields/tablefield/TableField.js");
__include("scout/form/fields/tagcloudfield/TagCloudField.js");
__include("scout/form/fields/treebox/TreeBox.js");
__include("scout/form/fields/treebox/TreeBoxLayout.js");
__include("scout/form/fields/treefield/TreeField.js");
__include("scout/form/fields/wizard/WizardProgressField.js");
__include("scout/form/fields/wrappedform/WrappedFormField.js");
// More misc. elements
__include("scout/desktop/AbstractNavigationButton.js"); // requires Button.js
__include("scout/desktop/NavigateDownButton.js");
__include("scout/desktop/NavigateUpButton.js");

__include("scout/mobileObjectFactories.js");
__include("scout/table/MobileTable.js");
__include("scout/desktop/MobileDesktop.js");
__include("scout/desktop/MobileOutline.js");
}(window.scout = window.scout || {}, jQuery));
