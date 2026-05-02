package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodPicture;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodPictureRepository extends JpaRepository<GoodPicture, Long> {

    List<GoodPicture> findByGoodId(Long goodId);

    @Query("SELECT p.good.id AS goodId, MIN(p.id) AS pictureId FROM GoodPicture p WHERE p.good.id IN :goodIds GROUP BY p.good.id")
    List<Tuple> findFirstPictureIdGroupedByGood(@Param("goodIds") List<Long> goodIds);
}
