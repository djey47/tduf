package fr.tduf.libunlimited.low.files.common.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.Optional;

/**
 * To be implemented by all prop enums
 */
public interface DataStoreProps {
    /**
     * @return value if it exists, empty otherwise
     */
    Optional<?> retrieveFrom(DataStore dataStore);

    void updateIn(DataStore dataStore, Object value);

    String getStoreFieldName();

    @Override
    String toString();
}
