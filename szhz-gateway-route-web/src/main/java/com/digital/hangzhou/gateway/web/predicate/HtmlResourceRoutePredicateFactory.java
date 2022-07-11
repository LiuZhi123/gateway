package com.digital.hangzhou.gateway.web.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

public class HtmlResourceRoutePredicateFactory extends AbstractRoutePredicateFactory<HtmlResourceRoutePredicateFactory.Config> {

    public HtmlResourceRoutePredicateFactory(){
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new Predicate<ServerWebExchange>() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                String url = exchange.getRequest().getURI().toString();
//                if (Pattern.matches("/grassRoots/g", url)){
//                    return false;
//                }
                if (url.endsWith(".css") || url.endsWith(".js")){
                    return false;
                }
                else {
                    return true;
                }
            }
        };
    }

    public static class Config{
        private String name;

        public String getName(){
            return name;
        }

        public void setName(String name){
            this.name = name;
        }
    }
}