package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.GoodPicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodPictureRepository extends JpaRepository<GoodPicture, Long> {

    List<GoodPicture> findByGoodId(Long goodId);
}
