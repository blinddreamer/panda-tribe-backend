package com.example.panda2.services;

import com.example.panda2.models.*;
import com.example.panda2.models.dtos.BlueprintDto;
import com.example.panda2.models.dtos.MaterialDto;
import com.example.panda2.models.dtos.SearchDto;
import com.example.panda2.repositories.interfaces.EveTypesRepository;
import com.example.panda2.services.contracts.BlueprintService;
import com.example.panda2.services.contracts.MaterialService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BlueprintServiceImpl implements BlueprintService {
    private final MaterialService materialsService;
    private final EveTypesRepository repository;

    @Override
    public BlueprintDto generateConstructionData(SearchDto searchDto){
        Integer quantity = searchDto.getQuantity();
        Integer discountBP = searchDto.getBlueprintMe();
        Double discountBR = searchDto.getBuildingRig();
        Integer discountB = searchDto.getBuilding();
        String blueprintName = searchDto.getShipName() + " Blueprint";
        Optional<EveType> eveType = repository.findEveTypeByTypeName(blueprintName);
        if (eveType.isEmpty()){
            return null;
        }
        List<MaterialDto> materialsList = materialsService.getMaterialsByActivity(eveType.get().getTypeId(), quantity, discountBR, discountBP, discountB);
        return BlueprintDto.builder()
                .blueprintName(blueprintName)
                .materialsList(materialsList)
                .build();
    }
}
