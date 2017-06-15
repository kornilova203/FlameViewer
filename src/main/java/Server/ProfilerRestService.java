package Server;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import com.intellij.openapi.util.io.StreamUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.RestService;
import org.jetbrains.io.Responses;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfilerRestService extends RestService {
    private static final Logger LOG = Logger.getInstance(ProfilerRestService.class.getName());
    private final String PROFILER_SERVICE_NAME = "profiler";
    private final Pattern GET_CSS_PATTERN = Pattern.compile("css.+css$");
    private final Pattern GET_JS_PATTERN = Pattern.compile("js.+js$");

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
        Matcher cssMatcher = GET_CSS_PATTERN.matcher(uri);
        Matcher jsMatcher = GET_JS_PATTERN.matcher(uri);
        // TODO: refactor
        if (cssMatcher.find()) {
            sendHtmlResponse(request, context, cssMatcher.group(), "text/css");
        } else if (jsMatcher.find()) {
            sendHtmlResponse(request, context, jsMatcher.group(), "text/javascript");
        } else if (Objects.equals(uri, "/profiler/results")) {
            sendHtmlResponse(request, context, "index.html", "text/html");
        } else {
            RestService.sendStatus(HttpResponseStatus.NOT_FOUND, false, context.channel());
            return "Not Found";
        }
        return null;
    }

    private void sendCss(FullHttpRequest request, ChannelHandlerContext context, String path) {

    }

    private void sendHtmlResponse(FullHttpRequest request,
                                  ChannelHandlerContext context,
                                  String fileName,
                                  String contentType) throws IOException {
        BufferExposingByteArrayOutputStream byteOut = new BufferExposingByteArrayOutputStream();
        InputStream pageStream = getClass().getResourceAsStream("/visualization/" + fileName);
        try {
            byteOut.write(StreamUtil.loadFromStream(pageStream));
            HttpResponse response = Responses.response(contentType, Unpooled.wrappedBuffer(byteOut.getInternalBuffer(), 0, byteOut.size()));
            Responses.addNoCache(response);
            response.headers().set("X-Frame-Options", "Deny");
            Responses.send(response, context.channel(), request);
        }
        finally {
            byteOut.close();
            pageStream.close();
        }
    }
}
