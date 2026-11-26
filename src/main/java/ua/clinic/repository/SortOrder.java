package ua.clinic.repository;

import java.util.Comparator;

public enum SortOrder {

    ASC {
        @Override
        public <K extends Comparable<? super K>> Comparator<K> comparator() {
            return Comparator.naturalOrder();
        }
    },

    DESC {
        @Override
        public <K extends Comparable<? super K>> Comparator<K> comparator() {
            return Comparator.reverseOrder();
        }
    };

    public abstract <K extends Comparable<? super K>> Comparator<K> comparator();
}
