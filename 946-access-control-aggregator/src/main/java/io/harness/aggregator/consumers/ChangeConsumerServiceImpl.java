package io.harness.aggregator.consumers;

import static io.harness.accesscontrol.principals.PrincipalType.SERVICE_ACCOUNT;
import static io.harness.accesscontrol.principals.PrincipalType.USER;
import static io.harness.accesscontrol.principals.PrincipalType.USER_GROUP;
import static io.harness.aggregator.ACLUtils.buildACL;

import static java.util.Collections.singleton;

import io.harness.accesscontrol.Principal;
import io.harness.accesscontrol.acl.persistence.ACL;
import io.harness.accesscontrol.common.filter.ManagedFilter;
import io.harness.accesscontrol.principals.usergroups.UserGroup;
import io.harness.accesscontrol.principals.usergroups.UserGroupService;
import io.harness.accesscontrol.resources.resourcegroups.ResourceGroup;
import io.harness.accesscontrol.resources.resourcegroups.ResourceGroupService;
import io.harness.accesscontrol.roleassignments.persistence.RoleAssignmentDBO;
import io.harness.accesscontrol.roles.Role;
import io.harness.accesscontrol.roles.RoleService;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;

@Singleton
@OwnedBy(HarnessTeam.PL)
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class ChangeConsumerServiceImpl implements ChangeConsumerService {
  private final RoleService roleService;
  private final UserGroupService userGroupService;
  private final ResourceGroupService resourceGroupService;

  @Override
  public List<ACL> getAClsForRoleAssignment(RoleAssignmentDBO roleAssignment) {
    Optional<Role> role = roleService.get(
        roleAssignment.getRoleIdentifier(), roleAssignment.getScopeIdentifier(), ManagedFilter.NO_FILTER);
    Optional<ResourceGroup> resourceGroup = resourceGroupService.get(
        roleAssignment.getResourceGroupIdentifier(), roleAssignment.getScopeIdentifier(), ManagedFilter.NO_FILTER);
    if (!role.isPresent() || !resourceGroup.isPresent()) {
      return new ArrayList<>();
    }

    Set<String> principals = new HashSet<>();
    if (USER_GROUP.equals(roleAssignment.getPrincipalType())) {
      Optional<UserGroup> userGroup =
          userGroupService.get(roleAssignment.getPrincipalIdentifier(), roleAssignment.getScopeIdentifier());
      userGroup.ifPresent(group -> principals.addAll(group.getUsers()));
    } else {
      principals.add(roleAssignment.getPrincipalIdentifier());
    }

    Set<String> resourceSelectors = new HashSet<>();
    if (Boolean.TRUE.equals(resourceGroup.get().getNestedScopesSelected())) {
      resourceSelectors = singleton("/**/*/*");
    } else if (resourceGroup.get().isFullScopeSelected()) {
      resourceSelectors = singleton("/*/*");
    } else if (resourceGroup.get().getResourceSelectors() != null) {
      resourceSelectors = resourceGroup.get().getResourceSelectors();
    }

    List<ACL> acls = new ArrayList<>();
    for (String permission : role.get().getPermissions()) {
      for (String principalIdentifier : principals) {
        for (String resourceSelector : resourceSelectors) {
          if (SERVICE_ACCOUNT.equals(roleAssignment.getPrincipalType())) {
            acls.add(buildACL(
                permission, Principal.of(SERVICE_ACCOUNT, principalIdentifier), roleAssignment, resourceSelector));
          } else {
            acls.add(buildACL(permission, Principal.of(USER, principalIdentifier), roleAssignment, resourceSelector));
          }
        }
      }
    }
    return acls;
  }
}
