/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

public class ToolException extends RuntimeException {

  private ErrorCode errorCode;

  public <T> ToolException(ErrorCode<T> errorCode) {
    super(null, null, true, false);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return this.errorCode;
  }

}
