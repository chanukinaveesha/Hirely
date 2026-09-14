package com.recruitsystem.entity.auth;

import com.recruitsystem.entity.company.ClientCompany;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hr_executives")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("HR_EXECUTIVE")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class HrExecutive extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_company_id")
    private ClientCompany clientCompany;

    @Override
    public UserRole getRole() {
        return UserRole.HR_EXECUTIVE;
    }
}
