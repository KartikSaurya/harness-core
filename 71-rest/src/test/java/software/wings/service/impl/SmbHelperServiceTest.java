package software.wings.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static software.wings.service.impl.instance.InstanceHelperTest.ACCOUNT_ID;

import com.google.inject.Inject;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import org.junit.Test;
import org.mockito.Mock;
import software.wings.WingsBaseTest;
import software.wings.beans.SmbConfig;
import software.wings.helpers.ext.jenkins.BuildDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmbHelperServiceTest extends WingsBaseTest {
  @Inject SmbHelperService smbHelperService;
  @Mock SMBClient smbClient;
  @Mock Connection connection;
  @Mock Session session;
  @Mock DiskShare diskShare;
  @Mock FileIdBothDirectoryInformation fileIdBothDirectoryInformation;
  @Mock SmbHelperService mockSMBHelperService;

  private static final String SHARE_FOLDER = "share";
  private static final String PATH = "";
  private static final String SEARCH_PATTERN = "*";
  private static final String DOMAIN = "test";
  private static final String USER = "test";
  private static final String PASSWORD = "test";
  private static final String SHARE_URL = "smb:\\\\10.0.0.1\\share";

  private static final SmbConfig smbConfig = SmbConfig.builder()
                                                 .accountId(ACCOUNT_ID)
                                                 .domain(DOMAIN)
                                                 .username(USER)
                                                 .password(PASSWORD.toCharArray())
                                                 .smbUrl(SHARE_URL)
                                                 .build();

  @Test
  public void shouldGetSmbPaths() throws IOException {
    // Mock SmbClient, Connection, DiskShare and Session
    doReturn(connection).when(smbClient).connect(smbHelperService.getSMBConnectionHost(smbConfig.getSmbUrl()));
    AuthenticationContext ac =
        new AuthenticationContext(smbConfig.getUsername(), smbConfig.getPassword(), smbConfig.getDomain());
    doReturn(session).when(connection).authenticate(ac);
    doReturn(diskShare).when(session).connectShare(SHARE_FOLDER);

    // List files and directories from share
    List<FileIdBothDirectoryInformation> fileInfoList = new ArrayList<>();
    fileInfoList.add(fileIdBothDirectoryInformation);
    when(diskShare.list(PATH, SEARCH_PATTERN)).thenReturn(fileInfoList);
    assertThat(fileInfoList).isNotNull().isNotEmpty().hasSize(1);
  }

  @Test
  public void shouldGetSmbConnectionHost() {
    assertThat(smbHelperService.getSMBConnectionHost(SHARE_URL)).isNotEmpty().isEqualTo("10.0.0.1");
    assertThat(smbHelperService.getSMBConnectionHost("smb://10.0.0.1/share")).isNotEmpty().isEqualTo("10.0.0.1");
  }

  @Test
  public void shouldGetSmbSharedFolder() {
    assertThat(smbHelperService.getSharedFolderName(SHARE_URL)).isNotEmpty().isEqualTo("share");
    assertThat(smbHelperService.getSharedFolderName("smb://10.0.0.1/share")).isNotEmpty().isEqualTo("share");
  }

  @Test
  public void shouldGetSmbArtifactDetails() throws IOException {
    // Mock SmbClient, Connection, DiskShare and Session
    doReturn(connection).when(smbClient).connect(smbHelperService.getSMBConnectionHost(smbConfig.getSmbUrl()));
    AuthenticationContext ac =
        new AuthenticationContext(smbConfig.getUsername(), smbConfig.getPassword(), smbConfig.getDomain());
    doReturn(session).when(connection).authenticate(ac);
    doReturn(diskShare).when(session).connectShare(SHARE_FOLDER);

    // List mock artifact build details
    List<BuildDetails> buildDetails = new ArrayList<>();
    buildDetails.add(BuildDetails.Builder.aBuildDetails().build());
    doReturn(buildDetails).when(mockSMBHelperService).getArtifactDetails(smbConfig, null, Collections.EMPTY_LIST);
    assertThat(buildDetails).isNotEmpty().hasSize(1);
  }
}