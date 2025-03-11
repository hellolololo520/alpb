package com.kt.uam.service;

import com.kt.uam.model.SkyCondition;
import com.kt.uam.repository.SkyConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class SkyConditionService {

    @Autowired
    private SkyConditionRepository skyConditionRepository;

    @PostConstruct
    public void initializeSkyConditions() {
        // 이미 데이터가 있는지 확인
        if (skyConditionRepository.count() == 0) {
            List<SkyCondition> skyConditions = Arrays.asList(
                    createSkyCondition("1", "맑음"),
                    createSkyCondition("3", "구름많음"),
                    createSkyCondition("4", "흐림"));

            skyConditionRepository.saveAll(skyConditions);
        }
    }

    private SkyCondition createSkyCondition(String code, String description) {
        SkyCondition skyCondition = new SkyCondition();
        skyCondition.setCode(code);
        skyCondition.setDescription(description);
        return skyCondition;
    }

    public String getDescription(String code) {
        SkyCondition skyCondition = skyConditionRepository.findByCode(code);
        return skyCondition != null ? skyCondition.getDescription() : code;
    }
}