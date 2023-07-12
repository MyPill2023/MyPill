package com.mypill.domain.cart.repository;

import com.mypill.domain.cart.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

    Optional<CartProduct> findByCartIdAndProductIdAndDeleteDateIsNull(Long cartId, Long ProductId);

    List<CartProduct> findByIdIn(List<Long> cartProductIds);
}