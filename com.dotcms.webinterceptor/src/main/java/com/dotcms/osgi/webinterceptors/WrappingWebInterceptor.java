package com.dotmarketing.osgi.webinterceptors;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import com.dotcms.filters.interceptor.Result;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotmarketing.util.UtilMethods;

/**
 * This web interceptor adds the access control allow origin in addition to overrides the request
 * and response
 * 
 * @author jsanca
 */
public class WrappingWebInterceptor implements WebInterceptor {

    final static String[] FILTER_PATHS = new String[] {"/*"};


    @Override
    public String[] getFilters() {
        return FILTER_PATHS;
    }

    @Override
    public Result intercept(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        // NEXT means the request will be passed on to the rest of the Filter/Servlet chain for further
        // processing
        Result result = Result.NEXT;


        result = new Result.Builder().next() // <--- building a NEXT result
                        .wrap(new PreloadRequest(request))
                        .wrap(new HeaderDecoratorResponse(response, request.getHeader("Origin"))).build();

        return result;
    }


    // just includes some dummy attributes by default
    private class PreloadRequest extends HttpServletRequestWrapper {

        private Map<String, Object> attributes = Map.of("hello", "This is a hello test", 
                        "pi", new Double(3.14159265359),
                        "goldenratio", new Double(1.6180339887))
                        ;

        public PreloadRequest(final HttpServletRequest request) {
            super(request);
        }

        @Override
        public Object getAttribute(final String name) {
            return this.attributes.getOrDefault(name, super.getAttribute(name));
        }
    }


    private class HeaderDecoratorResponse extends HttpServletResponseWrapper {

        public HeaderDecoratorResponse(final HttpServletResponse response, final String origin) {
            super(response);
            this.setHeader("Access-Control-Allow-Origin",
                            UtilMethods.isSet(origin) ? origin : "http://dotcms.webinterceptor.com");
            this.setHeader("X-WEBINTERCEPTOR", "WrappingWebInterceptor");

        }
    }
}
