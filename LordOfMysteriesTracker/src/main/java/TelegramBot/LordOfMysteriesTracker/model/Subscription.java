package TelegramBot.LordOfMysteriesTracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Subscription")
public class Subscription {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_ID", nullable = false, unique = true)
    private Long chatID;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ElementCollection
    @CollectionTable(
            name = "subscription_filters",
            joinColumns = @JoinColumn(name = "subscription_id")
    )
    @Column(name = "filter_type")
    @Enumerated(EnumType.STRING)
    private Set<FilterType> filters = new HashSet<>();

    public Subscription() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.filters.add(FilterType.ALL);
    }

    public Subscription(Long chatID) {
        this.chatID = chatID;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.filters.add(FilterType.ALL);
    }

    public void addFilter(FilterType filterType) {
        if (filterType != FilterType.ALL) {
            this.filters.remove(FilterType.ALL);
        }
        this.filters.add(filterType);
    }

    // очищаем ВСЕ фильтры, которые есть у пользователя
    public void clearFilters() {
        this.filters.clear();
        this.filters.add(FilterType.ALL);
    }

    // убираем только один из фильтров
    public void removeFilter(FilterType filterType) {
        this.filters.remove(filterType);
        if (this.filters.isEmpty()) {
            this.filters.add(FilterType.ALL);
        }
    }

    public Set<String> getFilterCodes() {
        Set<String> types = new HashSet<>();
        for (FilterType filterType : filters) {
            types.add(filterType.getType());
        }
        return types;
    }

    public void setFiltersFromCodes(Set<String> codes) {
        this.filters.clear();
        if (codes == null || codes.isEmpty() || codes.contains("all")) {
            this.filters.add(FilterType.ALL);
        } else {
            for (String code : codes) {
                try {
                    this.filters.add(FilterType.fromType(code));
                } catch (IllegalArgumentException e) {
                    // Игнорируем неизвестные коды
                }
            }
        }
    }

    public Set<FilterType> getFilters() {
        return filters;
    }

    public void setFilters(Set<FilterType> filters) {
        this.filters = filters;
    }

    public Long getChatID() {
        return chatID;
    }

    public void setChatID(Long chatID) {
        this.chatID = chatID;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
