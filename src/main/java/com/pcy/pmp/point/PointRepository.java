package com.pcy.pmp.point;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long>, PointCustomRepository {

}
