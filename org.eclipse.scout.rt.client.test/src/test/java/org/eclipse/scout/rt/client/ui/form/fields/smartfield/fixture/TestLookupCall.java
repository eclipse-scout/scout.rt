package org.eclipse.scout.rt.client.ui.form.fields.smartfield.fixture;

import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Lookup call for testing
 */
public class TestLookupCall extends LookupCall<Long> {
  private static final long serialVersionUID = 1L;

  @Override
  protected Class<? extends ILookupService<Long>> getConfiguredService() {
    return ITestLookupService.class;
  }

}
