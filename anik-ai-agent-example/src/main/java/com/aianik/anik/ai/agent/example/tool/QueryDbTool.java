package com.aianik.anik.ai.agent.example.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2026-05-24
 */
@Component
public class QueryDbTool {

    @Tool(name = "query_order", description = "Query order information")
    public String queryOrder(@ToolParam(description = "Order ID") String orderId) {
        return """
        {
          "orderId": "%s",
          "orderStatus": "DELIVERED",
          "productName": "Apple iPhone 16 Pro 256GB",
          "quantity": 1,
          "amount": 8999.00,
          "currency": "CNY",
          "buyerName": "Zhang San",
          "receiverName": "Zhang San",
          "receiverPhone": "138****8888",
          "receiverAddress": "No. 100, XX Road, Chaoyang District, Beijing",
          "createTime": "2026-05-24 10:30:00",
          "payTime": "2026-05-24 10:32:15",
          "deliveryTime": "2026-05-24 15:20:00",
          "finishTime": "2026-05-26 09:18:23",
          "trackingNo": "SF123456789CN",
          "logisticsCompany": "SF Express"
        }
        """.formatted(orderId);
    }
}
