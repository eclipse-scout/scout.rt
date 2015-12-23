package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.IOrganizeColumnsForm;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.AddCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.KeyColumn;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.ModifyCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.RemoveMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.menus.OrganizeColumnsMenu;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

/**
 * This class is used as a small JSON wrapper around the columns organize form. It exists because the whole column
 * organize logic is implemented on that form and we don't want to send the whole form to the browser when we click on a
 * table header. We should refactor table and columns organize form so state and action of these buttons can be accessed
 * without having a form instance.
 */
public class JsonOrganizeColumnCommands implements IJsonObject {

  private IOrganizeColumnsForm m_form;

  public JsonOrganizeColumnCommands(OrganizeColumnsMenu formToolButton) {
    formToolButton.ensureFormCreated();
    this.m_form = formToolButton.getForm();
    this.m_form.reload();
  }

  @Override
  public JSONObject toJson() {
    ITable table = m_form.getFieldByClass(ColumnsTableField.class).getTable();
    IMenu addMenu = table.getMenuByClass(AddCustomColumnMenu.class);
    IMenu removeMenu = table.getMenuByClass(RemoveMenu.class);
    IMenu modifyMenu = table.getMenuByClass(ModifyCustomColumnMenu.class);

    JSONObject json = new JSONObject();
    json.put("addVisible", addMenu.isVisible());
    json.put("removeVisible", removeMenu.isVisible());
    json.put("modifyVisible", modifyMenu.isVisible());
    return json;
  }

  public void doAction(String action) {
    ITable table = m_form.getFieldByClass(ColumnsTableField.class).getTable();
    if ("add".equals(action)) {
      table.getMenuByClass(AddCustomColumnMenu.class).doAction();
    }
    else if ("remove".equals(action)) {
      table.getMenuByClass(RemoveMenu.class).doAction();
    }
    else if ("modify".equals(action)) {
      table.getMenuByClass(ModifyCustomColumnMenu.class).doAction();
    }
  }

  /**
   * Simulate a click on a row in the table with the columns so add/remove/modify menus have the right state.
   *
   * @param column
   */
  public void selectColumn(IColumn column) {
    ITable table = m_form.getFieldByClass(ColumnsTableField.class).getTable();
    IColumn<IColumn<?>> keyColumn = table.getColumnSet().getColumnByClass(KeyColumn.class);
    for (ITableRow row : table.getRows()) {
      IColumn columnFromRow = (IColumn) row.getCell(keyColumn).getValue();
      if (column == columnFromRow) {
        table.selectRow(row);
        break;
      }
    }
  }

  public void reload() {
    m_form.reload();
  }

}
