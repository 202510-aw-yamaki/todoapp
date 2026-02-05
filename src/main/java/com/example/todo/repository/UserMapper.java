package com.example.todo.repository;

import com.example.todo.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    AppUser findByUsername(@Param("username") String username);

    int insert(AppUser user);
}
