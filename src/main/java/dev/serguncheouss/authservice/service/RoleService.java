package dev.serguncheouss.authservice.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import dev.serguncheouss.authservice.model.Role;
import dev.serguncheouss.authservice.repository.RoleRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;

        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role("ROLE_ADMIN"),
                    new Role("ROLE_USER")
            ));
        }
    }

    public Role create(String name) {
        final Role role = new Role(name);
        return roleRepository.save(role);
    }

    public Optional<Role> getByName(String name) {
        return roleRepository.findByName(name);
    }

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public boolean update(@NonNull Long id, @NonNull Role newRole) {
        if (roleRepository.existsById(id)) {
            newRole.setId(id);
            roleRepository.save(newRole);

            return true;
        }

        return false;
    }

    public boolean delete(@NonNull Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);

            return true;
        }

        return false;
    }

    public static Role convertFromMap(Map<?, ?> roleMap) {
        return new Role((long) roleMap.get("id"), (String) roleMap.get("title"));
    }
}
