package io.harness.security;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.context.GlobalContextData;
import io.harness.security.dto.Principal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(PL)
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@TypeAlias("SourcePrincipalContextData")
public class SourcePrincipalContextData implements GlobalContextData {
  public static final String SOURCE_PRINCIPAL = "SOURCE_PRINCIPAL";
  Principal principal;

  @Override
  public String getKey() {
    return SOURCE_PRINCIPAL;
  }
}
