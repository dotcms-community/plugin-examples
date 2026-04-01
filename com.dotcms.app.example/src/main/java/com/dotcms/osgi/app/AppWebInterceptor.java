package com.dotcms.osgi.app;


import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.dotcms.business.CloseDBIfOpened;
import com.dotcms.filters.interceptor.Result;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotcms.security.apps.AppSecrets;
import com.dotcms.security.apps.AppsAPI;
import com.dotcms.security.apps.Secret;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.web.HostWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.util.Logger;
import io.vavr.control.Try;

public class AppWebInterceptor implements WebInterceptor {


    private static final long serialVersionUID = 1L;


    protected final AppsAPI appsAPI;

    @Override
    public String[] getFilters() {
        return new String[] {"/*"};
    }


    public AppWebInterceptor() {
        this(WebAPILocator.getHostWebAPI(), APILocator.getAppsAPI());
    }

    protected AppWebInterceptor( final HostWebAPI hostWebAPI, final AppsAPI appsAPI) {



        this.appsAPI = appsAPI;
    }


    @CloseDBIfOpened
    @Override
    public Result intercept(final HttpServletRequest request, final HttpServletResponse response) throws IOException {


        Optional<AppConfig> config = config(request);
        
        
        config.ifPresent(c->Logger.info(AppWebInterceptor.class.getName(), "Got config:" + c));
            
        
        return Result.NEXT;
    }

    
    
    @CloseDBIfOpened
    @Override
    public boolean afterIntercept(final HttpServletRequest request, final HttpServletResponse response) {


        return true;

    }


    /**
     * Gets the secrets from the App - this will check the current host then the SYSTEMM_HOST for a
     * valid configuration. This lookup is low overhead and cached by dotCMS.
     * 
     * @param request
     * @return
     */
    public Optional<AppConfig> config(final HttpServletRequest request) {
        
        
        final Host host = WebAPILocator.getHostWebAPI().getCurrentHostNoThrow(request);

        Optional<AppSecrets> appSecrets = Try.of(
                        () -> APILocator.getAppsAPI().getSecrets(AppKeys.APP_KEY, true, host, APILocator.systemUser()))
                        .getOrElse(Optional.empty());

        if (!appSecrets.isPresent()) {
            return Optional.empty();
        }

        Map<String, Secret> secrets = appSecrets.get().getSecrets();


        boolean checkbox = Try.of(()->appSecrets.get().getSecrets().get(AppKeys.CHECKBOX.key).getBoolean()).getOrElse(Boolean.FALSE);
        String variableOne = Try.of(()->appSecrets.get().getSecrets().get(AppKeys.VARIABLE_ONE.key).getString()).getOrElse("n/a");
        char[] variableTwo = Try.of(()->appSecrets.get().getSecrets().get(AppKeys.VARIABLE_TWO.key).getValue()).getOrElse(new char[0]);
        String selectBox = Try.of(()->appSecrets.get().getSecrets().get(AppKeys.SELECT_VARIABLE.key).getString()).getOrElse("n/a");
        
        AppConfig config = AppConfig.builder()
                        .checkbox(checkbox)
                        .variableOne(variableOne)
                        .variableTwo(variableTwo)
                        .selectVariable(selectBox)
                        .build();

        
        
        

        return Optional.ofNullable(config);


    }


}
