/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import zjtech.piczz.downloadbook.SingleBookEntity.StatusEnum;

public interface SingleBookRep extends JpaRepository<SingleBookEntity, Long> {

  SingleBookEntity findByUrl(String url);

  @Override
  @EntityGraph(value = "book.all", type = EntityGraph.EntityGraphType.FETCH)
  List<SingleBookEntity> findAll();


  /**
   * 不使用jpa2.1的这个新特性，将会有很多sql查询语句。用了此特性后子表查询会使用left outer join方式去查询关联表
   */
  @EntityGraph(value = "book.all", type = EntityGraph.EntityGraphType.FETCH)
  List<SingleBookEntity> findByStatusIn(Collection<StatusEnum> statusEnumCollection);

  @Modifying
  @Query("update SingleBookEntity b set b.status=?2 where b.id=?1")
  int updatestatus(Long id, StatusEnum statusEnum);
}
