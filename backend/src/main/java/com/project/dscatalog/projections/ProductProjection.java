package com.project.dscatalog.projections;

// Agora a classe está herdando uma interface genérica
public interface ProductProjection extends IdProjection<Long> {

    String getName();
}
