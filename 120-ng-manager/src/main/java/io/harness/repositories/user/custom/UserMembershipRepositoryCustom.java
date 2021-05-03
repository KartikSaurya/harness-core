package io.harness.repositories.user.custom;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.core.user.entities.UserMembership;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

@OwnedBy(PL)
public interface UserMembershipRepositoryCustom {
  List<UserMembership> findAll(Criteria criteria);

  Page<UserMembership> findAll(Criteria criteria, Pageable pageable);

  Page<String> findAllUserIds(Criteria criteria, Pageable pageable);

  Set<String> filterUsersWithMembership(List<String> userIds, String accountIdentifier, @Nullable String orgIdentifier,
      @Nullable String projectIdentifier);

  Long getProjectCount(String userId);
}
