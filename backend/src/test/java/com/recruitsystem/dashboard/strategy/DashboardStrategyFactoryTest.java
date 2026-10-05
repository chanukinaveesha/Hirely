package com.recruitsystem.dashboard.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recruitsystem.entity.auth.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardStrategyFactoryTest {

    private DashboardStrategy strategyFor(UserRole role) {
        DashboardStrategy strategy = mock(DashboardStrategy.class);
        when(strategy.getRole()).thenReturn(role);
        return strategy;
    }

    @Test
    void getStrategy_returnsTheConcreteStrategyRegisteredForEachRole() {
        DashboardStrategy candidate = strategyFor(UserRole.JOB_SEEKER);
        DashboardStrategy recruiter = strategyFor(UserRole.RECRUITER);
        DashboardStrategy hr = strategyFor(UserRole.HR_EXECUTIVE);
        DashboardStrategy panel = strategyFor(UserRole.INTERVIEW_PANEL_MEMBER);
        DashboardStrategy admin = strategyFor(UserRole.SYSTEM_ADMINISTRATOR);

        DashboardStrategyFactory factory = new DashboardStrategyFactory(
                List.of(candidate, recruiter, hr, panel, admin));

        assertThat(factory.getStrategy(UserRole.JOB_SEEKER)).isSameAs(candidate);
        assertThat(factory.getStrategy(UserRole.RECRUITER)).isSameAs(recruiter);
        assertThat(factory.getStrategy(UserRole.HR_EXECUTIVE)).isSameAs(hr);
        assertThat(factory.getStrategy(UserRole.INTERVIEW_PANEL_MEMBER)).isSameAs(panel);
        assertThat(factory.getStrategy(UserRole.SYSTEM_ADMINISTRATOR)).isSameAs(admin);
    }

    @Test
    void getStrategy_unsupportedRole_throwsClearException() {
        DashboardStrategyFactory factory = new DashboardStrategyFactory(List.of());

        assertThatThrownBy(() -> factory.getStrategy(UserRole.JOB_SEEKER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JOB_SEEKER");
    }
}
