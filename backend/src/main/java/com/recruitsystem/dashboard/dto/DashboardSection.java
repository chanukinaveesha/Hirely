package com.recruitsystem.dashboard.dto;

/**
 * Strategy Pattern - result type returned by a DashboardStrategy.
 * Each implementation knows which field of DashboardResponse it belongs in,
 * so the client never has to branch on role or type to assemble the response.
 */
public interface DashboardSection {

    void applyTo(DashboardResponse.DashboardResponseBuilder builder);
}
