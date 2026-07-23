package com.project.dscatalog.repositories;

import com.project.dscatalog.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Busca uma role pelo nome da authority (ex.: ROLE_OPERATOR).
    Role findByAuthority(String authority);
}
