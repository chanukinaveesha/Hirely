package com.recruitsystem.dashboard.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.DashboardSection;
import org.junit.jupiter.api.Test;

class DashboardContextTest {

    @Test
    void execute_delegatesToCurrentStrategy() {
        DashboardStrategy strategy = mock(DashboardStrategy.class);
        DashboardSection section = mock(DashboardSection.class);
        when(strategy.buildSection(1L)).thenReturn(section);

        DashboardContext context = new DashboardContext();
        context.setStrategy(strategy);

        assertThat(context.execute(1L)).isSameAs(section);
    }

    @Test
    void setStrategy_swapsStrategyAtRuntimeOnSameContext() {
        DashboardStrategy first = mock(DashboardStrategy.class);
        DashboardSection firstSection = mock(DashboardSection.class);
        when(first.buildSection(1L)).thenReturn(firstSection);

        DashboardStrategy second = mock(DashboardStrategy.class);
        DashboardSection secondSection = mock(DashboardSection.class);
        when(second.buildSection(1L)).thenReturn(secondSection);

        DashboardContext context = new DashboardContext();

        context.setStrategy(first);
        assertThat(context.execute(1L)).isSameAs(firstSection);

        context.setStrategy(second);
        assertThat(context.execute(1L)).isSameAs(secondSection);
    }
}
