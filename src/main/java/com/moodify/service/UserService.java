package com.moodify.service;

import com.moodify.dto.UserResponseDto;
import com.moodify.model.Post;
import com.moodify.model.User;
import com.moodify.repository.CommentRepository; // <-- Импорт
import com.moodify.repository.LikeRepository;    // <-- Импорт
import com.moodify.repository.PostRepository;
import com.moodify.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;         // <-- Добавлена зависимость
    private final CommentRepository commentRepository;   // <-- Добавлена зависимость

    @Autowired
    public UserService(UserRepository userRepository,
                       PostRepository postRepository,
                       LikeRepository likeRepository,       // <-- Внедряем в конструктор
                       CommentRepository commentRepository) { // <-- Внедряем в конструктор
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;         // <-- Инициализируем
        this.commentRepository = commentRepository;   // <-- Инициализируем
    }

    // Маппинг User -> UserResponseDto (без изменений)
    private UserResponseDto mapUserToUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return dto;
    }

    // Метод getAllUsers (без изменений)
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        logger.debug("Admin fetching all users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::mapUserToUserResponseDto);
    }

    /**
     * Удаляет пользователя по ID.
     * Перед удалением пользователя:
     * 1. Удаляет все его посты (что каскадно удаляет комментарии и лайки к этим постам).
     * 2. Удаляет все лайки, оставленные этим пользователем под чужими постами.
     * 3. Удаляет все комментарии, оставленные этим пользователем под чужими постами.
     * @param userId ID пользователя для удаления.
     * @throws EntityNotFoundException если пользователь не найден.
     */
    @Transactional // Вся операция должна быть в одной транзакции
    public void deleteUser(Long userId) {
        logger.warn("[ADMIN ACTION] Attempting to delete user id: {}", userId);

        // (Опционально: Проверка, не пытается ли текущий админ удалить себя.
        // Потребует внедрения AuthenticationHelper и получения currentAdminId)
        // if (authenticationHelper.getCurrentUserId().equals(userId)) {
        //     throw new IllegalArgumentException("Admin cannot delete their own account via this method.");
        // }

        // 1. Находим пользователя
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // 2. Удаляем все посты пользователя
        // (Комментарии и лайки к этим постам удалятся каскадно благодаря настройке в Post.java)
        List<Post> userPosts = postRepository.findByUserId(userId);
        if (!userPosts.isEmpty()) {
            logger.warn("Deleting {} posts associated with user id: {}", userPosts.size(), userId);
            postRepository.deleteAllInBatch(userPosts); // Используем deleteAllInBatch для эффективности
        } else {
            logger.info("No posts found for user id: {} to delete.", userId);
        }

        // 3. Удаляем лайки, оставленные этим пользователем (под любыми постами)
        logger.warn("Deleting likes made by user id: {}", userId);
        likeRepository.deleteByUserId(userId); // Вызываем новый метод репозитория

        // 4. Удаляем комментарии, оставленные этим пользователем (под любыми постами)
        logger.warn("Deleting comments made by user id: {}", userId);
        commentRepository.deleteByUserId(userId); // Вызываем новый метод репозитория

        // 5. Удаляем самого пользователя
        // Связи в таблице user_roles (роли пользователя) удалятся автоматически
        // благодаря стандартному поведению JPA для связей ManyToMany при удалении одной из сторон.
        userRepository.delete(userToDelete);
        logger.info("User id: {} and their associated content (posts, likes, comments) deleted successfully by admin.", userId);
    }
}