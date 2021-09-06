package io.harness.ng.core.api.impl;

import static io.harness.accesscontrol.principals.PrincipalType.USER;
import static io.harness.accesscontrol.principals.PrincipalType.USER_GROUP;
import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.exception.WingsException.USER_SRE;
import static io.harness.ng.core.utils.UserGroupMapper.toDTO;
import static io.harness.ng.core.utils.UserGroupMapper.toEntity;
import static io.harness.outbox.TransactionOutboxModule.OUTBOX_TRANSACTION_TEMPLATE;
import static io.harness.remote.client.RestClientUtils.getResponse;
import static io.harness.springdata.TransactionUtils.DEFAULT_TRANSACTION_RETRY_POLICY;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.harness.accesscontrol.AccessControlAdminClient;
import io.harness.accesscontrol.principals.PrincipalDTO;
import io.harness.accesscontrol.roleassignments.api.RoleAssignmentFilterDTO;
import io.harness.accesscontrol.roleassignments.api.RoleAssignmentResponseDTO;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.Scope;
import io.harness.exception.DuplicateFieldException;
import io.harness.exception.InvalidArgumentsException;
import io.harness.exception.InvalidRequestException;
import io.harness.ng.authenticationsettings.remote.AuthSettingsManagerClient;
import io.harness.ng.beans.PageRequest;
import io.harness.ng.beans.PageResponse;
import io.harness.ng.core.api.UserGroupService;
import io.harness.ng.core.dto.UserGroupDTO;
import io.harness.ng.core.dto.UserGroupFilterDTO;
import io.harness.ng.core.entities.NotificationSettingConfig;
import io.harness.ng.core.events.UserGroupCreateEvent;
import io.harness.ng.core.events.UserGroupDeleteEvent;
import io.harness.ng.core.events.UserGroupUpdateEvent;
import io.harness.ng.core.invites.InviteType;
import io.harness.ng.core.invites.api.InviteService;
import io.harness.ng.core.invites.dto.CreateInviteDTO;
import io.harness.ng.core.user.entities.UserGroup;
import io.harness.ng.core.user.entities.UserGroup.UserGroupKeys;
import io.harness.ng.core.user.remote.dto.UserFilter;
import io.harness.ng.core.user.remote.dto.UserMetadataDTO;
import io.harness.ng.core.user.service.NgUserService;
import io.harness.notification.NotificationChannelType;
import io.harness.outbox.api.OutboxService;
import io.harness.remote.NGObjectMapperHelper;
import io.harness.remote.client.NGRestUtils;
import io.harness.repositories.ng.core.spring.UserGroupRepository;
import io.harness.user.remote.UserClient;
import io.harness.utils.RetryUtils;
import io.harness.utils.ScopeUtils;

import software.wings.beans.sso.SSOSettings;
import software.wings.beans.sso.SSOType;
import software.wings.security.authentication.SSOConfig;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.transaction.support.TransactionTemplate;

@OwnedBy(PL)
@Singleton
@Slf4j
public class UserGroupServiceImpl implements UserGroupService {
  private static final String ACCOUNT_ADMIN = "_account_admin";
  private final UserGroupRepository userGroupRepository;
  private final UserClient userClient;
  private final OutboxService outboxService;
  private final AccessControlAdminClient accessControlAdminClient;
  private final TransactionTemplate transactionTemplate;
  private final NgUserService ngUserService;
  private final AuthSettingsManagerClient managerClient;
  private final InviteService inviteService;
  private static final RetryPolicy<Object> retryPolicy =
      RetryUtils.getRetryPolicy("Could not find the user with the given identifier on attempt %s",
          "Could not find the user with the given identifier", Lists.newArrayList(InvalidRequestException.class),
          Duration.ofSeconds(5), 3, log);

  private final RetryPolicy<Object> transactionRetryPolicy = DEFAULT_TRANSACTION_RETRY_POLICY;

  @Inject
  public UserGroupServiceImpl(UserGroupRepository userGroupRepository, UserClient userClient,
      OutboxService outboxService, @Named("PRIVILEGED") AccessControlAdminClient accessControlAdminClient,
      @Named(OUTBOX_TRANSACTION_TEMPLATE) TransactionTemplate transactionTemplate, NgUserService ngUserService,
      AuthSettingsManagerClient managerClient, InviteService inviteService) {
    this.userGroupRepository = userGroupRepository;
    this.userClient = userClient;
    this.outboxService = outboxService;
    this.accessControlAdminClient = accessControlAdminClient;
    this.transactionTemplate = transactionTemplate;
    this.ngUserService = ngUserService;
    this.managerClient = managerClient;
    this.inviteService = inviteService;
  }

  @Override
  public UserGroup create(UserGroupDTO userGroupDTO) {
    try {
      UserGroup userGroup = toEntity(userGroupDTO);
      validate(userGroup);
      List<UserMetadataDTO> usersAlreadyPartOfScope = ngUserService.getUsersAtScope(userGroup.getUsers(),
          Scope.of(userGroup.getAccountIdentifier(), userGroup.getOrgIdentifier(), userGroup.getProjectIdentifier()));
      Set<String> userIdentifiersAlreadyPartOfScope = new HashSet<>();
      usersAlreadyPartOfScope.forEach(userMetadataDTO -> {
        userIdentifiersAlreadyPartOfScope.add(userMetadataDTO.getUuid());
        userIdentifiersAlreadyPartOfScope.add(userMetadataDTO.getEmail());
      });
      Set<String> inputUserIdentifiers = new HashSet<>(userGroup.getUsers());
      List<String> toBeInvitedUserIdentifiers =
          new ArrayList<>(Sets.difference(inputUserIdentifiers, userIdentifiersAlreadyPartOfScope));

      userGroup.setUsers(usersAlreadyPartOfScope.stream().map(UserMetadataDTO::getUuid).collect(toList()));
      UserGroup result = Failsafe.with(transactionRetryPolicy).get(() -> transactionTemplate.execute(status -> {
        UserGroup savedUserGroup = userGroupRepository.save(userGroup);
        outboxService.save(new UserGroupCreateEvent(userGroupDTO.getAccountIdentifier(), userGroupDTO));
        return savedUserGroup;
      }));
      invite(toBeInvitedUserIdentifiers,
          Scope.of(userGroup.getAccountIdentifier(), userGroup.getOrgIdentifier(), userGroup.getProjectIdentifier()),
          userGroup.getIdentifier());
      return result;
    } catch (DuplicateKeyException ex) {
      throw new DuplicateFieldException(
          String.format("Try using different user group identifier, [%s] cannot be used", userGroupDTO.getIdentifier()),
          USER_SRE, ex);
    }
  }

  private void invite(List<String> userIdentifiers, Scope scope, String userGroupIdentifier) {
    Set<UserMetadataDTO> users = new HashSet<>(ngUserService.getUsersByUserIdentifiers(userIdentifiers));
    Set<String> emails = new HashSet<>();
    users.forEach(userMetadataDTO -> emails.add(userMetadataDTO.getEmail()));
    Set<String> userIdentifiersAlreadyPartOfScope = new HashSet<>();
    users.forEach(userMetadataDTO -> {
      userIdentifiersAlreadyPartOfScope.add(userMetadataDTO.getUuid());
      userIdentifiersAlreadyPartOfScope.add(userMetadataDTO.getEmail());
    });
    userIdentifiers.forEach(userIdentifier -> {
      if (!userIdentifiersAlreadyPartOfScope.contains(userIdentifier)) {
        emails.add(userIdentifier);
      }
    });
    CreateInviteDTO createInviteDTO = CreateInviteDTO.builder()
                                          .users(new ArrayList<>(emails))
                                          .inviteType(InviteType.ADMIN_INITIATED_INVITE)
                                          .userGroups(singletonList(userGroupIdentifier))
                                          .build();
    inviteService.createInvitations(
        scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), createInviteDTO);
  }

  @Override
  public Optional<UserGroup> get(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String identifier) {
    Criteria criteria = createUserGroupFetchCriteria(accountIdentifier, orgIdentifier, projectIdentifier, identifier);
    return userGroupRepository.find(criteria);
  }

  @Override
  public UserGroup update(UserGroupDTO userGroupDTO) {
    UserGroup savedUserGroup = getOrThrow(userGroupDTO.getAccountIdentifier(), userGroupDTO.getOrgIdentifier(),
        userGroupDTO.getProjectIdentifier(), userGroupDTO.getIdentifier());
    UserGroup userGroup = toEntity(userGroupDTO);
    userGroup.setId(savedUserGroup.getId());
    userGroup.setVersion(savedUserGroup.getVersion());
    return updateInternal(userGroup, toDTO(savedUserGroup));
  }

  @Override
  public Page<UserGroup> list(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String searchTerm, Pageable pageable) {
    return userGroupRepository.findAll(
        createUserGroupFilterCriteria(accountIdentifier, orgIdentifier, projectIdentifier, searchTerm), pageable);
  }

  @Override
  public List<UserGroup> list(UserGroupFilterDTO userGroupFilterDTO) {
    validateFilter(userGroupFilterDTO);
    Criteria criteria =
        createUserGroupFilterCriteria(userGroupFilterDTO.getAccountIdentifier(), userGroupFilterDTO.getOrgIdentifier(),
            userGroupFilterDTO.getProjectIdentifier(), userGroupFilterDTO.getSearchTerm());
    if (isNotEmpty(userGroupFilterDTO.getDatabaseIdFilter())) {
      criteria.and(UserGroupKeys.id).in(userGroupFilterDTO.getDatabaseIdFilter());
    } else if (isNotEmpty(userGroupFilterDTO.getIdentifierFilter())) {
      criteria.and(UserGroupKeys.identifier).in(userGroupFilterDTO.getIdentifierFilter());
    }
    if (isNotEmpty(userGroupFilterDTO.getUserIdentifierFilter())) {
      criteria.and(UserGroupKeys.users).in(userGroupFilterDTO.getUserIdentifierFilter());
    }
    return userGroupRepository.findAll(criteria, Pageable.unpaged()).getContent();
  }

  public PageResponse<UserMetadataDTO> listUsersInUserGroup(
      Scope scope, String userGroupIdentifier, UserFilter userFilter, PageRequest pageRequest) {
    Optional<UserGroup> userGroupOptional =
        get(scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), userGroupIdentifier);
    if (!userGroupOptional.isPresent()) {
      return PageResponse.getEmptyPageResponse(pageRequest);
    }
    Set<String> userGroupMemberIds = new HashSet<>(userGroupOptional.get().getUsers());
    if (isEmpty(userFilter.getIdentifiers())) {
      userFilter.setIdentifiers(userGroupMemberIds);
    } else {
      userFilter.setIdentifiers(Sets.intersection(userFilter.getIdentifiers(), userGroupMemberIds));
    }
    return ngUserService.listUsers(scope, pageRequest, userFilter);
  }

  @Override
  public UserGroup delete(Scope scope, String identifier) {
    validateAtleastOneAdminExistIfUserGroupRemoved(scope, identifier);

    Optional<UserGroup> userGroupOptional =
        get(scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), identifier);
    if (userGroupOptional.isPresent() && userGroupOptional.get().isHarnessManaged()) {
      throw new InvalidRequestException("Cannot deleted a managed user group");
    }
    Criteria criteria = createUserGroupFetchCriteria(
        scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), identifier);
    return Failsafe.with(transactionRetryPolicy).get(() -> transactionTemplate.execute(status -> {
      UserGroup userGroup = userGroupRepository.delete(criteria);
      outboxService.save(new UserGroupDeleteEvent(userGroup.getAccountIdentifier(), toDTO(userGroup)));
      return userGroup;
    }));
  }

  @Override
  public boolean deleteByScope(Scope scope) {
    return Failsafe.with(transactionRetryPolicy).get(() -> transactionTemplate.execute(status -> {
      Criteria criteria =
          createScopeCriteria(scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier());
      List<UserGroup> deleteUserGroups = userGroupRepository.deleteAll(criteria);
      if (isNotEmpty(deleteUserGroups)) {
        deleteUserGroups.forEach(userGroup
            -> outboxService.save(new UserGroupDeleteEvent(userGroup.getAccountIdentifier(), toDTO(userGroup))));
      }
      return true;
    }));
  }

  @Override
  public boolean checkMember(String accountIdentifier, String orgIdentifier, String projectIdentifier,
      String userGroupIdentifier, String userIdentifier) {
    UserGroup existingUserGroup = getOrThrow(accountIdentifier, orgIdentifier, projectIdentifier, userGroupIdentifier);
    return existingUserGroup.getUsers().contains(userIdentifier);
  }

  @Override
  public UserGroup addMember(String accountIdentifier, String orgIdentifier, String projectIdentifier,
      String userGroupIdentifier, String userIdentifier) {
    UserGroup existingUserGroup = getOrThrow(accountIdentifier, orgIdentifier, projectIdentifier, userGroupIdentifier);
    UserGroupDTO oldUserGroup = (UserGroupDTO) NGObjectMapperHelper.clone(toDTO(existingUserGroup));

    if (existingUserGroup.getUsers().stream().noneMatch(userIdentifier::equals)) {
      log.info("[NGSamlUserGroupSync] Adding member {} to Existing Usergroup: {}", userIdentifier, existingUserGroup);
      existingUserGroup.getUsers().add(userIdentifier);
    } else {
      throw new InvalidRequestException(
          String.format("User %s is already part of User Group %s", userIdentifier, userGroupIdentifier));
    }
    return updateInternal(existingUserGroup, oldUserGroup);
  }

  @Override
  public void addUserToUserGroups(String accountIdentifier, String userId, List<UserGroup> userGroups) {
    if (isEmpty(userGroups)) {
      return;
    }

    for (UserGroup userGroup : userGroups) {
      if (!checkMember(accountIdentifier, userGroup.getOrgIdentifier(), userGroup.getProjectIdentifier(),
              userGroup.getIdentifier(), userId)) {
        log.info("[NGSamlUserGroupSync] Trying to add user {} to UserGroup:{}", userId, userGroup);
        addMember(accountIdentifier, userGroup.getOrgIdentifier(), userGroup.getProjectIdentifier(),
            userGroup.getIdentifier(), userId);
      } else {
        log.info("[NGSamlUserGroupSync] Not adding user {} to UserGroup:{} CheckMember failed ", userId, userGroup);
      }
    }
  }

  @Override
  public void addUserToUserGroups(Scope scope, String userId, List<String> userGroups) {
    if (isEmpty(userGroups)) {
      return;
    }
    userGroups.forEach(userGroup -> {
      if (!checkMember(scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), userGroup,
              userId)) {
        addMember(
            scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), userGroup, userId);
      }
    });
  }

  @Override
  public UserGroup removeMember(Scope scope, String userGroupIdentifier, String userIdentifier) {
    UserGroup existingUserGroup = getOrThrow(
        scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), userGroupIdentifier);
    UserGroupDTO oldUserGroup = (UserGroupDTO) NGObjectMapperHelper.clone(toDTO(existingUserGroup));
    validateAtleastOneAdminExistIfUserRemoved(scope, userGroupIdentifier, userIdentifier);
    existingUserGroup.getUsers().remove(userIdentifier);
    return updateInternal(existingUserGroup, oldUserGroup);
  }

  private void validateAtleastOneAdminExistIfUserRemoved(
      Scope scope, String userGroupIdentifier, String userIdentifier) {
    if (!ScopeUtils.isAccountScope(scope)) {
      return;
    }
    List<PrincipalDTO> admins = getAdmins(Scope.builder().accountIdentifier(scope.getAccountIdentifier()).build());
    boolean doesOtherAdminUsersExist = admins.stream()
                                           .filter(admin -> admin.getType().equals(USER))
                                           .map(PrincipalDTO::getIdentifier)
                                           .findFirst()
                                           .isPresent();
    if (doesOtherAdminUsersExist) {
      return;
    }
    List<String> adminUserGroupIds = admins.stream()
                                         .filter(admin -> admin.getType().equals(USER_GROUP))
                                         .map(PrincipalDTO::getIdentifier)
                                         .distinct()
                                         .collect(toList());
    List<UserGroup> adminUserGroups =
        list(UserGroupFilterDTO.builder().identifierFilter(new HashSet<>(adminUserGroupIds)).build());
    doesOtherAdminUsersExist = adminUserGroups.stream().anyMatch(userGroup -> {
      if (userGroup.getIdentifier().equals(userGroupIdentifier)) {
        return userGroup.getUsers().size() > (userGroup.getUsers().contains(userIdentifier) ? 1 : 0);
      } else {
        return !userGroup.getUsers().isEmpty();
      }
    });
    if (doesOtherAdminUsersExist) {
      return;
    }
    throw new InvalidRequestException(String.format(
        "%s is the last account admin for %s. Can't remove it", userIdentifier, scope.getAccountIdentifier()));
  }

  private void validateAtleastOneAdminExistIfUserGroupRemoved(Scope scope, String userGroupIdentifier) {
    List<PrincipalDTO> admins = getAdmins(Scope.builder().accountIdentifier(scope.getAccountIdentifier()).build());
    boolean doesOtherAdminUsersExist = admins.stream()
                                           .filter(admin -> admin.getType().equals(USER))
                                           .map(PrincipalDTO::getIdentifier)
                                           .findFirst()
                                           .isPresent();
    if (doesOtherAdminUsersExist) {
      return;
    }
    boolean doesOtherAdminUserGroupsExist =
        admins.stream()
            .filter(admin -> admin.getType().equals(USER_GROUP) && !admin.getIdentifier().equals(userGroupIdentifier))
            .map(PrincipalDTO::getIdentifier)
            .findFirst()
            .isPresent();
    if (doesOtherAdminUserGroupsExist) {
      return;
    }
    throw new InvalidRequestException(
        String.format("%s has the last account admins. Can not remove it", userGroupIdentifier));
  }

  @NotNull
  private List<PrincipalDTO> getAdmins(Scope scope) {
    PageResponse<RoleAssignmentResponseDTO> response =
        NGRestUtils.getResponse(accessControlAdminClient.getFilteredRoleAssignments(scope.getAccountIdentifier(),
            scope.getOrgIdentifier(), scope.getProjectIdentifier(), 0, 10000,
            RoleAssignmentFilterDTO.builder().roleFilter(Collections.singleton(ACCOUNT_ADMIN)).build()));
    return response.getContent()
        .stream()
        .map(dto -> dto.getRoleAssignment().getPrincipal())
        .distinct()
        .collect(toList());
  }

  @Override
  public void removeMemberAll(@NotNull String accountIdentifier, String orgIdentifier, String projectIdentifier,
      @NotNull String userIdentifier) {
    Criteria criteria =
        createCriteriaByScopeAndUsersIn(accountIdentifier, orgIdentifier, projectIdentifier, userIdentifier);
    List<UserGroup> userGroups = userGroupRepository.findAll(criteria);
    Scope scope = Scope.builder()
                      .accountIdentifier(accountIdentifier)
                      .orgIdentifier(orgIdentifier)
                      .projectIdentifier(projectIdentifier)
                      .build();
    List<String> userGroupIdentifiers = userGroups.stream().map(UserGroup::getIdentifier).collect(Collectors.toList());
    userGroupIdentifiers.forEach(userGroupIdentifier -> removeMember(scope, userGroupIdentifier, userIdentifier));
  }

  private Criteria createCriteriaByScopeAndUsersIn(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, @NotNull String userIdentifier) {
    Criteria criteria = createScopeCriteria(accountIdentifier, orgIdentifier, projectIdentifier);
    criteria.and(UserGroupKeys.users).in(userIdentifier);
    return criteria;
  }

  private UserGroup getOrThrow(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String identifier) {
    Optional<UserGroup> userGroupOptional = get(accountIdentifier, orgIdentifier, projectIdentifier, identifier);
    if (!userGroupOptional.isPresent()) {
      throw new InvalidArgumentsException("User Group in the given scope does not exist:" + identifier);
    }
    return userGroupOptional.get();
  }

  private UserGroup updateInternal(UserGroup newUserGroup, UserGroupDTO oldUserGroup) {
    log.info("[NGSamlUserGroupSync] Old User Group {}", oldUserGroup);
    validate(newUserGroup);
    List<String> addedUserIdentifiers = new ArrayList<>(
        Sets.difference(new HashSet<>(newUserGroup.getUsers()), new HashSet<>(oldUserGroup.getUsers())));
    try {
      UserGroup result = Failsafe.with(transactionRetryPolicy).get(() -> transactionTemplate.execute(status -> {
        log.info("[NGSamlUserGroupSync] Saving new User group {}", newUserGroup);
        UserGroup updatedUserGroup = userGroupRepository.save(newUserGroup);
        log.info("[NGSamlUserGroupSync] Saved New User Group Successfully");
        outboxService.save(
            new UserGroupUpdateEvent(updatedUserGroup.getAccountIdentifier(), toDTO(updatedUserGroup), oldUserGroup));
        return updatedUserGroup;
      }));
      invite(addedUserIdentifiers,
          Scope.of(newUserGroup.getAccountIdentifier(), newUserGroup.getOrgIdentifier(),
              newUserGroup.getProjectIdentifier()),
          newUserGroup.getIdentifier());
      return result;
    } catch (DuplicateKeyException ex) {
      throw new DuplicateFieldException(
          String.format("Try using different user group identifier, [%s] cannot be used", newUserGroup.getIdentifier()),
          USER_SRE, ex);
    }
  }

  private void validate(UserGroup userGroup) {
    if (userGroup.getNotificationConfigs() != null) {
      validateNotificationSettings(userGroup.getNotificationConfigs());
    }
    if (userGroup.getUsers() != null) {
      validateUsers(userGroup.getUsers());
      validateScopeMembership(userGroup);
    }
  }

  private void validateUsers(List<String> usersIds) {
    Set<String> duplicates = getDuplicates(usersIds);
    if (isNotEmpty(duplicates)) {
      throw new InvalidArgumentsException(
          String.format("Duplicate users %s provided in the user group", duplicates.toString()));
    }
  }

  private static <T> Set<T> getDuplicates(Iterable<T> elements) {
    Set<T> set = new HashSet<>();
    Set<T> duplicates = new HashSet<>();
    for (T element : elements) {
      if (!set.add(element)) {
        duplicates.add(element);
      }
    }
    return duplicates;
  }

  private void validateScopeMembership(UserGroup userGroup) {
    Scope scope = Scope.builder()
                      .accountIdentifier(userGroup.getAccountIdentifier())
                      .orgIdentifier(userGroup.getOrgIdentifier())
                      .projectIdentifier(userGroup.getProjectIdentifier())
                      .build();
    List<String> userIds = ngUserService.listUserIds(scope);
    Sets.SetView<String> invalidUserIds = Sets.difference(new HashSet<>(userGroup.getUsers()), new HashSet<>(userIds));
    if (isNotEmpty(invalidUserIds)) {
      throw new InvalidArgumentsException(getInvalidUserMessage(invalidUserIds));
    }
  }

  private void validateFilter(UserGroupFilterDTO filter) {
    if (isNotEmpty(filter.getIdentifierFilter()) && isNotEmpty(filter.getDatabaseIdFilter())) {
      throw new InvalidArgumentsException("Both the database id filter and identifier filter cannot be provided");
    }
  }

  private String getInvalidUserMessage(Set<String> invalidUserIds) {
    return String.format("The following user%s not valid: [%s]", invalidUserIds.size() > 1 ? "s are" : " is",
        String.join(", ", invalidUserIds));
  }

  private void validateNotificationSettings(List<NotificationSettingConfig> notificationSettingConfigs) {
    Set<NotificationChannelType> typeSet = new HashSet<>();
    for (NotificationSettingConfig config : notificationSettingConfigs) {
      if (typeSet.contains(config.getType())) {
        throw new InvalidArgumentsException(
            "Not allowed to create multiple notification setting of type: " + config.getType());
      }
      typeSet.add(config.getType());
    }
  }

  private Criteria createScopeCriteria(String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    Criteria criteria = new Criteria();
    criteria.and(UserGroupKeys.accountIdentifier).is(accountIdentifier);
    criteria.and(UserGroupKeys.orgIdentifier).is(orgIdentifier);
    criteria.and(UserGroupKeys.projectIdentifier).is(projectIdentifier);
    return criteria;
  }

  private Criteria createUserGroupFilterCriteria(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String searchTerm) {
    Criteria criteria = createScopeCriteria(accountIdentifier, orgIdentifier, projectIdentifier);
    if (isNotBlank(searchTerm)) {
      criteria.orOperator(Criteria.where(UserGroupKeys.name).regex(searchTerm, "i"),
          Criteria.where(UserGroupKeys.tags).regex(searchTerm, "i"));
    }
    return criteria;
  }

  private Criteria createUserGroupFetchCriteria(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String identifier) {
    Criteria criteria = createScopeCriteria(accountIdentifier, orgIdentifier, projectIdentifier);
    criteria.and(UserGroupKeys.identifier).is(identifier);
    return criteria;
  }

  private Criteria getUserGroupbySsoIdCriteria(String accountIdentifier, String ssoId) {
    Criteria criteria = createScopeCriteria(accountIdentifier, null, null);
    criteria.and(UserGroupKeys.isSsoLinked).is(true);
    criteria.and(UserGroupKeys.linkedSsoId).is(ssoId);
    return criteria;
  }

  @Override
  public List<UserGroup> getUserGroupsBySsoId(String accountIdentifier, String ssoId) {
    Criteria criteria = getUserGroupbySsoIdCriteria(accountIdentifier, ssoId);
    return userGroupRepository.findAll(criteria);
  }

  @Override
  public List<UserGroup> getUserGroupsBySsoId(String ssoId) {
    Criteria criteria = new Criteria();
    criteria.and(UserGroupKeys.isSsoLinked).is(true);
    criteria.and(UserGroupKeys.linkedSsoId).is(ssoId);
    return userGroupRepository.findAll(criteria);
  }

  private Criteria getUserGroupbySsoIdCriteria(String ssoId) {
    Criteria criteria = new Criteria();
    criteria.and(UserGroupKeys.isSsoLinked).is(true);
    criteria.and(UserGroupKeys.linkedSsoId).is(ssoId);
    return criteria;
  }

  @Override
  public UserGroup linkToSsoGroup(@NotBlank String accountIdentifier, String orgIdentifier, String projectIdentifier,
      @NotBlank String userGroupIdentifier, @NotNull SSOType ssoType, @NotBlank String ssoId,
      @NotBlank String ssoGroupId, @NotBlank String ssoGroupName) {
    UserGroup existingUserGroup = getOrThrow(accountIdentifier, orgIdentifier, projectIdentifier, userGroupIdentifier);
    UserGroupDTO oldUserGroup = (UserGroupDTO) NGObjectMapperHelper.clone(toDTO(existingUserGroup));

    if (TRUE.equals(existingUserGroup.getIsSsoLinked())) {
      throw new InvalidRequestException("SSO Provider already linked to the group. Try unlinking first.");
    }

    SSOConfig ssoConfig = getResponse(managerClient.getAccountAccessManagementSettings(accountIdentifier));

    List<SSOSettings> ssoSettingsList = ssoConfig.getSsoSettings();
    SSOSettings ssoSettings = null;
    for (SSOSettings ssoSetting : ssoSettingsList) {
      if (ssoSetting.getUuid().equals(ssoId)) {
        ssoSettings = ssoSetting;
        break;
      }
    }

    if (null == ssoSettings) {
      throw new InvalidRequestException("Invalid ssoId");
    }
    existingUserGroup.setIsSsoLinked(TRUE);
    existingUserGroup.setLinkedSsoType(ssoType);
    existingUserGroup.setLinkedSsoId(ssoId);
    existingUserGroup.setLinkedSsoDisplayName(ssoSettings.getDisplayName());
    existingUserGroup.setSsoGroupId(ssoGroupId);
    existingUserGroup.setSsoGroupName(ssoGroupName);

    return updateInternal(existingUserGroup, oldUserGroup);
  }

  @Override
  public UserGroup unlinkSsoGroup(@NotBlank String accountIdentifier, String orgIdentifier, String projectIdentifier,
      @NotBlank String userGroupIdentifier, boolean retainMembers) {
    UserGroup existingUserGroup = getOrThrow(accountIdentifier, orgIdentifier, projectIdentifier, userGroupIdentifier);
    UserGroupDTO oldUserGroup = (UserGroupDTO) NGObjectMapperHelper.clone(toDTO(existingUserGroup));

    if (FALSE.equals(existingUserGroup.getIsSsoLinked()) || existingUserGroup.getIsSsoLinked() == null) {
      throw new InvalidRequestException("Group is not linked to any SSO group.");
    }

    if (!retainMembers) {
      existingUserGroup.setUsers(emptyList());
      existingUserGroup = updateInternal(existingUserGroup, oldUserGroup);
    }

    existingUserGroup.setSsoGroupId(null);
    existingUserGroup.setIsSsoLinked(FALSE);
    existingUserGroup.setSsoGroupName(null);
    existingUserGroup.setLinkedSsoId(null);
    existingUserGroup.setLinkedSsoType(null);
    existingUserGroup.setLinkedSsoDisplayName(null);

    return updateInternal(existingUserGroup, oldUserGroup);
  }
}
