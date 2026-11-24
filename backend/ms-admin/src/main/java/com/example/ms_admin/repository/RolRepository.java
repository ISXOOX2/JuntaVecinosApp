package com.example.ms_admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ms_admin.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    boolean existsByCodigo(String codigo);
}
