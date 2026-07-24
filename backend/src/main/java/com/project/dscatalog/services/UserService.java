package com.project.dscatalog.services;


import com.project.dscatalog.dto.RoleDTO;
import com.project.dscatalog.dto.UserDTO;
import com.project.dscatalog.dto.UserInsertDTO;
import com.project.dscatalog.dto.UserUpdateDTO;
import com.project.dscatalog.entities.Role;
import com.project.dscatalog.entities.User;
import com.project.dscatalog.projections.UserDetailsProjection;
import com.project.dscatalog.repositories.RoleRepository;
import com.project.dscatalog.repositories.UserRepository;
import com.project.dscatalog.services.exceptions.DatabaseException;
import com.project.dscatalog.services.exceptions.ResourceEntityNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Camada de servico para cadastro, atualizacao e autenticacao de usuarios.
 */
@Service
public class UserService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthService authService;

    /**
     * Lista usuarios de forma paginada.
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        Page<User> list = repository.findAll(pageable);
        return list.map(UserDTO::new);
    }

    /**
     * Busca o 'usuário' logado.
     * @return
     */
    @Transactional(readOnly = true)
    public UserDTO findConnectedUser() {
        User entity = authService.authenticated();
        return new UserDTO(entity);
    }

    /**
     * Busca um usuario por id e retorna seu DTO.
     */
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        Optional<User> obj = repository.findById(id);
        User entity = obj.orElseThrow(() -> new ResourceEntityNotFoundException("Entity not found with id: " + id));
        log.info(entity.toString());
        return new UserDTO(entity);
    }

    /**
     * Cadastra um novo usuario com role padrao e senha criptografada.
     */
    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        User entity = new User();
        copyDtoToEntity(dto, entity);

        // Garante que novos usuários iniciem com o perfil padrão.
        entity.getRoles().clear();
        // Busca a role no banco e associa ao usuário.
        Role role = roleRepository.findByAuthority("ROLE_OPERATOR");
        entity.getRoles().add(role);

        // Armazena a senha com hash seguro (não em texto puro).
        entity.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        entity = repository.save(entity);
        return new UserDTO(entity);
    }

    /**
     * Atualiza os dados de um usuario existente.
     */
    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        try {
            User entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new UserDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
    }

    /**
     * Remove um usuario e trata erros de integridade referencial.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) throws DatabaseException {
        if (!repository.existsById(id)) {
            throw new ResourceEntityNotFoundException("Entity not found with id: " + id);
        }
        try {
            repository.deleteById(id);
            log.info("Delete User with id: " + id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Referential integrity failure!");
        }
    }

    /**
     * Copia dados de entrada para a entidade e sincroniza as roles associadas.
     */
    private void copyDtoToEntity(UserDTO dto, User entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        for (RoleDTO roleDTO : dto.getRoles()) {
            Role role = roleRepository.getReferenceById(roleDTO.getId());
            entity.getRoles().add(role);
        }
    }

    /**
     * Metodo exigido pelo Spring Security para carregar usuario e authorities no login.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
        if (result.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = new User();
        user.setEmail(result.get(0).getUsername());
        user.setPassword(result.get(0).getPassword());
        for (UserDetailsProjection projection : result) {
            user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
        }

        return user;
    }
}
