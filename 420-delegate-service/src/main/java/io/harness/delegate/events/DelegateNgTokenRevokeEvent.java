package io.harness.delegate.events;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.audit.ResourceTypeConstants;
import io.harness.beans.Scope;
import io.harness.delegate.beans.DelegateEntityOwner;
import io.harness.delegate.dto.DelegateNgTokenDTO;
import io.harness.delegate.utils.DelegateEntityOwnerHelper;
import io.harness.ng.core.Resource;
import io.harness.ng.core.ResourceScope;
import io.harness.ng.core.mapper.ResourceScopeMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@OwnedBy(HarnessTeam.DEL)
@Getter
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DelegateNgTokenRevokeEvent extends AbstractDelegateConfigurationEvent {
  public static final String DELEGATE_TOKEN_REVOKE_EVENT = "DelegateNgTokenRevokeEvent";
  private DelegateNgTokenDTO token;

  @Override
  public Resource getResource() {
    return Resource.builder().identifier(createTokenIdentifier()).type(ResourceTypeConstants.DELEGATE_TOKEN).build();
  }

  @Override
  public String getEventType() {
    return DELEGATE_TOKEN_REVOKE_EVENT;
  }

  @Override
  @JsonIgnore
  public ResourceScope getResourceScope() {
    return ResourceScopeMapper.getResourceScope(
        Scope.of(token.getAccountIdentifier(), token.getOrgIdentifier(), token.getProjectIdentifier()));
  }

  private String createTokenIdentifier() {
    DelegateEntityOwner owner =
        DelegateEntityOwnerHelper.buildOwner(token.getOrgIdentifier(), token.getProjectIdentifier());
    return token.getAccountIdentifier() + (owner != null ? "/" + owner.getIdentifier() : "") + ":" + token.getName();
  }
}
