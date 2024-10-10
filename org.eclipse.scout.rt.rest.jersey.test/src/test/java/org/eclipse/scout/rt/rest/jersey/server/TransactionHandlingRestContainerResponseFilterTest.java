package org.eclipse.scout.rt.rest.jersey.server;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.client.AntiCsrfClientFilter;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(PlatformTestRunner.class)
public class TransactionHandlingRestContainerResponseFilterTest {

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  protected Invocation.Builder request() {
    return ClientBuilder.newClient()
        .register(AntiCsrfClientFilter.class)
        .target("http://localhost:" + BEANS.get(JerseyTestApplication.class).getPort() + "/api/transactionHandling/throwExceptionInTransaction")
        .request()
        .accept(MediaType.APPLICATION_JSON);
  }

  @Test
  public void testFailingTransaction() {
    try (Response response = request().post(Entity.json("null"))) {
      assertEquals("When an exception is thrown during a commit, the response should signal an error.", 500, response.getStatus());
    }
  }
}
