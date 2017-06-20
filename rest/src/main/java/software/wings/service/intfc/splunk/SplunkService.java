package software.wings.service.intfc.splunk;

import ru.vyarus.guice.validator.group.annotation.ValidationGroups;
import software.wings.service.impl.splunk.SplunkLogElement;
import software.wings.utils.validation.Create;

import java.io.IOException;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by rsingh on 4/17/17.
 */
public interface SplunkService {
  @ValidationGroups(Create.class)
  Boolean saveLogData(@NotNull String appId, @Valid List<SplunkLogElement> logData) throws IOException;
}
