package org.eclipse.scout.rt.testing.platform.testcategory;

import org.junit.experimental.categories.Category;

/**
 * A marker interface for tests which require a lot of system resource. Some test environments have strict resource
 * limits and may not be able to provide e.g. 10k native threads. Test which require a lot of threads, cpu or memory
 * should be tagged with this category.
 * <p>
 * Used together with {@link Category} annotation in unit-tests.
 */
public interface ResourceIntensiveTest extends ITestCategory {
}
