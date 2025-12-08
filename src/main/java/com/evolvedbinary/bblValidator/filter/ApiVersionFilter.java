package com.evolvedbinary.bblValidator.filter;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Filter("/**")
public class ApiVersionFilter implements HttpServerFilter {

    @Value("${api.version}")
    private String version;

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Flux.from(chain.proceed(request))
                .doOnNext(response -> response.header("X-BBLVALIDATOR-VERSION", version));
    }
}
