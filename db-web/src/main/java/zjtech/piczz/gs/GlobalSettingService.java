/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.gs;

import java.util.Optional;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@CacheConfig(cacheNames = {"globalSetting"})
@Service
@Slf4j
public class GlobalSettingService {

  private final GlobalSettingRep rep;

  @Autowired
  public GlobalSettingService(GlobalSettingRep rep) {
    this.rep = rep;
  }

  @CachePut(key = "'gs'")
  @Transactional
  public GlobalSettingEntity saveOrUpdate(GlobalSettingEntity globalSettingEntity) {
    log.info("update service...........");
    return rep.save(globalSettingEntity);
  }


  @Cacheable(key = "'gs'")
  public Optional<GlobalSettingEntity> getOne() {
    log.info("get service...........");
    return rep.findById(1L);
  }

}
