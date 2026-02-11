package com.example.mangxahoi.Service;

import com.example.mangxahoi.Entity.District;
import com.example.mangxahoi.Entity.Province;
import com.example.mangxahoi.Entity.Ward;
import com.example.mangxahoi.Repository.DistrictRepo;
import com.example.mangxahoi.Repository.ProvinceRepo;
import com.example.mangxahoi.Repository.WardRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AddressSyncService {

    @Autowired
    RestTemplate rest;
    @Autowired
    ProvinceRepo provinceRepo;
    @Autowired
    DistrictRepo districtRepo;
    @Autowired
    WardRepo wardRepo;

    public void syncAll(){

        Province[] provinces = rest.getForObject(
                "https://provinces.open-api.vn/api/p/", Province[].class);

        provinceRepo.saveAll(List.of(provinces));

        for(Province p : provinces){
            Map data = rest.getForObject(
                    "https://provinces.open-api.vn/api/p/"+p.getCode()+"?depth=2",
                    Map.class);

            List<Map> districts = (List<Map>) data.get("districts");

            for(Map d : districts){
                districtRepo.save(new District(
                        (Integer)d.get("code"),
                        (String)d.get("name"),
                        p.getCode()
                ));

                Map wardData = rest.getForObject(
                        "https://provinces.open-api.vn/api/d/"+d.get("code")+"?depth=2",
                        Map.class);

                List<Map> wards = (List<Map>) wardData.get("wards");

                for(Map w : wards){
                    wardRepo.save(new Ward(
                            (Integer)w.get("code"),
                            (String)w.get("name"),
                            (Integer)d.get("code")
                    ));
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        if (provinceRepo.count() == 0) {
            syncAll();
        }
    }
}
