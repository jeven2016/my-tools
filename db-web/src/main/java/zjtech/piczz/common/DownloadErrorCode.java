/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.common;

import zjtech.modules.common.ErrorCode;

public class DownloadErrorCode extends ErrorCode<String> {

  public static final DownloadErrorCode BOOK_DUPLICATED = new DownloadErrorCode(
      "error.book.duplicated");


  public DownloadErrorCode(String code) {
    super(code);
  }
}
