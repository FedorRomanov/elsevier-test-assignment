package com.elsevier.test.assignment.aggregator;

import com.elsevier.test.assignment.offer.OfferService;
import com.elsevier.test.assignment.order.OrderService;
import com.elsevier.test.assignment.product.ProductService;

public class AggregatorService {
    private OrderService orderService;
    private OfferService offerService;
    private ProductService productService;

    public AggregatorService(OrderService orderService, OfferService offerService, ProductService productService) {
        this.orderService = orderService;
        this.offerService = offerService;
        this.productService = productService;
    }

    public EnrichedOrder enrich(int sellerId) {
        throw new UnsupportedOperationException();

    }
}
