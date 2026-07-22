package com.project.dscatalog.util;

import com.project.dscatalog.entities.Product;
import com.project.dscatalog.projections.IdProjection;
import com.project.dscatalog.projections.ProductProjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    // Reorganiza a lista de entidades para seguir a mesma ordem
    // da lista de projeções utilizada na consulta paginada.
    public static <ID> List<? extends IdProjection<ID>> replace(List<? extends IdProjection<ID>> ordered,
            List<? extends IdProjection<ID>> unordered) {

        // Mapeia as entidades pelo ID para permitir acesso rápido.
        Map<ID, IdProjection<ID>> map = new HashMap<>();
        for (IdProjection<ID> obj : unordered) {
            map.put(obj.getId(), obj);
        }

        // Reconstrói a lista respeitando a ordem da primeira consulta.
        List<IdProjection<ID>> result = new ArrayList<>();
        for (IdProjection<ID> obj : ordered) {
            result.add(map.get(obj.getId()));
        }

        return result;
    }
}
