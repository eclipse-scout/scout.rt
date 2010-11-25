/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.spellchecker.forms;

import java.util.Enumeration;
import java.util.Locale;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.spellchecker.ISpellCheckerService;
import org.eclipse.scout.rt.client.services.common.spellchecker.IUserDictionary;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.GeneralSettingsBox;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.ResetToStandardButton;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.UserDictionaryBox;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.GeneralSettingsBox.EnabledField;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.GeneralSettingsBox.IgnoreCaseField;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.GeneralSettingsBox.IgnoreDomainNamesField;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.GeneralSettingsBox.IgnoreWordsWithNumbersField;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.GeneralSettingsBox.LanguageField;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.GeneralSettingsBox.ShortcutField;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.UserDictionaryBox.EditorSequenceBox;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.UserDictionaryBox.UserDictionaryTableField;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.UserDictionaryBox.EditorSequenceBox.EditorButton;
import org.eclipse.scout.rt.client.services.common.spellchecker.forms.SpellCheckerOptionsForm.MainBox.UserDictionaryBox.EditorSequenceBox.EditorField;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.checkbox.AbstractCheckBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.service.SERVICES;

public class SpellCheckerOptionsForm extends AbstractForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SpellCheckerOptionsForm.class);

  public SpellCheckerOptionsForm() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("SC_DialogTitle_Options");
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public GeneralSettingsBox getGeneralSettingsBox() {
    return getFieldByClass(GeneralSettingsBox.class);
  }

  public UserDictionaryBox getUserDictionaryBox() {
    return getFieldByClass(UserDictionaryBox.class);
  }

  public EnabledField getEnabledField() {
    return getFieldByClass(EnabledField.class);
  }

  public ShortcutField getShortcutField() {
    return getFieldByClass(ShortcutField.class);
  }

  public LanguageField getLanguageField() {
    return getFieldByClass(LanguageField.class);
  }

  public IgnoreCaseField getIgnoreCaseField() {
    return getFieldByClass(IgnoreCaseField.class);
  }

  public IgnoreDomainNamesField getIgnoreDomainNamesField() {
    return getFieldByClass(IgnoreDomainNamesField.class);
  }

  public IgnoreWordsWithNumbersField getIgnoreWordsWithNumbersField() {
    return getFieldByClass(IgnoreWordsWithNumbersField.class);
  }

  public UserDictionaryTableField getUserDictionaryTableField() {
    return getFieldByClass(UserDictionaryTableField.class);
  }

  public EditorSequenceBox getEditorSequenceBox() {
    return getFieldByClass(EditorSequenceBox.class);
  }

  public EditorField getEditorField() {
    return getFieldByClass(EditorField.class);
  }

  public EditorButton getEditorButton() {
    return getFieldByClass(EditorButton.class);
  }

  public ResetToStandardButton getResetToStandardButton() {
    return getFieldByClass(ResetToStandardButton.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Order(10)
    public class GeneralSettingsBox extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("SC_Label_Main");
      }

      @Order(10)
      public class EnabledField extends AbstractCheckBox {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("SC_Label_Enable");
        }

        @Override
        protected String getConfiguredTooltipText() {
          return ScoutTexts.get("SC_Help_Enable");
        }

        @Override
        protected void execChangedValue() throws ProcessingException {
          boolean on = getValue();
          getShortcutField().setEnabled(on);
          getLanguageField().setEnabled(on);
          getIgnoreCaseField().setEnabled(on);
          getIgnoreDomainNamesField().setEnabled(on);
          getIgnoreWordsWithNumbersField().setEnabled(on);
        }
      }

      @Order(20)
      public class ShortcutField extends AbstractSmartField<String> {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("SC_Label_Shortcut");
        }

        @Override
        protected String getConfiguredTooltipText() {
          return ScoutTexts.get("SC_Help_Shortcut");
        }

        @Override
        protected Class<? extends LookupCall> getConfiguredLookupCall() {
          return SpellCheckerShortcutLookupCall.class;
        }

      }

      @Order(30)
      public class LanguageField extends AbstractSmartField<String> {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("SC_Language");
        }

        @Override
        protected String getConfiguredTooltipText() {
          return ScoutTexts.get("SC_Help_Language");
        }

        @Override
        protected Class<? extends LookupCall> getConfiguredLookupCall() {
          return SpellCheckerLanguageLookupCall.class;
        }

      }

      @Order(40)
      public class IgnoreCaseField extends AbstractCheckBox {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("SC_Label_IgnoreCase");
        }

        @Override
        protected String getConfiguredTooltipText() {
          return ScoutTexts.get("SC_Help_IgnoreCase");
        }

      }

      @Order(50)
      public class IgnoreDomainNamesField extends AbstractCheckBox {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("SC_Label_IgnoreDomainNames");
        }

        @Override
        protected String getConfiguredTooltipText() {
          return ScoutTexts.get("SC_Help_IgnoreDomainNames");
        }

      }

      @Order(60)
      public class IgnoreWordsWithNumbersField extends AbstractCheckBox {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("SC_Label_IgnoreWordsWithNumbers");
        }

        @Override
        protected String getConfiguredTooltipText() {
          return ScoutTexts.get("SC_Help_IgnoreWordsWithNumbers");
        }

      }

    }

    @Order(20)
    public class UserDictionaryBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("SC_Label_UserDictionary");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("SC_Help_UserDictionary");
      }

      @Order(10)
      public class EditorSequenceBox extends AbstractSequenceBox {
        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Order(10)
        public class EditorField extends AbstractStringField {
          @Override
          public boolean isSpellCheckEnabled() {
            return false;
          }
        }

        @Order(20)
        public class EditorButton extends AbstractButton {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("SC_Label_AddToDictionary");
          }

          @Override
          protected String getConfiguredTooltipText() {
            return ScoutTexts.get("SC_Help_AddToDictionary");
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            String word = getEditorField().getValue();
            if (word != null) {
              if (!getUserDictionaryTableField().getTable().getWordColumn().contains(word)) {
                getUserDictionaryTableField().getTable().addRowByArray(new Object[]{word});
              }
              getEditorField().setValue(null);
            }
          }
        }
      }

      @Order(20)
      public class UserDictionaryTableField extends AbstractTableField<UserDictionaryTableField.Table> {

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridH() {
          return 10;
        }

        @Order(10)
        public class Table extends AbstractTable {
          @Override
          protected boolean getConfiguredAutoResizeColumns() {
            return true;
          }

          public WordColumn getWordColumn() {
            return getColumnSet().getColumnByClass(WordColumn.class);
          }

          @Override
          protected void execRowsSelected(ITableRow[] rows) throws ProcessingException {
            if (rows.length > 0) {
              getEditorField().setValue(getWordColumn().getSelectedValue());
            }
          }

          @Order(10)
          public class WordColumn extends AbstractStringColumn {
            @Override
            protected boolean getConfiguredPrimaryKey() {
              return true;
            }

            @Override
            protected String getConfiguredHeaderText() {
              return ScoutTexts.get("SC_Label_IgnoredWord");
            }
          }

          @Order(20)
          public class RemoveWordMenu extends AbstractMenu {
            @Override
            protected String getConfiguredText() {
              return ScoutTexts.get("SC_Label_RemoveWord");
            }

            @Override
            protected String getConfiguredTooltipText() {
              return ScoutTexts.get("SC_Help_RemoveWord");
            }

            @Override
            protected boolean getConfiguredSingleSelectionAction() {
              return true;
            }

            @Override
            protected void execAction() throws ProcessingException {
              if (MessageBox.showDeleteConfirmationMessage(getWordColumn().getSelectedDisplayText())) {
                deleteRow(getSelectedRow());
              }
            }
          }

        }

      }

    }

    @Order(50)
    public class ResetToStandardButton extends AbstractButton {
      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("SC_Label_UseDefaults");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("SC_Help_UseDefaults");
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getEnabledField().setValue(true);
        getShortcutField().setValue("f7");
        getLanguageField().setValue(Locale.getDefault().toString());
        getIgnoreCaseField().setValue(false);
        getIgnoreDomainNamesField().setValue(true);
        getIgnoreWordsWithNumbersField().setValue(true);
      }
    }

    @Order(60)
    public class OkButton extends AbstractOkButton {
    }

    @Order(70)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
    @Override
    protected void execLoad() throws ProcessingException {
      getShortcutField().setEnabledGranted(false);
      //
      ISpellCheckerService sc = SERVICES.getService(ISpellCheckerService.class);
      getEnabledField().setValue(sc.isEnabled());
      getShortcutField().setValue("f7");
      getLanguageField().setValue(sc.getLanguage());
      getIgnoreCaseField().setValue(sc.isIgnoreCase());
      getIgnoreDomainNamesField().setValue(sc.isIgnoreDomainNames());
      getIgnoreWordsWithNumbersField().setValue(sc.isIgnoreWordsWithNumbers());
      // user dict
      UserDictionaryTableField.Table table = getUserDictionaryTableField().getTable();
      try {
        table.setTableChanging(true);
        //
        IUserDictionary dict = sc.getUserDictionary();
        // force dict reload
        try {
          dict.addWord("ReloadDictionaryTag");
          dict.deleteWord("ReloadDictionaryTag");
        }
        catch (Exception e) {
          // nop
        }
        Enumeration en = dict.words();
        while (en.hasMoreElements()) {
          String s = (String) en.nextElement();
          if (s != null && s.length() > 0) {
            table.addRowByArray(new Object[]{s});
          }
        }
      }
      finally {
        table.setTableChanging(false);
      }
    }

    @Override
    protected void execStore() throws ProcessingException {
      ISpellCheckerService sc = SERVICES.getService(ISpellCheckerService.class);
      boolean reinitNeeded = false;
      if (getUserDictionaryTableField().isSaveNeeded()) {
        reinitNeeded = true;
        UserDictionaryTableField.Table table = getUserDictionaryTableField().getTable();
        try {
          IUserDictionary dict = sc.getUserDictionary();
          dict.clear();
          for (String word : table.getWordColumn().getValues()) {
            dict.addWord(word);
          }
        }
        catch (Exception e) {
          LOG.warn(null, e);
        }
      }
      if (getGeneralSettingsBox().isSaveNeeded()) {
        reinitNeeded = true;
        sc.setEnabled(getEnabledField().getValue());
        sc.setLanguage(getLanguageField().getValue());
        sc.setIgnoreCase(getIgnoreCaseField().getValue());
        sc.setIgnoreDomainNames(getIgnoreDomainNamesField().getValue());
        sc.setIgnoreWordsWithNumbers(getIgnoreWordsWithNumbersField().getValue());
      }
      if (reinitNeeded) {
        sc.saveSettings();
        sc.reinitialize();
      }
    }
  }
}
