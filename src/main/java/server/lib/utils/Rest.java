package server.lib.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import server.Application;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Rest {

    private static final int TIMEOUT = 15_000;

    private static WebClient getClient() {
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS));
                });
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient))).build();
    }

    public static String sync(String url, HttpMethod method) {
        return sync(url, method, null);
    }

    public static String sync(String url, HttpMethod method, Object object) {
        return sync(url, method, object, null);
    }

    public static String sync(String url, HttpMethod method, Object data, HashMap<String, String> headers) {
        return sync(url, method, data, headers, null);
    }

    public static String sync(String url, HttpMethod method, Object data, HashMap<String, String> headers, MediaType contentType) {
        if (contentType == null) {
            contentType = MediaType.APPLICATION_JSON_UTF8;
        }
        WebClient client = getClient();
        WebClient.RequestBodyUriSpec spec = client.method(method);
        WebClient.RequestBodySpec uri = spec.uri(URI.create(url));
        uri.accept(MediaType.APPLICATION_JSON).acceptCharset(Charset.forName("UTF-8"));
        uri.header(HttpHeaders.CONTENT_TYPE, contentType.toString());
        if (headers != null) {
            for (String key : headers.keySet()) {
                uri.header(key, headers.get(key));
            }
        }
        if (data != null) {
            if (contentType == MediaType.APPLICATION_JSON_UTF8 || contentType == MediaType.APPLICATION_JSON) {
                if (data instanceof String) {
                    uri.body(BodyInserters.fromValue(data));
                } else {
                    uri.body(BodyInserters.fromValue(Application.GSON.toJson(data)));
                }
            } else {
                uri.body(BodyInserters.fromFormData((MultiValueMap<String, String>) data));
            }
        }
        return uri.retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    return response.bodyToMono(String.class).flatMap(error -> {
                        return Mono.error(new Throwable(error));
                    });
                }).bodyToMono(String.class).block();
    }
}
