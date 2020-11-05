package com.quorum.tessera.app;

import com.quorum.tessera.api.common.ApiResource;
import com.quorum.tessera.api.common.BaseResource;
import com.quorum.tessera.api.common.VersionResource;
import com.quorum.tessera.api.exception.*;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.apps.TesseraApp;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import javax.ws.rs.core.Application;
import java.util.Set;

// @Api
public abstract class TesseraRestApplication extends Application implements TesseraApp {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                EnhancedPrivacyNotSupportedExceptionMapper.class,
                PrivacyViolationExceptionMapper.class,
                AutoDiscoveryDisabledExceptionMapper.class,
                DecodingExceptionMapper.class,
                DefaultExceptionMapper.class,
                EnclaveNotAvailableExceptionMapper.class,
                EntityNotFoundExceptionMapper.class,
                KeyNotFoundExceptionMapper.class,
                NotFoundExceptionMapper.class,
                SecurityExceptionMapper.class,
                TransactionNotFoundExceptionMapper.class,
                WebApplicationExceptionMapper.class,
                NodeOfflineExceptionMapper.class,
                VersionResource.class,
                ApiResource.class,
                BaseResource.class,
                OpenApiResource.class,
                AcceptHeaderOpenApiResource.class);
    }

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.REST;
    }
}
