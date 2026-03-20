package com.ceos23.cgv.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_statics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserStatic {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_static_id")
    private Long id;

    @Column(nullable = false)
    private int cinetalkCount;

    @Column(nullable = false)
    private int followingCount;

    @Column(nullable = false)
    private int followerCount;

    @Column(nullable = false)
    private int badgeCount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
