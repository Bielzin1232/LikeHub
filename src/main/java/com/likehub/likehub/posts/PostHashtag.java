package com.likehub.likehub.posts;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "post_hashtags")
public class PostHashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(nullable = false)
    private String tag;
}