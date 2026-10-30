package com.likehub.likehub.auth.commons.api;
/**
 * Classe que contém as constantes de caminho da API
 */
public final class ApiPaths {

    private ApiPaths() {}

    // ==========================================================
    // RAIZ
    // ==========================================================
    public static final String API_V1        = "/api/v1";
    public static final String DOCS_ROOT     = "/api/docs";
    public static final String ACTUATOR_ROOT = "/actuator";

    // ==========================================================
    // AUTH
    // ==========================================================
    public static final class Auth {
        private Auth() {}

        public static final String BASE            = API_V1 + "/auth";
        public static final String REGISTER        = BASE + "/register";
        public static final String LOGIN           = BASE + "/login";
        public static final String REFRESH         = BASE + "/refresh";
        public static final String LOGOUT          = BASE + "/logout";
        public static final String LOGOUT_ALL      = BASE + "/logout-all";
        public static final String PASSWORD_CHANGE = BASE + "/password/change";
    }

    // ==========================================================
    // USERS / PERFIL
    // ==========================================================
    public static final class Users {
        private Users() {}

        public static final String BASE          = API_V1 + "/users";
        public static final String ME            = BASE + "/me";
        public static final String SEARCH        = BASE + "/search";
        public static final String BY_USERNAME   = BASE + "/{username}";
        public static final String POSTS         = BY_USERNAME + "/posts";
        public static final String SAVED         = ME + "/saved";
    }

    // ==========================================================
    // SEGUIR
    // ==========================================================
    public static final class Follow {
        private Follow() {}
        public static final String BASE      = ApiPaths.Users.BY_USERNAME;
        public static final String FOLLOW    = BASE + "/follow";
        public static final String FOLLOWERS = BASE + "/followers";
        public static final String FOLLOWING = BASE + "/following";
    }

    // ==========================================================
    // POSTS
    // ==========================================================
    public static final class Posts {
        private Posts() {}
        public static final String BASE  = API_V1 + "/posts";
        public static final String BY_ID = BASE + "/{postId}";
        public static final String SAVE  = BY_ID + "/save";
    }

    // ==========================================================
    // COMENTÁRIOS
    // ==========================================================
    public static final class Comments {
        private Comments() {}

        public static final String BASE    = API_V1 + "/comments";
        public static final String BY_ID   = BASE + "/{commentId}";
        public static final String REPLIES = BY_ID + "/replies";
        public static final String BY_POST = ApiPaths.Posts.BY_ID + "/comments";
    }

    // ==========================================================
    // LIKES
    // ==========================================================
    public static final class Likes {
        private Likes() {}
        public static final String BASE   = API_V1 + "/likes";
        public static final String TOGGLE = BASE + "/toggle";
        public static final String COUNT  = BASE + "/count/{targetId}";
        public static final String USERS  = BASE + "/{targetId}/users";
    }

    // ==========================================================
    // FEED
    // ==========================================================
    public static final class Feed {
        private Feed() {}
        public static final String BASE = API_V1 + "/feed";
    }

    // ==========================================================
    // EXPLORAR
    // ==========================================================
    public static final class Explore {
        private Explore() {}
        public static final String BASE = API_V1 + "/explore";
    }

    // ==========================================================
    // NOTIFICAÇÕES
    // ==========================================================
    public static final class Notifications {
        private Notifications() {}
        public static final String BASE         = API_V1 + "/notifications";
        public static final String UNREAD_COUNT = BASE + "/unread-count";
        public static final String READ_ALL     = BASE + "/read-all";
        public static final String READ         = BASE + "/{notificationId}/read";
    }
    // ==========================================================
    // IMAGENS
    // ==========================================================
    public static final class Images {
        private Images() {}
        public static final String BASE = API_V1 + "/images";
        public static final String upload = BASE + "/upload";
    }

}