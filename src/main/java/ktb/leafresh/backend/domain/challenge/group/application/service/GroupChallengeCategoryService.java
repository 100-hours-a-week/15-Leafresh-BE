package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChallengeCategoryService {

    private final GroupChallengeCategoryRepository categoryRepository;

    public List<GroupChallengeCategoryResponseDto> getCategories() {
        return categoryRepository.findAllByActivatedIsTrueOrderBySequenceNumberAsc()
                .stream()
                .map(category -> GroupChallengeCategoryResponseDto.builder()
                        .id(category.getId())
                        .category(category.getName())
                        .label(getLabelFromCategoryName(category.getName()))
                        .imageUrl(category.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    private String getLabelFromCategoryName(String name) {
        return switch (name) {
            case "ZERO_WASTE" -> "제로웨이스트";
            case "PLOGGING" -> "플로깅";
            case "CARBON_FOOTPRINT" -> "탄소 발자국";
            case "ENERGY_SAVING" -> "에너지 절약";
            case "UPCYCLING" -> "중고거래/업사이클";
            case "MEDIA" -> "서적, 영화";
            case "DIGITAL_CARBON" -> "디지털 탄소";
            case "VEGAN" -> "비건";
            case "ETC" -> "기타";
            default -> name;
        };
    }
}
