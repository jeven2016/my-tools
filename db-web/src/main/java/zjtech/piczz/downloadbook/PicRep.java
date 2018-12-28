package zjtech.piczz.downloadbook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface PicRep extends JpaRepository<SinglePictureEntity, Long> {

  /**
   * 不考虑缓存情况下的直接删除
   */
  @Query("delete from SinglePictureEntity a")
  @Modifying
  @Transactional
  void deleteAll();

}
