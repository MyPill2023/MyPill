package com.mypill.domain.cart.entity;

import com.mypill.domain.member.entity.Member;
import com.mypill.global.base.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartProduct> cartProducts = new ArrayList<>();

    public static Cart createCart(Member member){
        Cart cart = new Cart();
        cart.member= member;

        return cart;
    }
}