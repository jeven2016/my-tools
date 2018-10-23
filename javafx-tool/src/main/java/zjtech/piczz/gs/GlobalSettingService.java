/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.gs;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalSettingService {

  private final GlobalSettingRep rep;

  @Autowired
  public GlobalSettingService(GlobalSettingRep rep) {
    this.rep = rep;
  }

  @Transactional
  public GlobalSettingEntity saveOrUpdate(GlobalSettingEntity globalSettingEntity) {
    return rep.save(globalSettingEntity);
  }

  public Optional<GlobalSettingEntity> getOne() {
    return rep.findById(1L);
  }

  public List<GlobalSettingEntity> findAll() {
    return rep.findAll();
  }
}
