package com.example.pandatribe.services;

import com.example.pandatribe.models.dtos.BlueprintDto;
import com.example.pandatribe.models.dtos.MaterialDto;
import com.example.pandatribe.models.dtos.BlueprintRequest;
import com.example.pandatribe.models.industry.CostIndex;
import com.example.pandatribe.models.industry.SystemCostIndexes;
import com.example.pandatribe.models.industry.blueprints.BlueprintActivity;
import com.example.pandatribe.models.industry.blueprints.EveType;
import com.example.pandatribe.models.market.MarketPriceData;
import com.example.pandatribe.models.universe.SystemInfo;
import com.example.pandatribe.repositories.interfaces.EveCustomRepository;
import com.example.pandatribe.repositories.interfaces.EveTypesRepository;
import com.example.pandatribe.services.contracts.BlueprintService;
import com.example.pandatribe.services.contracts.IndustryService;
import com.example.pandatribe.services.contracts.MarketService;
import com.example.pandatribe.services.contracts.MaterialService;
import com.example.pandatribe.utils.Helper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BlueprintServiceImpl implements BlueprintService {
    public static final Integer LOCATION_ID = 60003760;
    private final MaterialService materialsService;
    private final MarketService marketService;
    private final EveTypesRepository repository;
    private final EveCustomRepository eveCustomRepository;
    private final IndustryService industryService;
    private final Helper helper;


    @Override
    public BlueprintDto getBlueprintData(BlueprintRequest blueprintRequest){
        Integer quantity = Optional.ofNullable(blueprintRequest.getQuantity()).orElse(1);
        Integer materialEfficiency = Optional.ofNullable(blueprintRequest.getBlueprintMe()).orElse(0);
        Integer discountBR = Optional.ofNullable(blueprintRequest.getBuildingRig()).orElse(0);
        Integer discountB = Optional.ofNullable(blueprintRequest.getBuilding()).orElse(0);
        String system = Optional.ofNullable(blueprintRequest.getSystem()).filter(s -> !s.isEmpty()).orElse("Jita");
        Double facilityTax = Optional.ofNullable(blueprintRequest.getFacilityTax()).orElse(0.0);
        String blueprintName = blueprintRequest.getBlueprintName();
        Optional<EveType> eveType = repository.findEveTypeByTypeName(blueprintName);
        if (eveType.isEmpty()){
            return null;
        }
        BlueprintActivity blueprintActivity = eveCustomRepository.getBluePrintInfo(eveType.get().getTypeId());
        if(Objects.nonNull(blueprintActivity)) {
            SystemInfo systemInfo = eveCustomRepository.getSystemId(system);

            Integer  matBlueprintId = blueprintActivity.getBlueprintId();
            List<MaterialDto> materialsList = materialsService.getMaterialsByActivity(matBlueprintId, quantity, discountBR, materialEfficiency, discountB, systemInfo.getSecurity());
            String activity = blueprintActivity.getActivityId().equals(11) ? "reaction" : "manufacturing";
            BigDecimal industryCosts = calculateIndustryTaxes(facilityTax, systemInfo.getSystemId(), materialsList, activity, discountB);
            return BlueprintDto.builder()
                    .blueprintName(blueprintName)
                    .quantity(quantity)
                    .materialsList(materialsList)
                    .icon(helper.generateIconLink(eveType.get().getTypeId()))
                    .craftPrice(materialsList.stream().map(MaterialDto::getSellPrice).reduce(BigDecimal.ZERO,BigDecimal::add).add(industryCosts))
                    .sellPrice(marketService.getItemPrice(LOCATION_ID, marketService.getItemMarketPrice(eveType.get().getTypeId())).multiply(BigDecimal.valueOf(quantity)))
                    .build();

        }
       return null;
    }

    private BigDecimal calculateIndustryTaxes(Double facilityTax, Integer systemId, List<MaterialDto> materials, String activity, Integer buildingIndex){
        BigDecimal eiv = materials.stream().map(MaterialDto::getAdjustedPrice).reduce(BigDecimal.ZERO,BigDecimal::add);
        Integer buildingBonus = helper.getBuildingBonus(buildingIndex).getCostReduction();
        Double surcharge = 4.0;
        Double costIndex = industryService.getSystemCostIndexes().stream()
                .filter(c -> c.getSystemId().equals(systemId))
                .flatMap(systemIndex -> systemIndex.getCostIndexes().stream())
                .filter(c -> c.getActivity().equals(activity))
                .findFirst()
                .map(CostIndex::getCostIndex)
                .orElse(0.0);
        BigDecimal price = eiv.multiply(BigDecimal.valueOf(costIndex));
        price = price.subtract(price.multiply(BigDecimal.valueOf(buildingBonus/100)));
        price = price.add(eiv.multiply(BigDecimal.valueOf(facilityTax/100)));
        price = price.add(eiv.multiply(BigDecimal.valueOf(surcharge/100)));
        return price.setScale(0, RoundingMode.CEILING);
    }
}
