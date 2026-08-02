package com.education.web.grading;

import com.education.web.auth.model.OrganizationEntity;
import com.education.web.auth.repository.OrganizationJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class GradingStrategyResolver {

    private final OrganizationJpaRepository organizations;
    private final SumStarGradingStrategy sumStrategy;
    private final AverageStarGradingStrategy averageStrategy;

    public GradingStrategyResolver(
            OrganizationJpaRepository organizations,
            SumStarGradingStrategy sumStrategy,
            AverageStarGradingStrategy averageStrategy
    ) {
        this.organizations = organizations;
        this.sumStrategy = sumStrategy;
        this.averageStrategy = averageStrategy;
    }

    public GradingMethod methodForOrganization(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return GradingMethod.SUM;
        }
        return organizations.findById(organizationId.trim())
                .map(OrganizationEntity::getGradingMethod)
                .map(GradingMethod::fromWire)
                .orElse(GradingMethod.SUM);
    }

    public StarGradingStrategy strategyForOrganization(String organizationId) {
        return strategyFor(methodForOrganization(organizationId));
    }

    public StarGradingStrategy strategyFor(GradingMethod method) {
        return method == GradingMethod.AVERAGE ? averageStrategy : sumStrategy;
    }
}
