package io.harness.registrars;

import static io.harness.rule.OwnerRule.BRIJESH;
import static io.harness.rule.OwnerRule.PRASHANT;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;

import io.harness.OrchestrationModuleListProvider;
import io.harness.OrchestrationTest;
import io.harness.category.element.UnitTests;
import io.harness.registries.registrar.AdviserRegistrar;
import io.harness.rule.Owner;
import io.harness.runners.GuiceRunner;
import io.harness.runners.ModuleProvider;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(GuiceRunner.class)
@ModuleProvider(OrchestrationModuleListProvider.class)
public class OrchestrationAdviserRegistrarTest extends OrchestrationTest {
  @Inject Map<String, AdviserRegistrar> adviserRegistrars;

  @Test
  @Owner(developers = PRASHANT)
  @Category(UnitTests.class)
  public void shouldTestRegister() {
    new OrchestrationAdviserRegistrar().testClassesModule();
  }

  @Test
  @Owner(developers = BRIJESH)
  @Category(UnitTests.class)
  public void testAllRegistrarsAreRegistered()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    Set<String> adviserRegistrarClasses = new HashSet<>();

    Reflections reflections = new Reflections("io.harness.registrars");
    for (Class clazz : reflections.getSubTypesOf(AdviserRegistrar.class)) {
      adviserRegistrarClasses.add(clazz.getName());
    }
    assertThat(adviserRegistrars.keySet()).isEqualTo(adviserRegistrarClasses);
  }
}