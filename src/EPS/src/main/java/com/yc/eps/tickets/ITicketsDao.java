package com.yc.eps.tickets;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yc.edsi.tickets.TicketsOrderPO;

public interface ITicketsDao {

    void insert(TicketsOrderPO ticketsOrderPO);

    void update(TicketsOrderPO ticketsOrderPO);

    List<TicketsOrderPO> getListByOrderStatus(@Param("statuses") List<String> statuses);

    List<TicketsOrderPO> getList(@Param("status") String status, @Param("communityId") Long communityId,
            @Param("ownerId") Long ownerId);

    TicketsOrderPO getTickets(@Param("orderId") String orderId, @Param("communityId") Long communityId,
            @Param("ownerId") Long ownerId);

    TicketsReturnPO getTicketsR(@Param("orderId") String orderId, @Param("ticketNo") String ticketNo);

    void insertTicketR(TicketsReturnPO ticketsReturnPO);

    void updateTicketR(TicketsReturnPO ticketsReturnPO);

}
