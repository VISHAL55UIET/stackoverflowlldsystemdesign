package dto;

import enums.SortBy;

import java.util.HashSet;
import java.util.Set;

/** Optional filters + sort order for searching questions. Build with {@link Builder}. */
public class SearchCriteria {

    private final String keyword;
    private final Set<String> tags;
    private final String authorId;
    private final SortBy sortBy;

    private SearchCriteria(Builder b) {
        this.keyword = b.keyword;
        this.tags = b.tags;
        this.authorId = b.authorId;
        this.sortBy = b.sortBy;
    }

    public String getKeyword() {
        return keyword;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getAuthorId() {
        return authorId;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keyword;
        private final Set<String> tags = new HashSet<>();
        private String authorId;
        private SortBy sortBy = SortBy.NEWEST;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder tag(String tag) {
            this.tags.add(tag.trim().toLowerCase());
            return this;
        }

        public Builder authorId(String authorId) {
            this.authorId = authorId;
            return this;
        }

        public Builder sortBy(SortBy sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(this);
        }
    }
}
