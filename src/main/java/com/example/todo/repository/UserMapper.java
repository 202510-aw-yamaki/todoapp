package com.example.todo.repository;

import com.example.todo.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    AppUser findByUsername(@Param("username") String username);

    Long findIdByUsername(@Param("username") String username);

    int updatePasswordAndRole(@Param("username") String username, @Param("password") String password, @Param("role") String role);

    java.util.List<AppUser> findAll();

    int insert(AppUser user);
}
