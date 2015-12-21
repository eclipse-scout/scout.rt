package org.eclipse.scout.rt.shared.extension.dto;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IInternalExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableField.MainBox.TableInOrigFormField.Table;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableFieldData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableFieldData.TableInOrigForm.TableInOrigFormRowData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableFieldExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableFieldExtension.TableInOrigFormFieldExtension.ContributedIntColumn;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableFieldExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableFieldExtensionData.TableInOrigFormFieldExtensionRowData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link TableFieldExtensionTest}</h3> Test DTO handling of Extensions to TableFields inside an existing
 * FormExtension
 *
 * @author Matthias Villiger
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableFieldExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testTableFieldExtension() {
    IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    extensionRegistry.register(OrigFormWithTableFieldExtensionData.class);
    extensionRegistry.register(OrigFormWithTableFieldExtension.class);

    // ensure the inner row data extension is registered automatically
    Assert.assertTrue(extensionRegistry.getContributionsFor(TableInOrigFormRowData.class).contains(TableInOrigFormFieldExtensionRowData.class));

    OrigFormWithTableField form = new OrigFormWithTableField();
    form.initForm();

    // test DTO round trip
    OrigFormWithTableFieldData ex = new OrigFormWithTableFieldData();
    form.exportFormData(ex);

    TableInOrigFormRowData firstRow = ex.getTableInOrigForm().getRows()[0];
    Assert.assertEquals(OrigFormWithTableField.FIRST_ROW_VALUE, firstRow.getOrig());
    Assert.assertEquals(OrigFormWithTableFieldExtension.VALUE_OF_CONTRIBUTED_COL, firstRow.getContribution(TableInOrigFormFieldExtensionRowData.class).getContributedInt());

    final String newValForFirstCol = "testle";
    final Integer newValForSecondCol = Integer.valueOf(222);
    firstRow.setOrig(newValForFirstCol);
    firstRow.getContribution(TableInOrigFormFieldExtensionRowData.class).setContributedInt(newValForSecondCol);

    form.importFormData(ex);

    Table tab = form.getTableInOrigFormField().getTable();
    Assert.assertEquals(newValForFirstCol, tab.getOrigColumn().getValue(0));
    Assert.assertEquals(newValForSecondCol, tab.getColumnSet().getColumnByClass(ContributedIntColumn.class).getValue(0));
  }
}
