package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for smartfield lookup using {@link ILookupRowProvider} and {@link LookupCall}.
 *
 * @since 5.2
 */
@Bean
public class LookupRowHelper {
  private static final Logger LOG = LoggerFactory.getLogger(LookupRowHelper.class);

  /**
   * Synchronous lookup
   *
   * @return list of resulting {@link ILookupRow}s
   */
  public <T> List<ILookupRow<T>> lookup(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall) {
    if (lookupCall == null) {
      return CollectionUtility.emptyArrayList();
    }

    beforeProvide(provider, lookupCall);
    final List<ILookupRow<T>> result = (List<ILookupRow<T>>) provider.provide(lookupCall);
    return afterProvide(provider, lookupCall, result);
  }

  /**
   * Asynchronous lookup
   *
   * @return list of resulting {@link ILookupRow}s
   */
  public <T> IFuture<List<ILookupRow<T>>> scheduleLookup(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall) {
    Assertions.assertNotNull(provider);
    return Jobs.schedule(new Callable<List<ILookupRow<T>>>() {

      @Override
      public List<ILookupRow<T>> call() throws Exception {
        LOG.debug("Fetching data");
        if (lookupCall == null) {
          LOG.warn("Fetching data for empty lookup call");
          return CollectionUtility.emptyArrayList();
        }
        beforeProvide(provider, lookupCall);
        final List<ILookupRow<T>> lookupRes = provider.provide(lookupCall);
        LOG.debug("Result received {}", lookupRes);
        return afterProvide(provider, lookupCall, lookupRes);
      }

    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent())
        .withName("Lookup [lookupCall={}, provider={}]", lookupCall != null ? lookupCall.getClass().getName() : "null", provider)
        .withExceptionHandling(null, false));
  }

  private <T> void beforeProvide(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall) {
    runInModelJob(new Runnable() {

      @Override
      public void run() {
        provider.beforeProvide(lookupCall);
      }
    });
  }

  private <T> List<ILookupRow<T>> afterProvide(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall, final List<ILookupRow<T>> result) {
    runInModelJob(new Runnable() {

      @Override
      public void run() {
        provider.afterProvide(lookupCall, result);
      }
    });
    return result;
  }

  /**
   * Ensures, that the {@link Runnable} is running in a model job by creating a new one, if necessary. Does not handle
   * exceptions
   */
  private <T> void runInModelJob(final Runnable runnable) {
    if (ModelJobs.isModelThread()) {
      runnable.run();
    }
    else {
      ModelJobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          runnable.run();
        }
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
          .withExceptionHandling(null, false))
          .awaitDone();
    }
  }

}
