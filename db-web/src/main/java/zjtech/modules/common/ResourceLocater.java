/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

import java.util.ResourceBundle;

public class ResourceLocater {

  private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("pc");

  public static ResourceBundle getBundle() {
//    return ResourceBundle.getBundle("pc", new Locale("eh", "US"));
    return resourceBundle;
  }
}
