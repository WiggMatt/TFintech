package ru.matthew.utils;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.matthew.dao.model.Event;
import ru.matthew.dao.model.Event_;

import java.time.LocalDate;

@Component
public class EventSpecification {

    private Specification<Event> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(Event_.title)),
                    "%" + title.toLowerCase() + "%"
            );
        };
    }

    private Specification<Event> hasLocationSlug(String locationSlug) {
        return (root, query, criteriaBuilder) -> {
            if (locationSlug == null || locationSlug.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get(Event_.location).get("slug"), locationSlug);
        };
    }

    private Specification<Event> isDateBetween(LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null && toDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (fromDate != null && toDate != null) {
                return criteriaBuilder.between(root.get(Event_.date), fromDate, toDate);
            } else if (fromDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(Event_.date), fromDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get(Event_.date), toDate);
            }
        };
    }

    public Specification<Event> buildSpecification(String title, String locationSlug, LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            root.fetch(Event_.location, JoinType.LEFT);
            return criteriaBuilder.and(
                    hasTitle(title).toPredicate(root, query, criteriaBuilder),
                    hasLocationSlug(locationSlug).toPredicate(root, query, criteriaBuilder),
                    isDateBetween(fromDate, toDate).toPredicate(root, query, criteriaBuilder)
            );
        };
    }
}