package io.harness.ng.core.events;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.audit.ResourceType;
import io.harness.beans.Scope;
import io.harness.event.Event;
import io.harness.ng.core.Resource;
import io.harness.ng.core.ResourceScope;
import io.harness.ng.core.dto.TokenDTO;
import io.harness.ng.core.mapper.ResourceScopeMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

@OwnedBy(PL)
@Getter
@NoArgsConstructor
public class TokenUpdateEvent implements Event {
  public static final String TOKEN_UPDATED = "TokenUpdated";
  private TokenDTO oldToken;
  private TokenDTO newToken;

  public TokenUpdateEvent(TokenDTO oldToken, TokenDTO newToken) {
    this.oldToken = oldToken;
    this.newToken = newToken;
  }

  @Override
  @JsonIgnore
  public ResourceScope getResourceScope() {
    return ResourceScopeMapper.getResourceScope(
        Scope.of(newToken.getAccountIdentifier(), newToken.getOrgIdentifier(), newToken.getProjectIdentifier()));
  }

  @Override
  @JsonIgnore
  public Resource getResource() {
    return Resource.builder().identifier(oldToken.getIdentifier()).type(ResourceType.TOKEN.name()).build();
  }

  @Override
  @JsonIgnore
  public String getEventType() {
    return TOKEN_UPDATED;
  }
}
