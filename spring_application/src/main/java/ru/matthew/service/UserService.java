package ru.matthew.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.matthew.dao.model.User;
import ru.matthew.dao.repository.UserRepository;
import ru.matthew.exception.ElementAlreadyExistsException;
import ru.matthew.exception.ElementWasNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public void save(User user) {
        log.info("Сохранение пользователя с именем {}", user.getUsername());
        userRepository.save(user);
        log.debug("Пользователь {} сохранен в базе данных", user.getUsername());
    }

    public void create(User user) {
        log.info("Создание пользователя с именем {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Попытка создать пользователя с существующим именем: {}", user.getUsername());
            throw new ElementAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Попытка создать пользователя с существующим email: {}", user.getEmail());
            throw new ElementAlreadyExistsException("Пользователь с таким email уже существует");
        }

        save(user);
        log.info("Пользователь {} успешно создан", user.getUsername());
    }

    public User getByUsername(String username) {
        log.info("Поиск пользователя по имени {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Пользователь с именем {} не найден", username);
                    return new ElementWasNotFoundException("Пользователь не найден");
                });
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public boolean existsByEmail(String email) {
        log.debug("Проверка существования пользователя с email {}", email);
        return userRepository.existsByEmail(email);
    }

    public User getByEmail(String email) {
        log.info("Поиск пользователя по email {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Пользователь с email {} не найден", email);
                    return new ElementWasNotFoundException("Пользователь не найден");
                });
    }
}
