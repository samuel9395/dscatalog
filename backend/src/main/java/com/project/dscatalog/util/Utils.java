package com.project.dscatalog.util;

import com.project.dscatalog.entities.Product;
import com.project.dscatalog.projections.ProductProjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    // Reorganiza a lista de entidades para seguir a mesma ordem
    // retornada pela consulta paginada (ProductProjection).
    public static List<Product> replace(List<ProductProjection> ordered, List<Product> unordered) {

        // Mapeia cada produto pelo seu ID para permitir busca rápida.
        Map<Long, Product> map = new HashMap<>();
        for (Product obj : unordered) {
            map.put(obj.getId(), obj);
        }

        // Reconstrói a lista respeitando a ordem da primeira consulta.
        List<Product> result = new ArrayList<>();
        for (ProductProjection obj : ordered) {
            result.add(map.get(obj.getId()));
        }

        return result;
    }
}
