package com.example.renttool.repository;

import com.example.renttool.model.CartItem;
import com.example.renttool.model.Tool;
import com.example.renttool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndTool(User user, Tool tool);

    void deleteByUser(User user);

    void deleteByUserAndTool(User user, Tool tool);
}
