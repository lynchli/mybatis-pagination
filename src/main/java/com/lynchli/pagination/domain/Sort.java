package com.lynchli.pagination.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author Lynch
 * @since 2015-04-24
 */
public class Sort implements Iterable<Sort.Order>{

    private final static Direction DEFAULT_DIRECTION = Direction.ASC;

    private List<Order> orders;

    public Sort(){}

    public Sort(String... properties){
        this.withOrder(properties);
    }

    public Sort(Direction direction, String... properties){
        this.withOrder(direction, properties);
    }

    public Sort withOrder(String... properties){
        return this.withOrder(DEFAULT_DIRECTION, properties);
    }

    public Sort withOrder(Direction direction, String... properties){
        if (properties == null || properties.length == 0) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }
        Optional<List<Order>> optional = Optional.ofNullable(orders);
        this.orders = optional.orElseGet(() -> {
            orders = new ArrayList<>(properties.length);
            for (String property: properties) {
                orders.add(new Order(direction, property));
            }
            return orders;
        });
        return this;
    }

    public Order getOrderFor(String property) {
        for (Order order : this) {
            if (order.getProperty().equals(property)) {
                return order;
            }
        }
        return null;
    }

    @Override
    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }

    public enum Direction{
        ASC, DESC;
    }

    public static class Order{

        private int hash;

        private Direction direction;
        private String property;

        public Order(String property) {
            this(DEFAULT_DIRECTION, property);
        }

        public Order(Direction direction, String property) {
            this.direction = direction;
            this.property = property;
        }

        @Override
        public int hashCode() {
            int result = hash;
            if(result == 0) {
                result = 31 * result + direction.hashCode();
                result = 31 * result + property.hashCode();
            }
            hash = result;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Order)) {
                return false;
            }

            Order that = (Order) obj;

            return this.direction.equals(that.direction) && this.property.equals(that.property);
        }

        public String getProperty() {
            return property;
        }

        public Direction getDirection() {
            return direction;
        }
    }
}
