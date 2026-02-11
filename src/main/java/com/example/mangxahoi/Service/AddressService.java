package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.InfoUser.LocationItemDTO;
import com.example.mangxahoi.Repository.DistrictRepo;
import com.example.mangxahoi.Repository.ProvinceRepo;
import com.example.mangxahoi.Repository.WardRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AddressService {
    private final ProvinceRepo provinceRepo;
    private final DistrictRepo districtRepo;
    private final WardRepo wardRepo;

    public List<LocationItemDTO> getProvinces() {
        return provinceRepo.findAllByOrderByNameAsc()
                .stream().map(p -> new LocationItemDTO(p.getCode(), p.getName()))
                .toList();
    }

    public List<LocationItemDTO> getDistricts(Integer provinceCode) {
        if (provinceCode == null) return List.of();
        return districtRepo.findByProvinceCodeOrderByNameAsc(provinceCode)
                .stream().map(d -> new LocationItemDTO(d.getCode(), d.getName()))
                .toList();
    }

    public List<LocationItemDTO> getWards(Integer districtCode) {
        if (districtCode == null) return List.of();
        return wardRepo.findByDistrictCodeOrderByNameAsc(districtCode)
                .stream().map(w -> new LocationItemDTO(w.getCode(), w.getName()))
                .toList();
    }
}
