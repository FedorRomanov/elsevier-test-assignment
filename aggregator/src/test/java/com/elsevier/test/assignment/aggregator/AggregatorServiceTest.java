package com.elsevier.test.assignment.aggregator;

import com.elsevier.test.assignment.offer.Offer;
import com.elsevier.test.assignment.offer.OfferService;
import com.elsevier.test.assignment.order.Order;
import com.elsevier.test.assignment.order.OrderService;
import com.elsevier.test.assignment.product.Product;
import com.elsevier.test.assignment.product.ProductService;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import static com.elsevier.test.assignment.offer.OfferCondition.AS_NEW;
import static com.elsevier.test.assignment.offer.OfferCondition.UNKNOWN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AggregatorServiceTest {

    private OrderService orderService = mock(OrderService.class);

    private OfferService offerService = mock(OfferService.class);

    private ProductService productService = mock(ProductService.class);

    private AggregatorService aggregatorService = new AggregatorService(orderService, offerService, productService);


    private final int sellerId = 1;
    private final int orderId = 2;
    private final int offerId = 3;
    private final int productId = 4;
    private String title = "Title";

    @Test
    public void simpleHappyFlow() {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenReturn(new Offer(offerId, AS_NEW));
        when(productService.getProduct(productId)).thenReturn(new Product(productId, title));

        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
    }

    @Test(timeout = 3500)
    public void offerAndProductServicesAreSlow() {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenAnswer(
                (InvocationOnMock invocationOnMock) -> {
                    Thread.sleep(1500);
                    return new Offer(offerId, AS_NEW);
                }
        );
        when(productService.getProduct(productId)).thenAnswer(
                (InvocationOnMock invocationOnMock) -> {
                    Thread.sleep(1500);
                    return new Product(productId, title);
                }
        );

       EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertThat(enrichedOrder.getOfferCondition(), is(AS_NEW));
        assertThat(enrichedOrder.getProductTitle(), is(title));
    }

    @Test
    public void offerServiceFailed() {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenThrow(new RuntimeException("Offer Service failed"));
        when(productService.getProduct(productId)).thenReturn(new Product(productId, title));

        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertThat(enrichedOrder.getProductTitle(), is(title));
        assertThat(enrichedOrder.getOfferId(), is(-1));
        assertThat(enrichedOrder.getOfferCondition(), is(UNKNOWN));
    }

    @Test
    public void productServiceFailed() {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenReturn(new Offer(offerId, AS_NEW));
        when(productService.getProduct(productId)).thenThrow(new RuntimeException("Product Service failed"));

        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertThat(enrichedOrder.getProductId(), is(-1));
        assertNull(enrichedOrder.getProductTitle());
        assertThat(enrichedOrder.getOfferId(), is(offerId));
        assertThat(enrichedOrder.getOfferCondition(), is(AS_NEW));
    }

    @Test
    public void productServiceAndOfferServiceFailed() {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenThrow(new RuntimeException("Offer Service failed"));
        when(productService.getProduct(productId)).thenThrow(new RuntimeException("Product Service failed"));

        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertThat(enrichedOrder.getOfferId(), is(-1));
        assertThat(enrichedOrder.getOfferCondition(), is(UNKNOWN));
        assertThat(enrichedOrder.getProductId(), is(-1));
        assertNull(enrichedOrder.getProductTitle());
    }

    @Test(expected = RuntimeException.class)
    public void orderServiceFailed() {
        when(orderService.getOrder(sellerId)).thenThrow(new RuntimeException("Order service failed"));

        aggregatorService.enrich(sellerId);
    }

    @Test
    public void productServiceAndOfferServiceNulls() {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenReturn(null);
        when(productService.getProduct(productId)).thenReturn(null);

        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);

        assertThat(enrichedOrder.getId(), is(orderId));
        assertThat(enrichedOrder.getOfferId(), is(-1));
        assertThat(enrichedOrder.getOfferCondition(), is(UNKNOWN));
        assertThat(enrichedOrder.getProductId(), is(-1));
        assertNull(enrichedOrder.getProductTitle());
    }

}
