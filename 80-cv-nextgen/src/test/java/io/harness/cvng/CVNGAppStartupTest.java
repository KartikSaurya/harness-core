package io.harness.cvng;

import static io.harness.rule.OwnerRule.RAGHU;
import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.rule.Owner;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.InetSocketAddress;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class CVNGAppStartupTest extends CategoryTest {
  public static MongoServer MONGO_SERVER;
  public static DropwizardTestSupport<VerificationConfiguration> SUPPORT;

  private static MongoServer startMongoServer() {
    final MongoServer mongoServer = new MongoServer(new MemoryBackend());
    mongoServer.bind("localhost", 0);
    return mongoServer;
  }

  private static void stopMongoServer() {
    if (MONGO_SERVER != null) {
      MONGO_SERVER.shutdownNow();
    }
  }

  private static String getMongoUri() {
    InetSocketAddress serverAddress = MONGO_SERVER.getLocalAddress();
    final ServerAddress addr = new ServerAddress(serverAddress);
    return String.format("mongodb://%s:%s/ng-harness", addr.getHost(), addr.getPort());
  }

  @BeforeClass
  public static void beforeClass() {
    MONGO_SERVER = startMongoServer();
    SUPPORT = new DropwizardTestSupport<>(VerificationApplication.class,
        ResourceHelpers.resourceFilePath("test-config.yml"), ConfigOverride.config("mongo.uri", getMongoUri()));
    SUPPORT.before();
  }

  @AfterClass
  public static void afterClass() {
    SUPPORT.after();
    stopMongoServer();
  }

  @Test
  @Owner(developers = RAGHU)
  @Category(UnitTests.class)
  public void testAppStartup() {
    final Client client = new JerseyClientBuilder().build();
    final Response response =
        client.target(String.format("http://localhost:%d/cv-nextgen/swagger.json", SUPPORT.getLocalPort()))
            .request()
            .get();
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    response.close();
  }
}
