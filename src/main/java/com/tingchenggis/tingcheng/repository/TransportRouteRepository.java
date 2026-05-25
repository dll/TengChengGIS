package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.TransportRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 交通线数据访问接口
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
@Repository
public interface TransportRouteRepository extends JpaRepository<TransportRoute, Long> {

    /**
     * 查询从指定亭子出发的所有交通线
     */
    List<TransportRoute> findByFromPavilionId(Long fromPavilionId);

    /**
     * 查询到达指定亭子的所有交通线
     */
    List<TransportRoute> findByToPavilionId(Long toPavilionId);

    /**
     * 查询两个亭子之间的交通线
     */
    @Query("SELECT tr FROM TransportRoute tr WHERE " +
           "(tr.fromPavilionId = :pavilionId1 AND tr.toPavilionId = :pavilionId2) OR " +
           "(tr.fromPavilionId = :pavilionId2 AND tr.toPavilionId = :pavilionId1)")
    List<TransportRoute> findRouteBetweenPavilions(@Param("pavilionId1") Long pavilionId1,
                                                    @Param("pavilionId2") Long pavilionId2);

    /**
     * 查询所有风景路线
     */
    List<TransportRoute> findByIsScenicRouteTrue();

    /**
     * 查询所有无障碍路线
     */
    List<TransportRoute> findByIsAccessibleTrue();

    /**
     * 根据路线类型查询
     */
    List<TransportRoute> findByRouteType(String routeType);

    List<TransportRoute> findByTransportMode(String transportMode);

    List<TransportRoute> findByFromPavilionIdAndTransportMode(Long fromPavilionId, String transportMode);
}
