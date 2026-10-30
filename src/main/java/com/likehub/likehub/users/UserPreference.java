package com.likehub.likehub.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_preferences_user_category",
                columnNames = {"user_id", "category_tag"}))
@Getter
@Setter
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "public_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_preferences_user"))
    private User user;

    @Column(name = "category_tag", nullable = false)
    private String categoryTag;

    @Column(nullable = false)
    private Long weight;
}
