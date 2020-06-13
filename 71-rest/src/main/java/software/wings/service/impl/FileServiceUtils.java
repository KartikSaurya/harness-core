package software.wings.service.impl;

import static io.harness.eraro.ErrorCode.FILE_INTEGRITY_CHECK_FAILED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.harness.beans.FileMetadata;
import io.harness.exception.WingsException;
import lombok.experimental.UtilityClass;
import org.bson.types.ObjectId;
import software.wings.beans.BaseFile;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * An utility class for recognizing the file ID format. And get file UUID out of the file ID.
 *
 * @author marklu on 2018-11-30
 */
@UtilityClass
public class FileServiceUtils {
  static final String GCS_ID_PREFIX = "_gcs_:";
  static final String FILE_PATH_SEPARATOR = "/";

  public static String getFileUuid(String fileId) {
    if (isGoogleCloudFileIdFormat(fileId)) {
      return parseGoogleCloudFileId(fileId).fileUuid;
    } else if (isMongoFileIdFormat(fileId)) {
      return fileId;
    } else {
      throw new IllegalArgumentException("Can't recognize the format of this file id: " + fileId);
    }
  }

  public static boolean isGoogleCloudFileIdFormat(String fileId) {
    boolean result = false;
    try {
      byte[] idBytes = Base64.getDecoder().decode(fileId);
      String idString = new String(idBytes, Charset.defaultCharset());
      result = idString.startsWith(GCS_ID_PREFIX);
    } catch (Exception e) {
      // Ignore, it means it's not the GCS file id format
    }
    return result;
  }

  public static boolean isMongoFileIdFormat(String fileId) {
    try {
      new ObjectId(fileId);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  static void verifyFileIntegrity(BaseFile baseFile, FileMetadata fileMetadata) {
    if (isNotBlank(baseFile.getChecksum()) && !fileMetadata.getChecksum().equals(baseFile.getChecksum())) {
      throw new WingsException(FILE_INTEGRITY_CHECK_FAILED);
    }
  }

  static GoogleCloudFileIdComponent parseGoogleCloudFileId(String fileId) {
    byte[] idBytes = Base64.getDecoder().decode(fileId);
    String idString = new String(idBytes, Charset.defaultCharset());
    if (!idString.startsWith(GCS_ID_PREFIX)) {
      throw new IllegalArgumentException("Mal-formed google cloud storage based file id '" + fileId + "'.");
    } else {
      String filePath = idString.substring(GCS_ID_PREFIX.length());
      GoogleCloudFileIdComponent component = new GoogleCloudFileIdComponent();
      component.filePath = filePath;

      String[] filePathParts = filePath.split(FILE_PATH_SEPARATOR);
      if (filePathParts.length == 2) {
        component.accountId = filePathParts[0];
        component.fileUuid = filePathParts[1];
      } else {
        component.fileUuid = filePathParts[0];
      }
      return component;
    }
  }

  static class GoogleCloudFileIdComponent {
    String filePath;
    String accountId;
    String fileUuid;
  }
}
