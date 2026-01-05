package com.articurated.repository;

import com.articurated.model.Return;
import com.articurated.model.enums.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    Optional<Return> findByReturnNumber(String returnNumber);
    List<Return> findByOrderId(Long orderId);
    List<Return> findByStatus(ReturnStatus status);
    boolean existsByReturnNumber(String returnNumber);
}



