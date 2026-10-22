package com.project.dscatalog.services;


import com.project.dscatalog.dto.CategoryDTO;
import com.project.dscatalog.dto.ProductDTO;
import com.project.dscatalog.dto.UriDTO;
import com.project.dscatalog.entities.Category;
import com.project.dscatalog.entities.Product;
import com.project.dscatalog.projections.ProductProjection;
import com.project.dscatalog.repositories.CategoryRepository;
import com.project.dscatalog.repositories.ProductRepository;
import com.project.dscatalog.services.exceptions.DatabaseException;
import com.project.dscatalog.services.exceptions.ResourceEntityNotFoundException;
import com.project.dscatalog.util.Utils;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Camada de serviço responsável pelas regras de negócio relacionadas aos produtos,
 * incluindo operações de CRUD, filtros avançados, paginação e mapeamento para DTOs.
 * <p>
 * Esta classe atua como intermediária entre a camada de controle (Controller)
 * e os repositórios de dados, centralizando a lógica e as transações da aplicação.
 * </p>
 */
@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private S3Service s3Service;

    /**
     * Lista produtos de forma paginada sem filtros.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable) {
        Page<Product> list = repository.findAll(pageable);
        return list.map(ProductDTO::new);
    }

    /**
     * Busca um produto específico pelo seu identificador único.
     * <p>
     * O método realiza uma consulta transacional somente leitura para recuperar o produto
     * e suas respectivas categorias, lançando uma exceção caso o registro não seja encontrado.
     * </p>
     *
     * @param id Identificador único do produto desejado.
     * @return Um {@link ProductDTO} contendo os dados do produto e suas categorias associadas.
     * @throws ResourceEntityNotFoundException Se nenhum produto for encontrado com o ID fornecido.
     */
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = repository.findById(id);

        // Recupera a entidade ou dispara uma exceção personalizada caso não exista
        Product entity = obj.orElseThrow(() -> new ResourceEntityNotFoundException("Entity not found with id: " + id));

        return new ProductDTO(entity, entity.getCategories());
    }

    /**
     * Insere um novo produto no banco de dados.
     * <p>
     * O método converte os dados recebidos no DTO para a entidade correspondente,
     * realiza a persistência e retorna o DTO atualizado contendo o identificador gerado.
     * </p>
     *
     * @param dto Objeto de transferência de dados contendo as informações do novo produto.
     * @return Um {@link ProductDTO} representando o produto recém-criado.
     */
    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();

        // Copia os dados do DTO para a nova entidade (incluindo o mapeamento de categorias)
        copyDtoToEntity(dto, entity);

        // Salva a entidade no banco de dados
        entity = repository.save(entity);

        return new ProductDTO(entity);
    }

    /**
     * Atualiza os dados de um produto existente com base no seu identificador.
     * <p>
     * Este método utiliza a referência da entidade para otimizar a operação de atualização
     * e trata possíveis falhas caso o registro não seja encontrado no banco de dados.
     * </p>
     *
     * @param id  Identificador único do produto que será atualizado.
     * @param dto Objeto de transferência de dados contendo as novas informações do produto.
     * @return Um {@link ProductDTO} representando o produto após a atualização.
     * @throws ResourceEntityNotFoundException Se nenhum produto for encontrado com o ID fornecido.
     */
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            // Obtém a referência do produto pelo ID de forma otimizada
            Product entity = repository.getReferenceById(id);

            // Atualiza os dados básicos e o relacionamento de categorias
            entity.setName(dto.getName());
            copyDtoToEntity(dto, entity);

            return new ProductDTO(entity);
        } catch (EntityNotFoundException e) {
            // Converte a exceção de persistência em uma exceção de negócio personalizada
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
    }

    /**
     * Remove um produto do banco de dados com base no seu identificador.
     * <p>
     * O método verifica previamente se o registro existe. Caso ocorra uma violação de
     * integridade referencial (por exemplo, o produto possui vínculos com outras tabelas),
     * a exceção nativa é tratada e convertida para uma exceção personalizada.
     * </p>
     *
     * @param id Identificador único do produto que será excluído.
     * @throws ResourceEntityNotFoundException Se nenhum produto for encontrado com o ID fornecido.
     * @throws DatabaseException               Se houver uma falha de integridade referencial ao tentar deletar o registro.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) throws DatabaseException {
        // Verifica se o produto realmente existe antes de tentar a exclusão
        if (!repository.existsById(id)) {
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
        try {
            repository.deleteById(id);
            log.info("Delete Product with id: " + id);
        }
        catch (DataIntegrityViolationException e) {
            // Trata violações de chave estrangeira ou restrições do banco de dados
            throw new DatabaseException("Referential integrity failure!");
        }
    }

    /**
     * Lista os produtos de forma paginada aplicando filtros por nome e categorias,
     * utilizando uma estratégia de consulta em duas etapas para otimizar a performance
     * e evitar o problema de N+1 consultas.
     * <p>
     * A primeira etapa busca apenas os IDs e dados essenciais da página atual (evitando
     * paginação em memória ou joins complexos que quebram o count). A segunda etapa
     * carrega as entidades completas com suas respectivas categorias usando os IDs obtidos.
     * </p>
     *
     * @param name       Filtro opcional para buscar produtos por nome (contendo o termo).
     * @param categoryId Filtro opcional contendo IDs de categorias separados por vírgula (ex: "1,2,3").
     * @param pageable   Parâmetros de paginação e ordenação (página atual, tamanho, direção).
     * @return Um {@link Page} contendo os {@link ProductDTO} correspondentes à página solicitada.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable) {

        // Converte a String "1,2,3" em uma lista de IDs de categoria.
        // Se nenhum filtro for informado, utiliza uma lista vazia.
        List<Long> categoryIds = Arrays.asList();
        if (categoryId != null && !categoryId.isEmpty()) {
            categoryIds = Arrays.stream(categoryId.split(","))
                    .map(Long::parseLong)
                    .toList();
        }

        // Primeira consulta: busca apenas os dados necessários para a paginação
        // (projeção com os IDs dos produtos da página atual).
        Page<ProductProjection> page = repository.searchProducts(categoryIds, name, pageable);

        // Extrai os IDs retornados pela primeira consulta.
        List<Long> productsIds = page.map(ProductProjection::getId).toList();

        // Segunda consulta: carrega as entidades completas com suas categorias
        // usando JOIN FETCH, evitando o problema de N+1 consultas.
        // Lista desordenada vinda do banco.
        List<Product> entities = repository.searchProductsWithCategories(productsIds);

        // Reorganiza as entidades na mesma ordem da consulta paginada,
        // preservando a ordenação original dos resultados.
        // Aqui foi feito o Cast de List<Product>, pois o metodo usado é genérico.
        // Lista convertida em lista ordenada, com base na ordenação da página.
        entities = (List<Product>) Utils.replace(page.getContent(), entities);

        // Converte as entidades para DTOs.
        List<ProductDTO> dtos = entities.stream()
                .map(product -> new ProductDTO(product, product.getCategories()))
                .toList();

        // Reconstrói a página utilizando os DTOs, preservando as informações
        // de paginação e a quantidade total de registros.
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    /**
     * Copia os dados de um {@link ProductDTO} para uma entidade {@link Product},
     * incluindo a atualização dos relacionamentos com as categorias.
     * <p>
     * Este método limpa a coleção atual de categorias da entidade e realiza a associação
     * com as novas categorias obtidas do repositório com base nos IDs informados no DTO.
     * </p>
     *
     * @param dto    Objeto de transferência de dados contendo as informações atualizadas do produto.
     * @param entity Entidade de produto que receberá os novos valores e mapeamento de categorias.
     */
    private void copyDtoToEntity(ProductDTO dto, Product entity) {
        // Copia os atributos básicos do produto
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());

        // Atualiza o relacionamento de categorias (limpa as antigas e adiciona as novas)
        entity.getCategories().clear();
        for (CategoryDTO catDTO : dto.getCategories()) {
            Category category = categoryRepository.getOne(catDTO.getId());
            entity.getCategories().add(category);
        }
    }

    /**
     * Realiza o upload de um arquivo para o Amazon S3 utilizando o serviço de storage
     * e encapsula a URL de acesso resultante em um DTO.
     *
     * @param file Arquivo recebido na requisição HTTP (Multipart) que será enviado ao S3.
     * @return Um {@link UriDTO} contendo a representação em string da URL pública do arquivo armazenado.
     */
    public UriDTO uploadFile(MultipartFile file) {
        URL url = s3Service.uploadFile(file);
        return new UriDTO(url.toString());
    }
}
