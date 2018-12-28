/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

public abstract class AbstractController {


  protected String getResource(String key) {
    return ResourceLocater.getBundle().getString(key);
  }
}
