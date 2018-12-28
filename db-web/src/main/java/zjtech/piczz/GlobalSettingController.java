/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import zjtech.modules.common.AbstractController;
import zjtech.piczz.gs.GlobalSettingEntity;
import zjtech.piczz.gs.GlobalSettingService;

@RestController
@RequestMapping("/global")
@Slf4j
@Lazy
public class GlobalSettingController extends AbstractController {

  private final GlobalSettingService settingService;


  @Autowired
  public GlobalSettingController(GlobalSettingService settingService) {
    this.settingService = settingService;
  }


  @PostMapping
  public Mono<GlobalSettingEntity> save(@RequestBody GlobalSettingEntity globalSettingEntity) {
    return Mono.just(settingService.saveOrUpdate(globalSettingEntity));
  }

  @GetMapping
  public Mono<GlobalSettingEntity> get() {
    return Mono.just(settingService.getOne().get());
  }
}
