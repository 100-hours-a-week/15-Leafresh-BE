package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCategoryResponseDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChallengeCategoryService {

    private final GroupChallengeCategoryRepository categoryRepository;

    public List<GroupChallengeCategoryResponseDto> getCategories() {
        try {
            List<GroupChallengeCategoryResponseDto> categories = categoryRepository
                    .findAllByActivatedIsTrueOrderBySequenceNumberAsc()
                    .stream()
                    .filter(category -> !category.getName().equalsIgnoreCase("ETC"))
                    .map(category -> GroupChallengeCategoryResponseDto.builder()
                            .category(category.getName())
                            .label(getLabelFromCategoryName(category.getName()))
                            .imageUrl(category.getImageUrl())
                            .build())
                    .collect(Collectors.toList());

            if (categories.isEmpty()) {
                throw new CustomException(ChallengeErrorCode.CHALLENGE_CATEGORY_LIST_EMPTY);
            }

            return categories;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ChallengeErrorCode.CHALLENGE_CATEGORY_READ_FAILED);
        }
    }

    private String getLabelFromCategoryName(String name) {
        return switch (name) {
            case "ALL" -> "전체";
            case "ZERO_WASTE" -> "제로웨이스트";
            case "PLOGGING" -> "플로깅";
            case "CARBON_FOOTPRINT" -> "탄소 발자국";
            case "ENERGY_SAVING" -> "에너지 절약";
            case "UPCYCLING" -> "업사이클";
            case "MEDIA" -> "문화 공유";
            case "DIGITAL_CARBON" -> "디지털 탄소";
            case "VEGAN" -> "비건";
            case "ETC" -> "기타";
            default -> name;
        };
    }
}
