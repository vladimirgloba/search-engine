package com.globa.search.engine.repository;
import com.globa.search.engine.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field,Long> {
    @Query(value = "SELECT * FROM field f WHERE f.name = ?1", nativeQuery = true)
    List<Field> findByTag(String path);

}
