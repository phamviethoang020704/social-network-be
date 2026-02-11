package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WardRepo extends JpaRepository<Ward,Integer> {
    List<Ward> findByDistrictCodeOrderByNameAsc(Integer districtCode);
}
