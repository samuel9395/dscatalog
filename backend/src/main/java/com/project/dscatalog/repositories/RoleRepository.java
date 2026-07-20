package com.project.dscatalog.repositories;

import com.project.dscatalog.entities.Role;
import com.project.dscatalog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


}
