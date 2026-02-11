package com.example.mangxahoi.Repository;

import com.example.mangxahoi.Entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepo extends JpaRepository<District,Integer> {
    List<District> findByProvinceCodeOrderByNameAsc(Integer provinceCode);
}
