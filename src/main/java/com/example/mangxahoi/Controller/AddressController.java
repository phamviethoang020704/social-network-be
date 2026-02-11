package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.InfoUser.LocationItemDTO;
import com.example.mangxahoi.Service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService locationService;

    @GetMapping("/provinces")
    public List<LocationItemDTO> provinces() {
        return locationService.getProvinces();
    }

    @GetMapping("/districts")
    public List<LocationItemDTO> districts(@RequestParam Integer provinceCode) {
        return locationService.getDistricts(provinceCode);
    }

    @GetMapping("/wards")
    public List<LocationItemDTO> wards(@RequestParam Integer districtCode) {
        return locationService.getWards(districtCode);
    }
}
