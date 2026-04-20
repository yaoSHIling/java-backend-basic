package com.example.basic.service.impl;

import com.example.basic.dao.UserDao;
import com.example.basic.model.User;
import com.example.basic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public List<User> list() {
        return userDao.findAll();
    }

    @Override
    public User getById(Long id) {
        return userDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void add(User user) {
        userDao.save(user);
    }

    @Override
    @Transactional
    public void update(User user) {
        userDao.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userDao.deleteById(id);
    }
}
