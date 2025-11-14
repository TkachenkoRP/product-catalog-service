package com.my.repository.impl;

import com.my.mapper.UserMapper;
import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepositoryImpl implements UserRepository {

    private final Map<Long, User> repository = new ConcurrentHashMap<>();

    private final AtomicLong currentId = new AtomicLong(1);

    public InMemoryUserRepositoryImpl() {
        save(new User("admin@my.ru", "Administrator", "aDmIn", UserRole.ROLE_ADMIN));
        save(new User("user@my.ru", "User", "UsEr", UserRole.ROLE_USER));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(repository.get(id))
                .map(UserMapper.INSTANCE::copyUser);
    }

    @Override
    public User save(User user) {
        Long id = currentId.getAndIncrement();
        user.setId(id);
        repository.put(id, UserMapper.INSTANCE.copyUser(user));
        return user;
    }

    @Override
    public User update(User user) {
        User existing = repository.get(user.getId());
        if (existing != null) {
            UserMapper.INSTANCE.updateUser(user, existing);
        }
        return UserMapper.INSTANCE.copyUser(existing);
    }

    @Override
    public boolean deleteById(Long id) {
        if (getById(id).isPresent()) {
            repository.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPresentByEmail(String email) {
        return repository.values().stream().anyMatch(
                u -> u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public Optional<User> getByEmailAndPassword(String email, String password) {
        for (User u : getAll()) {
            if (u.getEmail().equalsIgnoreCase(email)
                && u.getPassword().equals(password)) {
                return Optional.of(UserMapper.INSTANCE.copyUser(u));
            }
        }
        return Optional.empty();
    }

    public void loadData(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }

        long maxId = 0;

        for (User user : users) {
            repository.put(user.getId(), user);
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        currentId.set(maxId + 1);
        System.out.println("Загружено пользователей: " + users.size() + ", установлен Id: " + currentId.get());
    }
}
