/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.utils;

import static java.util.Arrays.stream;

import java.io.BufferedInputStream;

/**
 * Created by peeyushaggarwal on 9/6/16.
 */
public class FileTypeDetector {
  /**
   * Detect type file type.
   *
   * @param bufferedInputStream the buffered input stream
   * @return the file type
   */
  public static FileType detectType(BufferedInputStream bufferedInputStream) {
    return stream(FileType.values()).filter(fileType -> fileType.test(bufferedInputStream)).findFirst().get();
  }
}
