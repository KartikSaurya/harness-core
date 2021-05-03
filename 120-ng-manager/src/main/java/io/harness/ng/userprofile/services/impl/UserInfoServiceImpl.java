package io.harness.ng.userprofile.services.impl;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.core.user.TwoFactorAuthMechanismInfo;
import io.harness.ng.core.user.TwoFactorAuthSettingsInfo;
import io.harness.ng.core.user.UserInfo;
import io.harness.ng.userprofile.services.api.UserInfoService;
import io.harness.remote.client.RestClientUtils;
import io.harness.security.SourcePrincipalContextBuilder;
import io.harness.security.dto.PrincipalType;
import io.harness.security.dto.UserPrincipal;
import io.harness.user.remote.UserClient;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@OwnedBy(HarnessTeam.PL)
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
  @Inject private UserClient userClient;

  @Override
  public UserInfo getCurrentUser() {
    Optional<String> userEmail = getUserEmail();
    if (userEmail.isPresent()) {
      Optional<UserInfo> userInfo = RestClientUtils.getResponse(userClient.getUserByEmailId(userEmail.get()));
      return userInfo.get();
    } else {
      throw new IllegalStateException("user login required");
    }
  }

  @Override
  public UserInfo update(UserInfo userInfo) {
    Optional<String> userEmail = getUserEmail();
    if (userEmail.isPresent()) {
      userInfo.setEmail(userEmail.get());
      Optional<UserInfo> updatedUserInfo = RestClientUtils.getResponse(userClient.updateUser(userInfo));
      return updatedUserInfo.get();
    } else {
      throw new IllegalStateException("user login required");
    }
  }

  @Override
  public TwoFactorAuthSettingsInfo getTwoFactorAuthSettingsInfo(TwoFactorAuthMechanismInfo twoFactorAuthMechanismInfo) {
    Optional<String> userEmail = getUserEmail();
    if (userEmail.isPresent()) {
      Optional<TwoFactorAuthSettingsInfo> twoFactorAuthSettingsInfo = RestClientUtils.getResponse(
          userClient.getUserTwoFactorAuthSettings(twoFactorAuthMechanismInfo, userEmail.get()));
      return twoFactorAuthSettingsInfo.get();
    } else {
      throw new IllegalStateException("user login required");
    }
  }

  @Override
  public UserInfo updateTwoFactorAuthInfo(TwoFactorAuthSettingsInfo authSettingsInfo) {
    Optional<String> userEmail = getUserEmail();
    if (userEmail.isPresent()) {
      Optional<UserInfo> userInfo =
          RestClientUtils.getResponse(userClient.updateUserTwoFactorAuthInfo(userEmail.get(), authSettingsInfo));
      return userInfo.get();
    } else {
      throw new IllegalStateException("user login required");
    }
  }

  @Override
  public UserInfo disableTFA() {
    Optional<String> userEmail = getUserEmail();
    if (userEmail.isPresent()) {
      Optional<UserInfo> userInfo = RestClientUtils.getResponse(userClient.disableUserTwoFactorAuth(userEmail.get()));
      return userInfo.get();
    } else {
      throw new IllegalStateException("user login required");
    }
  }

  private Optional<String> getUserEmail() {
    String userEmail = null;
    if (SourcePrincipalContextBuilder.getSourcePrincipal() != null
        && SourcePrincipalContextBuilder.getSourcePrincipal().getType() == PrincipalType.USER) {
      userEmail = ((UserPrincipal) (SourcePrincipalContextBuilder.getSourcePrincipal())).getEmail();
    }
    return Optional.of(userEmail);
  }
}
