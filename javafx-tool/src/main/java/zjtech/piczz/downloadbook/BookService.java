/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zjtech.modules.common.ToolException;
import zjtech.piczz.common.DownloadErrorCode;
import zjtech.piczz.downloadbook.SingleBookEntity.StatusEnum;

@Service
@Slf4j
public class BookService {

  private final SingleBookRep bookRep;

  @Autowired
  public BookService(SingleBookRep bookRep) {
    this.bookRep = bookRep;
  }

  @Transactional
  public SingleBookEntity save(SingleBookEntity singleBookEntity) {
    if (singleBookEntity.getId() <= 0
        && bookRep.findByUrl(singleBookEntity.getUrl().trim()) != null) {
      throw new ToolException(DownloadErrorCode.BOOK_DUPLICATED);
    }
    return bookRep.save(singleBookEntity);
  }

  @Transactional
  public Collection<SingleBookEntity> saveList(Collection<SingleBookEntity> entities) {
    if (entities == null || entities.isEmpty()) {
      return new ArrayList<>();
    }
    return bookRep.saveAll(entities);
  }

  public List<SingleBookEntity> findAll() {
    return bookRep.findAll();
  }

  @Transactional
  public void deleteAll() {
    bookRep.deleteAllInBatch();
  }

  public List<SingleBookEntity> findByStatus(Stream<StatusEnum> statusEnumStream) {
    return bookRep.findByStatusIn(statusEnumStream.collect(Collectors.toList()));
  }

  public SingleBookEntity findByName(String name) {
    return bookRep.findByName(name);
  }

  @Transactional
  public int updateStatus(long id, StatusEnum statusEnum) {
    return bookRep.updatestatus(id, statusEnum);
  }

  @Transactional
  public void delete(long id) {
    bookRep.deleteById(id);
  }

  @Transactional
  public void fixNullStatus() {
    bookRep.updateNullStatus(StatusEnum.NEW_ADDED);
  }

}
