package Server;

import com.intellij.openapi.diagnostic.Logger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.RestService;
import org.jetbrains.io.Responses;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class ProfilerRestService extends RestService {
    private static final Logger LOG = Logger.getInstance(ProfilerRestService.class.getName());
    private final String PROFILER_SERVICE_NAME = "profiler";

    @NotNull
    @Override
    protected String getServiceName() {
        return PROFILER_SERVICE_NAME;
    }

    @Override
    protected boolean isMethodSupported(@NotNull HttpMethod method) {
        return method == HttpMethod.GET;
    }

    @Override
    protected boolean isPrefixlessAllowed() {
        return true;
    }

    @Override
    protected boolean isHostTrusted(@NotNull FullHttpRequest request) throws InterruptedException, InvocationTargetException {
        return true;
    }

    @Nullable
    @Override
    public String execute(@NotNull QueryStringDecoder urlDecoder,
                          @NotNull FullHttpRequest request,
                          @NotNull ChannelHandlerContext context) throws IOException {
        String uri = urlDecoder.uri();
        LOG.info("Request: " + uri);
        if (Objects.equals(uri, "/profiler/results")) {
            sendHtmlResponse(request, context);
        } else {
            RestService.sendStatus(HttpResponseStatus.NOT_FOUND, false, context.channel());
            return "Not Found";
        }
        return null;
    }

    private void sendHtmlResponse(FullHttpRequest request, ChannelHandlerContext context) {
        DefaultHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("Hello from Lucinda".getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        Responses.send(response, context.channel(), true);
//        BufferExposingByteArrayOutputStream byteOut = new BufferExposingByteArrayOutputStream();
//        InputStream pageStream = getClass().getResourceAsStream(pagePath);
//        try {
//            byteOut.write(StreamUtil.loadFromStream(pageStream));
//            HttpResponse response = Responses.response("text/html", Unpooled.wrappedBuffer(byteOut.getInternalBuffer(), 0, byteOut.size()));
//            Responses.addNoCache(response);
//            response.headers().set("X-Frame-Options", "Deny");
//            Responses.send(response, context.channel(), request);
//        }
//        finally {
//            byteOut.close();
//            pageStream.close();
//        }
    }
}
