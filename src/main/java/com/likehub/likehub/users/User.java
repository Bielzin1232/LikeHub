package com.likehub.likehub.users;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.likehub.likehub.notifications.Notification;
import com.likehub.likehub.posts.Post;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private final UUID publicId = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_followers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "follower_id"),
            indexes = {
                    @Index(name = "idx_user_followers_user", columnList = "user_id"),
                    @Index(name = "idx_user_followers_follower", columnList = "follower_id")
            }
    )
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers", fetch = FetchType.LAZY)
    private Set<User> following = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_user_roles_user")))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<UserRoles> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private final Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_type", nullable = false)
    private PerfilType perfilType = PerfilType.PUBLIC;

    @Column(name = "session_version", nullable = false)
    private long sessionVersion = 0;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    public void follow(User user) {
        this.following.add(user);
        user.getFollowers().add(this);
    }

    public void unfollow(User user) {
        this.following.remove(user);
        user.getFollowers().remove(this);
    }

    public static User create(String email, String username, String passwordHash,String displayName) {
        User u = new User();
        u.email = email;
        u.username = username;
        u.displayName = displayName;
        u.password = passwordHash;
        u.roles.add(UserRoles.USER);
        return u;
    }

    public void updatePasswordHash(String novoHash) {
        this.password = novoHash;
    }

    public void invalidateSessions() {
        sessionVersion++;
    }

    public Set<UserRoles> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public List<String> getRoleNames() {
        return roles.stream().map(Enum::name).sorted().toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(publicId, user.publicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicId);
    }

    @Override
    public String toString() {
        return "User[publicId=" + publicId + ", username=" + username + "]";
    }
}
