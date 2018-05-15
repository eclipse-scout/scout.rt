# org.eclipse.scout.rt.rest.jersey.client

This is a [Jersey](www.eclipse.org/ee4j/jersey) specific extension to *org.eclipse.scout.rt.rest*
that provides an `IGlobalRestClientConfigurator` which registers a custom `ExecutorServiceProvider`
for a Jersey REST `Client`.

By default, Jersey creates a new unbounded `java.util.concurrent.ExecutorService` to submit tasks
for asynchronous REST calls (see [DefaultClientAsyncExecutorProvider](https://github.com/eclipse-ee4j/jersey/blob/master/core-client/src/main/java/org/glassfish/jersey/client/DefaultClientAsyncExecutorProvider.java)).
When running jobs in a Scout platform, the [JobManager](http://eclipsescout.github.io/8.0/technical-guide.html#jobmanager)
uses a single `ExecutorService` instance that can be easily configured using system properties. By
including this module in the dependencies of your project, all asynchronous REST calls made via an
`IRestClientHelper` bean will be submitted to the same executor service.
