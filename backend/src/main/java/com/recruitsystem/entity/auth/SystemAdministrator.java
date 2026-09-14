package com.recruitsystem.entity.auth;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "system_administrators")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("SYSTEM_ADMINISTRATOR")
@NoArgsConstructor
@SuperBuilder
public class SystemAdministrator extends User {

    @Override
    public UserRole getRole() {
        return UserRole.SYSTEM_ADMINISTRATOR;
    }
}
