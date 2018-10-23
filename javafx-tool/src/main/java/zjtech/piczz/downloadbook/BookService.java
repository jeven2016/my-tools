/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import zjtech.modules.common.ToolException;
import zjtech.piczz.common.DownloadErrorCode;

@Service
@Slf4j
public class BookService {

  @Autowired
  SingleBookRep bookRep;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public SingleBookEntity save(SingleBookEntity singleBookEntity) {
    if (bookRep.findByUrl(singleBookEntity.getUrl().trim()) != null) {
      throw new ToolException(DownloadErrorCode.BOOK_DUPLICATED);
    }
    return bookRep.save(singleBookEntity);
  }

  public List<SingleBookEntity> findAll() {
    return bookRep.findAll();
  }

  @Transactional
  public void delete(long id) {
    bookRep.deleteById(id);
  }
}
